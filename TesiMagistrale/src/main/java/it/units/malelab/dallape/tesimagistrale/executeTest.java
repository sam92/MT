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
                synchronized (this) {
                    if (con.isStopped() || con.isPaused()) {
                        if (con.isPaused()) {
                            db.updateValueMap(task_id, con, stato, false);
                        }
                        break;
                    }
                    if (!db.existInSitesCollections(s) || reanalyze) {
                        if (!db.existInSTATE(s) && !s.isEmpty()) {
                            //inserisco tutti i sites nella lista dello stato.
                            db.getMongoDB().getCollection(ACTUAL_STATE).insertOne(new Document("site", s).append("task_id", task_id));
                        } else {
                            System.out.println("Already exist: " + s);
                        }
                    }
                }
            }
            /*
            //sarebbe da fare un'inizializzazione controllando in db quanti sono e nel caso aggiungendo
            long nrElement = 0;
            for (Boolean b : progress.values()) {
                if (!b) {
                    nrElement++;
                }
            }
            assert (nrElement == db.howMuchRemainsInCollection("task_id", task_id, ACTUAL_STATE));
             */
            for (String s : progress.keySet()) {
                con.setCurrent(s);
                if (!progress.get(s)) {
                    synchronized (this) {
                        if (con.isStopped() || con.isPaused()) {
                            break;
                        }
                        if ((!db.existInSitesCollections(s) || reanalyze) && !s.trim().isEmpty() && db.existInSTATE(s)) {
                            Site site=new SiteImplementation(s);
                            TestForm formTest = new TestFormImplementation(site,task_id);
                            if (!site.isUnreachable()) {
                                if (whatTest.size() >= 6 && (whatTest.contains("wordpress") || whatTest.contains("joomla") || whatTest.contains("plone") || whatTest.contains("drupal") || whatTest.contains("typo3"))) {
                                    formTest.testAllCMS();
                                } else {
                                    formTest.testHomepage();
                                    for (String current : whatTest) {
                                        switch (current) {
                                            case "wordpress":
                                                formTest.testWordpress();
                                                break;
                                            case "joomla":
                                                formTest.testJoomla();
                                                break;
                                            case "plone":
                                                formTest.testPlone();
                                                break;
                                            case "typo3":
                                                formTest.testTypo3();
                                                break;
                                            case "drupal":
                                                formTest.testDrupal();
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                                formTest.start();
                                site.insertTest(formTest);
                                /*
                                formTest.searchFormInThesePaths(formTest.listCMS());
                                if (!(con.isPaused() || con.isStopped())) {
                                    formTest.searchFormInLinkedPagesOfHomepage();
                                }*/
                                Test contactsTest= new TestContacts(site,formTest.getWebDriver() ,task_id);
                                site.insertTest(contactsTest);
                                formTest.quitWebDriver();
                            } else {
                                //is unreachable, inserisco in mappa cosi
                                System.out.println("Is Unreachable: " + s);
                            }
                            if (!(con.isPaused() || con.isStopped())) {
                                //site.setTASKID(task_id);
                                //site.setVisitedNow();
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
                        } else if (db.existInSitesCollections(s) && !reanalyze) {
                            if (!(con.isPaused() || con.isStopped())) {
                                Site alreadyExistent = db.getFromCollectionsSites(s);
                                //alreadyExistent.setTASKID(task_id);
                                //alreadyExistent.setVisitedNow();
                                db.updateSitesCollection(alreadyExistent);
                            }
                        }
                        if (!(con.isPaused() || con.isStopped())) {
                            progress.replace(s, false, true);
                            db.updateValueMap(task_id, con, stato, false);
                        }

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
                db.insertTaskID(task_id);
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
