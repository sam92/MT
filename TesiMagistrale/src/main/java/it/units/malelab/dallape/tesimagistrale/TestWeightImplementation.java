/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.sql.Timestamp;
import org.bson.Document;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author Samuele
 */
public class TestWeightImplementation implements TestWeight {

    private final Site current;
    private WebDriver wb;
    private Timestamp timestamp;
    private String id;
    private final String WEIGHT="WEIGHT";
    private double weight=0.0;
    private double threshold_KB=20.0;
    private String info = "Viene valutato il pego della pagina web (Non è detto che vengano eseguiti gli script js prima della valutazione).\n"
            + "result=1 Good. peso>200KB \n"
            + "        result=0 Unknow Non si sa \n"
            + "        result=-1 Bad. Peso<200KB \n"
            + "        result=-2 Not Applicable. Sito Unreachable\n";
    
    public static void main(String[] arg){
        String url="http://ronancremin.github.io/webpage-size-tests/redirect_step1.html";
        Test t=new TestWeightImplementation(new SiteImplementation(url), "testnew");
        t.start();
        System.out.println(t.getResult() + "\t"+(String)t.getDetails());
    }

    public TestWeightImplementation(Site site, String id) {
        current = site;
        //System.out.println("Url getted by ping: " + current.getRealUrl());

        /*if (wb != null) {
            //wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            //wb.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
        }*/
        timestamp = new Timestamp(System.currentTimeMillis());
        this.id = id;
            wb = (!site.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
            current.setTASKID(id);
    }

    public TestWeightImplementation(Site site, WebDriver webDriv, String id) {
        //current = new SiteImplementation(url);
        current = site;
        wb = webDriv;
        //wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        timestamp = new Timestamp(System.currentTimeMillis());
        this.id = id;
            current.setTASKID(id);
    }

    @Override
    public String getName() {
        return WEIGHT;
    }

    @Override
    public double getResult() {
        double result=0.0;
        /*
        1.0 Good weight>threshold
        0.0 Unknow weight=0.0 (default value)--> something went wrong
        -1.0 Bad weight<threshold
        -2.0 Not Applicable site unreachable
        */
        if(!current.isUnreachable()){
            if(weight>threshold_KB) result=1.0;
            else if(weight!=0.0 && weight<threshold_KB) result=-1.0;
        }
        else{
            result=-2.0;
        }
        System.out.println("RESULT "+getName()+ ": "+result);
        return result;
    }

    @Override
    public Object getDetails() {
        System.out.println("Weight: "+weight+" KB");
        return weight+" KB";
    }

    @Override
    public String getDescription() {
        return info;
    }

    public void setDescription(String info, boolean append) {
        if (append) {
            this.info = this.info + info;
        } else {
            this.info = info;
        }
    }

    public void setWeight(double w) {
        weight = w;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp t) {
        timestamp = t;
    }

    @Override
    public String toJSON() {
        return this.toDocument().toJson();
    }

    @Override
    public Document toDocument() {
        return new Document("name", getName()).append("timestamp", timestamp.toString()).append("task_id", id).append("info", info).append("result", getResult()).append("details", getDetails());
    }

    @Override
    public void setTaskID(String id) {
        this.id = id;
        current.setTASKID(id);
    }

    @Override
    public String getTaskID() {
        return id;
    }

    public static Test fromDocument(Document d, Site s) {
        assert s != null && d.getString("task_id") != null;
        TestWeightImplementation test = new TestWeightImplementation(s, null, d.getString("task_id"));
        if (d.getString("name") != null) {
            s.setRealUrl(d.getString("name"));
        }
        if (d.getString("timestamp") != null) {
            test.setTimestamp(Timestamp.valueOf(d.getString("timestamp")));
        }
        if (d.getString("info") != null) {
            test.setDescription(d.getString("info"), false);
        }
        if (d.get("details") != null) {
            test.setWeight(Double.parseDouble(d.getString("details").split("")[0]));
        }
        return test;
    }

    @Override
    public void start() {
        if(wb==null) wb = (!current.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
        if (!current.isUnreachable()) {
            wb.get(current.getUrl());
            String source = wb.getPageSource();
            double sizeInBytes = source.getBytes().length;
// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            double sizeInKB = sizeInBytes / 1024;
// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            //double sizeInMB = sizeInKB / 1024;
            weight=sizeInKB;
        }
    }

   @Override
    public WebDriver getWebDriver() {
        return wb;
    }

    @Override
    public void setWebDriver(WebDriver wb) {
        this.wb = wb;
    }

    @Override
    public void quitWebDriver() {
        if (wb != null) {
            wb.quit();
        }
    }

    @Override
    public void setThreshold(double t) {
        threshold_KB=t;
    }

}
