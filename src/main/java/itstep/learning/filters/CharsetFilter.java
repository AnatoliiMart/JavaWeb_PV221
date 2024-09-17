package itstep.learning.filters;

import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Singleton
public class CharsetFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        /*
            Особливості кодування символів у JSP полягає у тому, що її неможливо
            змінити після першого звернення на читання/запис
            Відповідно, перемикання кодування має здійснюватись якомога раніше,
            у первинних фільтрах системи
        */
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        System.out.println("Filter works for " + ((HttpServletRequest)request).getRequestURI());
        // filterChain - ланцюг фільтрів, передача упраління наступному з них
        // не автоматична, а здійснюється програмно. Якщо не передати,
        // то оброблення запиту припиняється
        filterChain.doFilter(request, response); // ~Next()
    }

    @Override
    public void destroy() {
       this.filterConfig = null;
    }
}
/*
* Аналогічно до сервлетів, фільтри теж потрібно реєструвати, теж є три способи
* - web.xml
* - @WebFilter - не гарантується порядок роботи фільтрів, тому є не поширеним способом
* - IoC (Guice)
* IoC: відмінності від консольного застосунку
*  Загальна схема
* 1. Конфігурація (реєстрація сервісів) - одноразово
* 2. Resolve - створення об'єктів з включенням до них залежностей (інжекція) - багаторазово
*
* У консолі пункт 2 доволі часто теж одноразовий - всі сутності створюються при запуску.
* У веб-застосунку навпаки, кожен запит ПОВИНЕН створювати новий об'єкт
* севлета/контролера. Відповідно пункт 2 виконується постійно
*
* Відвовідно до веб-проєкту, конфігурація інжектора має виконуватись при його старті,
* а використання - при кожному запиті. Оскільки кожен запит проходить через фільтри, інжектор
* вбудовується у проєкт в якості фільтру
* */
