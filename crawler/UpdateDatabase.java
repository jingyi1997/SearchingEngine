package crawler;

import util.DBConnection;

public class UpdateDatabase {
	private static DBConnection dbc;
	public static void main(String[] args)
	{
		dbc=new DBConnection();
		dbc.getFile();
	}
}
