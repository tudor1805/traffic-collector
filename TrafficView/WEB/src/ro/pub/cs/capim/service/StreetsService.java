package ro.pub.cs.capim.service;

import java.util.List;

import ro.pub.cs.capim.dao.StreetsDAO;
import ro.pub.cs.capim.domain.Streets;

public class StreetsService extends AbstractService<Streets, Long>{
	public List<String> getStreetName(double latitude, double longitude) {
		return ((StreetsDAO) getDao()).getStreetName(latitude, longitude);
	}
}
