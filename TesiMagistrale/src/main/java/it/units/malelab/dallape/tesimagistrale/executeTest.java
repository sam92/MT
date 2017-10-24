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

public class executeTest extends Thread {

    //private volatile boolean stop=false;
    private final String COLLECTION_SITES;
    private final String ACTUAL_STATE;
    private final Map<String, Boolean> progress;
    private final String task_id;
    private final Conditions con;
    private final List<String> whatTest;
    private final boolean reanalyze;
    private final String stato;

    public executeTest(String task_id, Conditions con, String nameStatoTable) {
        this.COLLECTION_SITES = con.getNameCollection();
        this.ACTUAL_STATE = con.getNameState();
        this.task_id = task_id;
        if (!con.getTests().contains("")) {
            con.getTests().add("");
        }
        this.con = con;
        this.reanalyze = con.getReanalyze();
        this.whatTest = con.getTests();
        stato = nameStatoTable;
        progress = con.getProgress();
    }

    @Override
    public void run() {
        try (database db = new database()) {
            for (String s : progress.keySet()) {
                System.out.println("For "+s+" : "+(!db.existInSitesCollections(s)));
                System.out.println("For "+s+" : "+reanalyze);
                if (!db.existInSitesCollections(s) || reanalyze) {
                    if (!db.existInSTATE(s) && !s.isEmpty()) {
                        //inserisco tutti i sites nella lista dello stato.
                        db.getMongoDB().getCollection(ACTUAL_STATE).insertOne(new Document("site", s).append("task_id", task_id));
                    } else {
                        System.out.println("Already exist: " + s);
                    }
                }
            }
            long nrElement = 0;
            for (Boolean b : progress.values()) {
                if (!b) {
                    nrElement++;
                }
            }
            //sarebbe da fare un'inizializzazione controllando in db quanti sono e nel caso aggiungendo
            assert (nrElement == db.howMuchRemainsInCollection("task_id", task_id, ACTUAL_STATE));

            for (String s : progress.keySet()) {
                con.setCurrent(s);
                if (!progress.get(s)) {
                    synchronized (this) {
                        if (con.isStopped()) {
                            break;
                        }
                        if ((!db.existInSitesCollections(s) || reanalyze) && !s.trim().isEmpty() && db.existInSTATE(s)) {
                            TestCase test = new TestCaseImplementation(s);
                            if (!((SiteImplementation) test.getSite()).isUnreachable()) {
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
                                            default:
                                                break;
                                        }
                                    }
                                }
                                test.searchFormInThisPattern(test.listCMS());
                                test.searchFormInLinkedPagesOfHomepage();
                                test.quitWebDriver();
                            } else {
                                //is unreachable, inserisco in mappa cosi
                                System.out.println("Is Unreachable: " + s);
                            }
                            Site site = (Site) test.getSite();
                            site.setTASKID(task_id);
                            site.setVisitedNow();
                            //db.insertSite(site, NAME_COLLECTION);
                            boolean result = db.updateSitesCollection(site);
                            if (!result) {
                                //o site non aveva utl (molto difficile) o replacement è fallito, in tal caso segnalo con errore
                                if (site.getUrl() == null) {
                                    System.out.println("CASIN BRUTTO");
                                } else {
                                    System.out.println("ERROR INSERTING " + site.getUrl() + "in db");
                                }
                            }
                        }
                        else if(db.existInSitesCollections(s) && !reanalyze){
                            Site alreadyExistent= db.getFromCollectionsSites(s);
                            alreadyExistent.setTASKID(task_id);
                            alreadyExistent.setVisitedNow();
                            db.updateSitesCollection(alreadyExistent);
                        }
                        progress.replace(s, false, true);
                        db.updateValueMap(task_id, con, stato, false);

                    }
                }
                //tolgo dalla coda di questo task il doc perché è stato appena scansionato
                        db.getMongoDB().getCollection(ACTUAL_STATE).deleteOne(new Document("site", s).append("task_id", task_id));
                        //System.out.println("Cancello " + s);
            }
            if (!con.isPaused()) {
                //ho fatto tutta la lista quindi posso rimuovere i siti con quel task dalla lista (nel caso in cui il thread sia stato killato prima di rimuovere tutto)
                db.getMongoDB().getCollection(ACTUAL_STATE).deleteMany(new Document("task_id", task_id));
                //rimuovo l'associazione tra task_id e thread tanto ho finito
                db.deleteTaskIDFromMap(task_id, stato);
                //map.remove(task_id);
            } else {
                db.updateValueMap(task_id, con, stato, false);
            }

        } catch (IllegalArgumentException | MongoException e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    public synchronized void kill() {
        con.setStopped(true);
        notify();
    }

    public synchronized void pause() {
        con.setPaused(true);
        notify();
    }
}
