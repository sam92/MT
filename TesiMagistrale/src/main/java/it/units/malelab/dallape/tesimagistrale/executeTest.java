/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

/**
 *
 * @author Samuele
 */
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class executeTest extends Thread {

    private final String NAME_COLLECTION;
    private final List<String> sites;
    private final String task_id;
    private Map<String,Thread> map;

    public executeTest(String NAME_COLLECTION, List<String> sites, String task_id, Map<String,Thread> map) {
        this.NAME_COLLECTION = NAME_COLLECTION;
        this.sites = sites;
        this.task_id = task_id;
        this.map=map;
    }

    @Override
    public void run() {
        try (database db = new database()) {
            if (!db.collectionExist(NAME_COLLECTION)) {
                db.createCollection(NAME_COLLECTION);
            }

            for (String s : sites) {
                if (!db.existADocumentWithThisUrlInSITES(s) && !s.trim().isEmpty()) {
                    System.out.println(NAME_COLLECTION);
                    //vecchia implementaz in Test.java
                    //SiteImplementation site= TestSite.analyzeSite(s);

                    TestCase test = new TestCaseImplementation(s);
                    test.searchFormInThisPattern(TestCaseImplementation.defaultPattern());
                    test.searchFormInLinkedPagesOfHomepage();
                    Site site = (Site) test.getSite();
                    site.setVisitedNow();
                    test.quitWebDriver();

                    site.setTASKID(task_id);
                    db.insertSite(site, NAME_COLLECTION);

                } else {
                    System.out.println("Already exist: " + s);
                }
                
                //tolgo dalla coda di questo task il doc perché è stato appena scansionato
                Document current = new Document("site", s);
                current.append("task_id", task_id);
                db.getMongoDB().getCollection("STATE_LIST_SITES").deleteOne(current);
            }
            //ho fatto tutta la lista quindi posso rimuovere i siti con quel task dalla lista (nel caso in cui il thread sia stato killato prima di rimuovere tutto)
            db.getMongoDB().getCollection("STATE_LIST_SITES").deleteMany(new Document("task_id",task_id));
            //rimuovo l'associazione tra task_id e thread tanto ho finito
            map.remove(task_id);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
    }
}
