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

public class executeTest extends Thread {

    //private volatile boolean stop=false;
    private final Map<String, Boolean> progress;
    private final String task_id;
    private final Conditions con;
    private final List<String> whatTest;
    private final boolean reanalyze;

    public executeTest(String task_id, Conditions con) {
        this.task_id = task_id;
        if (!con.getTests().contains("")) {
            con.getTests().add("");
        }
        this.con = con;
        this.reanalyze = con.getReanalyze();
        this.whatTest = con.getTests();
        progress = con.getProgress();
    }

    @Override
    public void run() {
        try (database db = new database()) {
            for (String s : progress.keySet()) {
                synchronized (this) {
                    if (con.isStopped() || con.isPaused()) {
                        if (con.isPaused()) {
                            db.updateValueMap(task_id, con, false);
                        }
                        break;
                    }
                    if (!db.existInSitesCollections(s) || reanalyze) {
                        if (!db.existInSTATE(s) && !s.isEmpty()) {
                            //inserisco tutti i sites nella lista dello stato.
                            db.insertOneInState(s, task_id);
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
                            //se esiste gia nella mappa dei siti si pesca da là e si inseriscono cose nuove, poi si risostituisce il tutto
                            Site site=db.getFromCollectionsSites(s);
                            if(site==null){
                                site = new SiteImplementation(s);
                            }
                            else{
                                ((SiteImplementation)site).reScanRealUrl();
                            }
                            TestForm formTest = new TestFormImplementation(site, task_id);
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
                                TestContacts contactsTest = new TestContactsImplementation(site, formTest.getWebDriver(), task_id);
                                contactsTest.start();
                                site.insertTest(contactsTest);
                                TestWeight scad = new TestWeightImplementation(site, contactsTest.getWebDriver(), task_id);
                                scad.start();
                                site.insertTest(scad);
                                scad.quitWebDriver();
                                System.out.println(site.toJSONString());
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
                            //NON HA PIU MOLTO SENSO SCANSIONARE SE ESISTE GIA VISTO CHE LO PRELEVO SE ESISTE
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
                            db.updateValueMap(task_id, con, false);
                        }

                    }
                }
                //tolgo dalla coda di questo task il doc perché è stato appena scansionato
                db.deleteOneFromState(s, task_id);
                //System.out.println("Cancello " + s);
            }
            if (!con.isPaused()) {
                //ho fatto tutta la lista quindi posso rimuovere i siti con quel task dalla lista (nel caso in cui il thread sia stato killato prima di rimuovere tutto)
                db.deleteManyFromState(task_id);
                //rimuovo l'associazione tra task_id e thread tanto ho finito
                db.deleteTaskIDFromMap(task_id);
                db.insertTaskID(task_id);
            } else {
                db.updateValueMap(task_id, con, false);
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
