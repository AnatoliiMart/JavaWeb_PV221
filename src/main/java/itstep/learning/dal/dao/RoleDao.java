package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class RoleDao {
    private final Connection connection;
    private final Logger logger;

    @Inject
    public RoleDao(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }
    public boolean installTables(){
        String sql = "CREATE TABLE IF NOT EXISTS user_roles (" +
                "role_id   CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "role_name VARCHAR(128) NOT NULL," +
                "canCreate INT NOT NULL," +
                "canRead   INT NOT NULL," +
                "canUpdate INT NOT NULL," +
                "canDelete INT NOT NULL," +
                "canBan    INT NOT NULL," +
                "canBlock  INT NOT NULL"  +
                ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
    }
}
