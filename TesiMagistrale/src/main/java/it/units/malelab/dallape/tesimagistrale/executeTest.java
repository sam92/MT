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
import java.util.Map;

public class executeTest extends Thread {

    //private volatile boolean stop=false;
    private final Map<String, Boolean> progress;
    private final String task_id;
    private final Conditions con;
    private final boolean reanalyze;
    private final String FORM = "FORM";
    private final String CONTACTS = "CONTACTS";
    private final String WEIGHT = "WEIGHT";

    public executeTest(String task_id, Conditions con) {
        this.task_id = task_id;
        this.con = con;
        this.reanalyze = con.getReanalyze();
        progress = con.getProgress();
    }

    @Override
    public void run() {
        try (database db = new database()) {
            for (String s : progress.keySet()) {
                if (!progress.get(s)) {
                    synchronized (this) {
                        //se è stoppato posso far qua le robe del db invece che in servlet job
                        if (con.isStopped() || con.isPaused()) {
                            if (con.isPaused()) {
                                db.updateValueMap(task_id, con, false);
                            }
                            break;
                        }
                        //if (!db.existInSTATE(s)) {
                        //inserisco tutti i sites nella lista dello stato.
                        db.insertOneInState(s, task_id); //in realtà faccio un update se non esiste
                        /*} else {
                            System.out.println("Already exist: " + s);
                        }*/
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
                        if (db.existInSTATE(s, task_id)) {
                            //se esiste gia nella mappa dei siti si pesca da là e si inseriscono cose nuove, poi si risostituisce il tutto
                            Site site = db.getFromCollectionsSites(s);
                            if (site == null) {
                                site = new SiteImplementation(s);
                            } else {
                                ((SiteImplementation) site).reScanRealUrl();
                            }
                            TestForm formTest = null;
                            TestContacts contactsTest = null;
                            TestWeight weight = null;
                            if (!site.isUnreachable()) {
                                if (con.getTests().contains(FORM) && (reanalyze || ((SiteImplementation) site).getTestWithThisName(FORM).isEmpty())) {
                                    formTest = new TestFormImplementation(site, task_id);
                                    formTest.start();
                                    site.insertTest(formTest);
                                }
                                if (con.getTests().contains(CONTACTS) && (reanalyze || ((SiteImplementation) site).getTestWithThisName(CONTACTS).isEmpty())) {
                                    contactsTest = new TestContactsImplementation(site, (formTest == null) ? null : formTest.getWebDriver(), task_id);
                                    contactsTest.start();
                                    site.insertTest(contactsTest);
                                }
                                if (con.getTests().contains(WEIGHT) && (reanalyze || ((SiteImplementation) site).getTestWithThisName(WEIGHT).isEmpty())) {
                                    weight = new TestWeightImplementation(site, (formTest == null) ? null : formTest.getWebDriver(), task_id);
                                    weight.start();
                                    site.insertTest(weight);
                                }
                                if (formTest != null) {
                                    formTest.quitWebDriver();
                                }
                                if (contactsTest != null) {
                                    contactsTest.quitWebDriver();
                                }
                                if (weight != null) {
                                    weight.quitWebDriver();
                                }
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
            //se arriva qua o è stato messo in pausa, o stop o ha finito quindi rimuovo tutto(se era in pausa verranno riaggiunti nello stato al riavvio del task)
            db.deleteManyFromState(task_id);
            if (!con.isPaused()) {
                //rimuovo l'associazione tra task_id e le conditions
                db.deleteTaskIDFromMap(task_id);
                //inserisco l'id nella collection che salva tutti i task completati con successo
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
