/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.ArrayList;
import java.util.Iterator;
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
    //private String task_id;
    //private Thread t;
    private boolean paused = false;
    private boolean stopped = false;
    private String current;
    private Map<String, Boolean> progress;

    public Conditions() {
    }

    public Conditions(List<String> sites, List<String> test, boolean reanalyze) {
        this.reanalyze = reanalyze;
        this.sites = sites;
        this.test = test;
        progress = new ConcurrentHashMap<>();
        for (String s : sites) {
            progress.put(s, false);
        }
        current="";
    }

    public Conditions(Map<String, Boolean> progress, List<String> test, boolean reanalyze) {
        this.reanalyze = reanalyze;
        this.sites = new ArrayList<>(progress.keySet());
        //this.task_id = taskID;
        this.test = test;
        this.progress = progress;
        current="";
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
/*
    public void setTaskID(String taskID) {
        this.task_id = taskID;
    }
*/
    public List<String> getSites() {
        return sites;
    }

    public List<String> getTests() {
        return test;
    }

    public boolean getReanalyze() {
        return reanalyze;
    }
/*
    public String getTaskID() {
        return task_id;
    }
*/
/*
    public Thread getThread() {
        return t == null ? null : (t.isAlive() ? t : null);
    }

    public void setThread(Thread t) {
        this.t = t;
    }
*/
    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setPaused(boolean value) {
        this.paused = value;
    }

    public void setStopped(boolean value) {
        this.stopped = value;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getCurrent() {
        return current;
    }

    public Map<String, Boolean> getProgress() {
        return progress;
    }

    public long getSizeSites() {
        return progress.keySet().size();
    }

    public int getNrAlreadyScan() {
        int size = 0;
        List<Boolean> l = new ArrayList<>(progress.values());
        Iterator<Boolean> it = l.iterator();
        while (it.hasNext()) {
            if (it.next()) {
                size++;
            }
        }
        return size;
    }

    public Document toDocument() {
        Document doc = new Document();
        String status = "undefined";
        if (paused) {
            status = "paused";
        } else if (stopped) {
            status = "stopped";
        }
        List<Document> lista=new ArrayList<>();
        for(String s: progress.keySet()){
            lista.add(new Document("site",s).append("done",progress.get(s)));
        }
        doc.append("progress", lista).append("test", test).append("reanalyze", reanalyze).append("status", status).append("current",current);
        return doc;
    }

    public String toJSON() {
        return this.toDocument().toJson();
    }

    public static Conditions fromJSON(String json) {
        Document doc = Document.parse(json);
        Map<String, Boolean> map = new ConcurrentHashMap<>();
        List<Document> mappa = (List<Document>) doc.get("progress");
        /*for (String s : mappa.keySet()) {
            map.put(s, mappa.getBoolean(s));
        }*/
        for(Document s: mappa){
            map.put(s.getString("site"), s.getBoolean("done"));
        }
        List<String> test = (List<String>) doc.get("test");
        //List<String> sites = new ArrayList<>(mappa.keySet());
        Conditions c = new Conditions(map, test, doc.getBoolean("reanalyze"));
        if(doc.getString("current")!=null){
            c.setCurrent(doc.getString("current"));
        }
        switch (doc.getString("status")) {
            case "paused":
                c.setPaused(true);
                break;
            case "stopped":
                c.setStopped(true);
                break;
            default:
                break;
        }
        return c;
    }

}
