package ro.pub.acs.traffic.collector.ws;

import ro.pub.acs.traffic.collector.domain.pojo.Markers;
import ro.pub.acs.traffic.collector.domain.pojo.Marker;
import ro.pub.acs.traffic.collector.utils.*;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.ModelAndView;
import ro.pub.acs.traffic.collector.domain.*;
import ro.pub.acs.traffic.collector.service.*;

@org.springframework.stereotype.Controller
@RequestMapping(value = "/traffic")
public class TrafficCollectorController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService usersService;

    @Autowired
    private JourneyService journeyService;

    @Autowired
    private ServletContext context;

    // Retrieve session object
    private static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    // Retrieve current user from the session object
    private User getCurrentUser() {
        HttpSession session = getSession();
        return (User) session.getAttribute("user");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public ModelAndView loginPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/traffic/login");

        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ModelAndView loginUser(
            Map<String, Object> map,
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        ModelAndView mav = new ModelAndView();

        User user = usersService.loginUser(username, password);
        if (user == null) {
            mav.setViewName("/traffic/login");
            mav.addObject("login_error", "yes");
        } else {
            HttpSession session = getSession();
            session.setAttribute("user", user);
            session.setAttribute("logged_at", new Date());

            mav.setViewName("redirect:" + "table");
        }

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/logout")
    public @ResponseBody
    ModelAndView logoutUser() {
        // Destroy the current session
        HttpSession session = getSession();
        session.invalidate();

        return new ModelAndView("redirect:login");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/account")
    public ModelAndView showAccountInfo() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/traffic/account");

        User user = getCurrentUser();
        if (user != null) {
            mav.addObject("name", user.getName());
            mav.addObject("username", user.getUsername());
        } else {
            // User is not logged in. Redirect to login page
            mav.setViewName("redirect:login");
        }

        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/account")
    public ModelAndView updateAccountInfo(
            @RequestParam("name") String name,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("password_re") String password_re) {

        ModelAndView mav = new ModelAndView();

        User user = getCurrentUser();
        if (user != null) {
            if (password.isEmpty() || !password.equals(password_re)) {
                mav.addObject("account_update_error", "yes");
            } else {
                user.setName(name);
                user.setUsername(username);
                user.setPassword(password);
                usersService.save(user);
            }

            mav.setViewName("/traffic/account");
            mav.addObject("account_try_update", "yes");
            mav.addObject("name", user.getName());
            mav.addObject("username", user.getUsername());
        } else {
            // User is not logged in. Redirect to login page
            mav.setViewName("redirect:login");
        }

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/table")
    public ModelAndView showTable(
            @RequestParam(value = "page", required = false, defaultValue = "1") int currPage) {

        ModelAndView mav = new ModelAndView();

        User user = getCurrentUser();
        if (user != null) {
            final int journeysPerPage = 5;

            List<Journey> journeysOnThisPage = new ArrayList<>();
            List<Journey> journeys = journeyService.findJourneysByUserId(user);

            int startIndex = (currPage - 1) * journeysPerPage;
            int endIndex = startIndex + journeysPerPage;
            for (int currIndex = startIndex;
                    currIndex < endIndex && currIndex < journeys.size();
                    currIndex++) {
                try {
                    Journey j = journeys.get(currIndex);
                    journeysOnThisPage.add(j);
                } catch (Exception ex) {
                }
            }

            int totalPages = journeys.size() / journeysPerPage + 1;

            mav.addObject("totalPages", totalPages);
            mav.addObject("userJourneys", journeysOnThisPage);
            mav.addObject("currentPage", currPage);
            mav.setViewName("/traffic/table");
        } else {
            // User is not logged in. Redirect to login page
            mav.setViewName("redirect:login");
        }

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/marker", produces = "application/xml")
    public @ResponseBody
    Markers getMarkers(
            @RequestParam(value = "id") int journeyId) {

        // Return an xml with location markers during this journey
        Markers markers = new Markers();

        User user = getCurrentUser();
        if (user != null) {
            Journey journey = journeyService.findById(journeyId);
            List<Marker> markersList = new ArrayList<>();

            // Check that the user is logged in and is the owner of the journey
            try {
                if (user.getIdUser() == journey.getIdUser().getIdUser()) {

                    Collection<JourneyData> journeyDataList
                            = journey.getJourneyDataCollection();

                    for (JourneyData jd : journeyDataList) {
                        Marker newMarker = new Marker();

                        // Categorize the marker based on speed
                        float speed = jd.getSpeed();
                        String speedMarker = "";
                        if (speed > 80) {
                            speedMarker = "fastest";
                        } else if (speed > 40) {
                            speedMarker = "fast";
                        } else if (speed > 1) {
                            speedMarker = "slow";
                        } else if (speed <= 1) {
                            speedMarker = "stop";
                        }

                        newMarker.setName("name=\"Date:" + jd.getTimestamp() + "\"");
                        newMarker.setAddress("address=\"Speed:" + jd.getSpeed() + "km/h\"");
                        newMarker.setLat("lat=" + jd.getLatitude());
                        newMarker.setLng("lng=" + jd.getLongitude());
                        newMarker.setSpeed("speed=" + jd.getSpeed());
                        newMarker.setType("type=" + speedMarker);
                        markersList.add(newMarker);
                    }
                }
            } catch (Exception ex) {
                // Skip
            }

            markers.setMarkers(markersList);
        }

        return markers;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/map")
    public ModelAndView getMap(
            @RequestParam(value = "id") int journeyId) {
        ModelAndView mav = new ModelAndView();

        User user = getCurrentUser();
        if (user != null) {
            mav.setViewName("/traffic/map");
        } else {
            mav.setViewName("redirect:login");
        }

        return mav;
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/stats", produces = "application/html")
    public ModelAndView getJourneyStats(
            @RequestParam(value = "id") int journeyId) {
        ModelAndView mav = new ModelAndView();

        User user = getCurrentUser();
        if (user != null) {
            Journey journey = journeyService.findById(journeyId);

            mav.addObject("maxSpeed", "");
            mav.addObject("tripLength", "");
            mav.addObject("tripTime", "");
            mav.addObject("avgSpeed", "");

            mav.setViewName("/traffic/stats");
        } else {
            mav.setViewName("redirect:login");
        }

        return mav;
    }

}
