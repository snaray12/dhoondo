package com.dhoondo;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class FolderFilter implements FileFilter {
	Set<String> ignoreFolders = null;
	
	public FolderFilter() {
		ignoreFolders = new HashSet<String>();
		String ignoreList = PropertiesManager.getProperties("IGNOREFOLDERNAMES");
		for(String folder : ignoreList.split(",")) {
			ignoreFolders.add(folder);
		}
	}

	@Override
	public boolean accept(File path) {
		boolean canAccept = true;
		if(path.isDirectory() && ignoreFolders.contains(path.getName())) {
			canAccept = false;
			System.out.println("Ignoring folder " + path.getName());
		} else {
			System.out.println("Accepting folder " + path.getName());
		}
		
		return canAccept;
	}

}
