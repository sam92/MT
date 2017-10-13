/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import com.mongodb.MongoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;

/*
idea fare un hashmap generica che contiene il taskid
ogni sito inserito in db segnala anche il task id
quando faccio partire un altro task guardo se il sito è visited e se ha lo stesso task id
 */
/**
 *
 * @author Samuele
 */
public class ServletJob_noPause extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext context = getServletContext();
//fare un metodo che restituisce la lista di siti in STATE_LIST_SITES con un certo task_id
        Map<String, Conditions> mappaConditions = (Map<String, Conditions>) context.getAttribute("map");
        Map<String, Thread> mappaThread = (Map<String, Thread>) new ConcurrentHashMap<String, Thread>();
        List<String> sitesInInput = Arrays.asList(request.getParameterValues("site"));
        List<String> test = Arrays.asList(request.getParameterValues("test"));
        String action = request.getParameter("condition");
        boolean reanalyze = Boolean.valueOf(request.getParameter("reanalyze"));

        if (action == null) {
            action = "";
        }
        String hash = request.getParameter("hash");
        if (hash == null) {
            hash = "";
        }

        String NAME_COLLECTION = "SITES";
        String STATE = "STATE_LIST_SITES";
        String MAPPING_HASH_THREAD = "HASH_THREAD"; //mappa hash-thread ossia task_id-thread 

//al client deve essere inviato l'hash perché deve poter visualizzare le info di quella request. al posto di un counter l'hash potrebbe essere il task_id
//fare una maps e mettere hash e task_id e salvarla in db, dopo tirare su quella per capire che task sono in sospeso
        try (database db = new database()) {
            if (!db.collectionExist(MAPPING_HASH_THREAD)) {
                db.createCollection(MAPPING_HASH_THREAD);
            }
            if (!db.collectionExist(STATE)) {
                db.createCollection(STATE);
            }

//faccio un altro servlet che fa partire i resume e fa il kill del thread
//inserisco tutti i sites nella lista dello stato.
            switch (action) {
                case "start":
                    if (mappaConditions.get(hash)== null) {//start condition
                        //potrei fare un hash dei siti quando arrivano e farlo diventare il task_id 
                        List<String> sites = new ArrayList<>();
                        for (String s : sitesInInput) {
                            sites.add(s.trim());
                        }
                        String phrase = "";
                        for (int i = 0; i < sites.size(); i++) {
                            phrase += sites.get(i);
                        }
                        String task_id = getHashSHA1(phrase);
                        for (String s : sites) {
                            if (!db.existSiteInSTATE(s) && !s.trim().isEmpty()) {
                                Document current = new Document("site", s);
                                current.append("task_id", task_id);
                                db.getMongoDB().getCollection(STATE).insertOne(current);
                            } else {
                                System.out.println("Already exist: " + s);
                            }
                        }
                        Conditions con=new Conditions(sites, test, reanalyze, task_id, NAME_COLLECTION, STATE);
                        Thread t = new executeTest_noPause_reanalyze(mappaConditions,con);
                        mappaConditions.put(task_id, con);
                        mappaThread.put(task_id, t);
                        t.start();
                        //mettere in db id task con lista di condizioni ()se fare
                        //fare un dispatcher e mandare indietro task_id
                    }                 
                    break;
                case "stop":
                    Thread toBeStopped = mappaThread.get(hash);
                    if (toBeStopped != null) {
                        ((executeTest) toBeStopped).kill();
                        //map.remove(hash);
                        //db.getMongoDB().getCollection(STATE).deleteMany(new Document("task_id", hash));
                        //fare un dispatcher fatto ad hoc che rimanda alla pagina principale
                    }
                    mappaConditions.remove(hash);
                    break;
                case "pause":
                    Thread toBePaused = mappaThread.get(hash);
                    if (toBePaused != null) ((executeTest) toBePaused).pause();
                    break;
                case "resume": Thread toBeResumed = mappaThread.get(hash);
                    if (toBeResumed != null) ((executeTest) toBeResumed).resumeFromPause();
                    break;
                default: //dispatcher
                    break;
            }

        } catch (IllegalArgumentException | MongoException e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
        
        //fare un dispatcher verso il jsp che gestisce la progress bar
        //response with progress bar and task_id to stop operations
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public static String getHashSHA1(String request) throws UnsupportedEncodingException {
        byte[] byteArray = request.trim().getBytes("UTF-8");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.getMessage());
        }

        return new String(md.digest(byteArray));
    }

}
