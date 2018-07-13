package crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class MyCrawlController {
		public static void main(String[] args) throws Exception{
		String rootFolder = "E:\\test\\temp";
	    int numberOfCrawlers = 5;
	
	    CrawlConfig config = new CrawlConfig();
	
	    config.setCrawlStorageFolder(rootFolder);
	    config.setMaxDepthOfCrawling(-1);
        config.setMaxPagesToFetch(12000);
	    config.setIncludeBinaryContentInCrawling(true);
	    config.setResumableCrawling(false);
	    String[] crawlDomains = {"http://international.nankai.edu.cn","http://jw.nankai.edu.cn",
	    		"http://ygb.nankai.edu.cn","http://xgb.nankai.edu.cn"
	    		,"http://edp.nankai.edu.cn","http://std.nankai.edu.cn",
	    		"http://nkuaa.nankai.edu.cn","http://rsc.nankai.edu.cn","http://ssrm.nankai.edu.cn"};
	
	    PageFetcher pageFetcher = new PageFetcher(config);
	    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
	    for (String domain : crawlDomains) {
	        controller.addSeed(domain);
	    }
	
	    MyCrawler.configure(crawlDomains);
	
	    controller.start(MyCrawler.class, numberOfCrawlers);
	}
}
