package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class Token {
    private UUID tokenId;
    private UUID userId;
    // based on:
    // https://en.wikipedia.org/wiki/JSON_Web_Token
    private Date exp;
    private Date iat;

    public Token() {
    }

    public Token(ResultSet rs) throws SQLException {
        setTokenId(UUID.fromString(rs.getString("token_id")));
        setUserId(UUID.fromString(rs.getString("user_id")));
        setExp(new Date(rs.getTimestamp("exp").getTime()));
        setIat(new Date(rs.getTimestamp("iat").getTime()));
    }


    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Date getExp() {
        return exp;
    }

    public void setExp(Date exp) {
        this.exp = exp;
    }

    public Date getIat() {
        return iat;
    }

    public void setIat(Date iat) {
        this.iat = iat;
    }
}
