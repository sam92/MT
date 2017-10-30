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
import static it.units.malelab.dallape.tesimagistrale.SiteImplementation.sanitization;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.openqa.selenium.*;

public class TestFormImplementation implements TestForm {

    public static void main(String[] arg) {
        List<String> sites = new ArrayList<>();
        //sites.add("www.trieste6.net");
        //sites.add("www.trieste2.it");
        sites.add("www.agi.it");
        sites.add("http://www.regione.fvg.it");
        /*sites.add("www.uslumbria1.gov.it");
        sites.add("www.uslumbria2.it");
        sites.add("www.asl.vt.it");
        sites.add("http://www.aslromag.info/web/");
        sites.add("www.asp.rc.it");
        sites.add("www.aobrotzu.it");
        sites.add("www.aas5.sanita.fvg.it");
        sites.add("www.aspvv.it");
        sites.add("http://xvcomunitamontanalazioegov.it");
        sites.add("http://www.asl.novara.it");
        sites.add("ulss.belluno.it");
        sites.add("www.aslnapoli1centro.it");
        sites.add("www.atssardegna.it");
        sites.add("www.aslteramo.it");
        sites.add("www.comune.bascape.pv.it");
        sites.add("http://www.aulss2.veneto.it");
        sites.add("www.accademialbertina.torino.it");
        sites.add("www.atorifiuticuneo.altervista.org");
        sites.add("www.atocaloreirpino.it");
        sites.add("www.atoambientecl1.it");
        sites.add("www.stabiatourism.it");
        sites.add("www.infoischiaprocida.it");
        sites.add("www.infocampiflegrei.it");
        sites.add("www.vicotourism.it");
        sites.add("www.capritourism.com");
        sites.add("www.pompeiturismo.it");
        sites.add("http://www.ass3.sanita.fvg.it");
        sites.add("www.arlas.campania.it");*/
        List<String> whatTest = new ArrayList<>();
        whatTest.add("wordpress");
        whatTest.add("joomla");
        whatTest.add("plone");
        whatTest.add("typo3");
        whatTest.add("drupal");
        whatTest.add("");
        String task_id = sites.size() + "" + sites.hashCode();
        for (String s : sites) {
            Site site = new SiteImplementation(s);
            TestForm test = new TestFormImplementation(site,task_id);
            if (!site.isUnreachable()) {
                if (whatTest.size() >= 6 && (whatTest.contains("wordpress") || whatTest.contains("joomla") || whatTest.contains("plone") || whatTest.contains("drupal") || whatTest.contains("typo3"))) {
                    test.testAllCMS();
                } else {
                    test.testHomepage();
                    for (String current : whatTest) {
                        switch (current) {
                            case "wordpress":
                                test.testWordpress();
                                break;
                            case "joomla":
                                test.testJoomla();
                                break;
                            case "plone":
                                test.testPlone();
                                break;
                            case "typo3":
                                test.testTypo3();
                                break;
                            case "drupal":
                                test.testDrupal();
                                break;
                            default:
                                break;
                        }
                    }
                }
                System.out.println("Testing for list of CMS");
                test.searchFormInThesePaths(test.listCMS());
                System.out.println("Done");
                System.out.println("Testing for link in homepage");
                test.searchFormInLinkedPagesOfHomepage();
                System.out.println(site.toJSONString());
                System.out.println("Done");
                test.quitWebDriver();

            } else {
                System.out.println("Already exist: " + s);
            }
        }
    }

    private final Site current;
    private WebDriver wb;
    private List<String> testCMS;
    private final Map<String, List<String[]>> existForm; //tengo traccia delle pagine che ho già scansionato cosi se nell'esecuzione devo riscansionarle guardo prima in map
    private final int SOGLIA_GET = 30;
    private final int SOGLIA_FOLLOW = 20;
    private int SOGLIA_IMPLICIT = 1;
    private Timestamp timestamp;
    private String id;
    private List<String[]> result_;
    private String info = "Vengono cercati tutti i form di login nella pagina e nei link della pagina.\n"
                + "result=1 Good. All forms found are in https \n"
                + "        result=0 Unknow (Not Found Form)\n"
                + "        result=-1 Bad. Found Form taken by http or form with http action\n"
                + "        result=-2 Not Applicable. Sito Unreachable\n"
                + "        0<result<1 Not Good at all. Result is the % of form in https. This means that there are forms taken by http (or with http action)\n";;

