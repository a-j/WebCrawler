package aj.crawler;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public class CrawlerDAO {

    public static void main(String...args) {

        Config noSSL = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig();
        Driver driver = GraphDatabase.driver("bolt://localhost",AuthTokens.basic("neo4j","Anand123"),noSSL); // <password>
        try (Session session = driver.session()) {

            // Create unique constraint to prevent duplicate domains
            session.run("CREATE CONSTRAINT ON (site:SITE) ASSERT site.name IS UNIQUE");

            Set<String> processedInput = new HashSet<>();
            Set<String> processedLinks = new HashSet<>();

            Map<String, Set<String>> map = new HashMap<>();
            Set<String> links = new HashSet<>();
            links.add("uhc.com");
            links.add("optumrx.com");
            links.add("optum360.com");
            map.put("optum.com", links);

            links = new HashSet<>();
            links.add("optum.com");
            links.add("myuhc.com");
            links.add("optum360.com");
            map.put("uhc.com", links);

            for (String inputDomain : map.keySet()) {
                StringBuilder query = new StringBuilder();
                if (!processedInput.contains(inputDomain)) {    // Node not inserted
                    if (processedLinks.contains(inputDomain)) {  // Node inserted already, just add label
                        query.append("MATCH (i:SITE{name:\"" + inputDomain + "\"}) SET i :INPUT").append(System.lineSeparator());

                        processedInput.add(inputDomain);
                    } else {
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


//            for (String inputDomain : map.keySet()) {
//                try {
//                    System.out.println("Inserting " + inputDomain);
//                    session.run("CREATE (:SITE:INPUT{name:\"" + inputDomain + "\"})").consume();
//                } catch (ClientException ce) {
//                    // ignore unique constraint violation
//                    System.out.println("Exception Occurred: " + ce.getMessage());
//                }
//
//                for (String link : map.get(inputDomain)) {
//                    try {
//                        System.out.println("Inserting " + link);
//                        session.run("CREATE (:SITE{name:\"" + link + "\"})").consume();
//                    } catch (ClientException ce) {
//                        // ignore unique constraint violation
//                        System.out.println("Exception Occurred: " + ce.getMessage());
//                    }
//                }
//            }

//            List data =
//                    asList(asList("Jim","Mike"),asList("Jim","Billy"),asList("Anna","Jim"),
//                            asList("Anna","Mike"),asList("Sally","Anna"),asList("Joe","Sally"),
//                            asList("Joe","Bob"),asList("Bob","Sally"));
//
//            String insertQuery = "UNWIND {pairs} as pair " +
//                    "MERGE (p1:Person {name:pair[0]}) " +
//                    "MERGE (p2:Person {name:pair[1]}) " +
//                    "MERGE (p1)-[:KNOWS]-(p2);";
//
//            session.run(insertQuery,singletonMap("pairs",data)).consume();

//            StatementResult result;
//
//            String foafQuery =
//                    " MATCH (person:Person)-[:KNOWS]-(friend)-[:KNOWS]-(foaf) "+
//                            " WHERE person.name = {name} " +
//                            "   AND NOT (person)-[:KNOWS]-(foaf) " +
//                            " RETURN foaf.name AS name ";
//            result = session.run(foafQuery, parameters("name","Joe"));
//            while (result.hasNext()) System.out.println(result.next().get("name"));

        }
    }
}