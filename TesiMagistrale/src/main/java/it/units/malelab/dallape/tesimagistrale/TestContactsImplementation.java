/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Samuele
 */
public class TestContactsImplementation implements TestContacts {

    /*public List searchForWebmasterContacts() {
        if (!current.isUnreachable()) {
            wb.get(current.getRealUrl());
            //public static final String EMAIL_VERIFICATION = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
            //becca il 99% delle email
            //https://stackoverflow.com/questions/11454798/how-can-i-check-if-some-text-exist-or-not-in-the-page
        }
        return new ArrayList<>();

    }*/
    private String EMAIL_VERIFICATION = "[a-zA-Z0-9]+[._a-zA-Z0-9!#$%&'*+-/=?^_`{|}~]*[a-zA-Z]*@[a-zA-Z0-9-\\-]{2,30}\\.[a-zA-Z.]{2,20}";
    private Site current;
    private WebDriver wb;
    private final int SOGLIA_GET = 30;
    private final int SOGLIA_FOLLOW = 20;
    private List<String> listEmail;
    private int SOGLIA_IMPLICIT = 1;
    private final String CONTACTS="CONTACTS";
    private Timestamp timestamp;
    private String id;
    private String info = "Vengono cercati tutti i form di login nella pagina e nei link della pagina.\n"
            + "result=1 Good. All forms found are in https \n"
            + "        result=0 Unknow (Not Found Form)\n"
            + "        result=-1 Bad. Found Form taken by http or form with http action\n"
            + "        result=-2 Not Applicable. Sito Unreachable\n"
            + "        0<result<1 Not Good at all. Result is the % of form in https. This means that there are forms taken by http (or with http action)\n";

    ;

