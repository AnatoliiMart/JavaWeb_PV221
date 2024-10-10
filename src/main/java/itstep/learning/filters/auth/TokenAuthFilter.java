package itstep.learning.filters.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dto.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Singleton
public class TokenAuthFilter implements Filter {
    private final TokenDao tokenDao;

    @Inject
    public TokenAuthFilter(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            UUID tokenId;
            User user = null;
            try {
                user = tokenDao.getUserByTokenId(UUID.fromString(token));
            } catch (Exception ignore) {
            }
            if (user!=null ){
                req.setAttribute("Claim.Sid", user.getId().toString());
                req.setAttribute("Claim.Name", user.getName());
                req.setAttribute("Claim.Avatar", user.getAvatar());
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}