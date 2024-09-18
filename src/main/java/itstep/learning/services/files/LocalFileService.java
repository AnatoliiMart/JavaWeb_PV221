package itstep.learning.services.files;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.stream.StringReader;
import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class LocalFileService implements FileService {
    private final String uploadPath;
    @Inject
    public LocalFileService(StringReader stringReader) {
        Map<String,String> ini = new HashMap<>();
        try(InputStream rs = this.getClass().getClassLoader().getResourceAsStream("files.ini")){
            String content = stringReader.read(rs);

            String[] lines = content.split("\n");
            for(String line : lines){
                String[] parts = line.split("=");
                ini.put(parts[0].trim(),parts[1].trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.uploadPath = ini.get("upload_path");
    }

    @Override
    public String upload(FileItem fileItem) {
        String formFileName = fileItem.getName(); // file name
        int dotPosition = formFileName.lastIndexOf(".");
        String extension = formFileName.substring(dotPosition);
        String filename;
        File file;
        do{
            filename = UUID.randomUUID() + extension;
            file = new File(uploadPath + filename);
        } while (file.exists()); // guarantee unique file name
        try {
            fileItem.write(file);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        return filename;
    }

    @Override
    public OutputStream download(String fileName) {
        return null;
    }
}