    public TestFormImplementation(Site site, String id) {
        current = site;
        testCMS = new ArrayList<>();
        existForm = new HashMap<>();
        result_=new ArrayList<>();
        //System.out.println("Url getted by ping: " + current.getRealUrl());
        wb = (!site.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
        /*if (wb != null) {
            //wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            //wb.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
        }*/
        timestamp=new Timestamp(System.currentTimeMillis());
        this.id=id;
        if(site!=null) current.setTASKID(id);
    }

    public TestFormImplementation(Site url, WebDriver webDriv, String id) {
        //current = new SiteImplementation(url);
        current = url;
        wb = webDriv;
        //wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        testCMS = new ArrayList<>();
        existForm = new HashMap<>();
        result_=new ArrayList<>();
        //existJSForm = new HashMap<>();
        timestamp=new Timestamp(System.currentTimeMillis());
        this.id=id;
        if(url!=null) current.setTASKID(id);
    }

    private void searchLogin(WebDriver driver, Site url, boolean cmsTest, List<String> listCMS) {
        if (!url.getRealUrl().equalsIgnoreCase("Unreachable") || driver != null) {
            boolean go = true;
            if (url.getRealUrl().equalsIgnoreCase("") || cmsTest) {
                try {
                    driver.get(url.getUrl());
                    driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET / 2, TimeUnit.SECONDS);
                    url.setRealUrl(driver.getCurrentUrl());
                    String path = url.getRealUrl();
                    System.out.println("Url getted by driver: " + path);
                    if (cmsTest) {
                        for (int i = 0; i < listCMS.size(); i++) {
                            listCMS.set(i, path + listCMS.get(i));
                        }
                    }
                } catch (TimeoutException e) {
                    System.out.println("Timeout reached: " + e.getMessage());
                    try {
                        driver.get(url.getRealUrl());
                        driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                        url.setRealUrl(driver.getCurrentUrl());
                        String path = url.getRealUrl();
                        System.out.println("Url getted by driver: " + path);
                        if (cmsTest) {
                            for (int i = 0; i < listCMS.size(); i++) {
                                listCMS.set(i, path + listCMS.get(i));
                            }
                        }
                    } catch (TimeoutException ex) {
                        System.out.println("Timeout reached twice! FAILURE: " + e.getMessage());
                        go = false;
                    }
                }

            }
            if (go) {
                for (String currentString : listCMS) {
                    System.out.print("Test in this PATH: " + currentString + "\t:");
                    //faccio un ping prima di iniziare tranne per current=""
                    if (!existForm.containsKey(currentString)) {

                        if (SiteImplementation.isReachable(currentString)) {
                            System.out.println("OK");
                            /*List<String[]> listInfoForm = */
                            searchForFormInThisPage(driver, currentString);
                            /*for(String[] currentInfoForm : listInfoForm){
                            if (!(currentInfoForm[0].isEmpty() && currentInfoForm[1].isEmpty() && currentInfoForm[2].isEmpty()) && !((SiteImplementation) url).existIntoResult(currentInfoForm)) {
                                System.out.println("Result:\n" + "Dove ho trovato il form: " + currentInfoForm[0] + "\n" + "Url dell'action del form: " + currentInfoForm[1] + "\n" + "Sito da cui provenivo o click:  " + currentInfoForm[2] + "\n");
                                ((SiteImplementation) url).insertIntoResult(currentInfoForm);
                            }
                            }*/
                        } else {
                            System.out.println("NOT reachable!");
                        }
                    }
                }
            }
        }
    }

