package ro.pub.cs.capim.ws;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import ro.pub.cs.capim.domain.Cabs;

import ro.pub.cs.capim.generic.GeographicInfo;
import ro.pub.cs.capim.generic.GeneralConstants;
import ro.pub.cs.capim.generic.StreetLoad;
import ro.pub.cs.capim.generic.Utils;
import ro.pub.cs.capim.service.CabsService;
import ro.pub.cs.capim.service.StreetsService;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
@RequestMapping(value = "/*")
public class SpringREST {
	private static final Logger logger = Logger.getLogger(SpringREST.class.getName());

	@Autowired
	private StreetsService streetsService;
	@Autowired
	private ServletContext context;
	@Autowired
	private CabsService cabsService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}/{lat}/{lon}")
	public @ResponseBody String doWork(@PathVariable("id") String id, 
			@PathVariable("lat") String lat, 
			@PathVariable("lon") String lon) {
		logger.log(Level.INFO, "Request: " + id + " | " + lat + " | " + lon);

		@SuppressWarnings("unchecked")
		Map<String, GeographicInfo> map = (Map<String, GeographicInfo>)
		context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP);
		if (map == null) {
			map = Collections.synchronizedMap( new HashMap<String, GeographicInfo>());
			context.setAttribute(GeneralConstants.SERVLET_CONTEXT_MAP, map);
		}

		long newTimestamp = System.currentTimeMillis();
		GeographicInfo geoInfo = 
			new GeographicInfo(Double.valueOf(lat), Double.valueOf(lon), newTimestamp);

		if (map.get(id) == null){
			geoInfo.speed = 0;
		}
		else {
			geoInfo.speed = Utils.distance(map.get(id), geoInfo, Utils.KILOMETERS) / 
							((newTimestamp - map.get(id).timestamp) / 3600000.0);
			geoInfo.speed = Math.min(geoInfo.speed, GeneralConstants.MAX_SPEED);
		}
		map.put(id, geoInfo);
				
		return "ok";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getRoute")
	public @ResponseBody String getOptimalRoute(@RequestBody String content) {	
		
		List<ArrayList<GeographicInfo>> routeList = 
			new ArrayList<ArrayList<GeographicInfo>>();
		List<ArrayList<String>> streetList = new ArrayList<ArrayList<String>>();
		
		List<Double> averageSpeeds = new ArrayList<Double>();
		List<Integer> nrOfCars = new ArrayList<Integer>();
		List<Double> costs = new ArrayList<Double>();
		
		@SuppressWarnings("unchecked")
		Map<String, GeographicInfo> map = (Map<String, GeographicInfo>) 
			context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP);	
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(content);

			JSONArray routes = (JSONArray) json.get("routes");
			System.out.println("No. streets: " + routes.size());
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
			
			int routeIndex = 0;
			for (Object o1 : routes.toArray()) {
				
				routeList.add(new ArrayList<GeographicInfo>());
				streetList.add(new ArrayList<String>());
				
				JSONObject route = (JSONObject) o1;
				JSONArray legs = (JSONArray) route.get("legs");
				JSONArray steps = (JSONArray) ((JSONObject) legs.get(0)).get("steps");

				int stepIndex = 0;
				for (Object o2 : steps.toArray()) {
					JSONObject step = (JSONObject) o2;
					JSONObject startLocation = (JSONObject) step.get("start_location");

					Double startLon = (Double) startLocation.get("Ka");
					Double startLat = (Double) startLocation.get("Ja");

					System.out.println(startLon != null ? startLon : "null");
					System.out.println(startLat != null ? startLat : "null");
					
					routeList.get(routeIndex).add(
							new GeographicInfo(startLat, startLon, 0));
					
					List<String> streetNames = 
						streetsService.getStreetName(startLat, startLon);
					if (streetNames.size() > 0)
						streetList.get(routeIndex).add(streetNames.get(0));
					
					bw.write(streetNames.toString());
					bw.write(Character.LINE_SEPARATOR);				
					
					stepIndex++;
				}
				
				int nrCars = 0;
				double totalSpeed = 0;
				if (map != null) {
					Set<String> carIds = map.keySet();
					for (String carId : carIds) {
						GeographicInfo carInfo = map.get(carId);
						for (GeographicInfo nodeInfo : routeList.get(routeIndex)) {
							double dist = Utils.distance(carInfo, nodeInfo, Utils.KILOMETERS);
							if (dist < 0.5) {
								List<String> streetNamesByNode = 
									streetsService.getStreetName(nodeInfo.lat, nodeInfo.lon);
								if (dist < 0.1) {
									nrCars++;
									totalSpeed += carInfo.speed;
								}
								else 
								if (streetNamesByNode.size() > 0) {
									List<String> streetNamesByCar = 
										streetsService.getStreetName(carInfo.lat, carInfo.lon);
									if (streetNamesByCar.size() > 0)
										if (streetNamesByNode.get(0).equals(streetNamesByCar.get(0))) {
											nrCars++;
											totalSpeed += carInfo.speed; 
										}
								}
							}
						}
					}
				}
				
				if (nrCars != 0) {
					double averageSpeed = totalSpeed / nrCars;
					averageSpeeds.add(averageSpeed);
					costs.add(routeList.get(routeIndex).size() / averageSpeed);
				}
				else {
					averageSpeeds.add(GeneralConstants.LEGAL_AVERAGE_SPEED);
					costs.add(-1.0);
				}
					
				nrOfCars.add(nrCars);
			
				routeIndex++;
			}
			
			bw.close();
			
		} catch (ParseException e) {
			e.printStackTrace();
			return "0," + GeneralConstants.LEGAL_AVERAGE_SPEED;
		} catch (Exception e) {
			e.printStackTrace();
			return "0," + GeneralConstants.LEGAL_AVERAGE_SPEED;
		}
				
		if (costs.size() == 0)
			return "0," + GeneralConstants.LEGAL_AVERAGE_SPEED;
		double minCost = Collections.min(costs);
		int index = costs.indexOf(minCost);
		if (index == -1)
			return "0," + GeneralConstants.LEGAL_AVERAGE_SPEED;
		double avgSpeed = averageSpeeds.size() > index ? averageSpeeds.get(index) :
													GeneralConstants.LEGAL_AVERAGE_SPEED;
		
		return costs.indexOf(minCost) + "," + avgSpeed;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getCabs")
	public @ResponseBody String getCabs() {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		
		/*@SuppressWarnings("unchecked")
		Map<String, GeographicInfo> map = (Map<String, GeographicInfo>) 
					context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP);
		if (map != null) {			
			Set<String> keys = map.keySet();
			for (String key : keys) {
				HashMap<String, String> subMap = new HashMap<String, String>();
				subMap.put(GeneralConstants.ID, String.valueOf(key));
				subMap.put(GeneralConstants.LAT, String.valueOf(map.get(key).lat));
				subMap.put(GeneralConstants.LON, String.valueOf(map.get(key).lon));
				subMap.put(GeneralConstants.SPEED, String.valueOf(map.get(key).speed));
				list.add(subMap);
			}
		}*/

		Cabs cabs = cabsService.findNthCabByTime(1);
		System.out.println(cabs);
		
		HashMap<String, String> subMap = new HashMap<String, String>();
		subMap.put(GeneralConstants.ID, cabs.getIdentifier());
		subMap.put(GeneralConstants.LAT, String.valueOf(cabs.getLatitude()));
		subMap.put(GeneralConstants.LON, String.valueOf(cabs.getLongitude()));
		//subMap.put(GeneralConstants.SPEED, String.valueOf(map.get(key).speed));
		list.add(subMap);
		
		Gson gson = new Gson();
		String ret = gson.toJson(list);
		System.out.println(ret);
		return ret;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getStatistics/{type}/{sort}/{nr}")
	public @ResponseBody String getStatistics(
			@PathVariable int type, @PathVariable final int sort, @PathVariable int nr) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		@SuppressWarnings("unchecked")
		Map<String, GeographicInfo> map = (Map<String, GeographicInfo>) 
			context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP);
		retMap.put("nrCars", map != null ? map.size() : 0);
		
		Map<String, Integer> streetLoad = new HashMap<String, Integer>();
		Map<String, Double> streetSpeed = new HashMap<String, Double>();
		if (map != null) {
			System.out.println("ok");
			Set<String> keys = map.keySet();
			for (String key : keys) {
				GeographicInfo geoInfo = map.get(key);
				List<String> streets = 
					streetsService.getStreetName(geoInfo.lat, geoInfo.lon);
				if (streets.size() > 0) {
					String streetName = streets.get(0);
					if (streetName != null) {
						if (streetLoad.get(streetName) != null) {
							streetLoad.put(streetName, streetLoad.get(streetName) + 1);
							streetSpeed.put(streetName, streetSpeed.get(streetName) + geoInfo.speed);
						}
						else {
							streetLoad.put(streetName, 1);
							streetSpeed.put(streetName, geoInfo.speed);
						}
					}
				}
			}
		}
		System.out.println("Finished DB queries");		
		
		List<StreetLoad> sortedList = new ArrayList<StreetLoad>();	
		if (type == 1) {
			// compute average speeds
			Set<String> keys = streetSpeed.keySet();
			for (String streetName : keys)
				streetSpeed.put(streetName, 
						streetSpeed.get(streetName) / streetLoad.get(streetName));
			
			for (String key : keys)
				if (streetSpeed.get(key) > 0)
					sortedList.add(new StreetLoad(
						key, streetSpeed.get(key)));
		}
		else {
			Set<String> keys = streetLoad.keySet();
			for (String key : keys) 
				sortedList.add(new StreetLoad(key, streetLoad.get(key)));
		}
				
		Collections.sort(sortedList, new Comparator<StreetLoad>() {
			public int compare(StreetLoad a, StreetLoad b) {
				return (int) (sort == 1 ? a.load - b.load : b.load - a.load);
			}
		});
		
		List<StreetLoad> retList = new ArrayList<StreetLoad>();
		int nrCars = map != null ? map.size() : 0;
		int index = 0;
		for (StreetLoad sLoad : sortedList) {
			if (index == nr)
				break;
			retList.add(sLoad);
			index++;
		}
		
		if (nrCars != 0) {
			if (type == 1) {
				double avgSpeed = 0;
				for (StreetLoad strLoad : sortedList)
					avgSpeed += strLoad.load;
				System.out.println("avg " + sortedList.size() + " " + avgSpeed);
				avgSpeed /= sortedList.size();
				if (avgSpeed < 0.1)
					avgSpeed =GeneralConstants.LEGAL_AVERAGE_SPEED;
				retList.add(new StreetLoad("Average", avgSpeed));
			}
			else
				retList.add(new StreetLoad("Average", nrCars / sortedList.size()));
		}
			
		Gson gson = new Gson();
		String ret = gson.toJson(retList);
		System.out.println(ret);
		return ret;
	}
}
