package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.dal.dao.RoleDao;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.services.hash.HashService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HomeServlet extends HttpServlet {
    private final HashService signatureService;
    private final UserDao userDao;
    private final TokenDao tokenDao;
    private final CategoryDao categoryDao;
    private final ProductDao productDao;
    private final RoleDao roleDao;
    private final CartDao cartDao;

    @Inject
    public HomeServlet(@Named("signature") HashService signatureService, UserDao userDao, TokenDao tokenDao, CategoryDao categoryDao, ProductDao productDao, RoleDao roleDao, CartDao cartDao) {
        this.signatureService = signatureService;
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.categoryDao = categoryDao;
        this.productDao = productDao;
        this.roleDao = roleDao;
        this.cartDao = cartDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("signature", signatureService.digest("123"));
        req.setAttribute("hash",
                userDao.installTables() &&
                        tokenDao.installTables() &&
                        categoryDao.installTables() &&
                        productDao.installTables() &&
                        roleDao.installTables() &&
                        cartDao.installTables()
                        ? "Ok" : "Failed");
        req.setAttribute("checkControl", (boolean) req.getAttribute("control") ? "Control Pass" : "Control Reject");
        // ~ return View()
        req.setAttribute("page", "home");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
