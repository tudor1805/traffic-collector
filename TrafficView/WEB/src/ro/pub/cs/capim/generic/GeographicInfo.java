package ro.pub.cs.capim.generic;

public class GeographicInfo {
	public double lat;
	public double lon;
	public long timestamp;
	public double speed;
	
	public GeographicInfo(double lat, double lon, long timestamp) {
		this.lat = lat;
		this.lon = lon;
		this.timestamp = timestamp;
	}
	
	public String toString() {
		return "lat: " + lat + ", lon: " + lon;
	}
}
