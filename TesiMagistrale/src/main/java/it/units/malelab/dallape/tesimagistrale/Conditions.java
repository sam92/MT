/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bson.Document;

/**
 *
 * @author Samuele
 */
public class Conditions {

    private List<String> sites;
    private List<String> test;
    private boolean reanalyze;
    private String taskID;
    private String NAME_COLLECTION;
    private String NAME_STATE;
    private Thread t;
    private Map<String, Boolean> progress;

    public Conditions() {
    }

    public Conditions(List<String> sites, List<String> test, boolean reanalyze, String taskID, String NAME_COLLECTION, String NAME_STATE, Thread t) {
        this.NAME_COLLECTION = NAME_COLLECTION;
        this.NAME_STATE = NAME_STATE;
        this.reanalyze = reanalyze;
        this.sites = sites;
        this.taskID = taskID;
        this.test = test;
        this.t = t;
        progress = new ConcurrentHashMap<>();
        for (String s : sites) {
            progress.put(s, false);
        }
    }

    public Conditions(List<String> sites, List<String> test, boolean reanalyze, String taskID, String NAME_COLLECTION, String NAME_STATE) {
        this.NAME_COLLECTION = NAME_COLLECTION;
        this.NAME_STATE = NAME_STATE;
        this.reanalyze = reanalyze;
        this.sites = sites;
        this.taskID = taskID;
        this.test = test;
        progress = new ConcurrentHashMap<>();
        for (String s : sites) {
            progress.put(s, false);
        }
    }
    public Conditions(Map<String,Boolean> progress, List<String> test, boolean reanalyze, String taskID, String NAME_COLLECTION, String NAME_STATE) {
        this.NAME_COLLECTION = NAME_COLLECTION;
        this.NAME_STATE = NAME_STATE;
        this.reanalyze = reanalyze;
        this.sites = new ArrayList<>(progress.keySet());
        this.taskID = taskID;
        this.test = test;
        this.progress = progress;
    }

    public void setSites(List<String> sites) {
        this.sites = sites;
    }

    public void setTests(List<String> test) {
        this.test = test;
    }

    public void setReanalyze(boolean reanalyze) {
        this.reanalyze = reanalyze;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public void setNameCollection(String nameCollection) {
        this.NAME_COLLECTION = nameCollection;
    }

    public void setNameState(String nameState) {
        this.NAME_STATE = nameState;
    }

    public List<String> getSites() {
        return sites;
    }

    public List<String> getTests() {
        return test;
    }

    public boolean getReanalyze() {
        return reanalyze;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getNameCollection() {
        return NAME_COLLECTION;
    }

    public String getNameState() {
        return NAME_STATE;
    }

    public Thread getThread() {
        return t == null ? null : (t.isAlive() ? t : null);
    }

    public void setThread(Thread t) {
        this.t = t;
    }

    public Map<String, Boolean> getProgress() {
        return progress;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.append("progress", progress).append("test", test).append("reanalyze", reanalyze).append("hash", taskID).append("collection", NAME_COLLECTION).append("state", NAME_STATE);
        return doc;
    }

    public String toJSON() {
        return this.toDocument().toJson();
    }

    public static Conditions fromJSON(String json) {
        Document doc = Document.parse(json);
        Map<String, Boolean> map = new ConcurrentHashMap<>();
        Document mappa = (Document) doc.get("progress");
        for (String s : mappa.keySet()) {
            map.put(s, mappa.getBoolean(s));
        }
        List<String> test = (List<String>) doc.get("test");
        //List<String> sites = new ArrayList<>(mappa.keySet());
        return new Conditions(map, test, doc.getBoolean("reanalyze"), doc.getString("hash"), doc.getString("collection"), doc.getString("state"));
    }

}
