package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import util.DBConnection;

public class MyCrawler extends WebCrawler{
	 private static final Pattern filters = Pattern.compile(
		        ".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v" +
		"|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	 private static final Pattern imgPatterns = Pattern.compile(".*(\\.(bmp|ico|gif|jpe?g|png|tiff?))$");
	 private static String[] crawlDomains;
	 private static String[] storageFolder={"xgb"};
	 private static DBConnection dbc;
	 public boolean shouldVisit(Page referringPage,WebURL url)
	 {
		 String href = url.getURL().toLowerCase();
		 if(href.length()>200)
		 {
			 return false;
		 }
		 if(filters.matcher(href).matches())
		 {
			 return false;
		 }
		 if(imgPatterns.matcher(href).matches())
		 {
			 return false;
		 }
		 for (String domain : crawlDomains) {
	            if (href.startsWith(domain)) {
	                return true;
	            }
	     }
		 return false;
	 }
	 public static void configure(String[] domain) {
	        crawlDomains = domain;
	       
	        
           
            for(int i=0;i<5;i++)
            {
            	File temp=new File("E:\\test\\storage\\"+storageFolder[i]);
            	if(!temp.exists())
            	{
            		temp.mkdirs();
            	}
            }
	        dbc=new DBConnection();
	 }
	 public void visit(Page page) {
	        String url = page.getWebURL().getURL().toLowerCase();
	        String prefix="";
	        String filetype=page.getContentType();
	        int docid = page.getWebURL().getDocid();
	        String coding=page.getContentCharset();
			String anchor=page.getWebURL().getAnchor();
	        System.out.println(url);
	        String temp=url.substring(7, 9);
	        switch (temp){
        	case "in":
        		prefix="international";
        		break;
        	case "st":
        		prefix="std";
        		break;
        	case "ed":
        		prefix="edp";
        		break;
        	case "ss":
        		prefix="ssrm";
        		break;
        	case "rs":
        		prefix="rsc";
        		break;
        	case "xg":
        		prefix="xgb";
        		break;
        	case "yg":
        		prefix="ygb";
        		break;
        	case "nk":
        		prefix="nkuaa";
        		break;
	        }
	        if (page.getParseData() instanceof HtmlParseData) {
	    			 
	        	 HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
	             String html = htmlParseData.getHtml();
	             String hashedName = UUID.randomUUID().toString();
	             String filename=prefix+"\\"+hashedName+".html";//数据库中文件路径
	             //写入本地
	             String htmlName ="E:\\test\\storage\\"+prefix+"\\"+hashedName+".html";
	             File htmlFile=new File(htmlName);
	             try 
	             {
	     			
	     			htmlFile.createNewFile();
	     		
	     			BufferedWriter htmlWriter = new BufferedWriter(new OutputStreamWriter(  
	     	                new FileOutputStream(htmlFile), "utf-8"));  
	     			
	     			htmlWriter.write(html);
	     			htmlWriter.flush();
	     			} catch (IOException e) {
	     			e.printStackTrace();
	     		 }
	             //插入数据库
	             WebInfo wi=new WebInfo(filetype,filename,docid,url,anchor);
	             int res=dbc.DBInsertWeb(wi);
	             if(res<0)
	             {
	            	 System.out.println("Failure in inserting html into database!");
	             }
	             Set<WebURL> links = htmlParseData.getOutgoingUrls();
	             for(WebURL link:links)
	             {
	            	 String tempurl=link.getURL();
	            	 if(shouldVisit(page,link))
	            	 {
	            		 
	            		 if(tempurl.contains("pdf")||tempurl.contains("doc")||tempurl.contains("xls"))
	            		 {
	            			if(link.getAnchor()!=null)
	            			{
	            				
	            				String extension = tempurl.substring(tempurl.lastIndexOf('.')+1);//文件扩展名
	            				WebInfo bwi=new WebInfo(extension,"",link.getDocid(),tempurl,link.getAnchor());
	            				res=dbc.DBInsertWeb(bwi);
	           	             	if(res<0)   
	           	             	{
	           	             		System.out.println("Failure in inserting bin into database!");
	           	             	}
	            			}
	            		 }
	            		 else
	            		 {
	            			 if(link.getDocid()!=-1)
	            			 {
	            				 dbc.DBInsertLink(docid, link.getDocid());
	            			 }
	            		 }
	            	 }
	             }
		    }
	        else
	        {
	        	 if (!(page.getParseData() instanceof BinaryParseData))
	        	            return;
	        	 String hashedName;
	        	 String extension;
	        	 String filename;
	        	 String binName;
	        	 if(dbc.getExtensionByUrl(url)==null)
	        	 {
	        		 return;
	        	 }
	        	 
	       		 //下载此文件并存在数据库里
	        	 hashedName = UUID.randomUUID().toString();
	        	 extension = url.substring(url.lastIndexOf('.'));//文件扩展名
	        	 filename=prefix+"\\"+hashedName+extension;//数据库中文件路径
	    	             //写入本地
	        	 binName ="E:\\test\\storage\\"+prefix+"\\"+hashedName+extension;
    			 try {
					Files.write(page.getContentData(), new File(binName));
				 } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Failure in writing binary data into file!");
				 }
	        	 
	             int res=dbc.UpdateByfile(filename,page.getWebURL().getDocid(),url);
	             if(res<0)
	             {
	            	 System.out.println("Failure in inserting binary info into database!");
	             }
	        }
	 }
}
