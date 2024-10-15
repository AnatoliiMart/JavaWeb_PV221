package itstep.learning.IoC;

import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.filters.auth.SessionAuthFilter;
import itstep.learning.filters.auth.TokenAuthFilter;
import itstep.learning.servlets.*;
import itstep.learning.servlets.shop.*;

public class WebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        // Третій, рекомдований спосіб реєстрації фільтрів ...
        filter("/*").through(CharsetFilter.class);
        filter("/*").through(ControlFilter.class);
        filter("/*").through(SessionAuthFilter.class);
        filter("/shop/*").through(TokenAuthFilter.class);
        // ... та сервлетів
        serve("/"        ).with(HomeServlet.class    );
        serve("/auth"    ).with(AuthServlet.class    );
        serve("/file/*"  ).with(DownloadServlet.class);
        serve("/servlets").with(ServletsServlet.class);
        serve("/signup"  ).with(SignUpServlet.class  );
        serve("/index"   ).with(IndexServlet.class   );
        serve("/spa"     ).with(SpaServlet.class     );


        serve("/shop/cart").with(CartServlet.class);
        serve("/shop/category").with(CategoryServlet.class);
        serve("/shop/product").with(ProductServlet.class);

        // !! не забути зняти з фільтрів сервлетів анотації @Web...
        // та додати анотації @Singleton

    }
}
