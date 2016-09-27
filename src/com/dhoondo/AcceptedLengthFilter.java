package com.dhoondo;

public class AcceptedLengthFilter implements Filters{

	final int ACCEPTEDLENGTH = 4;
	@Override
	public boolean accept(String text) {
		if(null != text && !text.trim().isEmpty() && text.length() > ACCEPTEDLENGTH) {
			return true;
		}
		return false;
	}

}
