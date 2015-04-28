package ro.pub.acs.traffic.collector.ws;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@org.springframework.stereotype.Controller
@RequestMapping(value = "/")
public class DefaultController {

    // Redirect the user to the default page
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public ModelAndView showDefaultPage() {
        return new ModelAndView("redirect:/traffic/login");
    }
}
