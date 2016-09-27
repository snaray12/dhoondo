package com.dhoondo;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public abstract class Search {
	
	private IndexReader reader = null;
	
	protected abstract Directory getIndexDirectory();
	protected abstract Directory getTaxonomyDirectory();
	protected abstract Analyzer getAnalyzer();
	protected abstract String getQueryCategory();
	protected abstract String getQueryString();
	protected abstract void processSearch();
	protected abstract void setQueryCategory(String queryCategory);
	protected abstract void setQueryString(String queryString);
	
	public void search() throws IOException {
		processSearch();
	}
	
	private IndexReader getIndexReader() throws IOException {
		if (null == reader) 
			reader = DirectoryReader.open(getIndexDirectory());
		return reader;		
	}


	protected void closeIndexReader() throws IOException {
		getIndexReader().close();
	}
	protected IndexSearcher getIndexSearcher() throws IOException {
		IndexSearcher searcher = new IndexSearcher(getIndexReader());
		return searcher;
	}

}
