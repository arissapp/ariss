package com.jala.ariss.model;

import com.jala.ariss.util.SpaceMath;

public class SpaceObject {

	private final Coord _coordinates;	
	private float _azimuth;
	private float _elevation;
	
	public SpaceObject(float latitude, float longitude){
		_coordinates = new Coord(latitude, longitude);
	}
	
	public SpaceObject(Coord coordinates){
		_coordinates = coordinates;
	}
	
	public SpaceObject(){
		_coordinates = new Coord();
	}
	
	public void setCoordinates(float latitude, float longitude){
		_coordinates.setLatitude(latitude);
		_coordinates.setLongitude(longitude);
	}
	
	public void setCoordinates(Coord coordinates){		
		_coordinates.setLatitude(coordinates.getLatitude());
		_coordinates.setLongitude(coordinates.getLongitude());
	}
	
	public Coord getCoordinates(){
		return _coordinates;
	}	
	
	public float getAzimuth(){
		return _azimuth;		
	}
	
	public float getElevation(){
		return _elevation;
	}
	
	public void calculateHorizontalCoordinates(Coord observerCoord){
		float localHourAngle = (float)Math.toRadians(_coordinates.getLongitude() - observerCoord.getLongitude());
		float observerLatitudeRad = (float)Math.toRadians(observerCoord.getLatitude());
		float objectLatitudeRad = (float)Math.toRadians(_coordinates.getLatitude());
		
		_elevation = SpaceMath.calculateElevation(observerLatitudeRad,
													objectLatitudeRad, 
													localHourAngle);
		
		_azimuth = (float)Math.toDegrees(SpaceMath.calculateAzimuth(objectLatitudeRad, localHourAngle, _elevation));
		_elevation = -(float)Math.toDegrees(_elevation);
	}
}
