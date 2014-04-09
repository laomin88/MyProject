package com.charmenli.scalephone.util;

public class Waiter {

	public static final void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
