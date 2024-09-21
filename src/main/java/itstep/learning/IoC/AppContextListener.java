package itstep.learning.IoC;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import itstep.learning.services.stream.BaosStringReader;
import itstep.learning.services.stream.StringReader;

import javax.servlet.ServletContextEvent;

public class AppContextListener extends GuiceServletContextListener {
    private StringReader stringReader = new BaosStringReader();

    @Override
    protected Injector getInjector() {
        ServicesModule servicesModule = new ServicesModule(stringReader);
        DbModule dbModule = new DbModule(stringReader);
        return Guice.createInjector(
                servicesModule,
                dbModule,
                new WebModule()
        );
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
//        stringReader
        // додаткові дії при створенні контексту
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        // додаткові дії при вимкненні застосунку
    }
}
/*
    ContextListener - підписник на подію створення контексту застосунку
    (деплою) запуску застосунку
*/