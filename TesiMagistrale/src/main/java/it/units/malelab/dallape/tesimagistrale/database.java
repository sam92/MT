/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

/**
 *
 * @author Samuele
 */
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author Samuele
 */
public class database implements java.lang.AutoCloseable {

    private final String URI_MONGOLAB = "mongodb://shazu:Z6nEiQta8KhqM6!OM1v&76t@ds157233.mlab.com:57233/thesis";
    private MongoDatabase db;
    private MongoClient mC;

    public database() throws IllegalArgumentException {
        MongoClientURI dbURI = new MongoClientURI(URI_MONGOLAB);
        mC = new MongoClient(dbURI);
        db = mC.getDatabase(dbURI.getDatabase());
        System.out.println("Connected to:" + db.getName());
        /*Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        //c = DriverManager.getConnection("jdbc:derby://localhost:1527/sample/APP");
        c = DriverManager.getConnection("jdbc:derby:samples; create=true");
         */
    }

    public boolean collectionExist(String nameCollection) {
        boolean exist = false;
        MongoIterable<String> collectionNames = db.listCollectionNames();
        MongoCursor<String> it = collectionNames.iterator();
        while (it.hasNext()) {
            if (it.next().equalsIgnoreCase(nameCollection)) {
                exist = true;
            }
        }
        return exist;
    }

    public void createCollection(String name) {
        if (!collectionExist(name)) {
            db.createCollection(name);
            System.out.println("Created table: " + name + " in db: " + db.getName());
        }
    }

    public boolean insertSite(Site a, String collection) throws MongoException {
        boolean b = collectionExist(collection);
        if (b) {
            //Document doc = Document.parse(a.toJSONString());
            //o anche a.toDocument()
            db.getCollection(collection).insertOne(a.toDocument());
        }
        else{
            createCollection(collection);
        }
        return b;
    }

    public boolean updateSite(Site a, String collection) throws MongoException {
        boolean b = collectionExist(collection);
        if (b) {
            if(a.getUrl()!=null){//to avoid error
                //db.getCollection(collection).updateOne(new Document("entityId", "12").append("nameIdentity.dob",new Document("$exists",false)), new Document("$push", new Document("nameIdentity", new Document("fName", "1223").append("lName", "2222222") .append("dob", "00").append("address", "789"))));
            Document doc = db.getCollection(collection).findOneAndReplace(new Document("url_site", a.getUrl()), a.toDocument(), new FindOneAndReplaceOptions().upsert(true));
            if (doc == null) {
                b = false;
            }
            }
            else{
                b=false;
            }
            
        }
        else{
            createCollection(collection);
        }
        return b;
    }

    public boolean insertListSites(List<Site> sites, String collection) throws MongoException {

        boolean b = collectionExist(collection);
        if (b) {
            List<Document> l = new ArrayList<>();
            for (Site site : sites) {
                l.add(Document.parse(site.toJSONString()));
            }
            db.getCollection(collection).insertMany(l);
        }
        else{
            createCollection(collection);
        }
        return b;
    }

    public boolean existADocumentWithThisUrlInSITES(String url) {
        return db.getCollection("SITES").find(new Document("url_site", url)).first()!=null;//getTheFirstDocumentWithThisKeyValue("url_site", url, "SITES") != null;
    }

    public boolean existSiteInSTATE(String site) {
        return db.getCollection("STATE_LIST_SITES").find(new Document("site", site)).first()!=null;//getTheFirstDocumentWithThisKeyValue("site", site, "STATE_LIST_SITES") != null;
    }

    //return the first Document with this url or null
    /*public Document getTheFirstDocumentWithThisKeyValue(String key, String value, String collection) {
        MongoCollection<Document> docs = db.getCollection(collection);
        //db.getCollection(collection).find(new Document(key, value)).first();
        FindIterable<Document> lista = null;
        if (value == null) {
            lista = docs.find();
        } else {
            lista = docs.find(new Document(key, value));
        }

        MongoCursor<Document> it = lista.iterator();
        Document a = null;
        if (it.hasNext()) {
            a = it.next();
            if (value == null) {
                if (!a.containsKey(key)) {
                    a = null;
                }
            }

        }
        return a;
    }*/

    public MongoCollection<Document> getDocumentsInThisCollection(String name) throws IllegalArgumentException {
        if(! collectionExist(name)) createCollection(name);
        return db.getCollection(name);
    }

    public MongoDatabase getMongoDB() {
        return db;
    }

    @Override
    public void close() {
        mC.close();
    }

    public long howMuchRemainsInSTATE(String task_id) {
        //si potrebbe guardare nelle conditions al posto di fidarsi di questo
        String collection="STATE_LIST_SITES";
        if(! collectionExist(collection)) createCollection(collection);
        return db.getCollection(collection).count(new Document("task_id", task_id));
    }

    public long howMuchRemainsInCollection(String key, Object value, String collection) {
        if(! collectionExist(collection)) createCollection(collection);
        return db.getCollection(collection).count(new Document(key, value));
    }
    public void insertValueMap(String key, Conditions con, String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Document document = db.getCollection(collection).find(new Document("task_id", key)).first();
        if (document == null) {
            document = new Document("task_id", key).append("value",con.toDocument());
            db.getCollection(collection).insertOne(document);
        }
            
    }
    
    public void updateValueMap(String key, Conditions con, String collection, boolean insertIfNotExixst) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Document document = db.getCollection(collection).find(new Document("task_id", key)).first();
        if (document == null && insertIfNotExixst) {
            document = new Document("task_id", key).append("value",con.toDocument());
            db.getCollection(collection).insertOne(document);
        }
        else{
            //https://stackoverflow.com/questions/29434207/mongodb-update-using-java-3-driver
            //db.getCollection(collection).updateOne(document, new Document("task_id", key).append("value",con.toDocument()));
            db.getCollection(collection).findOneAndReplace(document, new Document("task_id", key).append("value",con.toDocument()));
        }
            
    }
    
    public Conditions getConditionFromMap(String task_id, String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Conditions con = null;
        Document document = db.getCollection(collection).find(new Document("task_id", task_id)).first();
        if (document != null) {
            con=Conditions.fromJSON(((Document) document.get("value")).toJson());
        }
        return con;
    }
    
    public List<String> getTasksIDFromMap(String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        List<String> lista=new ArrayList<>();
        long length = db.getCollection(collection).count();
        List<Document> docs= (List<Document>) db.getCollection(collection).find().sort(new Document("_id",-1));
        for(Document d: docs){
            lista.add(d.getString("task_id"));
        }
        assert(lista.size()==length);
        return lista;
    }

    public Conditions deleteTaskIDFromMap(String task_id, String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Conditions con = null;
        Document document = db.getCollection(collection).find(new Document("task_id", task_id)).first();
        if (document != null) {
            // no test document, let's create one!
            con=Conditions.fromJSON(((Document) document.get("value")).toJson());
            db.getCollection(collection).deleteOne(document);
        }
        return con;
    }
}
