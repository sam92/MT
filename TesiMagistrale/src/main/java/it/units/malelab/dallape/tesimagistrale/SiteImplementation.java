/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author Samuele
 */
public class SiteImplementation implements Site {

    private final String url;
    private String url_after_get;
    //private boolean visited;
    //private Timestamp timestamp;
    //private final List<String[]> result;
    private String TASK_ID;
    private List<Test> listTests;
    private double result;

    /*private List<String> scanned;
    private int level;*/
    public SiteImplementation(String url) throws AssertionError {
        assert !url.trim().isEmpty();
        url = sanitization(url, true);
        this.url = url;
        //result = new ArrayList<>();
        TASK_ID = "";
        //visited = false;
        //scanned = new ArrayList<>();
        url_after_get = isReachable(url) ? "" : "Unreachable";
        //level = 0;
        listTests = new ArrayList<>();
    }

    public SiteImplementation(String url, String taskID) throws AssertionError {
        assert !url.trim().isEmpty();
        url = sanitization(url, true);
        this.url = url;
        //result = new ArrayList<>();
        TASK_ID = taskID;
        //visited = false;
        //scanned = new ArrayList<>();
        url_after_get = isReachable(url) ? "" : "Unreachable";
        //level = 0;
        listTests = new ArrayList<>();

    }

    @Override
    public void setRealUrl(String url_visited) {
        url_visited = sanitization(url_visited, true);
        url_after_get = url_visited;
    }
    
    public void reScanRealUrl(){
        url_after_get = isReachable(url) ? "" : "Unreachable";
    }

    /*
    @Override
    public void setVisitedNow() {
        visited = true;
        setTimestamp();

    }

    @Override
    public void setVisitedWhen(boolean value, Timestamp now) {
        visited = value;
        setTimestamp(now);
    }

    @Override
    public void setVisited(boolean value) {
        visited = value;
    }

    @Override
    public boolean isVisited() {
        return visited;
    }

    @Override
    public void setTimestamp() {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public void setTimestamp(Timestamp now) {
        timestamp = now;
    }

    public List<String[]> getResult() {
        return result;
    }
     */
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getRealUrl() {
        return url_after_get;
    }

    @Override
    public void setTASKID(String id) {
        TASK_ID = id;
    }

    @Override
    public String getTASKID() {
        return TASK_ID;
    }

    @Override
    public void insertTest(Test toBeAdded) {
        int index=-1;
        for(Test t: listTests){
            if(t.getName().equalsIgnoreCase(toBeAdded.getName()) && t.getTaskID().equalsIgnoreCase(toBeAdded.getTaskID())){
                index=listTests.indexOf(t);
            }
        }
        if(index>=0) listTests.add(index,toBeAdded);
        else{
            listTests.add(toBeAdded);
        }
    }

    @Override
    public List<Test> getListTests() {
        return listTests;
    }

    public void setListTests(List<Test> list) {
        listTests = list;
    }

    public List<Test> getTestWithThisName(String name) {
        List<Test> toBeReturned = new ArrayList<>();
        for (Test current : listTests) {
            if (current.getName().equalsIgnoreCase(name)) {
                toBeReturned.add(current);
            }
        }
        return toBeReturned;
    }

    public List<Test> getTestsWithThisID(String id) {
        List<Test> toBeReturned = new ArrayList<>();
        for (Test current : listTests) {
            if (current.getTaskID().equalsIgnoreCase(id)) {
                toBeReturned.add(current);
            }
        }
        return toBeReturned;
    }

    public Test getRecentTestForName(String name) {
        List<Test> tests = getTestWithThisName(name);
        return getRecentTest(tests);
    }
    public Test getRecentTestForID(String id) {
        List<Test> tests = getTestsWithThisID(id);
        return getRecentTest(tests);
    }

    private Test getRecentTest(List<Test> tests) {
        Test current = null;
        if (tests != null && !tests.isEmpty()) {
            current = tests.get(0);
            for (Test m : tests) {
                if (m.getTimestamp().after(current.getTimestamp())) {
                    current = m;
                }
            }
        }
        return current;
    }

