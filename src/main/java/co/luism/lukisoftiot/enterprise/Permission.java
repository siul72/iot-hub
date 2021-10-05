package co.luism.lukisoftiot.enterprise;/*
  ____        _ _ _                   _____           _
 |  __ \     (_) | |                 / ____|         | |
 | |__) |__ _ _| | |_ ___  ___      | (___  _   _ ___| |_ ___ _ __ ___  ___
 |  _  // _` | | | __/ _ \/ __|      \___ \| | | / __| __/ _ \ '_ ` _ \/ __|
 | | \ \ (_| | | | ||  __/ (__       ____) | |_| \__ \ ||  __/ | | | | \__ \
 |_|  \_\__,_|_|_|\__\___|\___|     |_____/ \__, |___/\__\___|_| |_| |_|___/
                                            __/ /
 Railtec Systems GmbH                      |___/
 6052 Hergiswil

 SVN file informations:
 Subversion Revision $Rev: $
 Date $Date: $
 Commmited by $Author: $
*/

import co.luism.lukisoftiot.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * webmanager
 * co.luism.lukisoftiot.enterprise
 * Created by luis on 03.10.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
@Entity
@Table(catalog = "lukiiot")
public class Permission extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer permissionId = 0;
    @Column(nullable = false, length = 128)
    private String name = "";
    @Column(nullable = false, length = 128)
    private String object = "ALL";
    @Column(nullable = false, length = 128)
    private String permission = "ALL";

    @ManyToOne
    @JoinColumn(name="roleId", insertable=false, updatable=false, nullable=false)
    private Role myRole = null;

    private Integer roleId;

    @Column
    private String updateBy ="";

    public Integer getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Role getMyRole() {
        return myRole;
    }

    public void setMyRole(Role myRole) {
        this.myRole = myRole;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

}
