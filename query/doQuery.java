package query;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;


public class doQuery {
	 public static IndexSearcher searcher = null; 
	 
	 public static Analyzer analyzer = null; 
	 public String searchDir=""; 
	 private static File indexFile = null; 
	 public Query query = null;  
	 public static QueryParser parser;
	 public WildcardQuery wildquery=null;
	 public doQuery(String indexfile)
	 {
		 searchDir=indexfile;
		 if(analyzer==null)
		 {
			 analyzer = new IKAnalyzer(); 
		 }
		 if(indexFile==null)
		 {
			 indexFile = new File(searchDir);
		 }
		 if (searcher == null) {  
	            indexFile = new File(searchDir);  
	            IndexReader reader;
				try {
					reader = DirectoryReader.open(FSDirectory  
					        .open(indexFile));
					searcher = new IndexSearcher(reader);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
	              
	     }
		 if(parser==null)
		 {
			 Map<String, Float> boosts = new HashMap<String, Float>();  
			 
			 boosts.put("title", 5.0f);  
	         boosts.put("contents", 1.0f);
	         boosts.put("anchor", 2.0f);  
	         
	         
	         parser = new MultiFieldQueryParser(Version.LUCENE_46, 
	        		 new String[]{"title","contents","anchor"}, analyzer,boosts);
		 }
	 }
	 public ScoreDoc[] doSearch(String queryStr,String queryType)
	 {
		 
         ScoreDoc[] hits=null;
         ScoreDoc[] res=null;
         int size=0;
       try{
			 
		     Sort sort1;
		     if(queryStr.contains("?")||queryStr.contains("*"))
		     {
					
		    	 Term term = new Term("contents", queryStr);
		    	 wildquery = new WildcardQuery(term); 
		    	 TopDocs topDocs = searcher.search(wildquery, 1000);
		    	 hits = topDocs.scoreDocs;

		     }
		     else
		     {
		    	 query = parser.parse(queryStr);  
		     
			     if(queryType.contains("html"))
			     {
			    	 sort1=new Sort(new SortField[]{SortField.FIELD_SCORE,
		                     new SortField("pagerank", SortField.Type.FLOAT)
		                     });
			    	 hits = searcher.search(query, null, 1000, sort1).scoreDocs;
		         }
			     else
			     {
			    	 sort1=new Sort(new SortField[]{SortField.FIELD_SCORE});
			    	 hits = searcher.search(query, null, 8000, sort1).scoreDocs;
			     }
		     }
		     int resindex=0;
		     for(ScoreDoc match:hits)
		     {
		    	 if(size>=200)
		    	 {
		    		 break;
		    	 }
		    	 else
		    	 {
		    		 Document doc = searcher.doc(match.doc);
		    		 if(doc.get("filetype").contains(queryType))
		    		 {
		    			 size++;
		    		 }
		    	 }
		     }
		     res=new ScoreDoc[size];
		     for(ScoreDoc match:hits)
		     {
		    	 if(resindex==200)
		    	 {
		    		 break;
		    	 }
		    	 else
		    	 {
		    		 Document doc = searcher.doc(match.doc);
		    		 if(doc.get("filetype").contains(queryType))
		    		 {
		    			 res[resindex]=match;
		    			 resindex++;
		    		 }
		    	 }
		     }
		     
		     
		       
	          
	            // Iterate through the results:  
	          
	         
         }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } 
       	
         return res;
	 }
}
         
