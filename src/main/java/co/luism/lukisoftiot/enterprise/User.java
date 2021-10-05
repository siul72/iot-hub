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
import co.luism.lukisoftiot.common.DiagnosticsConfig;
import co.luism.lukisoftiot.common.Utils;
import co.luism.lukisoftiot.webapputils.WebManagerFacade;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * datacollector
 * co.luism.lukisoftiot.enterprise
 * Created by luis on 23.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(catalog = "lukiiot")
public class User extends DiagnosticsPersistent {

    @Transient
    private static final Logger LOG = Logger.getLogger(User.class);

    @Transient
    private final static int ITERATION_NUMBER = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer userId = 0;

    @Column(unique = true, nullable = false, length = 32)
    private String login = "";

    @Column(nullable = true, length = 32)
    private String firstName = "";

    @Column(nullable = true, length = 32)
    private String lastName = "";

    @XmlTransient
    @ManyToOne(targetEntity = Organization.class)
    @JoinColumn(name="organizationId", insertable=false, updatable=false, nullable=false)
    private Organization myOrganization;

    @Column
    private Integer organizationId = 1;

    @javax.persistence.OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="userId")
    @javax.persistence.MapKey(name = "vehicleId")
    private Map<String, VehicleFavourite> favouriteHashMap = new HashMap<>();

    @XmlTransient
    @ManyToOne(targetEntity = Role.class)
    @JoinColumn(name="roleId", insertable=false, updatable=false, nullable=false)
    private Role myRole;

    @Column
    private Integer roleId = 1;

    @Column(nullable = false, length = 64)
    private String email = "";

    @Column(nullable = false, length = 32)
    private String password = "none";

    @Column(nullable = false, length = 32)
    private String salt;

    @Column(nullable = false, length = 16)
    private String language = "en";

    @Column
    private String updateBy ="";

    private String scramble(String in) {

        return Utils.md5(DiagnosticsConfig.getInstance().getSalt() + in);
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Organization getMyOrganization() {
        return myOrganization;
    }

    public void setMyOrganization(Organization organizationId) {
        this.myOrganization = organizationId;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        generatePassword();
    }

    public String getSalt() {
        return salt;
    }

//    public void setSalt(String salt) {
//        this.salt = salt;
//    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public Map<String, VehicleFavourite> getFavouriteHashMap() {
        return favouriteHashMap;
    }

    public void setFavouriteHashMap(Map<String, VehicleFavourite> favouriteHashMap) {
        this.favouriteHashMap = favouriteHashMap;
    }

    @Override
    public boolean create() {

        generatePassword();
        return super.create();
    }

    private void generatePassword() {
            // Uses a secure Random not a simple Random
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        }
        // Salt generation 64 bits long
        byte[] bSalt = new byte[8];
        random.nextBytes(bSalt);
        // Digest computation
        byte[] bDigest = new byte[0];
        try {
            bDigest = Utils.getHash(ITERATION_NUMBER,password,bSalt);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }
        this.password = Utils.byteToBase64(bDigest);
        this.salt = Utils.byteToBase64(bSalt);

    }


    public boolean authenticate(String thePassword) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if(password == null) {
            // TIME RESISTANT ATTACK (Even if the user does not exist the
            // Computation time is equal to the time needed for a legitimate user
            password = "000000000000000000000000000=";
        }

        if(salt == null) {
            salt = "00000000000=";
        }

        byte[] bDigest = Utils.base64ToByte(password);
        byte[] bSalt = Utils.base64ToByte(salt);

       // Compute the new DIGEST
       byte[] proposedDigest = Utils.getHash(ITERATION_NUMBER, thePassword, bSalt);
       return Arrays.equals(proposedDigest, bDigest);

    }

    @Override
    public boolean equals(Object other){
        if(other instanceof User){
            if(this.getLogin().equals(((User) other).getLogin())){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.login.hashCode();
        return hash;

    }

    public boolean isFavourite(String vehicleId){
        VehicleFavourite vf = this.favouriteHashMap.get(vehicleId);
        if(vf == null){
            return false;
        }

        return vf.getFavourite();
    }

    public void setFavourite(String vehicleId, Boolean favourite){
        VehicleFavourite vf = this.favouriteHashMap.get(vehicleId);
        if(vf == null){
            Vehicle vehicle = WebManagerFacade.getInstance().getVehicle(vehicleId);
            if(WebManagerFacade.getInstance().getAllVehicles(this).contains(vehicle)){
                vf = new VehicleFavourite(this.userId, vehicleId, favourite);
                vf.create();
                this.favouriteHashMap.put(vehicleId, vf);
            } else {
                LOG.warn("favourite not created");
            }
            return;
        }
        vf.setFavourite(favourite);
        vf.setUpdateBy(this.getLogin());
        vf.update();
    }


}
