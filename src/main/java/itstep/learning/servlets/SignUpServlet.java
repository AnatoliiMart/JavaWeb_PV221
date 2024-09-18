package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.User;
import itstep.learning.models.form.UserSignupFormModel;
import itstep.learning.rest.RestResponse;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formParse.FormParseResult;
import itstep.learning.services.formParse.FormParseService;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class SignUpServlet extends HttpServlet {
    private final FormParseService formParseService;
    private final FileService fileService;
    private final UserDao userDao;

    @Inject
    public SignUpServlet(FormParseService formParseService, FileService fileService, UserDao userDao) {
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.userDao = userDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute( "page", "signup" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestResponse restResponse = new RestResponse();
        resp.setContentType( "application/json" );
        UserSignupFormModel model;
        try{
            model = getModelFromRequest(req);
        } catch (Exception e) {
            restResponse.setStatus( "Error" );
            restResponse.setData( e.getMessage() );
            resp.getWriter().print(
                    new Gson().toJson( restResponse )
            );
            return;
        }

        // send model to DB
        User user = userDao.signup( model );
        if ( user == null ) {
            restResponse.setStatus( "Error" );
            restResponse.setData("500 DB Error, details on server logs" );
            resp.getWriter().print(
                    new Gson().toJson( restResponse )
            );
            return;
        }

        restResponse.setStatus( "Ok" );
        restResponse.setData( model );
        resp.getWriter().print(
                new Gson().toJson( restResponse )
        );
    }

    private UserSignupFormModel getModelFromRequest(HttpServletRequest req) throws Exception {
        SimpleDateFormat dateParser =
                new SimpleDateFormat("yyyy-MM-dd");

        FormParseResult res = formParseService.parse( req );

        UserSignupFormModel model = new UserSignupFormModel();

        model.setName( res.getFields().get("user-name") );
        if( model.getName() == null || model.getName().isEmpty() )
            throw new Exception("Missing or empty required field: 'user-name'");

        model.setEmail( res.getFields().get("user-email") );
        if( model.getEmail() == null || model.getEmail().isEmpty())
            throw new Exception("Missing or empty required field: 'user-name'");

        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!model.getEmail().matches(regex))
            throw new Exception("Invalid email address: '"+model.getEmail()+"'");


        model.setPassword(res.getFields().get("user-password"));
        if (model.getPassword() == null || model.getPassword().isEmpty())
            throw new Exception("Missing or empty required field: 'user-password'");

        if (res.getFields().get("user-repeat") == null || res.getFields().get("user-repeat").isEmpty())
            throw new Exception("Missing or empty required field: 'user-repeat'");

        if (model.getPassword().equals(res.getFields().get("user-repeat")))
            throw new Exception("Passwords do not match");

        try {
            model.setBirthdate(
                    dateParser.parse(
                            res.getFields().get("user-birthdate")
                    )
            );
            new Date();
            if (model.getBirthdate() == null || new Date().equals(model.getBirthdate()) || model.getBirthdate().after(new Date())){
                throw new Exception("Invalid birthdate: '" + model.getBirthdate()+"'");
            }
        }
        catch( ParseException ex ) {
            throw new Exception( ex.getMessage() );
        }
        // save the avatar file and took his saved name
        String uploadedName = null;
        FileItem avatar = res.getFiles().get("user-avatar");
        if ( avatar.getSize() > 0){
            if (!isImageFile(avatar.getContentType()))
                throw new Exception("Invalid file type! Choose another file!");
            uploadedName = fileService.upload( avatar );
            model.setAvatar( uploadedName );
        }
        System.out.println(uploadedName);

        return model;
    }

    private boolean isImageFile(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/bmp") ||
                contentType.equals("image/svg+xml");
    }
    //    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        FormParseResult result = formParseService.parse(req);
//        System.out.println(result.getFields().size() + " " + result.getFields().toString() + " " + result.getFiles().size());
//        req.setAttribute("fields", result.getFields());
//        req.setAttribute("files", result.getFiles());
//        req.setAttribute("page", "userData");
//        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
//    }
}


