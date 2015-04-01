package ro.pub.acs.traffic.collector.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "stats")
public class Stats implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "id_user")
    private String idUser;

    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "server_info")
    private String serverInfo;

    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "page")
    private String page;

    @Basic(optional = false)
    @NotNull
    @Column(name = "type")
    private int type;

    @Basic(optional = false)
    @NotNull
    @Column(name = "login_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginDate;

    public Stats() {
    }

    public Stats(Integer id) {
        this.id = id;
    }

    public Stats(Integer id, String idUser, String serverInfo, String page, int type, Date loginDate) {
        this.id = id;
        this.idUser = idUser;
        this.serverInfo = serverInfo;
        this.page = page;
        this.type = type;
        this.loginDate = loginDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    @Override
    public String toString() {
        return "ro.pub.acs.traffic.collector.dao.Stats[ id=" + id + " ]";
    }

}
