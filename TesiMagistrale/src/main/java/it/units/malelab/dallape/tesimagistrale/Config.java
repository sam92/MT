/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.bson.Document;

/**
 *
 * @author Samuele
 */
public class Config implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context=sce.getServletContext();
        HashMap<Long,Thread> map=new HashMap<>();
        context.setAttribute("map", map);
        String nameCollectionID="currentTASKIDCounter";
        AtomicLong counter = new AtomicLong(0);
        //cerca in db l'id a cui sono arrivato e rimettilo in mappa
        try (database db = new database()) {
            if(!db.collectionExist(nameCollectionID)){
                db.createCollection(nameCollectionID);
            }
            Document a=db.getTheFirstDocumentWithThisKeyValue("ID", null, nameCollectionID);
            if(db.getMongoDB().getCollection(nameCollectionID).count()>0 && a!=null){
                //c'è qualcosa nel db basta prelevare il docs corrispondente
                long id= a.getLong("ID");
                counter.set(id);
                counter.incrementAndGet();
            }
            else{
                //nel db non c'è niente
                long value= counter.getAndIncrement();
                db.getMongoDB().getCollection(nameCollectionID).insertOne(new Document("ID", value));
            }
            
            } catch (Exception e) {
                System.out.println(e.getMessage());
            //throw new RuntimeException(e);
            }
        
        context.setAttribute("counter", counter);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context=sce.getServletContext();
        context.removeAttribute("map");
        String nameCollectionID="currentTASKIDCounter";
        AtomicLong counter= (AtomicLong) context.getAttribute("counter");
        try (database db = new database()) {
            if(!db.collectionExist(nameCollectionID)){
                db.createCollection(nameCollectionID);
            }
            Document a=db.getTheFirstDocumentWithThisKeyValue("ID", null, nameCollectionID);
            if(db.getMongoDB().getCollection(nameCollectionID).count()>0 && a!=null){
                //c'è qualcosa nel db basta prelevare il docs corrispondente
                db.getMongoDB().getCollection(nameCollectionID).updateOne(a, new Document("ID", counter.getAndIncrement()));
            }
            else{
                //nel db non c'è niente
                db.getMongoDB().getCollection(nameCollectionID).insertOne(new Document("ID", counter.getAndIncrement()));
                
            }
            
            } catch (Exception e) {
                System.out.println(e.getMessage());
            //throw new RuntimeException(e);
            }
        //salva in db l'id request a cui sono arrivato
    }

}
