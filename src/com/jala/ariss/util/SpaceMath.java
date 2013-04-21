package com.jala.ariss.util;

public class SpaceMath {
	
	public static float calculateAzimuth(float objectLatitude, float localHourAngle, float elevation){
		return (float)Math.asin(Math.cos(objectLatitude) * (Math.sin(localHourAngle))/Math.cos(elevation));		
	}
	
	public static float calculateElevation(float observerLatitude, float objectLatitude, float localHourAngle){
		return (float)Math.asin(Math.sin(observerLatitude)*Math.sin(objectLatitude) 
				+ Math.cos(observerLatitude)*Math.cos(objectLatitude)*Math.cos(localHourAngle));
	}
			
}
