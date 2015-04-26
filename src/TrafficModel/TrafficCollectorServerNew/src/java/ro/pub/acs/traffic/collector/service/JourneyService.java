package ro.pub.acs.traffic.collector.service;

import java.util.List;
import ro.pub.acs.traffic.collector.dao.*;
import ro.pub.acs.traffic.collector.domain.*;

public class JourneyService extends AbstractService<Journey, Integer> {

    public List<Journey> findJourneysByUserId(User userId) {
        return ((JourneyDAO) getDao()).findJourneyByUserId(userId);
    }

}
