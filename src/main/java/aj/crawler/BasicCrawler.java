package aj.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Crawler to search a website for all linked url's
 */
public class BasicCrawler extends WebCrawler {

    private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|jpeg|png|bmp|ico))$");
    Set<String> internalLinks = new HashSet<>();
    Set<String> externalLinks = new HashSet<>();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();

        System.out.println("***** href: " + href);
        if (FILTERS.matcher(href).matches()) {
            return false;
        }
        System.out.println("***** shouldVisit() called *****");

        return href.contains("www.optum.com/");
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);
        System.out.println("Domain: " + page.getWebURL().getDomain());

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Number of outgoing links: " + links.size());
            System.out.println("Outgoing links: " + links);
            processUrls(links, page.getWebURL());
            System.out.println("Number of internal links: " + internalLinks.size());
            System.out.println("Number of external links: " + externalLinks.size());
            System.out.println("Internal links: " + internalLinks);
            System.out.println("External links: " + externalLinks);
        }
    }

    public void processUrls(Set<WebURL> urls, WebURL pageUrl) {
        for (WebURL url : urls) {
            if (url.getDomain().equals(pageUrl.getDomain())) {
                internalLinks.add(url.getDomain());
            } else {
                externalLinks.add(url.getDomain());
            }
        }
    }

    @Override
    public void onBeforeExit() {
        System.out.println("Number of internal links: " + internalLinks.size());
        System.out.println("Number of external links: " + externalLinks.size());
        try {
            FileWriter fileWriter = new FileWriter(new File("data/output-" + getThread().getName() + ".txt"));

            fileWriter.write("Internal Links:\n");
            for (String url : internalLinks) {
                fileWriter.write(url);
                fileWriter.write("\n");
            }
            fileWriter.write("\n");

            fileWriter.write("External Links:\n");
            for (String url : externalLinks) {
                fileWriter.write(url);
                fileWriter.write("\n");
            }
        } catch (IOException ioe) {
            System.out.println("IOException Occurred");
            ioe.printStackTrace();
        }
    }
}
