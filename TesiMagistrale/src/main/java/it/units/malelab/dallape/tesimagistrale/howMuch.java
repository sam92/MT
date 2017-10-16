/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import com.mongodb.MongoException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author Samuele
 */
public class howMuch extends HttpServlet {

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
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        String task_id = request.getParameter("hash");
        ServletContext context = getServletContext();
        Map<String, Conditions> mapTask = (Map<String, Conditions>) context.getAttribute("map");
        Conditions c = mapTask.get(task_id);
        if (c != null) {
            int total = c.getSites().size();
            int alreadyscan = c.getNrAlreadyScan();
            while (total != alreadyscan) {
                if (alreadyscan != c.getNrAlreadyScan()) {
                    alreadyscan = c.getNrAlreadyScan();
                    StringBuilder data = new StringBuilder(128);
                    data.append("{\"current\":").append(c.getCurrent()).append(",").append("\"current_nr\":").append(alreadyscan).append(",").append("\"total\":").append(total).append("}\n\n");
                    System.out.println("DATA: " + data);
                    PrintWriter writer = response.getWriter();
                    // write the event type (make sure to include the double newline)
                    writer.write("event: " + "status" + "\n\n");
                    // write the actual data
                    // this could be simple text or could be JSON-encoded text that the
                    // client then decodes
                    writer.write("data: " + data.toString() + "\n\n");
                    // flush the buffers to make sure the container sends the bytes
                    writer.flush();
                    response.flushBuffer();
                    //writeEvent(response, "status", data.toString());
                }
            }
            PrintWriter writer = response.getWriter();
                    writer.write("event: " + "status" + "\n\n");
                    writer.write("data: " + "{\"complete\":true}" + "\n\n");
                    writer.flush();
                    response.flushBuffer();
            //writeEvent(response, "status", "{\"complete\":true}");
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

    /*protected void writeEvent(HttpServletResponse resp,
            String event, String message)
            throws IOException {

        // get the writer to send text responses
        PrintWriter writer = resp.getWriter();

        // write the event type (make sure to include the double newline)
        writer.write("event: " + event + "\n\n");

        // write the actual data
        // this could be simple text or could be JSON-encoded text that the
        // client then decodes
        writer.write("data: " + message + "\n\n");

        // flush the buffers to make sure the container sends the bytes
        writer.flush();
        resp.flushBuffer();
    }*/

}
