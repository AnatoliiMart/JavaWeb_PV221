package itstep.learning.services.formParse;

import javax.servlet.http.HttpServletRequest;

public interface FormParseService {
    FormParseResult parse( HttpServletRequest request ) ;
}
