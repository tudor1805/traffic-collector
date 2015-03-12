package ro.pub.cs.capim.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "streets")
public class Streets implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "street_id")
	private Long streetId;
	
	@Column(name = "name")
	private String name;

	@Column(name = "maxspeed")
	private Integer maxSpeed;
	
	@Column(name = "oneway")
	private Boolean oneWay;

	public Long getStreetId() {
		return streetId;
	}

	public void setStreetId(Long streetId) {
		this.streetId = streetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(Integer maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public Boolean getOneWay() {
		return oneWay;
	}

	public void setOneWay(Boolean oneWay) {
		this.oneWay = oneWay;
	}

	@Override
	public String toString() {
		return "Streets [streetId=" + streetId + ", name=" + name
				+ ", maxSpeed=" + maxSpeed + ", oneWay=" + oneWay + "]";
	}
}
