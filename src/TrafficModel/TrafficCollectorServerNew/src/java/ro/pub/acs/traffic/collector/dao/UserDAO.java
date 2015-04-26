package ro.pub.acs.traffic.collector.dao;

import java.util.List;
import org.hibernate.Query;
import ro.pub.acs.traffic.collector.domain.Location;
import ro.pub.acs.traffic.collector.domain.User;

public class UserDAO extends GenericDAO<User, Long> {

    public User findUserByUUID(String uuid) {
        String query_cmd
                = "select u from ro.pub.acs.domain.Users as u "
                + "where u.uuid = :uuid";

        Query query = getSession().createQuery(query_cmd);
        query.setParameter("uuid", uuid);
        List<User> users = query.setMaxResults(1).list();

        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }

        return null;
    }

    public User loginUser(String username, String password) {
        String query_cmd
                = "select u from ro.pub.acs.traffic.collector.domain.User as u "
                + "where "
                + "u.username = :username and "
                + "u.password = :password";

        Query query = getSession().createQuery(query_cmd);
        query.setParameter("username", username);
        query.setParameter("password", password);
        List<User> users = query.setMaxResults(1).list();

        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }

        return null;
    }
}
