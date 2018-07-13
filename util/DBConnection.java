package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import crawler.WebInfo;

public class DBConnection {
	private Connection conn = null;
    private String url = "jdbc:mysql://localhost/webinfo"; // URL指向要访问的数据库名
    private String user = "root"; // MySQL配置时的用户名
    private String password = "root"; // MySQL配置时的密码
    public DBConnection()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public ResultSet getAllPage()
    {
    	PreparedStatement pstmt;
    	ResultSet res=null;
    	String sql="select * from myinfo";
    	try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			res=pstmt.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return res;
    }
    
    public Connection getConnection()
    {
    	return conn;
    }
    public int DBInsertWeb(WebInfo wi)
    {
    	int i=-1;
        try
        {
        	PreparedStatement pstmt;
        	String sql = "insert into myinfo (URL,pathname,docID,filetype,anchor) values(?,?,?,?,?)";
        	pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	pstmt.setString(1, wi.url);
        	pstmt.setString(2, wi.relpath);
        	pstmt.setInt(3, wi.docId);
        	pstmt.setString(4, wi.filetype);
        	pstmt.setString(5, wi.anchor);
        	i = pstmt.executeUpdate();
        	pstmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return i;
    }
    public int DBInsertLink(int p,int c)
    {
    	int i=-1;
        try
        {
        	PreparedStatement pstmt;
        	String sql = "insert into map (PId, CId) values(?,?)";
        	pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	
        	pstmt.setInt(1, p);
        	pstmt.setInt(2, c);
        	
        	i = pstmt.executeUpdate();
        	pstmt.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failure in inserting into map!");
        }
        return i;
    }
    public int UpdateByfile(String filename,int docID,String url)
    {
    	int res=-1;
        try
        {
        	String sql = "update myinfo set pathname=?,docID=? where URL=?";
            PreparedStatement pstmt;
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	
        	pstmt.setString(1,filename);
        	pstmt.setInt(2, docID);
        	pstmt.setString(3, url);
        	res = pstmt.executeUpdate();
        
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return res;
    }
    public String getExtensionByUrl(String url)
    {
    	String res=null;
        try
        {
        	String sql = "select * from myinfo where URL=?";
            PreparedStatement pstmt;
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	
        	pstmt.setString(1, url);
        	ResultSet rs = pstmt.executeQuery();
        	
        	while(rs.next())
        	{
        		res=rs.getString("filetype");
        	}
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return res;
    }
    public void resetPathName()
    {
    	
        try
        {
        	String sql = "select * from myinfo where URL like '%nkuaa%'";
            PreparedStatement pstmt;
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	
        	ResultSet rs = pstmt.executeQuery();
        	
        	while(rs.next())
        	{
        		String pres=rs.getString("pathname");
        		String url=rs.getString("URL");
        		pres="nkuaa"+pres;
        		sql = "update myinfo set pathname=? where URL=?";
        		pstmt = (PreparedStatement) conn.prepareStatement(sql);
        		pstmt.setString(1, pres);
        		pstmt.setString(2, url);
        		pstmt.executeUpdate();
        	}
        }catch(Exception e){
            e.printStackTrace();
        }
        
       
    }
    public void DownLoadBin()
    {
    	try
        {
        	String sql = "select * from myinfo where pathname=''";
            PreparedStatement pstmt;
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	
        	ResultSet rs = pstmt.executeQuery();
    	    FileOutputStream fileOut = null;  
    	    HttpURLConnection mconn = null;  
    	    InputStream inputStream = null;
        	int docid=12115;
        	while(rs.next())
        	{
        		
        		String url=rs.getString("URL");
        		String temp=url.substring(7, 9);
        		String prefix="";
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
        		String hashedName = UUID.randomUUID().toString();
        		String extension = url.substring(url.lastIndexOf('.'));//文件扩展名
	        	String filename=prefix+"\\"+hashedName+extension;//数据库中文件路径
	        	//写入本地
	        	String binName ="E:\\test\\storage\\"+prefix+"\\"+hashedName+extension;
	        	URL httpUrl=new URL(url);  
	            mconn=(HttpURLConnection) httpUrl.openConnection();  
	            
	            mconn.setDoInput(true);    
	            mconn.setDoOutput(true);    
	            //连接指定的资源   
	            mconn.connect();  
	            //获取网络输入流  
	            try{
		            inputStream=mconn.getInputStream();  
		            BufferedInputStream bis = new BufferedInputStream(inputStream);  
		            //判断文件的保存路径后面是否以/结尾  
		     
		            fileOut = new FileOutputStream(binName);  
		            BufferedOutputStream bos = new BufferedOutputStream(fileOut);  
		              
		            byte[] buf = new byte[4096];  
		            int length = bis.read(buf);  
		            //保存文件  
		            while(length != -1)  
		            {  
		                bos.write(buf, 0, length);  
		                length = bis.read(buf);  
		            }  
		            bos.close();  
		            bis.close();  
		            mconn.disconnect(); 
		            sql = "update myinfo set pathname=?,docID=? where URL=?";
	        		pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        		pstmt.setString(1, filename);
	        		pstmt.setInt(2, docid);
	        		pstmt.setString(3, url);
	        		pstmt.executeUpdate();
		        	docid++;
	            }
	            catch(Exception e)
	            {
	            	continue;
	            }
	        	
        	}
        }catch(Exception e){
            e.printStackTrace();
            
        }
    }
    public double getpr(int docid)
    {
    	double res=0f;
    	try
        {
        	PreparedStatement pstmt;
        	String sql = "select * from pagerank where docID=?";
        	pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	pstmt.setInt(1, docid);
        	ResultSet i= pstmt.executeQuery();
        	while(i.next())
        	{
        		res=i.getDouble(3);
        		break;
        	}
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failure in getting pagerank!");
        }
    	return res;
    }
    public void getFile()
    {
    	ResultSet i;
        try
        {
        	PreparedStatement pstmt;
        	String sql = "select * from myinfo";
        	pstmt = (PreparedStatement) conn.prepareStatement(sql);
        	
        	
        	i = pstmt.executeQuery();
        	int count=0;
        	int totalcount=0;
        	while(i.next())
        	{
        		String relpath=i.getString("pathname");
        		String abpath="E:\\test\\storage\\"+relpath;
        		
        		File f=new File(abpath);
        		totalcount++;
        		if(!f.exists())
        		{
        			System.out.println(abpath);
        			count++;
        		}
        		
        	}
        	System.out.println(totalcount);
        	System.out.println(count);
        	pstmt.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failure in inserting into map!");
        }
       
    }
	public int getOutNum(int i) {
		// TODO Auto-generated method stub
		PreparedStatement pstmt;
    	ResultSet res=null;
    	int resnum=0;
    	String sql="select * from map where PId=?";
    	try {
    		
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, i);
			res=pstmt.executeQuery();
			while(res.next())
			{
				resnum++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resnum;
	}
	public ResultSet getParentSet(int i) {
		
	    	PreparedStatement pstmt;
	    	ResultSet res=null;
	    	String sql="select * from map where CId=?";
	    	try {
	    		
				pstmt = (PreparedStatement) conn.prepareStatement(sql);
				pstmt.setInt(1, i);
				res=pstmt.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	return res;
	}
	public void stPageRank(double[] pr) {
		
    	PreparedStatement pstmt;
    	String sql="insert into pagerank(docID,score) values (?,?)";
    	for(int i=0;i<pr.length;i++){
	    	try {
	    		
				pstmt = (PreparedStatement) conn.prepareStatement(sql);
				pstmt.setInt(1, i);
				pstmt.setDouble(2, pr[i]);
				pstmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	System.out.println("insert into database successfully!");
	}	
}
