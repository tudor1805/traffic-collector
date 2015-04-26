package ro.pub.acs.traffic.collector.ws;

import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.pub.acs.traffic.collector.utils.*;

import org.json.simple.JSONObject;
import ro.pub.acs.traffic.collector.service.*;

@Controller
@RequestMapping(value = "/rest/*")
public class TrafficCollectorREST {

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService usersService;

    @Autowired
    private ServletContext context;

    private static final CryptTool ct = new CryptTool();

    private static final Logger logger
            = Logger.getLogger(TrafficCollectorREST.class.getName());

    private void prettyPrintJSON(JSONObject json) {
        // To string method prints it with specified indentation.
        System.out.println(json.toJSONString());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test")
    public @ResponseBody
    String test() {

        return "test";
    }

}
