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
import org.json.JSONObject;

/**
 *
 * @author Samuele
 */
public class SiteImplementation_noReanalyze implements Site {

    private final String url;
    private String url_after_get;
    private boolean visited;
    private Timestamp timestamp;
    private final List<String[]> result;
    private String TASK_ID;

    public SiteImplementation_noReanalyze(String url) throws AssertionError {
        assert !url.trim().isEmpty();
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
        result = new ArrayList<>();
        TASK_ID = "";
        visited=false;
        url_after_get=isReachable(url) ? "" : "Unreachable";
    }

    public SiteImplementation_noReanalyze(String url, String taskID) throws AssertionError {
        assert !url.trim().isEmpty();
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
        result = new ArrayList<>();
        TASK_ID = taskID;
        visited=false;
        url_after_get=isReachable(url) ? "" : "Unreachable";
    }

    @Override
    public void setRealUrl(String url_visited) {
        if (url_visited.endsWith("/")) {
            url_visited = url_visited.substring(0, url_visited.length() - 1);
        }
        if (!url_visited.startsWith("http")) {
            url_visited = "http://" + url_visited;
        }
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

    public int whereIsThisValueInsideResult(String urlpage) {
        int res = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i)[0].equalsIgnoreCase(urlpage)) {
                res = i;
            }
        }
        return res;
    }

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
        String[] l = new String[3];
        l[0] = location_form.trim();
        l[1] = action.trim();
        l[2] = link_click.trim();
        insertIntoResult(l);
    }

    public void insertIntoResult(String[] terna) throws AssertionError {
        assert terna.length == 3;
        boolean alreadyExist = false;
        if (!(terna[0].trim().isEmpty() && terna[1].trim().isEmpty() && terna[2].trim().isEmpty())) {
            if (terna[0].endsWith("/")) {
                terna[0] = terna[0].substring(0, terna[0].length() - 1);
            }
            if (terna[1].endsWith("/")) {
                terna[1] = terna[1].substring(0, terna[1].length() - 1);
            }
            if (terna[2].endsWith("/")) {
                terna[2] = terna[2].substring(0, terna[2].length() - 1);
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
        json.put("url_site", url);
        json.put("url_site_true", url_after_get);
        json.put("visited", visited);
        json.put("timestamp", timestamp);
        json.put("task_id", TASK_ID);
        json.put("result", new JSONArray(list));
        return json;
    }

    public static SiteImplementation_noReanalyze fromJSON(JSONObject json) {
        SiteImplementation_noReanalyze site = new SiteImplementation_noReanalyze(json.getString("url_site"));
        site.setVisited(json.getBoolean("visited"));
        site.setRealUrl(json.getString("url_site_true"));
        site.setTASKID(json.getString("task_id"));
        site.setTimestamp((Timestamp) json.get("timestamp"));
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

    public static boolean isReachable(String url) {
        boolean reachable = false;
        try {
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            reachable = connection.getResponseCode() >=200 && connection.getResponseCode()<400;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return reachable;
    }

    @Override
    public Document toDocument() {
        return Document.parse(toJSONString());
    }

    @Override
    public boolean isUnreachable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
