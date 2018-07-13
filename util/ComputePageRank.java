package util;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ComputePageRank {
	public static double MAX = 0.0000001;  
	private static final float alpha = 0.85f; 
	public static double[] init;  
	private static int tp= 12514;
    public static double[] pr;  
	public static DBConnection dbc=new DBConnection();
	public static int[] outlinks;
	private static boolean checkMax() {  
	        boolean flag = true;  
	        for (int i = 0; i < pr.length; i++) {  
	            if (Math.abs(pr[i] - init[i]) > MAX) {  
	                flag = false;  
	                break;  
	            }  
	        }  
	        return flag;  
	}  
	public static void calout()
	{
		outlinks=new int[tp];
		for(int i=0;i<tp;i++)
		{
			outlinks[i]=0;
			outlinks[i]=dbc.getOutNum(i);//求i指向的页面数
			System.out.println("第"+i+"个页面的出链数为"+outlinks[i]);
		}
	}
	public static void CalPageRank()  {
		init=new double[tp];
		for(int i=0;i<tp;i++)
		{
			init[i]=0.0f;
		}
		System.out.println("初始化出链开始!");
		calout();
		
		System.out.println("初始化出链结束!");
        pr = doPageRank(); 
        int irt=1;
        while (checkMax()) {   
            System.arraycopy(pr, 0, init, 0, init.length);
            System.out.println("第"+irt+"轮迭代开始！");
            pr = doPageRank();
            System.out.print("第"+irt+"轮迭代结果为: ");
            System.out.println(pr);
            irt++;
        } 
        dbc.stPageRank(pr);
	}
	private static double[] doPageRank() {  
        double[] pr = new double[tp];  
        for (int i = 0; i < tp; i++) {  
            double temp = 0;  
            ResultSet PSet=dbc.getParentSet(i);//求所有指向i的页面
            try {
				while(PSet.next())
				{
					int pid=PSet.getInt(2);
					if(pid>=tp)
					{
						continue;
					}
					temp=temp+init[pid]/outlinks[pid];
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            pr[i] = alpha + (1 - alpha) * temp;
            System.out.println("第"+i+"个页面当前的pageRank为"+pr[i]);
        }  
        return pr;  
    }
}