    private void searchForFormInThisPage(WebDriver driver, String url) {
        url = SiteImplementation.sanitization(url, true);
        System.out.println("Searching for forms in: " + url);
        List<String[]> actions = new ArrayList<>();
        if (!existForm.containsKey(url)) {
            try {
                driver.get(url);
                driver.manage().timeouts().implicitlyWait(SOGLIA_IMPLICIT * 400, TimeUnit.MILLISECONDS);
                driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                String currentUrl = driver.getCurrentUrl();
                System.out.print("Searching for Form in real url " + currentUrl + " : ");
                List<WebElement> elements = driver.findElements(By.tagName("form"));
                for (WebElement el : elements) { //cerco in tutti i form
                    //   .// significa cerca nei figli (di form) che sono input di tipo password
                    if (!el.findElements(By.xpath(".//input[@type='password']")).isEmpty()) {
                        actions.add(new String[]{currentUrl.trim(), el.getAttribute("action").trim()/*, url.trim()*/});
                    }/* else if (!el.findElements(By.xpath("//input[@type='password']")).isEmpty()) { //non devo cercare in tutta la pagina
                        exist = true;
                        action[0] = currentUrl.trim();
                        action[1] = el.getAttribute("action").trim();
                        action[2] = url.trim();
                        actions.add(action);
                    }*/ else if (!el.findElements(By.xpath(".//input[@type='Password']")).isEmpty()) {
                        actions.add(new String[]{currentUrl.trim(), el.getAttribute("action").trim()/*, url.trim()*/});
                        //currentUrl.trim(); sito dove ho trovato il form
                        //el.getAttribute("action").trim(); url action
                        // url.trim(); sito da cui provenivo o su cui ho fatto click
                    }
                }
                System.out.println(!actions.isEmpty() ? "\t Found" : "\t Not Found");
                if (actions.isEmpty()) {
                    //cerco se ci sono form scritti in javascript. La ricerca non è precisa, cerco in utti gli elementi
                    //che hanno un campo di tipo password. quindi potrei prendere anche gli stessi elementi già presi nei form. Per questo motivo c'è l'if
                    //uno potrebbe domandarsi se con l'if possa mancarne qualcuno
                    System.out.print("Searching for JSForm in real url " + currentUrl + " : ");
                    if (!driver.findElements(By.xpath(".//input[@type='password']")).isEmpty()) {
                        //currentUrl.trim(); sito dove ho trovato il form
                        //"javascript"; url action
                        // url.trim(); sito da cui provenivo o su cui ho fatto click
                        actions.add(new String[]{currentUrl.trim(), "javascript"/*, url.trim()*/});
                    } else if (!driver.findElements(By.xpath("//input[@type='Password']")).isEmpty()) {
                        actions.add(new String[]{currentUrl.trim(), "javascript"/*, url.trim()*/});
                    } else if (!driver.findElements(By.xpath("//*[@type='password']")).isEmpty()) {
                        actions.add(new String[]{currentUrl.trim(), "javascript"/*, url.trim()*/});
                    }
                    System.out.println(!actions.isEmpty() ? "\t Found" : "\t Not Found");
                }
                //in qualunque caso inserisco la lista anche se è vuota
                existForm.put(url, actions);
                if (SOGLIA_IMPLICIT != 1) {
                    SOGLIA_IMPLICIT = 1;
                }
            } catch (TimeoutException e) {
                System.out.println("Timeout reached: " + e.getMessage());
            } catch (org.openqa.selenium.StaleElementReferenceException ex) {
                if (SOGLIA_IMPLICIT < SOGLIA_FOLLOW) {
                    SOGLIA_IMPLICIT = SOGLIA_IMPLICIT * 2;
                    searchForFormInThisPage(driver, url);
                }
                System.out.println(ex.getMessage());
            }
        }
        /*else {
            if (existForm.get(url) != null) {
                actions=existForm.get(url);
                System.out.println("Already exist form mapped from: " + url);
            }

        }
        return actions;*/
    }

