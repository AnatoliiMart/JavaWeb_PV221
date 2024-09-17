package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.services.hash.HashService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@Singleton
public class HomeServlet extends HttpServlet {
    private  final HashService hashService;
    private final HashService signatureService;
    private final Connection connection;
    @Inject
    public HomeServlet(@Named("digest") HashService hashService, @Named("signature") HashService signatureService, Connection connection) {
        this.hashService = hashService;
        this.signatureService = signatureService;
        this.connection = connection;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("signature", signatureService.digest("123"));
        req.setAttribute("hash", connection == null ? "No connection" : "Connection established");
        req.setAttribute("checkControl", (boolean)req.getAttribute("control") ? "Control Pass" : "Control Reject");
        // ~ return View()
        req.setAttribute("page", "home");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
