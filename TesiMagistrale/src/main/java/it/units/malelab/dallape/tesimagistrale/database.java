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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.InsertOneOptions;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author Samuele
 */
public class database implements java.lang.AutoCloseable {

    private final String URI_MONGOLAB = "mongodb://shazu:Z6nEiQta8KhqM6!OM1v&76t@ds231315.mlab.com:31315/thesis";
    private MongoDatabase db;
    private MongoClient mC;
    private final String COLLECTION_SITES = "SITES";
    private final String ACTUAL_STATE = "STATE_LIST_SITES";
    private final String TASKID_CONDITIONS = "TASKID_CONDITIONS"; //mappa hash-conditions
    private final String TASKID = "TASKID";

    public database() throws IllegalArgumentException {
        MongoClientURI dbURI = new MongoClientURI(URI_MONGOLAB);
        mC = new MongoClient(dbURI);
        db = mC.getDatabase(dbURI.getDatabase());
        System.out.println("Connected to:" + db.getName());
    }

    public boolean collectionExist(String nameCollection) {
        boolean exist = false;
        List<String> lista = db.listCollectionNames().into(new ArrayList<String>());
        for (String s : lista) {
            if (s.equalsIgnoreCase(nameCollection)) {
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
        } else {
            createCollection(collection);
        }
        return b;
    }

    public boolean updateSitesCollection(Site a) throws MongoException {
        boolean b = true;
        if (a.getUrl() != null) {//to avoid error
            //db.getCollection(collection).updateOne(new Document("entityId", "12").append("nameIdentity.dob",new Document("$exists",false)), new Document("$push", new Document("nameIdentity", new Document("fName", "1223").append("lName", "2222222") .append("dob", "00").append("address", "789"))));
            Document doc = db.getCollection(COLLECTION_SITES).findOneAndReplace(new Document("url_site", a.getUrl()), a.toDocument(), new FindOneAndReplaceOptions().upsert(true));
            /*if (doc == null) {
                b = false;
            }*/ //se non c'è lo inserisco e quindi doc==null potrebbe essere che viene inserito lo stesso
        } else {
            b = false;
        }
        return b;
    }

    public void insertTaskID(String task_id) {
        Document task = new Document("task_id", task_id);
        db.getCollection(TASKID).findOneAndReplace(task, task, new FindOneAndReplaceOptions().upsert(true));
    }

    public List<String> getTaskIDs() {
        List<Document> lista = db.getCollection(TASKID).find().into(new ArrayList<Document>());

        List<String> list = new ArrayList<>();
        for (Document d : lista) {
            String task = d.getString("task_id");
            if (getDocuments(task).size() > 0) {
                list.add(task);
            }
        }
        return list;
    }

    public boolean existInSitesCollections(String url) {
        return db.getCollection(COLLECTION_SITES).find(new Document("url_site", url.trim())).first() != null;//getTheFirstDocumentWithThisKeyValue("url_site", url, "SITES") != null;
    }

    public boolean existInSTATE(String site) {
        return db.getCollection(ACTUAL_STATE).find(new Document("site", site)).first() != null;//getTheFirstDocumentWithThisKeyValue("site", site, "STATE_LIST_SITES") != null;
    }

    public Site getFromCollectionsSites(String name) {
        return SiteImplementation.fromDocument(db.getCollection(COLLECTION_SITES).find(new Document("url_site", name)).first());
    }

    public List<Site> getDocuments(String task_id) {
        List<Document> lista;
        if (task_id.isEmpty()) {
            lista = db.getCollection(COLLECTION_SITES).find().into(new ArrayList<Document>());
        } else {
            lista = db.getCollection(COLLECTION_SITES).find(new Document("task_id", task_id)).into(new ArrayList<Document>());
        }
        List<Site> list = new ArrayList<>();
        for (Document d : lista) {
            list.add(SiteImplementation.fromDocument(d));
        }
        return list;
    }

    public MongoCollection<Document> getDocumentsInThisCollection(String name) throws IllegalArgumentException {
        //if(! collectionExist(name)) createCollection(name);
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
        if (!collectionExist(ACTUAL_STATE)) {
            createCollection(ACTUAL_STATE);
        }
        return db.getCollection(ACTUAL_STATE).count(new Document("task_id", task_id));
    }

    public long howMuchRemainsInCollection(String key, Object value, String collection) {
        if (!collectionExist(collection)) {
            createCollection(collection);
        }
        return db.getCollection(collection).count(new Document(key, value));
    }

    public void insertValueMap(String key, Conditions con, String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Document document = db.getCollection(collection).find(new Document("task_id", key)).first();
        if (document == null) {
            document = new Document("task_id", key).append("value", con.toDocument());
            db.getCollection(collection).insertOne(document);
        }

    }

    public void updateValueMap(String key, Conditions con, String collection, boolean insertIfNotExixst) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Document document = db.getCollection(collection).find(new Document("task_id", key)).first();
        if (document == null && insertIfNotExixst) {
            document = new Document("task_id", key).append("value", con.toDocument());
            db.getCollection(collection).insertOne(document);
        } else {
            //https://stackoverflow.com/questions/29434207/mongodb-update-using-java-3-driver
            //db.getCollection(collection).updateOne(document, new Document("task_id", key).append("value",con.toDocument()));
            db.getCollection(collection).findOneAndReplace(document, new Document("task_id", key).append("value", con.toDocument()));
        }

    }

    public Conditions getConditionFromMap(String task_id, String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        Conditions con = null;
        Document document = db.getCollection(collection).find(new Document("task_id", task_id)).first();
        if (document != null) {
            con = Conditions.fromJSON(((Document) document.get("value")).toJson());
        }
        return con;
    }

    public List<String> getTasksIDFromMap(String collection) {
        if (!this.collectionExist(collection)) {
            db.createCollection(collection);
        }
        List<String> lista = new ArrayList<>();
        long length = db.getCollection(collection).count();
        List<Document> docs = (List<Document>) db.getCollection(collection).find().sort(new Document("_id", -1));
        for (Document d : docs) {
            lista.add(d.getString("task_id"));
        }
        assert (lista.size() == length);
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
            con = Conditions.fromJSON(((Document) document.get("value")).toJson());
            db.getCollection(collection).deleteOne(document);
        }
        return con;
    }
}