    public void setResult(double res) {
        result = res;
    }

    public double getResult() {
        return result;
    }

    public void calculateResult(List<String> nameTestToConsider) {
        List<Test> toCalculate = new ArrayList<>();
        for (String s : nameTestToConsider) {
            if (getRecentTestForName(s) != null) {
                toCalculate.add(getRecentTestForName(s));
            }
        }
        double res = 0.0;
        for (Test current : toCalculate) {
            res = res + current.getResult();
        }
        result = res / toCalculate.size();
    }

    public void calculateOneResultName(String name) {
        result = getRecentTestForName(name).getResult();
    }
    public void calculateOneResultID(String id) {
        result = getRecentTestForID(id).getResult();
    }

    public void calcolateResultOnRemain() {
        double res = 0.0;
        for (Test current : listTests) {
            res = res + current.getResult();
        }
        if (listTests.size() > 0) {
            result = res / listTests.size();
        } else {
            result = 0.0;
        }
    }

    /*
    private void insertIntoResultValues(String location_form, String action, String link_click) {
        location_form = sanitization(location_form, false);
        action = sanitization(action, false);
        link_click = sanitization(link_click, false);
        String[] l = new String[3];
        l[0] = location_form;
        l[1] = action;
        l[2] = link_click;
        insertIntoResult(l);
    }

    private void insertIntoResult(String[] terna) throws AssertionError {
        assert terna.length == 3;
        boolean alreadyExist = false;
        if (!(terna[0].trim().isEmpty() && terna[1].trim().isEmpty() && terna[2].trim().isEmpty())) {
            for (String a : terna) {
                a = sanitization(a, false);
            }
            for (String[] t : result) {
                if (t[0].equalsIgnoreCase(terna[0]) && t[1].equalsIgnoreCase(terna[1]) && t[2].equalsIgnoreCase(terna[2])) {
                    alreadyExist = true;
                }
            }
        }
    }

    private boolean existIntoResult(String[] terna) {
        assert terna.length == 3;
        boolean exist = false;
        for (String a : terna) {
            a = sanitization(a, false);
        }
        for (String[] current : result) {
            if (current[0].equalsIgnoreCase(terna[0]) && current[1].equalsIgnoreCase(terna[1]) && current[2].equalsIgnoreCase(terna[2])) {
                exist = true;
            }
        }
        return exist;
    }

    private boolean alreadyFindInUrlOrFormLocation(String value) {
        boolean exist = false;
        value = sanitization(value, false);
        for (String[] current : result) {
            if (current[0].equalsIgnoreCase(value) || current[1].equalsIgnoreCase(value) || current[2].equalsIgnoreCase(value)) {
                exist = true;
            }
        }
        return exist;
    }

    public List<String> getUrlScanned() {
        return scanned;
    }

    public boolean isAlreadyScanned(String value) {
        value = sanitization(value, false);
        return scanned.contains(value);
    }

    public void setScanned(String value) {
        value = sanitization(value, false);
        if (!scanned.contains(value)) {
            scanned.add(value);
        }
    }
    public int getLevel(){
        return level;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        List<JSONObject> list = new ArrayList<>();
        for (String[] current : result) {
            JSONObject w = new JSONObject();
            w.put("link_click", current[2]);
            w.put("action", current[1]);
            w.put("location_form", current[0]);
            list.add(w);
        }
        if (url != null) {
            json.put("url", url);
        }
        if (url_after_get != null) {
            json.put("url_true", url_after_get);
        }
        if (TASK_ID != null) {
            json.put("task_id", TASK_ID);
        }
        json.put("visited", visited);
        if (timestamp != null) {
            json.put("timestamp", timestamp.toString());
        }
        json.put("result", new JSONArray(list));
        return json;
    }

    public static SiteImplementation fromJSON(JSONObject json) {
        SiteImplementation site = new SiteImplementation(json.getString("url_site"));
        try {
            site.setVisited(json.getBoolean("visited"));
        } catch (JSONException e) {
            site.setVisited(false);
        }
        try {
            site.setRealUrl(json.getString("url_site_true"));
        } catch (JSONException e) {
            site.setRealUrl("");
        }
        try {
            //site.setTimestamp(Timestamp.valueOf(json.getString("timestamp")));
            site.setTASKID(json.getString("task_id"));
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        JSONArray result = json.getJSONArray("result");
        for (int i = 0; i < result.length(); i++) {
            if (!result.isNull(i)) {
                String[] a = (String[]) result.get(i);
                site.insertIntoResult(a);
            }
        }
        return site;
    }*/
    @Override
    public String toJSONString() {
        return this.toDocument().toJson();
    }

