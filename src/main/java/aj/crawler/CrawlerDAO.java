package aj.crawler;

import org.neo4j.driver.v1.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CrawlerDAO {

    public static void main(String...args) {
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> links = new HashSet<>();
//        links.add("uhc.com");
//        links.add("optumrx.com");
//        links.add("optum360.com");
//        map.put("optum.com", links);
//
//        links = new HashSet<>();
//        links.add("optum.com");
//        links.add("myuhc.com");
//        links.add("optum360.com");
//        map.put("uhc.com", links);
        map.put("uhcretiree.com", getData("uhcretiree.com", "data/uhcretiree.txt"));
        map.put("pharmacysaver.com", getData("pharmacysaver.com", "data/pharmacysaver.txt"));
        map.put("unitedpharmacysaver.com", getData("unitedpharmacysaver.com", "data/unitedpharmacysaver.txt"));
        map.put("uhcmedicaresolutions.com", getData("uhcmedicaresolutions.com", "data/uhcmedicaresolutions.txt"));
        map.put("aarpmedicareplans.com", getData("aarpmedicareplans.com", "data/aarpmedicareplans.txt"));

        System.out.println(map);
        saveToDB(map);
    }

    public static Set<String> getData(String site, String fileName) {
        Set<String> links = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str;
            while ( (str = reader.readLine()) != null ) {
                links.add(str);
            }
            reader.close();
        } catch (IOException ioe) {
            System.out.println("IOException Occurred: " + ioe.getMessage());
            ioe.printStackTrace();
        }
        return links;
    }

    public static void saveToDB(Map<String, Set<String>> map) {
        Config noSSL = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig();
        Driver driver = GraphDatabase.driver("bolt://localhost",AuthTokens.basic("neo4j","Anand123"),noSSL); // <password>

        try (Session session = driver.session()) {

            // Create unique constraint to prevent duplicate domains
            session.run("CREATE CONSTRAINT ON (site:SITE) ASSERT site.name IS UNIQUE");

            Set<String> processedInput = new HashSet<>();
            Set<String> processedLinks = new HashSet<>();

            for (String inputDomain : map.keySet()) {
                StringBuilder query = new StringBuilder();
                if (!processedInput.contains(inputDomain)) {
                    if (processedLinks.contains(inputDomain)) {  // Node inserted already, just add label
                        query.append("MATCH (i:SITE{name:\"" + inputDomain + "\"}) SET i :INPUT").append(System.lineSeparator());

                        processedInput.add(inputDomain);
                    } else {    // Node not inserted
                        query.append("CREATE (i:SITE:INPUT{name:\"" + inputDomain + "\"})").append(System.lineSeparator());

                        processedInput.add(inputDomain);
                        processedLinks.add(inputDomain);
                    }
                }

                int i = 0;
                for (String link : map.get(inputDomain)) {
                    if (!processedLinks.contains(link)) {   // Node not inserted - Create node & relationship
                        query.append("CREATE (o" + ++i +":SITE{name:\"" + link + "\"})").append(System.lineSeparator());
                        query.append("CREATE (i)-[:LINKS_TO]->(o"+ i + ")").append(System.lineSeparator());
                        processedLinks.add(link);
                    } else {    // Node already inserted - Create only relationship
                        query.append("WITH i").append(System.lineSeparator());
                        query.append("MATCH (i1:INPUT), (i2:SITE) ");
                        query.append("WHERE i1.name=\"" + inputDomain + "\" AND i2.name=\"" + link + "\" ");
                        query.append("CREATE (i1)-[:LINKS_TO]->(i2)");
                        query.append(System.lineSeparator());
                    }
                }
                System.out.println("Query: \n" + query.toString());
                session.run(query.toString()).consume();
            }
        }
    }
}