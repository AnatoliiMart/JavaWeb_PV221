package itstep.learning.services.formParse;


import java.util.Map;
import org.apache.commons.fileupload.FileItem;
public interface FormParseResult {
    Map<String, String> getFields() ;
    Map<String, FileItem> getFiles() ;   // ~ IFormFile
}
