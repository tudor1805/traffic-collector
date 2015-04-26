package ro.pub.acs.traffic.collector.service;

import ro.pub.acs.traffic.collector.dao.LocationDAO;
import ro.pub.acs.traffic.collector.domain.Location;

public class LocationService extends AbstractService<Location, Long> {

    public Location findLocationByUserId(String userId) {
        return ((LocationDAO) getDao()).findLocationByUserId(userId);
    }
}
