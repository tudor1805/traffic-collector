package ro.pub.cs.capim.job;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.pub.cs.capim.domain.AvailableCars;
import ro.pub.cs.capim.generic.GeneralConstants;
import ro.pub.cs.capim.generic.GeographicInfo;
import ro.pub.cs.capim.service.AvailableCarsService;
import ro.pub.cs.capim.service.StreetsService;

@Component("persistJob")
public class PersistJob {
	@Autowired
	ServletContext context;
	
	@Autowired
	AvailableCarsService availableCarsService;
	
	@Autowired
	StreetsService streetService;
	
	public void executePersist() {
		if (1==1)
			return;
		
		System.out.println("Persist: " + context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP));
		
		@SuppressWarnings("unchecked")
		Map<String, GeographicInfo> map = (Map<String, GeographicInfo>)
			context.getAttribute(GeneralConstants.SERVLET_CONTEXT_MAP);
		
		if (map == null)
			return;
		
		Set<String> keySet = map.keySet();
		
		if (keySet == null)
			return;
		
		for (String key : keySet) {
			GeographicInfo geoInfo = map.get(key);
			
			AvailableCars availableCars = new AvailableCars();
			availableCars.setIdentifier(key);
			availableCars.setLatitude(geoInfo.lat);
			availableCars.setLongitude(geoInfo.lon);
			availableCarsService.save(availableCars);
		}	
	}
}