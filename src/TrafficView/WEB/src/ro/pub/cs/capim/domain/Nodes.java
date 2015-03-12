package ro.pub.cs.capim.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nodes")
public class Nodes implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "node_id")
	private String nodeId;
	
	@Column(name = "street1_id")
	private Long street1Id;

	@Column(name = "street2_id")
	private Long street2Id;
	
	@Column(name = "latitude")
	private Double latitude;
	
	@Column(name = "longitude")
	private Double longitude;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Long getStreet1Id() {
		return street1Id;
	}

	public void setStreet1Id(Long street1Id) {
		this.street1Id = street1Id;
	}

	public Long getStreet2Id() {
		return street2Id;
	}

	public void setStreet2Id(Long street2Id) {
		this.street2Id = street2Id;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "Nodes [nodeId=" + nodeId + ", street1Id=" + street1Id
				+ ", street2Id=" + street2Id + ", latitude=" + latitude
				+ ", longitude=" + longitude + "]";
	}
}
