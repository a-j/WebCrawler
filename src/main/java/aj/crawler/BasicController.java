package aj.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.time.LocalDate;

public class BasicController {
    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "data"; // tmp folder for use during crawling
        int numberOfCrawlers = 1;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setPolitenessDelay(400);
        config.setMaxDepthOfCrawling(2);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("http://www.optum.com");

        final long startTime = System.currentTimeMillis();
        controller.startNonBlocking(BasicCrawler.class, numberOfCrawlers);
        controller.waitUntilFinish();
        final long endTime = System.currentTimeMillis();
        System.out.println("Elapsed Time (in seconds): " + (endTime - startTime)/1000 );
    }
}
