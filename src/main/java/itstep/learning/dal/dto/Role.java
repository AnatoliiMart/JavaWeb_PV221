package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Role {
    private UUID roleId;
    private String roleName;
    private int canCreate;
    private int canRead;
    private int canUpdate;
    private int canDelete;
    private int canBan;
    private int canBlock;

    public Role() {
    }
    public Role(ResultSet resultSet) throws SQLException {
        setRoleId(UUID.fromString(resultSet.getString("role_id")));
        setRoleName(resultSet.getString("role_name"));
        setCanCreate(resultSet.getInt("canCreate"));
        setCanRead(resultSet.getInt("canRead"));
        setCanUpdate(resultSet.getInt("canUpdate"));
        setCanDelete(resultSet.getInt("canDelete"));
        setCanBan(resultSet.getInt("canBan"));
        setCanBlock(resultSet.getInt("canBlock"));
    }


    public UUID getRoleId() {
        return roleId;
    }

    public Role setRoleId(UUID roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public Role setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public int getCanCreate() {
        return canCreate;
    }

    public Role setCanCreate(int canCreate) {
        this.canCreate = canCreate;
        return this;
    }

    public int getCanRead() {
        return canRead;
    }

    public Role setCanRead(int canRead) {
        this.canRead = canRead;
        return this;
    }

    public int getCanUpdate() {
        return canUpdate;
    }

    public Role setCanUpdate(int canUpdate) {
        this.canUpdate = canUpdate;
        return this;
    }

    public int getCanDelete() {
        return canDelete;
    }

    public Role setCanDelete(int canDelete) {
        this.canDelete = canDelete;
        return this;
    }

    public int getCanBan() {
        return canBan;
    }

    public Role setCanBan(int canBan) {
        this.canBan = canBan;
        return this;
    }

    public int getCanBlock() {
        return canBlock;
    }

    public Role setCanBlock(int canBlock) {
        this.canBlock = canBlock;
        return this;
    }
}
