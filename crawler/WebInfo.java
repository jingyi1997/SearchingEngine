package crawler;


public class WebInfo {

	public String filetype;
	public String relpath;
	public int docId;
	public String url;
	
	public String anchor;
	public WebInfo(String f,String p,int id,String u,String a)
	{
		filetype=f;
		relpath=p;
		docId=id;
		anchor=a;
		url=u;
	}
	

}
