package ro.pub.cs.capim.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Query;

import ro.pub.cs.capim.domain.Nodes;
import ro.pub.cs.capim.domain.Streets;
import ro.pub.cs.capim.generic.GeographicInfo;
import ro.pub.cs.capim.generic.Utils;

public class StreetsDAO extends GenericDAO<Streets, Long>{
	public static final String QUERY1 = 
		"select n from ro.pub.cs.capim.domain.Nodes as n " +
		"where (n.latitude >= :latMin and n.latitude <= :latMax) " +
		"and (n.longitude >= :lonMin and n.longitude <= :lonMax)";

	public static final String QUERY2 = 
		"select s from ro.pub.cs.capim.domain.Streets as s " +
		"where s.streetId = :id" ;

	@SuppressWarnings("unchecked")
	public List<String> getStreetName(double latitude, double longitude) {
		double delta = 0.001;
		
		List<Nodes> nodes = new ArrayList<Nodes>();
		boolean nodesFound = false;
		
		while (true) {
			Query query = getSession().createQuery(QUERY1);
			query.setParameter("latMin", latitude - delta);
			query.setParameter("latMax", latitude + delta);
			query.setParameter("lonMin", longitude - delta);
			query.setParameter("lonMax", longitude + delta);
			nodes = query.list();
			
			// if we find a node that has street associated to it
			for (Nodes node : nodes) {
				if (node.getStreet1Id() != -1 || node.getStreet2Id() != -1) {
					nodesFound = true;
					break;
				}
			}
			
			if (nodesFound)
				break;
				
			delta = delta * 2;
		};
		
		// we find the node that is the closest to our car
		double minDistance = Double.MAX_VALUE;
		Nodes minNode = null;
		for (Nodes node : nodes) {
			double distance = Utils.distance(
					new GeographicInfo(node.getLatitude(), node.getLongitude(), 0),
					new GeographicInfo(node.getLatitude(), node.getLongitude(), 0),
					Utils.KILOMETERS);
			if (distance < minDistance) {
				minDistance = distance;
				minNode = node;
			}
		}
		
		if (minNode == null)
			return new ArrayList<String>();
		
		long streetId = 
			(minNode.getStreet1Id() != null && minNode.getStreet1Id() != -1) ?
				minNode.getStreet1Id() :
				(minNode.getStreet2Id() != null && minNode.getStreet2Id() != -1) ?
					minNode.getStreet2Id() : -1;
					
		if (streetId == -1)
			return new ArrayList<String>();
		
		Query query = getSession().createQuery(QUERY2);
		query.setParameter("id", minNode.getStreet1Id() != -1 ? 
								minNode.getStreet1Id() : minNode.getStreet2Id());
		List<Streets> streets = query.list();

		List<String> streetNames = new ArrayList<String>();
		for (Streets street : streets)
			streetNames.add(street.getName());

		return streetNames;
	}
}
