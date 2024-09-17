package itstep.learning.IoC;

import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.servlets.*;

public class WebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        // Третій, рекомдований спосіб реєстрації фільтрів ...
        filter("/*").through(CharsetFilter.class);
        filter("/*").through(ControlFilter.class);
        // ... та сервлетів
        serve("/"        ).with(HomeServlet.class    );
        serve("/servlets").with(ServletsServlet.class);
        serve("/signup"  ).with(SignUpServlet.class  );
        serve("/index"   ).with(IndexServlet.class   );

        // !! не забути зняти з фільтрів сервлетів анотації @Web...
        // та додати анотації @Singleton

    }
}
