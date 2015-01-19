package ro.pub.cs.capim.service;

import ro.pub.cs.capim.dao.CabsDAO;
import ro.pub.cs.capim.domain.Cabs;

public class CabsService extends AbstractService<Cabs, Long> {
	public Cabs findNthCabByTime(int n) {
		return ((CabsDAO) getDao()).findNthCabByTime(n);
	}
}
