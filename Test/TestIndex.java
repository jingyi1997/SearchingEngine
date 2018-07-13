package Test;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import Index.CreateIndex;
import query.doQuery;
import util.DBConnection;

public class TestIndex {
	public static doQuery ci;
	public static void main(String[] args) throws IOException
	{
		ci=new doQuery("E:\\test\\index");
		
		ScoreDoc[] sd =ci.doSearch("*ÄÏ¿ª*","html");
		for(int i=0;i<sd.length;i++)  
        {  
            Document doc = ci.searcher.doc(sd[i].doc);  
            
            System.out.println(doc.get("title"));
            
        } 
		System.out.println(sd.length);
	}
	

}
