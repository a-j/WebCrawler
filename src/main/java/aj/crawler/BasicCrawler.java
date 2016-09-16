package aj.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Crawler to search a website for all linked url's
 */
public class BasicCrawler extends WebCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCrawler.class);
    private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|jpeg|png|bmp|ico))$");
    private Set<String> internalLinks = new HashSet<>();
    private Set<String> externalLinks = new HashSet<>();

    private final List<String> SOCIAL_SITES = Arrays.asList("google.com", "youtube.com", "facebook.com", "twitter.com", "linkedin.com");
    private final List<String> PRODUCT_SITES = Arrays.asList("adobe.com", "microsoft.com", "doubleclick.net", "aboutads.info", "googletagmanager.com", "opera.com", "mozilla.org");

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();

        if (FILTERS.matcher(href).matches()) {
            return false;
        }
        LOGGER.debug("Current url:{} Domain: {} ", url.getURL(), url.getDomain());
        LOGGER.debug("Parent url:{} Domain: {} ", referringPage.getWebURL().getURL(), referringPage.getWebURL().getDomain());

        return url.getDomain().equals(referringPage.getWebURL().getDomain());
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        LOGGER.info("Visiting URL: {}", url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            processUrls(links, page.getWebURL());
        }
    }

    private void processUrls(Set<WebURL> urls, WebURL pageUrl) {
        for (WebURL url : urls) {
            if (url.getDomain().equals(pageUrl.getDomain())) {
                internalLinks.add(url.getDomain());
            } else {
                externalLinks.add(getCategory(url.getDomain()));
            }
        }
    }

    private String getCategory(String url) {
        if (url.endsWith(".gov") || url.endsWith(".us")) {
            return "GOV SITES";
        } else if (SOCIAL_SITES.contains(url)) {
            return "SOCIAL SITES";
        } else if (PRODUCT_SITES.contains(url)) {
            return "PRODUCT SITES";
        } else {
            return url;
        }
    }

    @Override
    public void onBeforeExit() {
        LOGGER.info("Number of internal links: {}", internalLinks.size());
        LOGGER.info("Number of external links: {}", externalLinks.size());
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
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ioe) {
            LOGGER.error("IOException Occurred", ioe);
        }
    }
}
