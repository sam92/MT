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
import com.mongodb.client.FindIterable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class executeTest extends Thread {

    //private volatile boolean stop=false;
    private final String COLLECTION_SITES;
    private final String ACTUAL_STATE;
    private List<String> sites;
    private final String task_id;
    private Map<String, Conditions> map;
    private List<String> whatTest;
    private boolean reanalyze;
    private boolean stopped;
    private boolean paused;

    public executeTest(Map<String, Conditions> map, Conditions con) {
        this.COLLECTION_SITES = con.getNameCollection();
        this.ACTUAL_STATE=con.getNameState();
        this.sites = con.getSites();
        this.task_id = con.getTaskID();
        this.map = map;
        if (!con.getTests().contains("")) {
            con.getTests().add("");
        }
        this.reanalyze=con.getReanalyze();
        this.whatTest = con.getTests();
        stopped = false;
        paused=false;
    }

    @Override
    public void run() {
        try (Database db = new Database()) {
            if (!db.collectionExist(COLLECTION_SITES)) {
                db.createCollection(COLLECTION_SITES);
            }
            if (!db.collectionExist(ACTUAL_STATE)) {
                db.createCollection(ACTUAL_STATE);
            }
            if((long)sites.size()!=db.howMuchRemainsInCollection("task_id",task_id, ACTUAL_STATE)){
                //prendi quelli che sono nello stato e sovrascrivi la lista
                sites=new ArrayList<>();
                FindIterable<Document> fi=db.getDocumentsInThisCollection(ACTUAL_STATE).find(new Document().append("task_id", task_id));
                Iterator<Document> it=fi.iterator();
                while(it.hasNext()){
                    sites.add(it.next().getString("site"));
                }
            }
            for (String s : sites) {
                synchronized (this) {
                    if (stopped) {
                        break;
                    }
                    if ((!db.existADocumentWithThisUrlInSITES(s) || reanalyze) && !s.trim().isEmpty()) {
                        System.out.println(COLLECTION_SITES);
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
                        //db.insertSite(site, NAME_COLLECTION);
                        db.updateSite(site, COLLECTION_SITES);
                    } else {
                        System.out.println("Already exist: " + s);
                    }
                //tolgo dalla coda di questo task il doc perché è stato appena scansionato
                db.getMongoDB().getCollection(ACTUAL_STATE).deleteOne(new Document("site", s).append("task_id", task_id));
                }
            }
            if(!paused){
            //ho fatto tutta la lista quindi posso rimuovere i siti con quel task dalla lista (nel caso in cui il thread sia stato killato prima di rimuovere tutto)
            db.getMongoDB().getCollection(ACTUAL_STATE).deleteMany(new Document("task_id", task_id));
            //rimuovo l'associazione tra task_id e thread tanto ho finito
            map.remove(task_id);
            }

        } catch (IllegalArgumentException | MongoException e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    public synchronized void kill() {
        stopped = true;
        notify();
    }
    public synchronized void pause(){
        paused=true;
        stopped = true;
        notify();
    }

    public long getLenghtSites() {
        return sites.size();
    }
}