    @Override
    public boolean isUnreachable() {
        return url_after_get.equalsIgnoreCase("Unreachable");
    }

    public static boolean isReachable(String url) {
        boolean reachable = false;
        try {
            url = sanitization(url, true);
            System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.connect();
            reachable = connection.getResponseCode() >= 200 && connection.getResponseCode() < 500 && connection.getResponseCode() != 404;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return reachable;
    }

    @Override
    public Document toDocument() {
        List<Document> list = new ArrayList<>();
        /*for (String[] current : result) {
            Document w = new Document().append("link_click", current[2]).append("action", current[1]).append("location_form", current[0]);
            list.add(w);
        }*/
        for (Test t : listTests) {
            list.add(t.toDocument());
        }
        Document doc = new Document("url", url).append("url_true", url_after_get).append("task_id", TASK_ID).append("result", result).append("tests", list);
        //Document doc = new Document("url", url).append("url_true", url_after_get).append("task_id", TASK_ID).append("result",result).append("tests", list);
        //Document doc = new Document("$push", new Document("result", new Document("timestamp", timestamp).append("url_finded", new JSONArray(list))));
        return doc;
    }

    public static Site fromDocument(Document d) {
        Site s = new SiteImplementation(d.getString("url"));
        if (d.getString("url_true") != null) {
            s.setRealUrl(d.getString("url_true"));
        }
        if (d.getString("task_id") != null) {
            s.setTASKID(d.getString("task_id"));
        }
        /*if (d.getBoolean("visited") != null) {
            s.setVisited(d.getBoolean("visited"));
        }
        if (d.getString("timestamp") != null) {
            s.setTimestamp(Timestamp.valueOf(d.getString("timestamp")));
        }*/
        if (d.get("tests") != null) {
            List<Document> result = (List<Document>) d.get("tests");
            //String[] current = new String[3];
            for (Document doc : result) {
                //current[0] = doc.getString("location_form");
                //current[1] = doc.getString("action");
                //current[2] = doc.getString("link_click");
                //((SiteImplementation) s).insertIntoResult(current);
                s.insertTest(TestFormImplementation.fromDocument(doc, s));

            }
        }
        return s;
    }

    public static String sanitization(String value, boolean isUrl) {
        if (value.trim().endsWith("/")) {
            value = value.trim().substring(0, value.trim().length() - 1);
        }
        if (isUrl) {
            if (!value.trim().startsWith("http")) {
                value = "http://" + value.trim();
            }
        }
        //if value passed is a whitespace
        return value.trim();
    }

    private Site getSiteFiltered(List<String> recentTestToShow, String task_id) {
        Site s = fromDocument(this.toDocument());
        List<Test> list = new ArrayList<>();
        if (task_id.isEmpty()) {
            for (String test : recentTestToShow) {
                if (((SiteImplementation) s).getRecentTestForName(test) != null) {
                    list.add(((SiteImplementation) s).getRecentTestForName(test));
                }
            }
        } else {
            if (((SiteImplementation) s).getTestsWithThisID(task_id) != null) {
                list = ((SiteImplementation) s).getTestsWithThisID(task_id);
            }
        }
        if(list==null){
            list=new ArrayList<>();
        }
        ((SiteImplementation) s).setListTests(list);
        ((SiteImplementation) s).calcolateResultOnRemain();
        return s;
    }

    public Site getSiteFiltered(String task_id) {
        return getSiteFiltered(null, task_id);
    }

    public Site getSiteFiltered(List<String> recentTestToShow) {
        return getSiteFiltered(recentTestToShow, "");
    }
}
