package ro.pub.acs.traffic.collector.dao;

import java.util.List;
import org.hibernate.Query;
import ro.pub.acs.traffic.collector.domain.*;

public class LocationDAO extends GenericDAO<Location, Long> {

    public static final String QUERY
            = "select l from ro.pub.acs.domain.Location as l "
            + "where l.id_user = :uid";

    public Location findLocationByUserId(String userId) {
        Query query = getSession().createQuery(QUERY);
        query.setParameter("uid", userId);
        List<Location> locations = query.setMaxResults(1).list();

        if (locations != null && !locations.isEmpty()) {
            return locations.get(0);
        }

        return null;
    }
}
