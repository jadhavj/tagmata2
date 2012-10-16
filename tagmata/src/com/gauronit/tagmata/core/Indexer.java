/*******************************************************************************
 * Copyright (c) 2012 Gauronit Technologies.
 * All rights reserved. Tagmata and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jayesh Jadhav - initial API and implementation
 ******************************************************************************/
package com.gauronit.tagmata.core;

import com.gauronit.tagmata.util.IOUtil;
import com.gauronit.tagmata.util.IndexerUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.util.Version;

public class Indexer {

	private static boolean loaded = false;
	private static final Logger logger = Logger.getLogger(Indexer.class
			.getName());
	private static Indexer indexer;
	private static String indexDir;
	private static final String MAIN_INDEX = "main";
	private IndexSearcher mainIndexSearcher;
	private IndexWriter mainIndexWriter;
	private Map<String, IndexSearcher> indexSearchers = new LinkedHashMap<String, IndexSearcher>();

	private Indexer() {
	}

	public ArrayList getIndexNames() {
		try {
			IndexReader ir = IndexReader.open(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX),
					new SimpleFSLockFactory(indexDir + File.separator
							+ MAIN_INDEX)));
			mainIndexSearcher = new IndexSearcher(ir);
			ArrayList<String[]> indexNames = new ArrayList<String[]>();
			for (Map.Entry index : indexSearchers.entrySet()) {
				String indexName = (String) index.getKey();
				Query q = new QueryParser(Version.LUCENE_35, "indexName",
						new StandardAnalyzer(Version.LUCENE_35))
						.parse(indexName);
				TopScoreDocCollector collector = TopScoreDocCollector.create(1,
						true);
				mainIndexSearcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				indexNames
						.add(new String[] {
								indexName,
								mainIndexSearcher.doc(hits[0].doc).get(
										"displayName") });
			}
			ir.close();
			mainIndexSearcher.close();
			return indexNames;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Indexer currentInstance() throws Exception {
		try {
			if (!loaded) {
				indexer = new Indexer();
				indexer.loadIndexes();
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to create instance of Indexer", ex);
			throw ex;
		}
		return indexer;
	}

	private synchronized void loadIndexes() throws Exception {
		try {
			Properties props = new Properties();
			//props.load(new FileInputStream(new File("indexer.properties")));
			indexDir = new File(".").getCanonicalPath() + File.separator
					+ "indexes"; // props.getProperty("indexDir");
			IndexReader ir = null;
			try {
				ir = IndexReader.open(FSDirectory.open(new File(indexDir
						+ File.separator + MAIN_INDEX),
						new SimpleFSLockFactory(indexDir + File.separator
								+ MAIN_INDEX)));
				mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
						indexDir + File.separator + MAIN_INDEX)),
						new IndexWriterConfig(Version.LUCENE_35,
								new StandardAnalyzer(Version.LUCENE_35)));
			} catch (Exception ex) {
				try {
					IndexWriter iw = new IndexWriter(FSDirectory.open(new File(
							indexDir + File.separator + MAIN_INDEX)),
							new IndexWriterConfig(Version.LUCENE_35,
									new StandardAnalyzer(Version.LUCENE_35)));
					iw.close();
					mainIndexWriter = new IndexWriter(
							FSDirectory.open(new File(indexDir + File.separator
									+ MAIN_INDEX)), new IndexWriterConfig(
									Version.LUCENE_35, new StandardAnalyzer(
											Version.LUCENE_35)));
					ir = IndexReader.open(FSDirectory.open(new File(indexDir
							+ File.separator + MAIN_INDEX),
							new SimpleFSLockFactory(indexDir + File.separator
									+ MAIN_INDEX)));
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failed creating main index", e);
					System.exit(0);
				}
			}
			mainIndexSearcher = new IndexSearcher(ir);
			Query q = new WildcardQuery(new Term("indexName", "*"));
			TopScoreDocCollector collector = TopScoreDocCollector.create(10000,
					false);
			mainIndexSearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (ScoreDoc hit : hits) {
				Document doc = mainIndexSearcher.doc(hit.doc);
				String indexName = doc.get("indexName");
				IndexReader reader = IndexReader.open(FSDirectory.open(
						new File(indexDir + File.separator + indexName),
						new SimpleFSLockFactory(indexDir + File.separator
								+ indexName)));
				IndexSearcher searcher = new IndexSearcher(reader);
				indexSearchers.put(indexName, searcher);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to load indexes", ex);
			throw ex;
		} finally {
			mainIndexWriter.close();
		}
		loaded = true;
	}

	public String createIndex(String indexDisplayName) {
		try {
			UUID uuid = UUID.randomUUID();
			String indexName = uuid.toString().substring(
					uuid.toString().length() - 8, uuid.toString().length());
			IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexDir
					+ File.separator + indexName)), new IndexWriterConfig(
					Version.LUCENE_35, new StandardAnalyzer(Version.LUCENE_35)));
			iw.prepareCommit();
			iw.commit();
			iw.close();
			// indexWriters.put(indexName, iw);
			IndexReader ir = IndexReader.open(FSDirectory.open(new File(
					indexDir + File.separator + indexName),
					new SimpleFSLockFactory(indexDir + File.separator
							+ indexName)));
			IndexSearcher is = new IndexSearcher(ir);
			indexSearchers.put(indexName, is);

			Document doc = new Document();
			doc.add(new Field("displayName", indexDisplayName, Store.YES,
					Index.NOT_ANALYZED));
			doc.add(new Field("indexName", indexName, Store.YES,
					Index.NOT_ANALYZED));

			mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			mainIndexWriter.addDocument(doc);
			mainIndexWriter.commit();
			mainIndexWriter.close();
			mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			return indexName;
		} catch (IOException ex) {
			Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null,
					ex);
			return null;
		}
	}

	public void deleteIndex(String indexName) {
		try {
			mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			Query q = new QueryParser(Version.LUCENE_35, "indexName",
					new StandardAnalyzer(Version.LUCENE_35)).parse(indexName);
			mainIndexWriter.deleteDocuments(q);
			mainIndexWriter.commit();
			mainIndexWriter.close();
			indexSearchers.get(indexName).getIndexReader().close();
			indexSearchers.get(indexName).close();
			indexSearchers.remove(indexName);

			IndexWriter writer = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + indexName)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			writer.deleteAll();
			writer.prepareCommit();
			writer.commit();
			writer.close();

			IOUtil.deleteDir(new File(indexDir + File.separator + indexName));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
	}

	public void renameIndex(String indexName, String indexDisplayName) {
		try {
			mainIndexWriter.close();
			mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			Document doc = new Document();
			doc.add(new Field("displayName", indexDisplayName, Store.YES,
					Index.NOT_ANALYZED));
			doc.add(new Field("indexName", indexName, Store.YES,
					Index.NOT_ANALYZED));

			mainIndexWriter.updateDocument(new Term("indexName", indexName),
					doc, new StandardAnalyzer(Version.LUCENE_35));
			mainIndexWriter.prepareCommit();
			mainIndexWriter.commit();

			mainIndexWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void deleteCards(ArrayList<CardSnapshot> cardSnaps) {
		try {
			for (CardSnapshot cardSnap : cardSnaps) {
				IndexWriter writer = new IndexWriter(FSDirectory.open(new File(
						indexDir + File.separator + cardSnap.getIndexName())),
						new IndexWriterConfig(Version.LUCENE_35,
								new StandardAnalyzer(Version.LUCENE_35)));
				writer.deleteDocuments(new Term("id", cardSnap.getId()));
				writer.prepareCommit();
				writer.commit();
				writer.close();
			}
		} catch (Exception ex) {
		}
	}

	public void saveCard(String title, String tags, String text,
			String indexName) {
		try {
			IndexWriter writer = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + indexName)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));

			Document doc = new Document();
			doc.add(new Field("title", title, Store.YES, Index.ANALYZED));
			doc.add(new Field("tags", tags, Store.YES, Index.ANALYZED));
			doc.add(new Field("text", text, Store.YES, Index.ANALYZED));
			doc.add(new Field("analyzedText", text, Store.YES, Index.ANALYZED));
			doc.add(new Field("indexName", indexName, Store.YES, Index.ANALYZED));
			doc.add(new Field("id", UUID.randomUUID().toString(), Store.YES,
					Index.NOT_ANALYZED));
			writer.addDocument(doc, new StandardAnalyzer(Version.LUCENE_35));
			writer.prepareCommit();
			writer.commit();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void saveBookmark(String id, String indexName) {
		try {
			mainIndexWriter.close();
			mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));

			Document doc = new Document();
			doc.add(new Field("qcId", id, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("qcIndexName", indexName, Store.YES,
					Index.NOT_ANALYZED));

			mainIndexWriter.updateDocument(new Term("id", id), doc);
			mainIndexWriter.prepareCommit();
			mainIndexWriter.commit();
			mainIndexWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void deleteBookmark(String id, String indexName) {
		try {
			mainIndexWriter = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX)),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			mainIndexWriter.deleteDocuments(new Term("qcId", id));
			mainIndexWriter.prepareCommit();
			mainIndexWriter.commit();
			mainIndexWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public ArrayList<CardSnapshot> getBookmarks() {
		ArrayList<CardSnapshot> cardSnaps = new ArrayList();
		try {
			IndexReader ir = IndexReader.open(FSDirectory.open(new File(
					indexDir + File.separator + MAIN_INDEX),
					new SimpleFSLockFactory(indexDir + File.separator
							+ MAIN_INDEX)));
			mainIndexSearcher = new IndexSearcher(ir);

			Query q = new WildcardQuery(new Term("qcId", "*"));
			TopScoreDocCollector collector = TopScoreDocCollector.create(10000,
					false);
			mainIndexSearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (ScoreDoc hit : hits) {
				Document doc = mainIndexSearcher.doc(hit.doc);
				IndexReader reader = IndexReader.open(FSDirectory.open(
						new File(indexDir + File.separator
								+ doc.get("qcIndexName")),
						new SimpleFSLockFactory(indexDir + File.separator
								+ doc.get("qcIndexName"))));
				IndexSearcher searcher = new IndexSearcher(reader);

				q = new TermQuery(new Term("id", doc.get("qcId")));
				collector = TopScoreDocCollector.create(10000, false);
				searcher.search(q, collector);
				ScoreDoc[] hits2 = collector.topDocs().scoreDocs;

				doc = searcher.doc(hits2[0].doc);

				cardSnaps.add(new CardSnapshot("", doc));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return cardSnaps;
	}

	public void updateCard(CardSnapshot cardSnap, String title, String tags,
			String text) {
		try {
			IndexWriter writer = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + cardSnap.getIndexName())),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));

			Document doc = new Document();
			doc.add(new Field("title", title, Store.YES, Index.ANALYZED));
			doc.add(new Field("tags", tags, Store.YES, Index.ANALYZED));
			doc.add(new Field("text", text, Store.YES, Index.ANALYZED));
			doc.add(new Field("analyzedText", text, Store.YES, Index.ANALYZED));
			doc.add(new Field("indexName", cardSnap.getIndexName(), Store.YES,
					Index.ANALYZED));
			doc.add(new Field("id", cardSnap.getId(), Store.YES,
					Index.NOT_ANALYZED));

			writer.updateDocument(new Term("id", cardSnap.getId()), doc,
					new StandardAnalyzer(Version.LUCENE_35));
			writer.prepareCommit();
			writer.commit();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ArrayList<CardSnapshot> search(String searchText,
			ArrayList<String> indexNames, boolean searchInTitle,
			boolean searchInTags, boolean searchInText, boolean superFuzzy) {
		ArrayList<CardSnapshot> cardSnaps = new ArrayList();
		try {
			ArrayList<IndexSearcher> searchers = new ArrayList<IndexSearcher>();

			for (String indexName : indexNames) {
				IndexReader reader = IndexReader.open(FSDirectory.open(
						new File(indexDir + File.separator + indexName),
						new SimpleFSLockFactory(indexDir + File.separator
								+ indexName)));
				IndexSearcher searcher = new IndexSearcher(reader);
				searchers.add(searcher);
			}

			BooleanQuery query = new BooleanQuery();
			if (searchInTitle) {
				IndexerUtil.getTokenizedQuery(query, "title", searchText,
						superFuzzy);
			}
			if (searchInTags) {
				IndexerUtil.getTokenizedQuery(query, "tags", searchText,
						superFuzzy);
			}
			if (searchInText) {
				IndexerUtil.getTokenizedQuery(query, "text", searchText,
						superFuzzy);
				IndexerUtil.getTokenizedQuery(query, "analyzedText",
						searchText, superFuzzy);
			}

			for (IndexSearcher searcher : searchers) {
				TopScoreDocCollector collector = TopScoreDocCollector.create(
						10000, false);
				searcher.search(query, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				for (ScoreDoc hit : hits) {
					Document doc = searcher.doc(hit.doc);

					TokenStream stream = TokenSources.getTokenStream("text",
							doc.get("analyzedText"), new StandardAnalyzer(
									Version.LUCENE_20.LUCENE_35));
					QueryScorer scorer = new QueryScorer(query, "analyzedText");
					Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 20);
					Highlighter highlighter = new Highlighter(scorer);
					highlighter.setTextFragmenter(fragmenter);
					String[] fragments = highlighter.getBestFragments(stream,
							doc.get("text"), 5);
					String highlights = "";

					for (String fragment : fragments) {
						highlights += fragment + "...";
					}

					if (highlights.equals("")) {
						String text = doc.get("text");
						if (text.length() > 100) {
							highlights += doc.get("text").substring(0, 100);
						} else {
							highlights += doc.get("text");
						}
					}

					cardSnaps.add(new CardSnapshot(highlights, doc));
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return cardSnaps;
	}

	public static void main(String[] args) {
		try {
			IndexWriter writer = new IndexWriter(FSDirectory.open(new File(
					indexDir + File.separator + "null")),
					new IndexWriterConfig(Version.LUCENE_35,
							new StandardAnalyzer(Version.LUCENE_35)));
			writer.deleteDocuments(new Term("indexName", "null"));
			writer.prepareCommit();
			writer.commit();
			writer.close();
			
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to Initialize", ex);
		}
	}
}