    public TestContactsImplementation(Site site, String id) {
        current = site;
        listEmail = new ArrayList<>();
        wb = (!site.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
        timestamp = new Timestamp(System.currentTimeMillis());
        this.id = id;
        current.setTASKID(id);
    }

    public TestContactsImplementation(Site site, WebDriver webDriv, String id) {
        current = site;
        wb = webDriv;
        listEmail = new ArrayList<>();
        timestamp = new Timestamp(System.currentTimeMillis());
        this.id = id;
            current.setTASKID(id);
    }
    @Override
    public void start() {
        if(wb==null) wb = (!current.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
        if (!current.getRealUrl().equalsIgnoreCase("Unreachable") || wb != null) {
            List<String> listaLink = searchAndFollowLink(wb, current);
            for (String currentString : listaLink) {
                System.out.print("Search contacts in this PATH: " + currentString + "\t:");
                if (currentString.equalsIgnoreCase(current.getUrl()) || SiteImplementation.isReachable(currentString)) {
                    System.out.println("OK");
                    searchForEmailInThisPage(wb, currentString);

                } else {
                    System.out.println("NOT reachable!");
                }
            }
        }
    }

    private void searchForEmailInThisPage(WebDriver driver, String url) {
        url = SiteImplementation.sanitization(url, true);
        System.out.println("Searching for email in: " + url);
        boolean exist = false;
        try {
            if(!SiteImplementation.sanitization(driver.getCurrentUrl(),false).equals(url)){
                driver.get(url);
                driver.manage().timeouts().implicitlyWait(SOGLIA_IMPLICIT * 400, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                System.out.println("SON QUA");
            }
            String currentUrl = driver.getCurrentUrl();
            System.out.print("Searching for Email in real url " + currentUrl + " : ");
            List<WebElement> elements = driver.findElements(By.tagName("a"));
            Pattern pattern = Pattern.compile(EMAIL_VERIFICATION);
            for (WebElement el : elements) { //cerco in tutti i form
                //   .// significa cerca nei figli (di form) che sono input di tipo password
                String text;
                if (el.isDisplayed()) {
                    text = el.getAttribute("href");
                    if (text.startsWith("mailto")) {
                        Matcher m = pattern.matcher(text); //qua si potrebbe anche fare una substring
                        if(m.matches() && !listEmail.contains(m.group())){
                            listEmail.add(m.group());
                            exist = true;
                        }
                        
                    }/* else {
                        text=el.getAttribute("innerText");
                        Matcher m = pattern.matcher(text);
                        if (m.matches()) {
                            System.out.println(text);
                            if(!listEmail.contains(m.group())) listEmail.add(m.group());
                            exist = true;
                        }
                    }*/
                }
            }
            if (!exist) {
                for (String s : new String[]{"span", "p"}) {
                    elements = driver.findElements(By.tagName(s));
                    for (WebElement el : elements) { //cerco in tutti i div
                        String text;
                        if (el.isDisplayed()) {
                            //text = el.getText();
                            text=el.getAttribute("innerText");
                            Matcher m = pattern.matcher(text);
                            while(m.find()){
                                if(!listEmail.contains(m.group())) listEmail.add(m.group());
                                exist=true;
                            }
                        }
                    }
                }
            }
            System.out.println(exist ? "\t Found" : "\t Not Found");
            //in qualunque caso inserisco la lista anche se è vuota
            if (SOGLIA_IMPLICIT != 1) {
                SOGLIA_IMPLICIT = 1;
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout reached: " + e.getMessage());
        } catch (org.openqa.selenium.StaleElementReferenceException ex) {
            System.out.println("Stale Reference Exception");
            if (SOGLIA_IMPLICIT < SOGLIA_FOLLOW) {
                SOGLIA_IMPLICIT = SOGLIA_IMPLICIT * 10;
                searchForEmailInThisPage(driver, url);
            }
            System.out.println(ex.getMessage());
        }
    }

    /*
    return the list of path containing a login form in wordpress or joomla
     */
    private List<String> searchAndFollowLink(WebDriver driver, Site url) {
        System.out.println("Search for link to follow in: " + url.getRealUrl());
        List<String> linkToLogin = new ArrayList<>();
        linkToLogin.add(url.getUrl());
        if (!url.getRealUrl().equalsIgnoreCase("Unreachable")) {
            try {
                if (url.getRealUrl().trim().isEmpty()) {
                    driver.get(url.getUrl());
                    driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                    url.setRealUrl(driver.getCurrentUrl());
                } else {
                    driver.get(url.getRealUrl());
                    driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                }
                System.out.println("Got: " + driver.getCurrentUrl());
                List<WebElement> elements = new ArrayList<>();
                boolean notFinished = true;
                List<String> nameLogin = defaultWordsEmail();
                int i = -1;
                while (elements.isEmpty() && notFinished) {
                    i++;
                    try {
                        //System.out.println("Searching for partial link...");
                        elements = driver.findElements(By.xpath("//a[contains(@href, '" + nameLogin.get(i) + "')]"));

                        System.out.println("PartialLink matching" + nameLogin.get(i) + ":\t" + elements.size());
                        //System.out.println(driver.getPageSource());
                        if (elements.isEmpty()) {
                            System.out.println("A matching" + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath("//a[contains(text(), '" + nameLogin.get(i) + "')]")).size());
                            elements = driver.findElements(By.xpath("//a[contains(text(), '" + nameLogin.get(i) + "')]"));
                        } else if (elements.isEmpty()) {
                            elements = driver.findElements(By.partialLinkText(nameLogin.get(i)));//non credo che funzioni questo
                        } /*if(elements.isEmpty()){
                elements=driver.findElements(By.xpath("//*[contains(@href, '" + nameLogin.get(i) + "')]"));
            }*/ else if (elements.isEmpty()) {
                            System.out.println("Button matching" + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin.get(i) + "')]")).size());
                            elements = driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin.get(i) + "')]"));
                        }
                        for (WebElement el : elements) {
                            // se è un bottone clicco, se invece è un link provo a navigarci verso
                            if (el.getTagName().equalsIgnoreCase("a")) {
                                String urlHref = SiteImplementation.sanitization(el.getAttribute("href"), false);
                                System.out.println("Find A element with href: " + urlHref);
                                if (urlHref.startsWith("http")) {
                                    if (!linkToLogin.contains(urlHref)) {
                                        linkToLogin.add(urlHref);
                                    }
                                } else {
                                    if (el.isDisplayed() && el.isEnabled()) {
                                        try {
                                            el.click();
                                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                                            String currentUrl = SiteImplementation.sanitization(driver.getCurrentUrl(), false);
                                            if (!linkToLogin.contains(currentUrl)) {
                                                linkToLogin.add(currentUrl);
                                            }
                                            System.out.println("After click on A arrived a:  " + currentUrl);
                                        } catch (TimeoutException e) {
                                            System.out.println("Timeout reached: " + e.getMessage());
                                            try {
                                                driver.get(url.getRealUrl());
                                                driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                                            } catch (TimeoutException exe) {
                                                System.out.println("FAILURE on GET after timeout: " + exe.getMessage());
                                                notFinished = false;
                                            }
                                        }
                                    } else {
                                        try {
                                            urlHref = SiteImplementation.sanitization(urlHref, true);
                                            driver.navigate().to(urlHref);
                                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                                            String currentUrl = SiteImplementation.sanitization(driver.getCurrentUrl(), false);
                                            if (!linkToLogin.contains(currentUrl)) {
                                                linkToLogin.add(currentUrl);
                                            }
                                            System.out.println("After click on A arrived a:  " + currentUrl);
                                        } catch (TimeoutException e) {
                                            System.out.println("Timeout reached: " + e.getMessage());
                                            try {
                                                driver.get(url.getRealUrl());
                                                driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                                            } catch (TimeoutException exe) {
                                                System.out.println("FAILURE on GET after timeout: " + exe.getMessage());
                                                notFinished = false;
                                            }
                                        }
                                    }
                                }
                            } else {
                                //navigate
                                if (el.getTagName().contains("button")) {
                                    //then click
                                    if (el.isDisplayed() && el.isEnabled()) {
                                        try {
                                            el.click();
                                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                                            String currentUrl = SiteImplementation.sanitization(driver.getCurrentUrl(), false);
                                            if (!linkToLogin.contains(currentUrl)) {
                                                linkToLogin.add(currentUrl);
                                                System.out.println("After click on Button arrived a:  " + currentUrl);
                                            }
                                            driver.navigate().back();
                                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                                        } catch (TimeoutException e) {
                                            System.out.println("Timeout reached: " + e.getMessage());
                                            try {
                                                driver.get(url.getRealUrl());
                                                driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                                            } catch (TimeoutException exe) {
                                                System.out.println("FAILURE on GET after timeout: " + exe.getMessage());
                                                notFinished = false;
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    } catch (StaleElementReferenceException stl) {
                        System.out.println(stl.getMessage());
                        try {
                            driver.get(url.getRealUrl());
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                        } catch (TimeoutException exe) {
                            System.out.println("FAILURE on GET after stale error : " + exe.getMessage());
                            notFinished = false;
                        }
                    }
                    if (i == nameLogin.size() - 1) {
                        notFinished = false;
                    }
                }
                System.out.println("Link find in " + url.getUrl() + " to follow:");
                System.out.println("--------------------");

                if (linkToLogin.contains(url.getRealUrl())) {
                    linkToLogin.remove(url.getRealUrl());
                }
                for (String s : linkToLogin) {
                    System.out.println(s);
                }
                System.out.println("--------------------");
            } catch (TimeoutException ex) {
                System.out.println("Timeout reached! " + ex.getMessage());
            }
            System.out.println();
        }
        return linkToLogin;
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

    public static List<String> defaultWordsEmail() {
        List<String> nameLogin = new ArrayList<>();
        nameLogin.add("contatti");
        nameLogin.add("Contatti");
        nameLogin.add("contattaci");
        nameLogin.add("Contattaci");
        nameLogin.add("Contacts");
        nameLogin.add("contacts");
        nameLogin.add("contact");
        nameLogin.add("Contact");
        nameLogin.add("contact us");
        return nameLogin;
    }

    @Override
    public double getResult() {
        /*
        rate=1 Good Found email
        rate=0 Unknow 
        rate=-1 Bad Not Found Email
        rate=-2 Not Applicable Sito Unreachable
         */
        double rate;
        List<String> result = (List<String>) this.getDetails();
        if (current.isUnreachable()) {
            rate = -2.0;
        } else if (result.isEmpty()) {
            rate = -1.0;
        } else {
            boolean adminContact=false;
            List<String> webMasterContacts=new ArrayList<>();
            webMasterContacts.add("admin");
            webMasterContacts.add("webmaster");
            webMasterContacts.add("info");
            webMasterContacts.add("help");  
            for(String s: result){
                for(String contact: webMasterContacts){
                    if(s.contains(contact)){
                        adminContact=true;
                        break;
                    }
                }
                if(adminContact) break;
            }
            if(adminContact){
                    rate=1.0;
                }
            else{
                rate=0.0;
            }
        }
        System.out.println("RESULT "+getName()+ ": "+rate);
        return rate;
    }

    @Override
    public Object getDetails() {
        for(String s : listEmail) System.out.println(s);
        return listEmail;
    }

    public void setListEmail(List<String> lista) {
        listEmail = lista;
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

    @Override
    public String getName() {
        return CONTACTS;
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
        return new Document("name", getName()).append("timestamp", timestamp.toString()).append("task_id", id).append("info", info).append("result", getResult()).append("details", (List<String>)getDetails());
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
        TestContactsImplementation test = new TestContactsImplementation(s, null, d.getString("task_id"));
        if (d.getString("name") != null) {
            s.setRealUrl(d.getString("name"));
        }
        if (d.getString("timestamp") != null) {
            test.setTimestamp(Timestamp.valueOf(d.getString("timestamp")));
        }
        if (d.getString("info") != null) {
            test.setDescription(d.getString("info"), false);
        }
        List<String> result_ = new ArrayList<>();
        if (d.get("details") != null) {
            List<Document> result = (List<Document>) d.get("details");
            for (Document doc : result) {
                result_.add(String.valueOf(doc));
            }
            test.setListEmail(result_);
        }
        return test;
    }

    @Override
    public void setRegex(String regex) throws PatternSyntaxException{
        Pattern.compile(regex);
        EMAIL_VERIFICATION=regex;
    }
}
