package aj.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class BasicController {
    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "~/IdeaProjects/WebCrawler/data";
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setPolitenessDelay(400);
        config.setMaxDepthOfCrawling(1);
//        config.setMaxPagesToFetch(-1);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://www.optum.com/");

        controller.startNonBlocking(BasicCrawler.class, numberOfCrawlers);

        //Thread.sleep(300 * 1000);

        //controller.shutdown();
        controller.waitUntilFinish();
    }
}
