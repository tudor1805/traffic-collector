package ro.pub.acs.traffic.collector.dao;

import java.util.List;
import org.hibernate.Query;
import ro.pub.acs.traffic.collector.domain.*;

public class JourneyDAO extends GenericDAO<Journey, Integer> {
     
    public List<Journey> findJourneyByUserId(User userId) {
        final String journeysQuery
                = "select l from ro.pub.acs.traffic.collector.domain.Journey as l "
                + "where l.idUser = :uid";
        
        Query query = getSession().createQuery(journeysQuery);
        query.setParameter("uid", userId);
        List<Journey> journeys = query.list();

        return journeys;
    }

}
