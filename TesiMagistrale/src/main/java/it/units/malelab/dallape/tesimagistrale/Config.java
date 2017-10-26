/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;


import java.util.List;
import java.util.Map;
//import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author Samuele
 */
public class Config implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context=sce.getServletContext();
        Map<String,Thread> map=new ConcurrentHashMap<>();
        context.setAttribute("map", map);
        //scelta di progetto, se viene giù il servlet non tengo le code in pausa


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context=sce.getServletContext();
        //Map<String,Thread> map=(Map<String,Thread>) context.getAttribute("map");
        //Set<String> taskNotFinished=map.keySet();
        context.removeAttribute("map");
        /*
        String TASKID_CONDITIONS = "TASKID_CONDITIONS"; 
        String ACTUAL_STATE = "STATE_LIST_SITES";
        try(database db=new database()){
           //quando viene tirato giù il servlet cancello tutti i job che erano in esecuzione e li inserisco nella tabella che raccoglie i job analizzati nel tempo
           List<String> lista=db.getTasksIDFromMap(TASKID_CONDITIONS);
           db.deleteCollection(TASKID_CONDITIONS);
           for(String s: lista){
               db.insertTaskID(s);
           }
           db.deleteCollection(ACTUAL_STATE);
        }*/
    }

}
