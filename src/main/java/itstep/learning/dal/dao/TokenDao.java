package itstep.learning.dal.dao;

import com.google.inject.Inject;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;

import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenDao {
    private final Logger logger;
    private final Connection connection;

    @Inject
    public TokenDao(Logger logger, Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }


    public Token createToken(User user) throws SQLException {

        Token token = new Token();
        token.setTokenId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setIat(new Date(System.currentTimeMillis()));
        token.setExp(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
        String sql = "INSERT INTO tokens (token_id, user_id, iat, exp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token.getTokenId().toString());
            ps.setString(2, token.getUserId().toString());
            ps.setTimestamp(3, new Timestamp(token.getIat().getTime()));
            ps.setTimestamp(4, new Timestamp(token.getExp().getTime()));
            ps.executeUpdate();
            return token;
        }
    }

    public Token getNotExpiredTokenByUserId(UUID userId) throws SQLException {
        Token token = new Token();
        token.setUserId(userId);
        String sql = "SELECT * FROM tokens WHERE user_id = ? AND exp > ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                token.setTokenId(UUID.fromString(rs.getString("token_id")));
                token.setUserId(UUID.fromString(rs.getString("user_id")));
                token.setIat(rs.getTimestamp("iat"));
                token.setExp(rs.getTimestamp("exp"));

                long currentTime = System.currentTimeMillis();
                long remainingTimeMillis = token.getExp().getTime() - currentTime;

                if (remainingTimeMillis > 0) {
                    long additionalTimeMillis = Math.round((1000 * 60 * 5) / 2.0);
                    token.setExp(new Date(currentTime + additionalTimeMillis));

                    String updateSql = "UPDATE tokens SET exp = ? WHERE token_id = ?";
                    try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                        updatePs.setTimestamp(1, new Timestamp(token.getExp().getTime()));
                        updatePs.setString(2, token.getTokenId().toString());
                        updatePs.executeUpdate();
                    }
                    return token;
                }
            }
        }
        return null;
    }


    public boolean installTables() {
        String sql =
                "CREATE TABLE IF NOT EXISTS tokens (" +
                        "token_id CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                        "user_id  VARCHAR(128) NOT NULL," +
                        "exp      DATETIME         NULL, " +
                        "iat      DATETIME     NOT NULL   DEFAULT CURRENT_TIMESTAMP" +
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