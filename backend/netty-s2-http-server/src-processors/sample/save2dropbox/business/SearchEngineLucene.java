package sample.save2dropbox.business;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import rfx.server.util.StringUtil;
import sample.save2dropbox.model.Item;

/**
 * @author trieu
 * why not Solr or Elastic Seach, we need consider (Jimfs is an in-memory file system for lucene's Directory) https://github.com/google/jimfs
 *
 */
public class SearchEngineLucene {

	static void addUserWithKeyword(IndexWriter w, Item item)
			throws IOException {
		Document doc = new Document();
		doc.add(new TextField("keywords", item.getKeywordsAsString(), Field.Store.YES));
		doc.add(new StringField("user_id", item.getUser_id()+"", Field.Store.YES));
		doc.add(new StringField("title", item.getTitle(), Field.Store.YES));
		doc.add(new StringField("post_id", item.getPost_id()+"", Field.Store.YES));
		doc.add(new StringField("dp_link", item.getDp_link(), Field.Store.YES));
		doc.add(new StringField("link", item.getLink(), Field.Store.YES));
		w.addDocument(doc);
	}
	
	static Item documentToItem(Document doc){
		int post_id = StringUtil.safeParseInt(doc.get("post_id"));
		String keywords = doc.get("keywords");
		String title = doc.get("title");
		String dp_link = doc.get("dp_link");
		String link = doc.get("link");
		int user_id = StringUtil.safeParseInt(doc.get("user_id"));
		return new Item(post_id, keywords, dp_link, title, link, user_id);
	}
	
	public static boolean indexItems(List<Item> items){
		boolean create = false;
		Directory directory = null;
		try {
			// 0. Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching			

			// 1. create the index			
			File indexDirFile = new File("data/lucene-index");
			if ( ! indexDirFile.exists() || !indexDirFile.isDirectory()) {
				create = indexDirFile.mkdir();
			} else {
				create = true;
			}
			
			System.out.println(indexDirFile.getAbsolutePath());
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

			// To store an index on disk
			directory = FSDirectory.open(indexDirFile);

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,	analyzer);

			IndexWriter w = new IndexWriter(directory, config);
			
			for (Item item : items) {
				addUserWithKeyword(w, item);
			}
			w.commit();
			w.close();			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(directory != null){
				try {
					directory.close();
				} catch (IOException e) {}
			}
		}
		return create;
	}
	
	public static List<Item> searchItemsByKeywords(List<String> keywords, int user_id){
		List<Item> matchedItems = new ArrayList<>();
		Directory directory = null;
		try {
			// 0. Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching
			// 1. the index
			boolean create = false;
			File indexDirFile = new File("data/lucene-index");
			if ( ! indexDirFile.exists() || !indexDirFile.isDirectory()) {
				create = indexDirFile.mkdir();
			} else {
				create = true;
			}
			
			System.out.println(indexDirFile.getAbsolutePath());
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

			// To store an index on disk
			directory = FSDirectory.open(indexDirFile);

			// 2. query
			StringBuilder querystr = new StringBuilder();
			for (String keyword : keywords) {
				querystr.append("\"").append(keyword).append("\" ");
			}
			System.out.println(querystr);
		
			Query kq = new QueryParser(Version.LUCENE_47, "keywords", analyzer).parse(querystr.toString().trim());
			
			BooleanQuery query = new BooleanQuery();
			query.add(kq, Occur.MUST);
			
			if(user_id > 0){
				//only recommend items that not owned by user
				Query uq = new QueryParser(Version.LUCENE_47, "user_id", analyzer).parse("\""+user_id+"\"");
				query.add(uq, Occur.MUST_NOT);
			}

			// 3. search
			int hitsPerPage = 10;
			IndexReader reader = DirectoryReader.open(directory);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// 4. display results
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				matchedItems.add(documentToItem(d));
				//System.out.println((i + 1) + ". " + d.get("keywords") + "\t"+ d.get("title"));
			}
			
			// is no need to access the documents any more.
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(directory != null){
				try {
					directory.close();
				} catch (IOException e) {}
			}
		}	
		return matchedItems;
	}

	public static void main(String[] args) {
//		indexItems(Arrays.asList(
//				new Item(5, "cloud computing, cloud storage", "http:/111", "item 5", "http:/111", 1)
//				,new Item(6, "big data, cloud computing", "http:/111", "item 6", "http:/111", 1)
//				));
		//indexItems(new ArrayList<Item>());
		List<Item> items = searchItemsByKeywords(Arrays.asList("framework"), 0);
		for (Item item : items) {
			System.out.println(item);
		}
		
	}
}
