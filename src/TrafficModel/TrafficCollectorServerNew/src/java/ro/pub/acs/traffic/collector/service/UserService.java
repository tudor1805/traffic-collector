package ro.pub.acs.traffic.collector.service;

import ro.pub.acs.traffic.collector.dao.*;
import ro.pub.acs.traffic.collector.domain.*;

public class UserService extends AbstractService<User, Long> {

    public User findUserByUUID(String userId) {
        return ((UserDAO) getDao()).findUserByUUID(userId);
    }
    
    public User loginUser(String username, String password) {
        return ((UserDAO) getDao()).loginUser(username, password);
    }
}
