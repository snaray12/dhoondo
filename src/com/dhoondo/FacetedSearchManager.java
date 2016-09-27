package com.dhoondo;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class FacetedSearchManager extends Search {
	private Analyzer analyzer;
	
	int hitsPerPage = 1000;

	private Directory indexDir;

	private Directory taxonomyDir;

	private String queryCat;

	private String queryStr;
	
	public FacetedSearchManager(Directory indexDir, Directory taxonomyDir, Analyzer analyzer) {
		this.indexDir = indexDir;
		this.taxonomyDir = taxonomyDir;
		this.analyzer = analyzer;
	}

	@Override
	public Directory getIndexDirectory() {
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
	
	@Override
	protected void processSearch() {
		try {
			IndexSearcher searcher = getIndexSearcher();
			TaxonomyReader tr = new DirectoryTaxonomyReader(getTaxonomyDirectory());
			FacetsCollector fc = new FacetsCollector();
			FacetsConfig config = new FacetsConfig();
			DrillDownQuery ddq = new DrillDownQuery(config);
			ddq.add(getQueryCategory(), getQueryString());
			TopDocs docs = FacetsCollector.search(searcher, ddq, this.hitsPerPage, fc);
			ScoreDoc[] hits = docs.scoreDocs;

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				System.out.println((i + 1) + ". " + d.get("name") + "\t" + d.get("path"));
			}
			Facets facets = new FastTaxonomyFacetCounts(tr, config, fc);
			FacetResult results = facets.getTopChildren(this.hitsPerPage, getQueryCategory());
			closeIndexReader();
			tr.close();
			if (null != results) {
				System.out.println(results);
				LabelAndValue[] lav = results.labelValues;
				for(LabelAndValue lv: lav) {
					System.out.println(lv.label + " - " + lv.value);
				}
			} else {
				System.out.println("0 Results found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
