package com.dhoondo;

@FunctionalInterface
public interface Filters {

	boolean accept(String text);
}
