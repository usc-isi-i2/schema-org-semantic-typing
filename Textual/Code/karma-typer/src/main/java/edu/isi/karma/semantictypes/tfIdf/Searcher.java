package edu.isi.karma.semantictypes.tfIdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;

/**
 * This class is responsible for predicting top-k suggestions for textual data
 * using TF-IDF based cosine similarity approach and checking if a document for
 * a semantic label already exists
 * 
 * @author ramnandan
 * 
 */
public class Searcher {
	private IndexSearcher indexSearcher = null;
	private Analyzer analyzer = null;
	private QueryParser parser = null;
	
	public static int MAX_TESTING_CONTENT_LENGTH = 1000000;

	public Searcher(String filepath, String fieldName) throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
				filepath)));
		indexSearcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer(Version.LUCENE_48);
		if (fieldName.equalsIgnoreCase(Indexer.LABEL_FIELD_NAME)) {
			parser = new QueryParser(Version.LUCENE_48,
					Indexer.LABEL_FIELD_NAME, analyzer);
		} else {
			parser = new QueryParser(Version.LUCENE_48,
					Indexer.CONTENT_FIELD_NAME, analyzer);
		}

	}

	public List<SemanticTypeLabel> getTopK(int k, String content)
			throws ParseException, IOException {
		List<SemanticTypeLabel> result = new ArrayList<>();
		
		//content = content.toLowerCase().replaceAll("and", " ").replaceAll("or", " ").replaceAll("\\+", "").replaceAll("\\-", "");
		content = content.toLowerCase().replaceAll(" and ", " ").replaceAll(" or ", " ").replaceAll("\\+", "").replaceAll("\\-", "");
		content = content.trim();
		if(content== null || content.length() == 0){
			return null;
		}
		//The above was done to protect from the following exception
		/*
		 * NV NV NV ': Encountered " <OR> "OR "" at line 1, column 453.
	Was expecting one of:
    <NOT> ...
    "+" ...
    "-" ...
    <BAREOPER> ...
    "(" ...
    "*" ...
    <QUOTED> ...
    <TERM> ...
    <PREFIXTERM> ...
    <WILDTERM> ...
    <REGEXPTERM> ...
    "[" ...
    "{" ...
    <NUMBER> ...
    <TERM> ...
    "*" ...

		 */
		
		//int spaces = content.length() - content.replace(" ", "").length();
		int clauses = content.split("\\s").length;
		if (clauses > BooleanQuery.getMaxClauseCount()) {
			BooleanQuery.setMaxClauseCount(clauses);
		}
		/**
		 * @author pranav
		 * @date 11th July 
		 * @patch applied : This has been done so as we can save on memory.
		 * 
		 * But, still after a certain point, the memory is overflowing.
		 * We have a limit on the number of MAXIMUM TESTING EXAMPLES but each 
		 * testing example could be huge. This is leading to 
		 * Java out of heap space memory error
		 * 
		 * Suppose the content length = x
		 * Then the query length = 2*x (+-) delta
		 * Thus, on an average thrice amount of memory may be required at any instant
		 * 
		 * So, we need to fix this. Instead of now, putting a limit on number of 
		 * testing tuples, I am now going to put a limit on the length of the 
		 * content.
		 * 
		 */
		
		int contentLength = content.length();
		if(contentLength > MAX_TESTING_CONTENT_LENGTH){
			content = content.substring(0, MAX_TESTING_CONTENT_LENGTH);
		}

		//System.out.println("Query: " + content);
		Query query = parser.parse(QueryParser.escape(content));
		//System.out.println(query.toString());
		
		System.out.println("Query Length : " +  query.toString().length() + "Content Length : " + content.length());		
		content=null; //Done so as to save on memory.
		

		TopDocs results = indexSearcher.search(query, k);
		/**
		 * @author pranav
		 * @date 11th July 
		 * @patch applied : This has been done so as we can save on memory.
		 */
		query=null; //Done so as to save on memory.
		ScoreDoc[] hits = results.scoreDocs;
		
		//System.out.println("Num Hits:" + hits.length);
		
		for (int i = 0; i < hits.length; i++) {
			Document doc = indexSearcher.doc(hits[i].doc);
			String labelString = doc.get(Indexer.LABEL_FIELD_NAME);
			result.add(new SemanticTypeLabel(labelString, hits[i].score));
		}
		return result;
	}

	public Document getDocumentForLabel(String label) throws IOException {
		Query query = new TermQuery(
				new Term(Indexer.LABEL_FIELD_NAME, label));
		TopDocs results = indexSearcher.search(query, 10);
		ScoreDoc[] hits = results.scoreDocs;

		for(int i=0; i<hits.length; i++) {
			Document doc = indexSearcher.doc(hits[i].doc);
			String labelString = doc.get(Indexer.LABEL_FIELD_NAME);
			if (labelString.equalsIgnoreCase(label)) // document for
															// exact semantic
															// label already
															// exists
			{
				return doc;
			}		
				
		}
		return null;
	}
	

	public void close() {
		try {
			indexSearcher.getIndexReader().close();
		} catch (IOException e) {
			// Ignore
		}
	}

}
