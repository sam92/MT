/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;


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
        Map<String,Conditions> map=new ConcurrentHashMap<>();
        context.setAttribute("map", map);
        //scelta di progetto, se viene giù il servlet non tengo le code in pausa


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context=sce.getServletContext();
        //Map<String,Thread> map=(Map<String,Thread>) context.getAttribute("map");
        //Set<String> taskNotFinished=map.keySet();
        context.removeAttribute("map");
        //salvare la map hash-thread cioè i job che devono essere ancora completati
        
        //salva in db l'id request a cui sono arrivato
    }

}
