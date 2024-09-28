package itstep.learning.filters.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@Singleton
public class SessionAuthFilter implements Filter {
    private final UserDao userDao;

    @Inject
    public SessionAuthFilter(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String qs = request.getQueryString();
        if (qs != null && qs.matches("(^|&|\\?)logout=true($|&)")) {
            session.removeAttribute("userId");
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            UUID userId = (UUID) session.getAttribute("userId");
            if (userId != null) {
                User user = userDao.getUserById(userId);
                if (user != null) {
                    request.setAttribute("Claim.Sid", userId);
                    request.setAttribute("Claim.Name", user.getName());
                    request.setAttribute("Claim.Avatar", user.getAvatar());
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