    /*
    return the list of path containing a login form in wordpress or joomla
     */
    private List<String> searchAndFollowLink(WebDriver driver, Site url) {
        System.out.println("Search for link to follow in: " + url.getRealUrl());
        List<String> linkToLogin = new ArrayList<>();
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
                List<String> nameLogin = defaultWordsLogin();
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
                            System.out.println("Button matching" + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath("//button[contains(text(), '" + nameLogin.get(i) + "')]")).size());
                            elements = driver.findElements(By.xpath("//button[contains(text(), '" + nameLogin.get(i) + "')]"));
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
                if (linkToLogin.contains(url.getUrl()) && testCMS.contains("")) {
                    linkToLogin.remove(url.getUrl());
                }
                if (linkToLogin.contains(url.getRealUrl()) && testCMS.contains("")) {
                    linkToLogin.remove(url.getRealUrl());
                }
                List<String[]> tmp = new ArrayList<>();
                for (String s : existForm.keySet()) {
                    for (String[] l : existForm.get(s)) {
                        tmp.add(new String[]{l[0], l[1], s});
                    }
                }
                List<String> linkToLog = new ArrayList<>(linkToLogin);
                for (String s : linkToLog) {
                    boolean exist = false;
                    for (String[] duo : tmp) {
                        s = SiteImplementation.sanitization(s, false);
                            if (duo[0].equalsIgnoreCase(s) || duo[1].equalsIgnoreCase(s) || duo[2].equalsIgnoreCase(s)) {
                                exist = true;
                            }
                    }
                    if (exist) {
                        linkToLogin.remove(s);
                    }
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

    /*
    @Override
    public Site getSite() {
        return current;
    }
     */
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
    public void searchFormInThesePaths(List pattern) {
        if (!current.isUnreachable()) {
            if (pattern.isEmpty()) {
                testAllCMS();
                pattern = testCMS;
            }
            searchLogin(wb, current, true, (List<String>) pattern);
        }
    }

    @Override
    public void searchFormInLinkedPagesOfHomepage() {
        if (!current.isUnreachable()) {
            List<String> listaLink = searchAndFollowLink(wb, current);
            if (!listaLink.isEmpty()) {
                searchLogin(wb, current, false, listaLink);
            }
        }
    }

     public void start(){
         searchFormInThesePaths(this.listCMS());
         searchFormInLinkedPagesOfHomepage();
     }
     
    @Override
    public List<String> listCMS() {
        return testCMS;
    }

    public static List<String> defaultWordsLogin() {
        List<String> nameLogin = new ArrayList<>();
        nameLogin.add("Login");
        nameLogin.add("login");
        nameLogin.add("admin");
        nameLogin.add("Accesso");
        nameLogin.add("accesso");
        nameLogin.add("accedi");
        nameLogin.add("Accedi");
        nameLogin.add("Entra");
        nameLogin.add("Sign in");
        nameLogin.add("Log in");
        nameLogin.add("Signin");
        nameLogin.add("riservata");
        nameLogin.add("sign");
        nameLogin.add("signin");
        return nameLogin;
    }

    @Override
    public void testWordpress() {
        if (testCMS != null) {
            if (!testCMS.contains("/wp-login.php")) {
                testCMS.add("/wp-login.php");
            }
            if (!testCMS.contains("/login")) {
                testCMS.add("/login");
            }
        }
    }

    @Override
    public void testJoomla() {
        if (testCMS != null) {
            if (!testCMS.contains("/administrator")) {
                testCMS.add("/administrator");
            }
            if (!testCMS.contains("/login")) {
                testCMS.add("/login");
            }
        }
    }

    @Override
    public void testPlone() {
        if (testCMS != null) {
            if (!testCMS.contains("/login")) {
                testCMS.add("/login");
            }
        }
    }

    @Override
    public void testDrupal() {
        if (testCMS != null) {
            if (!testCMS.contains("/user")) {
                testCMS.add("/user");
            }
        }
    }

    @Override
    public void testTypo3() {
        if (testCMS != null) {
            if (!testCMS.contains("/member")) {
                testCMS.add("/member");
            }
        }
    }

    @Override
    public void testHomepage() {
        if (testCMS != null) {
            if (!testCMS.contains("")) {
                testCMS.add("");
            }
        }
    }

    @Override
    public void testAllCMS() {
        if (testCMS != null) {
            testWordpress();
            testJoomla();
            testPlone();
            testDrupal();
            testTypo3();
            testHomepage();
        }
    }

    @Override
    public double getResult() {
        /*
        rate=1 Good
        rate=0 Unknow (Not Found Form)
        rate=-1 Bad Found http page with form or action in http
        rate=-2 Not Applicable Sito Unreachable
        between 1.0 and 0.0 if not all form find were in https.
         */
        double rate;
        List<String[]> result = (List<String[]>) this.getDetails();
        if (current.isUnreachable()) {
            rate = -2.0;
        } else if (result.isEmpty()) {
            rate = 0.0;
        } else {
            double value = 0.0;
            double total = result.size();
            for (String[] el : result) {
                if (el[0].startsWith("https") && el[1].startsWith("https")) {
                    value = value + 1.0;
                }
            }
            if (value == 0.0) {
                rate = -1; //tutti i form che ho trovato sono in http
            } else {
                rate = value / total;
            }
        }
        System.out.println("RESULT= " + rate);
        return rate;
    }

    @Override
    public Object getDetails() {
        List<String[]> result = new ArrayList<>();
        for (String s : existForm.keySet()) {
            for (String[] l : existForm.get(s)) {
                result.add(new String[]{l[0], l[1], s});
            }
        }
        for (String[] s : result) {
            System.out.println(s[0]);
            System.out.println(s[1]);
            System.out.println(s[2]);
            System.out.println();
        }
        if(result.size()<=result_.size()) result_=result;
        return result;
    }
public void setResult_(List<String[]> lista){
    result_=lista;
}
    @Override
    public String getDescription() {
        return info;
    }

    public void setDescription(String info, boolean append){
        if(append) this.info=this.info+info;
        else this.info=info;
    }
    @Override
    public String getName() {
        return "FORM";
    }
    @Override
    public Timestamp getTimestamp(){
        return timestamp;
    }
public void setTimestamp(Timestamp t){
        timestamp=t;
    }
    @Override
    public String toJSON() {
        return this.toDocument().toJson();
    }

    @Override
    public Document toDocument() {
        List<Document> list = new ArrayList<>();
        for (String[] terna : (List<String[]>)getDetails()) {
            Document w = new Document().append("link_click", terna[2]).append("action", terna[1]).append("location_form", terna[0]);
            list.add(w);
        }
       return new Document("name", getName()).append("timestamp", timestamp.toString()).append("task_id", id).append("info", info).append("result",getResult()).append("details", list);
    }

    @Override
    public void setTaskID(String id) {
        this.id=id;
        current.setTASKID(id);
    }

    @Override
    public String getTaskID() {
        return id;
    }
    public static Test fromDocument(Document d, Site s){
        assert s!=null && d.getString("task_id")!=null;
        TestFormImplementation test = new TestFormImplementation(s,null,d.getString("task_id"));
        if (d.getString("name") != null) {
            s.setRealUrl(d.getString("url_true"));
        }
        if (d.getString("timestamp") != null) {
            test.setTimestamp(Timestamp.valueOf(d.getString("timestamp")));
        }
        if (d.getString("info") != null) {
            test.setDescription(d.getString("info"),false);
        }
        List<String[]> result_=new ArrayList<>();
        if (d.get("details") != null) {
            List<Document> result = (List<Document>) d.get("details");
            for (Document doc : result) {
                result_.add(new String[]{doc.getString("location_form"),doc.getString("action"),doc.getString("link_click")});
            }
            test.setResult_(result_);
        }
        return test;
    }

}
