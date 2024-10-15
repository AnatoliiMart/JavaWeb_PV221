package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestServlet;


import javax.naming.AuthenticationException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

@Singleton
public class AuthServlet extends RestServlet {
    private final Logger logger;
    private final UserDao userDao;
    private final TokenDao tokenDao;


    @Inject
    public AuthServlet(Logger logger, UserDao userDao, TokenDao tokenDao) {
        this.logger = logger;
        this.userDao = userDao;
        this.tokenDao = tokenDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            super.sendRest(401, "Missing Authorization header");
            return;
        }
        if (!authHeader.startsWith("Basic ")) {
            super.sendRest(401, "Basic Authorization scheme only");
        }
        String credentials64 = authHeader.substring(6);
        String credentials;
        try {
            credentials = new String(Base64.getUrlDecoder().decode(credentials64));
        } catch (IllegalArgumentException ex) {
            super.sendRest(401, "Illegal credentials");
            logger.warning(ex.getMessage());
            return;
        }
        String[] parts = credentials.split(":", 2);
        try {
            User user = userDao.authenticate(parts[0], parts[1]);
            Token token = tokenDao.getNotExpiredTokenByUserId(user.getId());
            if (token == null) {
                token = tokenDao.createToken(user);

                logger.info("Created new token: " + token);
            }
            else{
                logger.info("Token prolongation: " + token);
            }
            super.sendRest(200, token);
        } catch (AuthenticationException | SQLException e) {
            logger.warning(e.getMessage());
        }
    }


}
