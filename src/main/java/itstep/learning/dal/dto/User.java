package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String avatar;
    private Role role;
    private Date birthdate;
    private Date signupDt;
    private Date deleteDt;

    public User() {
    }

    public User(ResultSet rs, Role role) throws SQLException {
        String id;
        try {
            id = rs.getString("user-id");
        } catch (Exception ignore) {
            id = rs.getString("id");
        }
        setId(UUID.fromString(id));
        setName(rs.getString("name"));
        setEmail(rs.getString("email"));
        setAvatar(rs.getString("avatar"));
        setBirthdate(rs.getDate("birthdate"));
        setSignupDt(new Date(rs.getTimestamp("signup_dt").getTime()));
        setRole(role);
        Timestamp timestamp = rs.getTimestamp("delete_dt");
        if (timestamp != null) {
            setDeleteDt(new Date(timestamp.getTime()));
        }
    }
    public User(ResultSet rs) throws SQLException {
        String id;
        try {
            id = rs.getString("user-id");
        } catch (Exception ignore) {
            id = rs.getString("id");
        }
        setId(UUID.fromString(id));
        setName(rs.getString("name"));
        setEmail(rs.getString("email"));
        setAvatar(rs.getString("avatar"));
        setBirthdate(rs.getDate("birthdate"));
        setSignupDt(new Date(rs.getTimestamp("signup_dt").getTime()));
        Timestamp timestamp = rs.getTimestamp("delete_dt");
        if (timestamp != null) {
            setDeleteDt(new Date(timestamp.getTime()));
        }
    }
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Date getSignupDt() {
        return signupDt;
    }

    public void setSignupDt(Date signupDt) {
        this.signupDt = signupDt;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }

    public void setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
    }
}

/*
                 DAL (Data Access Layer) / (Data Context)
                     /          \
DTO (Data Transfer Object)     DAO (Data Access Object)
(Entity)

[User]                  [UserSecurity]
Id  ___________         Id  (SID - Security Identity)
Name           \_______ UserId
Email                   Login
Avatar                  Salt
Birthdate               Dk
SignupDt                RoleId
DeleteDt

 */