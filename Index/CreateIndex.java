package Index;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.DBConnection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import org.apache.poi.hwpf.extractor.WordExtractor;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class CreateIndex {
	public float pageRank[];
	public static DBConnection db=new DBConnection();
	private static String searchDir="E:\\test\\index";
	private static File indexFile=null;
	private ResultSet rs=null; 
	private static IndexSearcher searcher = null;  
    private static Analyzer analyzer = null;  
    public Query query = null; 
	public void CreateMyIndex()
	{
		int indexId=1;
		Pattern p = Pattern.compile("\t|\r|\n");
		rs=db.getAllPage();
		Directory directory=null;
		IndexWriter indexWriter = null;  
		try {
			indexFile = new File(searchDir);  
			
            if (!indexFile.exists()) {  
                indexFile.mkdir();  
            }  
            directory=FSDirectory.open(indexFile);
            analyzer = new IKAnalyzer();  
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46,  
                    analyzer);  
            indexWriter = new IndexWriter(directory, iwc);  
            Document doc = null; 
            
			while(rs.next())
			{
				String filepath="E:\\test\\storage\\"+rs.getString("pathname");
				File stoFile=new File(filepath);
				if(stoFile.exists())//文件存在，可以建立索引
				{
					doc=new Document();
					String filetype=rs.getString("filetype");
					String Url=rs.getString("URL");
					String anchor=rs.getString("anchor");
					String title="";
					int pageRank=-1;
					
					if(filetype.equals("doc"))//文本类型文件
					{
						title=anchor;
						InputStream in=new FileInputStream(filepath);
						String content="";
						try{
							WordExtractor wordExtractor = new WordExtractor(in);
							content= wordExtractor.getText();
							Matcher m = p.matcher(content);
							content = m.replaceAll("");
						//content.replace('?', ' ');
						//System.out.println("content: "+content.substring(10,100));
						    doc.add(new Field("contents",content,TextField.TYPE_STORED));
						    wordExtractor.close();
						}catch(Exception e)
						{
							continue;
							
						}
					}
					else
					{
						if(filetype.equals("docx"))
						{
							title=anchor;
							InputStream in=new FileInputStream(filepath);
							String content="";
							try{
								XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(new XWPFDocument(in));
								content=xwpfWordExtractor.getText();
								Matcher m = p.matcher(content);
								content = m.replaceAll("");
								//content.replace('?', ' ');
								//System.out.println("content: "+content.substring(10,100));
								doc.add(new Field("contents",content,TextField.TYPE_STORED));
								xwpfWordExtractor.close();
							}catch(Exception e)
							{
								continue;
							}
						}
						else
						{
							if(filetype.equals("pdf"))
							{
								title=anchor;
								InputStream in=new FileInputStream(filepath);
								String content="";
								try{
									PDFParser parser = new PDFParser(in);
				                    parser.parse();
				                    PDDocument pdDocument = parser.getPDDocument();
				                    PDFTextStripper stripper = new PDFTextStripper();
				                    // 创建Field对象，并放入doc对象中
				                    content=stripper.getText(pdDocument);
				                    Matcher m = p.matcher(content);
									content = m.replaceAll("");
									//content.replace('?', ' ');
				                    //System.out.println("content: "+content);
				                    doc.add(new Field("contents",content ,
				                    		TextField.TYPE_STORED));
				                    // 关闭文档
				                    pdDocument.close();
								}catch(Exception e)
								{
									continue;
								}
							}
							else
							{
								if(filetype.contains("xls"))
								{
									title=anchor;
									String content=anchor;
									doc.add(new Field("contents",content ,
				                    		TextField.TYPE_STORED));
								}
								else//网页文档
								{
									int dbID=rs.getInt("docID");
									double pr=db.getpr(dbID);
									System.out.println("PageRank: "+pr);
									try{
										org.jsoup.nodes.Document htmlDoc = Jsoup.parse(stoFile, "UTF-8");
										Elements t=htmlDoc.getElementsByTag("title");
										title=t.text();
										if(anchor==null)
										{
											anchor=title;
										}
										String content=htmlDoc.text();
										//System.out.println("content: "+content.substring(10,100));
										doc.add(new Field("contents", content,
					                    		TextField.TYPE_STORED));
										doc.add(new FloatField("pageRank", (float) pr,
												 Field.Store.YES));
									}catch(Exception e)
									{
										continue;
									}
									
								}
							}
						}
					}
					//System.out.println("title: "+title);
					//System.out.println("anchor: "+anchor);
					doc.add(new Field("title", title,TextField.TYPE_STORED));
					doc.add(new Field("anchor", anchor,TextField.TYPE_STORED));
					FieldType fieldType = new FieldType();  
			        fieldType.setIndexed(false);//set 是否索引  
			        fieldType.setStored(true);//set 是否存储  
			        fieldType.setTokenized(false);//set 是否分类 
					doc.add(new Field("filetype", filetype,fieldType));
					doc.add(new Field("URL", Url,fieldType));
					doc.add(new Field("path", filepath,fieldType));
					indexWriter.addDocument(doc);
					System.out.println("Build Index :"+indexId+" successfully!");
					indexId++;

							
							
				}
			}
			indexWriter.close(); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Build Index successfully!");
	}
	
}
