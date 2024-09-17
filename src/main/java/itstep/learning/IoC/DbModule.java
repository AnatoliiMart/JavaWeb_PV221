package itstep.learning.IoC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import itstep.learning.services.stream.StringReader;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbModule extends AbstractModule {
    private final StringReader reader;
    private Connection connection = null;
    private Driver mySqlDriver = null;

    public DbModule(StringReader reader) {
        this.reader = reader;
    }

    @Provides // методи провайдери -- керована інжекція
    private Connection getConnection() {
        // кожна точка інжекції типу даних "Connection" буде запускати цей метод
        // i його повернення інжектувати як залежність
        if (connection == null) {
            Map<String,String> ini = new HashMap<>();
            try(InputStream rs = this.getClass().getClassLoader().getResourceAsStream("db.ini")){
                String content = reader.read(rs);

                String[] lines = content.split("\n");
                for(String line : lines){
                    String[] parts = line.split("=");
                    ini.put(parts[0].trim(),parts[1].trim());
                }
                System.out.printf(
                        String.format("jdbc:%s://%s:%s/%s",
                        ini.get("dbms"),
                        ini.get("host"),
                        ini.get("port"),
                        ini.get("schema")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try{
                mySqlDriver = new com.mysql.cj.jdbc.Driver();
                // реєструємо його
                DriverManager.registerDriver(mySqlDriver);

                connection = DriverManager.getConnection(
                        String.format("jdbc:%s://%s:%s/%s",
                                ini.get("dbms"),
                                ini.get("host"),
                                ini.get("port"),
                                ini.get("schema")),
                        ini.get("user"),
                        ini.get("password"));
            } catch (SQLException e) {
                System.err.println("DbModule::getConnection() "+e.getMessage());
            }
        }
        return connection;
    }
}
