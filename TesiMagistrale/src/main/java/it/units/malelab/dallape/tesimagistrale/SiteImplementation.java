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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Samuele
 */
public class SiteImplementation implements Site {

    private final String url;
    private String url_after_get;
    private boolean visited;
    private Timestamp timestamp;
    private final List<String[]> result;
    private String TASK_ID;
    private List<String> scanned;

    public SiteImplementation(String url) throws AssertionError {
        assert !url.trim().isEmpty();
        url=sanitization(url, true);
        this.url = url;
        result = new ArrayList<>();
        TASK_ID = "";
        visited = false;
        scanned=new ArrayList<>();
        url_after_get = isReachable(url) ? "" : "Unreachable";
    }

    public SiteImplementation(String url, String taskID) throws AssertionError {
        assert !url.trim().isEmpty();
        url=sanitization(url, true);
        this.url = url;
        result = new ArrayList<>();
        TASK_ID = taskID;
        visited = false;
        scanned=new ArrayList<>();
        url_after_get = isReachable(url) ? "" : "Unreachable";
    }

    @Override
    public void setRealUrl(String url_visited) {
        url_visited=sanitization(url_visited, true);
        url_after_get = url_visited;
    }

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

    /*public int whereIsThisValueInsideResult(String urlpage) {
        int res = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i)[0].equalsIgnoreCase(urlpage)) {
                res = i;
            }
        }
        return res;
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

    public void insertIntoResultValues(String location_form, String action, String link_click) {
        location_form=sanitization(location_form, false);
        action=sanitization(action, false);
        link_click=sanitization(link_click, false);
        String[] l = new String[3];
        l[0] = location_form;
        l[1] = action;
        l[2] = link_click;
        insertIntoResult(l);
    }

    public void insertIntoResult(String[] terna) throws AssertionError {
        assert terna.length == 3;
        boolean alreadyExist = false;
        if (!(terna[0].trim().isEmpty() && terna[1].trim().isEmpty() && terna[2].trim().isEmpty())) {
            for(String a : terna){
            a=sanitization(a, false);
        }
            for (String[] t : result) {
                if (t[0].equalsIgnoreCase(terna[0]) && t[1].equalsIgnoreCase(terna[1]) && t[2].equalsIgnoreCase(terna[2])) {
                    alreadyExist = true;
                }
            }
            if (!alreadyExist) {
                result.add(terna);
            }
        }
    }
    
    public boolean existIntoResult(String[] terna){
        assert terna.length == 3;
        boolean exist = false;
        for(String a : terna){
            a=sanitization(a, false);
        }
        for(String[] current : result){
            if(current[0].equalsIgnoreCase(terna[0]) && current[1].equalsIgnoreCase(terna[1]) &&current[2].equalsIgnoreCase(terna[2])) exist=true;
        }
        return exist;
    }
    
        public boolean alreadyFindInUrlOrFormLocation(String value){
        boolean exist = false;
        value=sanitization(value, false);
        for(String[] current : result){
            if(current[0].equalsIgnoreCase(value) || current[1].equalsIgnoreCase(value) || current[2].equalsIgnoreCase(value)) exist=true;
        }
        return exist;
    }
    public List<String> getUrlScanned(){
        return scanned;
    }
    public boolean isAlreadyScanned(String value){
        value=sanitization(value, false);
        return scanned.contains(value);
    }
    public void setScanned(String value){
        value=sanitization(value, false);
        if(!scanned.contains(value)) scanned.add(value);
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
        if(url!=null) json.put("url_site", url);
        if(url_after_get!=null) json.put("url_site_true", url_after_get);
        if(TASK_ID!=null) json.put("task_id",TASK_ID);
        json.put("visited", visited);
        if(timestamp!=null) json.put("timestamp", timestamp.toString());
        json.put("result", new JSONArray(list));
        return json;
    }

    public static SiteImplementation fromJSON(JSONObject json) {
        SiteImplementation site= new SiteImplementation(json.getString("url_site"));
        try{
            site.setVisited(json.getBoolean("visited"));
        }
        catch(JSONException e){
            site.setVisited(false);
        }
        try{
        site.setRealUrl(json.getString("url_site_true"));
        }
        catch(JSONException e){
            site.setRealUrl("");
        }
        try{
        site.setTimestamp(Timestamp.valueOf(json.getString("timestamp")));
        site.setTASKID(json.getString("task_id"));
        }
        catch(JSONException e){
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
    }

    @Override
    public String toJSONString() {
        return this.toJSON().toString();
    }

    @Override
    public String toString() {
        String a = "URL: " + url + "\n" + "URL_GET: " + url_after_get + "\n" + "VISITED: " + visited + "\n" + "TIME: " + timestamp + "\n" + "TASK_ID: " + TASK_ID + "\n\n";
        for (String[] s : result) {
            a = a + "WHERE_FORM: " + s[0] + "\n" + "ACTION: " + s[1] + "\n" + "LINKED FROM: " + s[2] + "\n\n";
        }
        return a;
    }
    @Override
    public boolean isUnreachable(){
        return "Unreachable".equals(url_after_get);
    }

    public static boolean isReachable(String url) {
        boolean reachable = false;
        try {
            url=sanitization(url, true);
            System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.connect();
            reachable = connection.getResponseCode() >= 200 && connection.getResponseCode() < 500 && connection.getResponseCode()!=404;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return reachable;
    }

    @Override
    public Document toDocument() {
        List<Document> list = new ArrayList<>();
        for (String[] current : result) {
            Document w = new Document().append("link_click", current[2]).append("action", current[1]).append("location_form", current[0]);
            list.add(w);
        }      
        Document doc=new Document("url_site", url).append("url_site_true", url_after_get).append("task_id",TASK_ID).append("visited", visited).append("timestamp", timestamp.toString()).append("result",list);
        //Document doc = new Document("$push", new Document("result", new Document("timestamp", timestamp).append("url_finded", new JSONArray(list))));
        return doc;
    }
    public static String sanitization(String value, boolean isUrl){
        if (value.trim().endsWith("/")) {
            value = value.trim().substring(0, value.trim().length() - 1);
        }
        if(isUrl){
            if (!value.trim().startsWith("http")) {
            value = "http://" + value.trim();
        }
        }
        //if value passed is a whitespace
        return value.trim();
    }
}
