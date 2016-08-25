package aj.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Crawler to search a website for all linked url's
 */
public class BasicCrawler extends WebCrawler {

    private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|png|bmp))$");
    public static int i = 1;

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();

        if (FILTERS.matcher(href).matches()) {
            return false;
        }

        return href.contains("www.optum.com/");
//        return true;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println(i++ + ") URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Number of outgoing links: " + links.size());
        }
    }
}
