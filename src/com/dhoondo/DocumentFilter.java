package com.dhoondo;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class DocumentFilter implements FileFilter {
	Set<String> keySet = null;

	Set<String> set = null;
	
	public DocumentFilter () {
		keySet = new HashSet<String>();
		set = new HashSet<String>();
		String extns = PropertiesManager.getProperties("ALLOWEDEXTENSIONS");
		for(String extn : extns.split(",")) {
			set.add(extn);
		}
	}
	@Override
	public boolean accept(File pathname) {
		int lastIndexOfDot = pathname.getName().lastIndexOf(".");
//		System.out.println(lastIndexOfDot);
		String extn = pathname.getName().toLowerCase().substring(lastIndexOfDot+1);
//		System.out.println(extn);
		if(keySet.add(extn)) {
			System.out.println(keySet);
		}
		return set.contains(extn);
	}

}
