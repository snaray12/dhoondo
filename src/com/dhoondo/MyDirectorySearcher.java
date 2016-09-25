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
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
		processFacetedSearch(queryCategory, querystr);
	}
	
	public MyDirectorySearcher(Directory d, Analyzer a, String queryCategory, String queryStr) {
		this.index = d;
		this.analyzer = a;
		processSearchOperation(queryCategory, queryStr);
	}

	public MyDirectorySearcher(String directory, Directory d, Directory taxoDir, Analyzer a, FileFilter filter, FileFilter folderFilter) {
		this.index = d;
		this.taxoDir = taxoDir;
		this.analyzer = a;
		processIndexCreation(directory, filter, folderFilter);
	}
	private void processSearchOperation(String queryCategory, String querystr) {
		int hitsPerPage = 1000;
		processSearchOperation(queryCategory, querystr, hitsPerPage);
	}

	private void processSearchOperation(String queryCategory, String querystr, int hitsPerPage) {
		try {
			Query q = new QueryParser(queryCategory, this.analyzer).parse(querystr);
			// 3. search
//			TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(10);
//			MultiCollector.wrap(topScoreDocCollector, facetCollector);
			IndexReader reader = DirectoryReader.open(this.index);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			
			// Retrieve results

					
//			FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);			
//			List<FacetResult> results = new ArrayList<FacetResult>();
			// Count both "Publish Date" and "Author" dimensions
//			Facets facets = new FastTaxonomyFacetCounts(tr, config, fc);
//			results.add(facets.getTopChildren(10, "extn"));
			
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				System.out.println((i + 1) + ". " + d.get("name") + "\t" + d.get("path"));
			}

			// reader can only be closed when there
			// is no need to access the documents any more.
			
			reader.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processFacetedSearch(String queryCategory, String querystr) {
		FacetResult results = null;
		try {
			IndexReader reader = DirectoryReader.open(this.index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TaxonomyReader tr = new DirectoryTaxonomyReader(this.taxoDir);
			FacetsCollector fc = new FacetsCollector();
			FacetsConfig config = new FacetsConfig();
			DrillDownQuery ddq = new DrillDownQuery(config);
			ddq.add(queryCategory, querystr);
			FacetsCollector.search(searcher, ddq, 10, fc);
			Facets facets = new FastTaxonomyFacetCounts(tr, config, fc);
			results = facets.getTopChildren(10, queryCategory);
			
			reader.close();
			tr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (null != results) {
			System.out.println(results);
			LabelAndValue[] lav = results.labelValues;
			for(LabelAndValue lv: lav) {
				System.out.println(lv.label + " - " + lv.value);
			}
		} else {
			System.out.println("0 Results found");
		}
	}

	private void processIndexCreation(String directory, FileFilter filter, FileFilter folderFilter) {
		IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);


		try {
			IndexWriter w = new IndexWriter(this.index, config);
			TaxonomyWriter tw = new DirectoryTaxonomyWriter(this.taxoDir);
			parseDirectory(directory, filter, w, tw, folderFilter);
			tw.close();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseDirectory(String dataDirPath, FileFilter filter, IndexWriter w, TaxonomyWriter tw, FileFilter folderFilter) throws IOException {
		File[] files = new File(dataDirPath).listFiles();

		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				addDoc(w, file.getName(), file.getAbsolutePath(), tw);
			} else if (file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && folderFilter.accept(file)) {
				parseDirectory(file.getPath(), filter, w, tw, folderFilter);
			}
		}
	}

	private void addDoc(IndexWriter w, String name, String path, TaxonomyWriter tw) throws IOException {
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
		addFacetDoc(w, name, path, tw, doc);
	}
	private void addFacetDoc(IndexWriter w, String name, String path, TaxonomyWriter tw, Document doc) {
//		addKeywordsFacet(name, doc, " ");
		String extn = name.substring(name.lastIndexOf(".")+1);
		FacetField ff = new FacetField("extn", extn);
		doc.add(ff);
		FacetField title = new FacetField("title", name);
		doc.add(title);
		FacetsConfig config = new FacetsConfig();
		try {
			w.addDocument(config.build(tw, doc));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addKeywordsFacet(String name, Document doc, String token) {
		StringTokenizer tokenizer = new StringTokenizer(name, token);
		while (tokenizer.hasMoreTokens()) {
			String element = (String) tokenizer.nextElement();
			if (element.contains(".")) {
				addKeywordsFacet(element, doc, ".");
			} else if (element.contains("-")) {
				addKeywordsFacet(element, doc, "-");
			} else if (element.contains("_")) {
				addKeywordsFacet(element, doc, "_");
			}
			doc.add(new FacetField("keywords", element));
		}
	}

	public static void main(String[] args) {
		
		//1. HD
		//2. HD1
		//3. KB
		//4. Archana
		//5. CEH
		String directoryToIndex = "E:\\G1777";
		String indexDirectoryPath = "F:\\index";
		String taxoDirPath = "F:\\index\\taxodir";
		String querystr = "java";
		String queryCategory = "name";
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
