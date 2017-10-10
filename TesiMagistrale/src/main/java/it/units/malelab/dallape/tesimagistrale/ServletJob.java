/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
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
        AtomicLong counter = (AtomicLong) context.getAttribute("counter");
        Map<Long, Thread> map = (Map<Long, Thread>) context.getAttribute("map");
        List<String> sites = Arrays.asList(request.getParameterValues("site"));
        String NAME_COLLECTION = "SITES";
        String STATE = "STATE_LIST_SITES";
        String MAPPING_HASH_TASK = "HASH_TASK"; //mappa hash-task 
        long task_id = counter.getAndIncrement();
//potrei fare un hash dei siti quando arrivano e associarlo ad un task_id 
    String phrase="";
    for(int i=0; i<sites.size(); i++){
        phrase+=sites.get(i);
    }
    String hash= getHashSHA1(phrase);
//al client deve essere inviato l'hash perché deve poter visualizzare le info di quella request. al posto di un counter l'hash potrebbe essere il task_id
//fare una maps e mettere hash e task_id e salvarla in db, dopo tirare su quella per capire che task sono in sospeso
        try (database db = new database()) {
            if (!db.collectionExist(MAPPING_HASH_TASK)) {
                db.createCollection(MAPPING_HASH_TASK);
            }
            if (!db.collectionExist(STATE)) {
                db.createCollection(STATE);
            }

//faccio un altro servlet che fa partire i resume e fa il kill del thread
//inserisco lo stato-> prima dovrei guardare se c'era uno stato prima
//tipo if(resume) check in stato quali mancano 

//inserisco tutti i sites nella lista dello stato. 
            for (String s : sites) {
                if (!db.existSiteInSTATE(s) && !s.trim().isEmpty()) {
                    Document current = new Document("site", s);
                    current.append("task_id", task_id);
                    db.getMongoDB().getCollection(STATE).insertOne(current);
                } else {
                    System.out.println("Already exist: " + s);
                }
            }
            db.getMongoDB().getCollection(STATE).count();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
        Thread t = new executeTest(NAME_COLLECTION, sites, task_id);
        map.put(task_id, t);
        t.start();
        //response with progress bar
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
       byte[] byteArray= request.trim().getBytes("UTF-8");
    MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.getMessage());
        }

    return new String(md.digest(byteArray));
}

}
