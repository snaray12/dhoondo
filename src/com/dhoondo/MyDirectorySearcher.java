/**
 * This is my comments section
 * I can enter anything in this section
 * But basically I will enter details such as Author, Date of creation, Version, description about the class
 * 
 */

package com.dhoondo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

public class MyDirectorySearcher {
	private Analyzer analyzer;
	private Directory index;
	private Directory taxoDir;
	final static int CREATE = 1;
	final static int SEARCH = 2;
	final static int FACET_SEARCH=3;

	public MyDirectorySearcher(Directory d, Directory taxoDir, Analyzer a, String queryCategory, String querystr) {
		this.index = d;
		this.taxoDir = taxoDir;
		this.analyzer = a;
		Search facetedSearch = new FacetedSearchManager(d, taxoDir, a);
		facetedSearch.setQueryCategory(queryCategory);
		facetedSearch.setQueryString(querystr);
		try {
			facetedSearch.search();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MyDirectorySearcher(Directory d, Analyzer a, String queryCategory, String queryStr) {
		this.index = d;
		this.analyzer = a;
		Search simpleSearch = new SimpleSearchManager(a, d);
		simpleSearch.setQueryCategory(queryCategory);
		simpleSearch.setQueryString(queryStr);
		try {
			simpleSearch.search();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MyDirectorySearcher(String directory, Directory d, Directory taxoDir, Analyzer a, FileFilter filter, FileFilter folderFilter) {
		this.index = d;
		this.taxoDir = taxoDir;
		this.analyzer = a;
//		processIndexCreation(directory, filter, folderFilter);
		try {
			Indexer indexer = new Indexer(this.index, this.taxoDir, this.analyzer);
			parseDirectory(directory, filter, indexer, folderFilter);
			indexer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseDirectory(String dataDirPath, FileFilter filter, Indexer indexer, FileFilter folderFilter) throws IOException {
		File[] files = new File(dataDirPath).listFiles();

		if (null != files) {
			for (File file : files) {
				if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
					indexer.addDoc(file.getName(), file.getAbsolutePath());
				} else if (file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && folderFilter.accept(file)) {
					parseDirectory(file.getPath(), filter, indexer, folderFilter);
				}
			}	
		}
		
	}

	public static void main(String[] args) {
		
		//1. HD
		//2. HD1
		//3. KB
		//4. Archana
		//5. CEH
		String directoryToIndex = "G:\\";
		String indexDirectoryPath = "F:\\index";
		String taxoDirPath = "F:\\index\\taxodir";
		String querystr = "security";
		String queryCategory = "keywords";
		Directory index = null;
		Directory taxoDir = null;
		int mode = SEARCH;
		if (args.length > 0) {
			mode = Integer.parseInt(args[0]);
		}

		// index = new RAMDirectory();
		Path path = Paths.get(indexDirectoryPath);
		Path taxoPath = Paths.get(taxoDirPath);
		try {
			index = new MMapDirectory(path);
			taxoDir = new MMapDirectory(taxoPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		switch (mode) {
		case CREATE:
			new MyDirectorySearcher(directoryToIndex, index, taxoDir, new StandardAnalyzer(), new DocumentFilter(), new FolderFilter());
			break;
		case SEARCH:
			new MyDirectorySearcher(index, new StandardAnalyzer(), queryCategory, querystr);
			break;
		case FACET_SEARCH:
			new MyDirectorySearcher(index, taxoDir, new StandardAnalyzer(), queryCategory, querystr);
			break;
		default:
			new MyDirectorySearcher(index, taxoDir, new StandardAnalyzer(), queryCategory,querystr);
		}
	}

}

// http://www.lucenetutorial.com/lucene-in-5-minutes.html
