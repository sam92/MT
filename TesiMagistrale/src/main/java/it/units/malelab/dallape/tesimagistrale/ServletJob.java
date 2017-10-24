/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import com.mongodb.MongoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;

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
        //carica da db la mappa e fai le modifiche solo sul db. Crea classe che crea db e permette di inserire oggetti
        //Map<String, Conditions> mapTask = new ConcurrentHashMap<>();

        Map<String, Thread> mapThread;
        String action = request.getParameter("condition");
        if (action == null) {
            action = "";
        }
        String task_id = request.getParameter("task_id");
        if ((task_id != null && !task_id.isEmpty()) || action.equals("start")) {
            //si entra nel processo solo se viene inviato un task_id o in alternativa se la condizione è START
            if (task_id == null) {
                task_id = "";
            }

            String COLLECTION_SITES = "SITES";
            String ACTUAL_STATE = "STATE_LIST_SITES";
            String TASKID_CONDITIONS = "TASKID_CONDITIONS"; //mappa hash-conditions

//al client deve essere inviato l'hash perché deve poter visualizzare le info di quella request. al posto di un counter l'hash potrebbe essere il task_id
            try (database db = new database()) {
                //TO DO
                if (!db.collectionExist(TASKID_CONDITIONS)) {
                    db.createCollection(TASKID_CONDITIONS);
                }
                if (!db.collectionExist(ACTUAL_STATE)) {
                    db.createCollection(ACTUAL_STATE);
                }
                if (!db.collectionExist(COLLECTION_SITES)) {
                    db.createCollection(COLLECTION_SITES);
                }
//SE C'E ROBA NELLO STATO MA NON ESISTE IL CORRISPONDENTE TASK ID IN TASKID-CONDITION RIMUOVI TUTTO DALLO STATO
                switch (action) {
                    case "start":
                        Conditions con = db.getConditionFromMap(task_id, TASKID_CONDITIONS);
                        boolean resume = false;
                        if (con == null) {//start condition non ci sono altri task uguali iniziati
                            List<String> sitesInInput = new ArrayList<>(Arrays.asList(request.getParameterValues("site")));
                            List<String> test = new ArrayList<>(Arrays.asList(request.getParameterValues("test")));
                            boolean reanalyze = Boolean.valueOf(request.getParameter("reanalyze"));
                            List<String> sites = new ArrayList<>();
                            Pattern pattern = Pattern.compile("^(https?:\\/\\/)?(www\\.)?([\\w]+\\.)+[‌​\\w]{2,63}\\/?\\S+$");
                            for (String s : sitesInInput) {
                                if (!s.trim().isEmpty()) {
                                    Matcher m = pattern.matcher(s.trim());
                                    if(m.matches()) sites.add(SiteImplementation.sanitization(s, true));
                                }
                            }
                            String phrase = "";
                            for (int i = 0; i < sites.size(); i++) {
                                phrase += sites.get(i);
                            }
                            task_id = DigestUtils.sha1Hex(phrase);
                            con = new Conditions(sites, test, reanalyze, COLLECTION_SITES, ACTUAL_STATE);
                            db.insertValueMap(task_id, con, TASKID_CONDITIONS);
                            //mapTask.put(task_id, con);
                        } else if (con.isPaused()) { //resume condition
                            resume = true;
                            //con = mapTask.get(task_id);
                            con.setPaused(false);
                            db.updateValueMap(task_id, con, TASKID_CONDITIONS, false);
                        } else {
                            //is already started and someone has ask the same, i.e. refreshing the page
                            break;
                        }
                        Thread t = new executeTest(task_id, con, TASKID_CONDITIONS);
                        mapThread = (Map<String, Thread>) context.getAttribute("map");
                        mapThread.put(task_id, t);
                        context.setAttribute("map", mapThread);
                        t.start();

                        if (!resume) {
                            //fare un dispatcher verso il jsp che gestisce la progress bar
                            //response with progress bar and task_id to stop operations
                            //fare un dispatcher e mandare indietro task_id
                            request.setAttribute("task_id", task_id);
                            request.getRequestDispatcher("progress.jsp").forward(request, response);
                        }

                        break;
                    case "stop":
                        //Conditions c = mapTask.remove(task_id);
                        if (!task_id.isEmpty()) {
                            Conditions c = db.deleteTaskIDFromMap(task_id, TASKID_CONDITIONS);
                            mapThread = (Map<String, Thread>) context.getAttribute("map");
                            if (c != null && mapThread.get(task_id) != null) {
                                Thread toBeStopped = mapThread.get(task_id);
                                ((executeTest) toBeStopped).kill();
                                mapThread.remove(task_id);
                                context.setAttribute("map", mapThread);
                                //mappaConditions.remove(hash);
                                //db.getMongoDB().getCollection(STATE).deleteMany(new Document("task_id", hash));
                                //fare un dispatcher fatto ad hoc che rimanda alla pagina principale
                            }
                            //sovrascrivere mappa in db
                        }
                        request.getRequestDispatcher("stop.html").forward(request, response);
                        break;
                    case "pause":
                        //setta nelle conditions come lista di sites quelli che sono rimasti in db
                        //Conditions condit = mapTask.get(task_id);
                        if (!task_id.isEmpty()) {
                            Conditions condit = db.getConditionFromMap(task_id, TASKID_CONDITIONS);
                            mapThread = (Map<String, Thread>) context.getAttribute("map");
                            if (condit != null && mapThread.get(task_id) != null) {
                                Thread toBePaused = mapThread.get(task_id);
                                ((executeTest) toBePaused).pause();
                                condit.setPaused(true);
                                db.updateValueMap(task_id, condit, TASKID_CONDITIONS, false);
                                mapThread.remove(task_id);
                                context.setAttribute("map", mapThread);
                            }
                            //sovrascrivere la mappa in db
                        }
                        break;
                    default: //dispatcher 404
                        break;
                }

            } catch (IllegalArgumentException | MongoException e) {
                System.out.println(e.getMessage());
                //throw new RuntimeException(e);
            }
        } else {
            request.getRequestDispatcher("error.html").forward(request, response);
        }
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

}
