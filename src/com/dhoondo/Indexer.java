package com.dhoondo;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;

public class Indexer {
	private IndexWriter w;
	private TaxonomyWriter tw;
	
	public Indexer(Directory indexDir, Analyzer analyzer) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		this.w = new IndexWriter(indexDir, config);
	}
	
	public Indexer(Directory indexDir, Directory taxonomyDir, Analyzer analyzer) throws IOException {
		this(indexDir, analyzer);
		this.tw = new DirectoryTaxonomyWriter(taxonomyDir);
	}
	
	public void addDoc(String name, String path) throws IOException {
//		System.out.println(name + " " + path);
		Document doc = new Document();
		String extn = name.substring(name.lastIndexOf(".")+1);
		
//		addKeywords(name, doc, " ");
		
		doc.add(new TextField("extn", extn, Field.Store.YES));
		doc.add(new TextField("name", name, Field.Store.YES));
//
//		// use a string field for isbn because we don't want it tokenized
		doc.add(new StringField("path", path, Field.Store.YES));
//		w.addDocument(doc);
		addFacetDoc(name, path,doc);
	}
	private void addFacetDoc(String name, String path, Document doc) {
//		addKeywordsFacet(name, doc, " ");
		String extn = name.substring(name.lastIndexOf(".")+1);
		FacetField ff = new FacetField("extn", extn);
		doc.add(ff);
		FacetField title = new FacetField("title", name);
		doc.add(title);
		FacetsConfig config = new FacetsConfig();
		config.setMultiValued("keywords", true);
		addKeywordsFacet(name, doc, " ");
		try {
			this.w.addDocument(config.build(this.tw, doc));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addKeywordsFacet(String name, Document doc, String token) {
		StringTokenizer tokenizer = new StringTokenizer(name, token);
		Filters textLengthFilter = new AcceptedLengthFilter();
		while (tokenizer.hasMoreTokens()) {
			String element = (String) tokenizer.nextElement();
			if (element.contains(".")) {
				addKeywordsFacet(element, doc, ".");
			} else if (element.contains("-")) {
				addKeywordsFacet(element, doc, "-");
			} else if (element.contains("_")) {
				addKeywordsFacet(element, doc, "_");
			}
			if (textLengthFilter.accept(element)) {
				doc.add(new FacetField("keywords", element));				
			}
		}
	}
	
	public void close() {
		try {
			this.tw.close();
			this.w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
