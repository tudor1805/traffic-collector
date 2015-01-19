package ro.pub.cs.capim.dwr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import ro.pub.cs.capim.generic.GeneralConstants;
import ro.pub.cs.capim.generic.GeographicInfo;

public class ApplicationDwr {
	public List<HashMap<String, String>> getTrafficData(ServletContext context) {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		
		@SuppressWarnings("unchecked")
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
		}
		
		return list;
	}
	
	public void clearTrafficData(ServletContext context) {
		@SuppressWarnings("unchecked")
		Map<String, GeographicInfo> map = (Map<String, GeographicInfo>) 
					context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP);
		if (map != null)
			map.clear();
	}
}
