package itstep.learning.servlets;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.User;
import itstep.learning.models.form.UserSignupFormModel;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formParse.FormParseResult;
import itstep.learning.services.formParse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.naming.AuthenticationException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.rmi.ServerException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@Singleton
public class SignUpServlet extends RestServlet {
    private final FormParseService formParseService;
    private final FileService fileService;
    private final UserDao userDao;
    private final Logger logger;

    @Inject
    public SignUpServlet(FormParseService formParseService, FileService fileService, UserDao userDao, Logger logger) {
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.userDao = userDao;
        this.logger = logger;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getMethod().toUpperCase()) {
            case "PATCH":
                doPatch(req, resp);
                break;
            default:
                super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("page", "signup");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userLogin = req.getParameter("user-email");
        String userPassword = req.getParameter("user-password");
        logger.info("User login: " + userLogin + " password: " + userPassword);

        if (userLogin == null || userLogin.isEmpty() || userPassword == null || userPassword.isEmpty()) {
            super.sendRest(401, "Missing or empty credentials");
            return;
        }

        try {
            User user = userDao.authenticate(userLogin, userPassword);
            if (user == null) {
                super.sendRest(401, "Credentials rejected");
                return;
            }
            // утримання авторизації - сесії
            // зберігаємо у сесію відомості про користувача
            HttpSession session = req.getSession();
            session.setAttribute("userId", user.getId());
            super.sendRest(200, user);
        } catch (AuthenticationException | ServerException e) {
            super.sendRest(422, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserSignupFormModel model;
        try {
            model = getModelFromRequest(req);
        } catch (Exception e) {
            super.sendRest(400, e.getMessage());
            return;
        }

        // send model to DB
        User user = userDao.signup(model);
        if (user == null) {
            super.sendRest(500, "DB Error, details on server logs");

            return;
        }
        super.sendRest(200, model);
    }

    private UserSignupFormModel getModelFromRequest(HttpServletRequest req) throws Exception {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");

        FormParseResult res = formParseService.parse(req);

        UserSignupFormModel model = new UserSignupFormModel();

        model.setName(res.getFields().get("user-name"));
        if (model.getName() == null || model.getName().isEmpty())
            throw new Exception("Missing or empty required field: 'user-name'");

        model.setEmail(res.getFields().get("user-email"));
        if (model.getEmail() == null || model.getEmail().isEmpty())
            throw new Exception("Missing or empty required field: 'user-name'");

        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!model.getEmail().matches(regex)) throw new Exception("Invalid email address: '" + model.getEmail() + "'");


        model.setPassword(res.getFields().get("user-password"));
        if (model.getPassword() == null || model.getPassword().isEmpty())
            throw new Exception("Missing or empty required field: 'user-password'");

        if (res.getFields().get("user-repeat") == null || res.getFields().get("user-repeat").isEmpty())
            throw new Exception("Missing or empty required field: 'user-repeat'");

        if (!model.getPassword().equals(res.getFields().get("user-repeat")))
            throw new Exception("Passwords do not match");

        try {
            model.setBirthdate(dateParser.parse(res.getFields().get("user-birthdate")));
            new Date();
            if (model.getBirthdate() == null || new Date().equals(model.getBirthdate()) || model.getBirthdate().after(new Date())) {
                throw new Exception("Invalid birthdate: '" + model.getBirthdate() + "'");
            }
        } catch (ParseException ex) {
            throw new Exception(ex.getMessage());
        }
        // save the avatar file and took his saved name
        String uploadedName = null;
        FileItem avatar = res.getFiles().get("user-avatar");
        if (avatar.getSize() > 0) {
            if (!isImageFile(avatar.getContentType())) throw new Exception("Invalid file type! Choose another file!");
            uploadedName = fileService.upload(avatar);
            model.setAvatar(uploadedName);
        }
        System.out.println(uploadedName);

        return model;
    }

    private boolean isImageFile(String contentType) {
        return contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif") || contentType.equals("image/bmp") || contentType.equals("image/svg+xml");
    }

    /*
        Утримання авторизації - забезпечення часового проміжку, протягом якого
        не перезапитуються парольні дані
        Схеми:
         - за токенами (розподілена архітектура бек/фронт):
               при автентифікації видається токен
               при запитах передається токен
         - за сесіями (серверними сесіями)
               при автентифікації стартує сесія
               при запиті перевіряється сесія
       Токен (від англ. жетон/посвідчення) - дані, що ідентифікують їх власника
       Комунікація:
       1. Одержання токена (Автентифікація)
       GET /auth? a)login&password
       b)Authorization: Bearer token
       -> token
       2. Використання токена (авторизація)
       GET /spa
       Authorization: Bearer token
    */
}


