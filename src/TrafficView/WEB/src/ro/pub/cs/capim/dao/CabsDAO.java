package ro.pub.cs.capim.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;

import ro.pub.cs.capim.domain.Cabs;

public class CabsDAO extends GenericDAO<Cabs, Long>{
	public static final String QUERY = 
		"select c from ro.pub.cs.capim.domain.Cabs as c " +
		"where c.time < :time order by c.time";
	
	public Cabs findNthCabByTime(int n) {
		Query query = getSession().createQuery(QUERY);
		query.setParameter("time", new Date(System.currentTimeMillis()));
		List<Cabs> streets = query.setMaxResults(1).list();
		return streets.size() != 0 ? streets.get(0) : null;
	}
}
