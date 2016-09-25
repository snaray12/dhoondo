package com.dhoondo;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesManager {
	
	private Properties props;
	private static PropertiesManager manager;
	private String path = "myprops.properties";
	private PropertiesManager() {
		this.props = new Properties();
		this.loadProps(path);
	}
	
	public static String getProperties(String key) {
		if(null == manager) {
			manager = new PropertiesManager();
		}
		return manager.getProperty(key);
	}
	
	private String getProperty(String key) {
		return this.props.getProperty(key);
	}
	
	private void loadProps(String filePath) {
		try {
			this.props.load(new InputStreamReader(new FileInputStream(new File(filePath))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
