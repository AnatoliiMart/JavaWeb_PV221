package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.services.hash.HashService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@Singleton
public class HomeServlet extends HttpServlet {
    private final HashService signatureService;
    private final Connection connection;
    private final UserDao userDao;
    private final TokenDao tokenDao;
    private final CategoryDao categoryDao;

    @Inject
    public HomeServlet(@Named("signature") HashService signatureService, Connection connection, UserDao userDao, TokenDao tokenDao, CategoryDao categoryDao) {
        this.signatureService = signatureService;
        this.connection = connection;
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.categoryDao = categoryDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("signature", signatureService.digest("123"));
        req.setAttribute("hash",
                userDao.installTables() &&
                        tokenDao.installTables() &&
                        categoryDao.installTables()
                        ? "Ok" : "Failed");
        req.setAttribute("checkControl", (boolean) req.getAttribute("control") ? "Control Pass" : "Control Reject");
        // ~ return View()
        req.setAttribute("page", "home");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
