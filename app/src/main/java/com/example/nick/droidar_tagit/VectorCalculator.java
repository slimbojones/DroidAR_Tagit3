package com.example.nick.droidar_tagit;

import util.Vec;

/**
 * Created by nick on 1/18/2018.
 */

public class VectorCalculator {

	public Vec getVec2(Vec vec1, Vec vec2) {

		double long1 = vec1.x;
		double lat1 = vec1.y;
		double alt1 = vec1.z;

		double long2 = vec2.x;
		double lat2 = vec2.y;
		double alt2 = vec2.z;

		final int EARTH_RADIUS = 6371000;
		Vec calcVec;

		// Common values
		double b = EARTH_RADIUS + alt2;
		double c = EARTH_RADIUS + alt1;

		double b2 = b * b;
		double c2 = c * c;
		double bc2 = 2 * b * c;

		// Longitudinal calculations
		double alpha = long2 - long1;
		// Conversion to radian
		alpha = alpha * Math.PI / 180;
		// Small-angle approximation
		double cos = 1 - alpha * alpha / 2; //Math.cos(alpha);
		// Use the law of cosines / Al Kashi theorem
		double x = Math.sqrt(b2 + c2 - bc2 * cos);

		// Repeat for latitudinal calculations
		alpha = lat2 - lat1;
		alpha = alpha * Math.PI / 180;
		cos = 1 - alpha * alpha / 2; //Math.cos(alpha);
		double y = Math.sqrt(b2 + c2 - bc2 * cos);

		// Obtain vertical difference, too
		double z = alt2 - alt1;
		calcVec = new Vec((float) x, (float) y, (float) z);

		return calcVec;
	}

	public Vec getVec(double long1, double lat1, double alt1, double long2, double lat2, double alt2) {

		Vec calcVec;

		double x = long2 - long1;
		double y = lat2 - lat1;
		x = (x * 111133.3333);
		y = (y * 111133.3333);

		// Obtain vertical difference, too
		double z = alt2 - alt1;
		calcVec = new Vec((float) x, (float) y, (float) z);

		return calcVec;
	}
}
