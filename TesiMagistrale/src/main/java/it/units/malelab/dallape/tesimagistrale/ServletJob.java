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
public class ServletJob extends HttpServlet {

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
        Map<String, Conditions> mappaConditions = (Map<String, Conditions>) context.getAttribute("map");
        String action = request.getParameter("condition");
        if (action == null) {
            action = "";
        }
        String hash = request.getParameter("hash");
        if (hash == null) {
            hash = "";
        }

        String COLLECTION_SITES = "SITES";
        String ACTUAL_STATE = "STATE_LIST_SITES";
        String HASH_CONDITIONS = "HASH_CONDITIONS"; //mappa hash-thread ossia task_id-thread 

//al client deve essere inviato l'hash perché deve poter visualizzare le info di quella request. al posto di un counter l'hash potrebbe essere il task_id
//fare una maps e mettere hash e task_id e salvarla in db, dopo tirare su quella per capire che task sono in sospeso
        try (database db = new database()) {
            //TO DO
            /*if (!db.collectionExist(HASH_CONDITIONS)) {
                db.createCollection(HASH_CONDITIONS);
            }*/
            if (!db.collectionExist(ACTUAL_STATE)) {
                db.createCollection(ACTUAL_STATE);
            }

            switch (action) {
                case "start":
                    Conditions con;
                    if(!hash.trim().isEmpty()){
                    if (mappaConditions.get(hash) == null) {//start condition
                        List<String> sitesInInput = Arrays.asList(request.getParameterValues("site"));
                        List<String> test = Arrays.asList(request.getParameterValues("test"));
                        boolean reanalyze = Boolean.valueOf(request.getParameter("reanalyze"));
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
                                //inserisco tutti i sites nella lista dello stato.
                                db.getMongoDB().getCollection(ACTUAL_STATE).insertOne(new Document("site", s).append("task_id", task_id));
                            } else {
                                System.out.println("Already exist: " + s);
                            }
                        }
                        con = new Conditions(sites, test, reanalyze, task_id, COLLECTION_SITES, ACTUAL_STATE);
                        mappaConditions.put(task_id, con);
                    } else {
                        //sto facendo una resume
                        con = mappaConditions.get(hash);
                    }
                    Thread t = new executeTest(mappaConditions, con);
                    con.setThread(t);
                    t.start();
                    //fare un dispatcher e mandare indietro task_id
                    }
                    break;
                case "stop":
                    Conditions c = mappaConditions.get(hash);
                    if (c != null && c.getThread() != null) {
                        Thread toBeStopped = c.getThread();
                        ((executeTest) toBeStopped).kill();
                        //mappaConditions.remove(hash);
                        //db.getMongoDB().getCollection(STATE).deleteMany(new Document("task_id", hash));
                        //fare un dispatcher fatto ad hoc che rimanda alla pagina principale
                    } else if (c != null && c.getThread() == null) {
                        mappaConditions.remove(hash);
                    }
                    break;
                case "pause":
                    //setta nelle conditions come lista di sites quelli che sono rimasti in db
                    Conditions condit = mappaConditions.get(hash);
                    if (condit != null && condit.getThread() != null) {
                        Thread toBePaused = condit.getThread();
                        ((executeTest) toBePaused).pause();

                        //fare un dispatcher fatto ad hoc che rimanda alla pagina principale
                    } else if (condit != null && condit.getThread() == null) {
                        mappaConditions.remove(hash);
                    }
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
