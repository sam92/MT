/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Samuele
 */
public class getResult extends HttpServlet {

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
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter(); database db = new database()) {
            String task_id = request.getParameter("task_id");

            if (task_id == null || task_id.trim().isEmpty()) {
                //restituisco tutto il db come json
                task_id = "";
                //se task_id è presente restituisco solo le cose con questo task_id
            }
            List<Site> lista = db.getDocuments(task_id);
            List<Site> newLista = new ArrayList<>();
            if (task_id.isEmpty()) {
                List<String> test = new ArrayList<>();
                test.add("WEIGHT");
                test.add("FORM");
                test.add("CONTACTS");
                for (Site s : lista) {
                    newLista.add(((SiteImplementation) s).getSiteFiltered(test));
                }
                lista = newLista;
            }
            else{
                for (Site s : lista) {
                    newLista.add(((SiteImplementation) s).getSiteFiltered(task_id));
                }
                lista = newLista;
            }
            
            out.println(")]}',"); //for security reason, angularjs ignore that
            out.println("[");
            for (int i = 0; i < lista.size(); i++) {
                out.print(lista.get(i).toJSONString());
                if (i != lista.size() - 1) {
                    out.print(",");
                }
            }
            out.println("]");
            out.flush();
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
