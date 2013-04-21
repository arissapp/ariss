package com.jala.ariss.model;

public class Coord {
	private float latitude;
	private float longitude;
	
	public Coord(float lat, float lon){
		this.latitude = lat;
		this.longitude = lon;
	}

	public Coord(){
		this.latitude = 0;
		this.longitude = 0;
	}
	
	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	
	
}
