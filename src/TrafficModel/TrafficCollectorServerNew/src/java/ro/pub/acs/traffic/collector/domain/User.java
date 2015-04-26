package ro.pub.acs.traffic.collector.domain;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.*;
import ro.pub.acs.traffic.collector.domain.Location;

@Entity
@Table(name = "user")

public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_user")
    private Integer idUser;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "username")
    private String username;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "password")
    private String password;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "facebook_id")
    private String facebookId;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "name")
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 512)
    @Column(name = "uuid")
    private String uuid;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idFriendUser")
    private Collection<UserContact> userContactCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUser")
    private Collection<UserContact> userContactCollection1;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private Location location;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUser")
    private Collection<Journey> journeyCollection;

    public User() {
    }

    public User(Integer idUser) {
        this.idUser = idUser;
    }

    public User(Integer idUser, String username, String password, String facebookId, String name, String uuid) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.facebookId = facebookId;
        this.name = name;
        this.uuid = uuid;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlTransient
    public Collection<UserContact> getUserContactCollection() {
        return userContactCollection;
    }

    public void setUserContactCollection(Collection<UserContact> userContactCollection) {
        this.userContactCollection = userContactCollection;
    }

    @XmlTransient
    public Collection<UserContact> getUserContactCollection1() {
        return userContactCollection1;
    }

    public void setUserContactCollection1(Collection<UserContact> userContactCollection1) {
        this.userContactCollection1 = userContactCollection1;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @XmlTransient
    public Collection<Journey> getJourneyCollection() {
        return journeyCollection;
    }

    public void setJourneyCollection(Collection<Journey> journeyCollection) {
        this.journeyCollection = journeyCollection;
    }

    @Override
    public String toString() {
        return "ro.pub.acs.traffic.collector.domain2.User[ idUser=" + idUser + " ]";
    }
    
}
