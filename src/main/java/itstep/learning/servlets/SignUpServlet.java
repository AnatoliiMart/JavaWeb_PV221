package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.formParse.FormParseResult;
import itstep.learning.services.formParse.FormParseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class SignUpServlet extends HttpServlet {
    private final FormParseService formParseService;
    @Inject
    public SignUpServlet(FormParseService formParseService) {
        this.formParseService = formParseService;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("page", "signup");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult result = formParseService.parse(req);
        System.out.println(result.getFields().size() + " " + result.getFields().toString() + " " + result.getFiles().size());
    }
}
