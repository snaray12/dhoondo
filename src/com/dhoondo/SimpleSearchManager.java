package com.dhoondo;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class SimpleSearchManager extends Search{
	private Analyzer analyzer;
	
	int hitsPerPage = 1000;

	private Directory indexDir;

	private Directory taxonomyDir;

	private String queryCat;

	private String queryStr;

	@Override
	public Directory getIndexDirectory() {
		System.out.println("Index Directory is "+this.indexDir);
		return this.indexDir;
	}

	@Override
	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	@Override
	protected Directory getTaxonomyDirectory() {
		return this.taxonomyDir;
	}

	@Override
	protected String getQueryCategory() {
		return this.queryCat;
	}

	@Override
	protected String getQueryString() {
		return this.queryStr;
	}
	
	@Override
	public void setQueryCategory(String queryCategory) {
		this.queryCat = queryCategory;
	}
	
	@Override
	protected void setQueryString(String queryString) {
		this.queryStr = queryString;
	}
	
	private int getHitsPerPage() {
		return this.hitsPerPage;
	}
	
	public SimpleSearchManager(Analyzer analyzer, Directory indexDir) {
		this.analyzer = analyzer;
		this.indexDir = indexDir;
	}
	
	public SimpleSearchManager(Analyzer analyzer, Directory indexDir, String searchCategory, String searchQuery) {
		this(analyzer, indexDir);
		this.queryCat = searchCategory;
		this.queryStr = searchQuery;
	}

	@Override
	protected void processSearch() {
		try {
			Query q = new QueryParser(getQueryCategory(), getAnalyzer()).parse(getQueryString());
			IndexSearcher searcher = getIndexSearcher();
			TopDocs docs = searcher.search(q, getHitsPerPage());
			ScoreDoc[] hits = docs.scoreDocs;

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				System.out.println((i + 1) + ". " + d.get("name") + "\t" + d.get("path"));
			}
			closeIndexReader();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
