package ro.pub.cs.capim.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


public class Utils {
	public static int RADIUS = 6371;
	public static double DELTA = 1.45;
	public static double VAL = 3600 * 1000;

	public static double VICINITY_LAT = 0.0025;
	public static double VICINITY_LON = 0.0050;

	public static final char KILOMETERS = 'K';
	public static final char MILES = 'M';
	
	public static double distanceBetweenAandB(GeographicInfo node1, GeographicInfo node2) {
		double d = Math.acos(Math.sin(node1.lat) * Math.sin(node2.lat) + 
				Math.cos(node1.lat) * Math.cos(node2.lat) *
				Math.cos(node2.lon - node1.lon)) * RADIUS;
		return d * DELTA;
	}

	public static double distance(GeographicInfo node1, GeographicInfo node2, char unit) {
		double theta = node1.lon - node2.lon;
		double dist = Math.sin(deg2rad(node1.lat)) * Math.sin(deg2rad(node2.lat)) + 
						Math.cos(deg2rad(node1.lat)) * Math.cos(deg2rad(node2.lat)) * 
						Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == KILOMETERS) {
			dist = dist * 1.609344;
		} else if (unit == MILES) {
			dist = dist * 0.8684;
		}
		return (dist);
	}
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public static boolean isInVicinity(GeographicInfo info1, GeographicInfo info2) {
		return Math.abs(info1.lat - info2.lat) <= VICINITY_LAT &&
		Math.abs(info1.lon - info2.lon) <= VICINITY_LON;
	}

	/*public static ArrayList<String> getStreetName(double latitude, double longitude) {
    	double delta = 0.001;
    	ArrayList<String> list = new ArrayList<String>();
    	HashSet<Integer> set = new HashSet<Integer>();

    	try {
    		// joining the two tables is slow, so is better
    		// to do the required action in two steps
    		StringBuffer sb = new StringBuffer();
    		Statement s = c.createStatement();
    		ResultSet rs;
    		int tmp;

    		do {
    			sb.delete(0, sb.length());
	    		sb.append("SELECT street1_id, street2_id FROM ");
	    		sb.append(nodesTable);
	    		sb.append(" WHERE (latitude >= ");
	    		sb.append(latitude - delta);
	    		sb.append(" and latitude <= ");
	    		sb.append(latitude + delta);
	    		sb.append(") and (longitude >= ");
	    		sb.append(longitude - delta);
	    		sb.append(" and longitude <= ");
	    		sb.append(longitude + delta);
	    		sb.append(");");
	    		rs = s.executeQuery(sb.toString());
	    		while (rs.next()) {
	    			if ((tmp = rs.getInt(1)) != -1)
	    				set.add(tmp);
	    			if ((tmp = rs.getInt(2)) != -1)
	    				set.add(tmp);
	    		}
	    		// binary search if no street id was found
	    		delta = delta * 2;
    		} while (set.size() == 0);

			sb.delete(0, sb.length());
    		sb.append("SELECT name FROM ");
    		sb.append(streetsTable);
    		sb.append(" WHERE (");
    		Iterator it = set.iterator();
    		tmp = (Integer)it.next();
    		while (it.hasNext()) {
    			sb.append("street_id=");
    			sb.append(tmp);
    			sb.append(" or ");
    			tmp = (Integer)it.next();
    		}
    		sb.append("street_id=");
    		sb.append(tmp);
    		sb.append(");");

    		rs = s.executeQuery(sb.toString());
    		while (rs.next()) {
    			list.add(rs.getString(1));
    		}
    	} catch (SQLException e) {
    		System.out.println("[getStreetName] " + e.getMessage());
    	}
    	return list;
    }*/
}
