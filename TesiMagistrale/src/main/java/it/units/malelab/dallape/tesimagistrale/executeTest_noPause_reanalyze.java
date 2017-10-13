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
import com.mongodb.MongoException;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class executeTest_noPause_reanalyze extends Thread {

    //private volatile boolean stop=false;
    private final String NAME_COLLECTION;
    private final String NAME_STATE;
    private final List<String> sites;
    private final String task_id;
    private Map<String, Conditions> map;
    private List<String> whatTest;
    private boolean stopped;

    public executeTest_noPause_reanalyze(Map<String, Conditions> map, Conditions con) {
        this.NAME_COLLECTION = con.getNameCollection();
        this.NAME_STATE=con.getNameState();
        this.sites = con.getSites();
        this.task_id = con.getTaskID();
        this.map = map;
        if (!con.getTests().contains("")) {
            con.getTests().add("");
        }
        this.whatTest = con.getTests();
        stopped = false;
    }

    @Override
    public void run() {
        try (database db = new database()) {
            if (!db.collectionExist(NAME_COLLECTION)) {
                db.createCollection(NAME_COLLECTION);
            }

            for (String s : sites) {
                synchronized (this) {
                    if (stopped) {
                        break;
                    }
                    if (!db.existADocumentWithThisUrlInSITES(s) && !s.trim().isEmpty()) {
                        System.out.println(NAME_COLLECTION);

                        TestCase test = new TestCaseImplementation(s);
                        if (whatTest.size() >= 6 && (whatTest.contains("wordpress") || whatTest.contains("joomla") || whatTest.contains("plone") || whatTest.contains("drupal") || whatTest.contains("typo3"))) {
                            test.testAll();
                        } else {
                            test.testHomepage();
                            for (String current : whatTest) {
                                switch (current) {
                                    case "wordpress":
                                        test.testWordpress();
                                        break;
                                    case "joomla":
                                        test.testJoomla();
                                        break;
                                    case "plone":
                                        test.testPlone();
                                        break;
                                    case "typo3":
                                        test.testTypo3();
                                        break;
                                    case "drupal":
                                        test.testDrupal();
                                        break;
                                    default: break;
                                }
                            }
                        }
                        test.searchFormInThisPattern(test.listCMS());
                        test.searchFormInLinkedPagesOfHomepage();
                        Site site = (Site) test.getSite();
                        site.setVisitedNow();
                        test.quitWebDriver();
                        db.insertSite(site, NAME_COLLECTION);

                    } else {
                        System.out.println("Already exist: " + s);
                    }
                }
                //tolgo dalla coda di questo task il doc perché è stato appena scansionato
                db.getMongoDB().getCollection(NAME_STATE).deleteOne(new Document("site", s).append("task_id", task_id));
            }
            //ho fatto tutta la lista quindi posso rimuovere i siti con quel task dalla lista (nel caso in cui il thread sia stato killato prima di rimuovere tutto)
            db.getMongoDB().getCollection(NAME_STATE).deleteMany(new Document("task_id", task_id));
            //rimuovo l'associazione tra task_id e thread tanto ho finito
            map.remove(task_id);

        } catch (IllegalArgumentException | MongoException e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    public synchronized void kill() {
        stopped = true;
        notify();
    }

    public long getLenghtSites() {
        return sites.size();
    }
}
