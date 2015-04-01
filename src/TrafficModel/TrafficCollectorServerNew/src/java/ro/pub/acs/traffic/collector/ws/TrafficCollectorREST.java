package ro.pub.acs.traffic.collector.ws;

import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.pub.acs.traffic.collector.service.LocationService;

@Controller
public class TrafficCollectorREST {

    @Autowired
    private LocationService locationService;
    @Autowired
    private ServletContext context;

    private static final Logger logger
            = Logger.getLogger(TrafficCollectorREST.class.getName());

    @RequestMapping(method = RequestMethod.POST, value = "/updateLocation")
    public @ResponseBody
    String updateLocation(@RequestBody String content) {

        return "updateLocation";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/requestFriendUpdates")
    public @ResponseBody
    String requestFriendUpdates() {

        return "requestFriendUpdates";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/cancelFriendUpdates")
    public @ResponseBody
    String cancelFriendUpdates() {

        return "requestFriendUpdates";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test")
    public @ResponseBody
    String test() {

        return "test";
    }

}
