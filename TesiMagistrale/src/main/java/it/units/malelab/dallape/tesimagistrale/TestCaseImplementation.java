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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.openqa.selenium.*;

public class TestCaseImplementation implements TestCase {

    public static void main(String[] arg) {
        List<String> sites = new ArrayList<>();
        //sites.add("www.trieste6.net");
        //sites.add("www.trieste2.it");
        sites.add("http://www.regione.fvg.it");
        sites.add("www.uslumbria1.gov.it");
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
        sites.add("www.arlas.campania.it");
        List<String> whatTest = new ArrayList<>();
        whatTest.add("wordpress");
        whatTest.add("joomla");
        whatTest.add("plone");
        whatTest.add("typo3");
        whatTest.add("drupal");
        whatTest.add("");
        String phrase = "";
        for (int i = 0; i < sites.size(); i++) {
            phrase += sites.get(i);
        }
        String task_id = DigestUtils.sha1Hex(phrase);
        for (String s : sites) {
            TestCase test = new TestCaseImplementation(s);
            if (!((SiteImplementation) test.getSite()).isUnreachable()) {
                if (whatTest.size() >= 6 && (whatTest.contains("wordpress") || whatTest.contains("joomla") || whatTest.contains("plone") || whatTest.contains("drupal") || whatTest.contains("typo3"))) {
                    test.testAll();
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
                test.searchFormInThisPattern(test.listCMS());
                Site site = (Site) test.getSite();
                site.setTASKID(task_id);
                site.setVisitedNow();
                System.out.println(site.toJSONString());
                System.out.println("Done");
                System.out.println("Testing for link in homepage");
                test.searchFormInLinkedPagesOfHomepage();
                site = (Site) test.getSite();
                site.setTASKID(task_id);
                site.setVisitedNow();
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
    private Map<String, String[]> existForm; //il top sarebbe usare il db per inserire tutti i dati delle scansioni cosi non serve rifarle
//private Map<String, String[]> existJSForm;
    private final int SOGLIA_GET = 30;
    private final int SOGLIA_FOLLOW = 20;

    public TestCaseImplementation(String url) {
        current = new SiteImplementation(url);
        testCMS = new ArrayList<>();
        existForm = new HashMap<>();
        //existJSForm = new HashMap<>();
        System.out.println("Url getted by ping: " + current.getRealUrl());
        wb = (!current.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
        //if (wb != null) {
        //    wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //}
    }

    public TestCaseImplementation(String url, WebDriver webDriv) {
        current = new SiteImplementation(url);
        wb = webDriv;
        //wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        testCMS = new ArrayList<>();
        existForm = new HashMap<>();
        //existJSForm = new HashMap<>();
    }

    private void searchLogin(WebDriver driver, Site url, boolean cmsTest, List<String> listCMS) {
        if (!url.getRealUrl().equalsIgnoreCase("Unreachable") || driver != null) {
            boolean go=true;
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
                    try{
                    driver.get(url.getRealUrl());
                    driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET * 2, TimeUnit.SECONDS);
                    url.setRealUrl(driver.getCurrentUrl());
                    String path = url.getRealUrl();
                    System.out.println("Url getted by driver: " + path);
                    if (cmsTest) {
                        for (int i = 0; i < listCMS.size(); i++) {
                            listCMS.set(i, path + listCMS.get(i));
                        }
                    }
                    }
                    catch(TimeoutException ex){
                        System.out.println("Timeout reached twice! FAILURE: " + e.getMessage());
                        go=false;
                    }
                }

            }
            if(go){
            for (String currentString : listCMS) {
                System.out.println("Test CMS for: " + currentString);
                //faccio un ping prima di iniziare tranne per current=""
                if (!((SiteImplementation) url).isAlreadyScanned(currentString)) {
                    Boolean start = SiteImplementation.isReachable(currentString);
                    System.out.println("Is this reachable : " + start);
                    if (start/*|| (reachable.containsKey(currentString) ? reachable.get(currentString) : SiteImplementation.isReachable(currentString))*/) {
                        ((SiteImplementation) url).setScanned(currentString);
                        String[] coppiePageAction = searchForFormInThisPage(driver, currentString);
                        if (!(coppiePageAction[0].isEmpty() && coppiePageAction[1].isEmpty() && coppiePageAction[2].isEmpty()) && !((SiteImplementation) url).existIntoResult(coppiePageAction)) {
                            System.out.println("Result:\n" + "Dove ho trovato il form: " + coppiePageAction[0] + "\n" + "Url dell'action del form: " + coppiePageAction[1] + "\n" + "Sito da cui provenivo o click:  " + coppiePageAction[2] + "\n");
                            ((SiteImplementation) url).insertIntoResult(coppiePageAction);
                        }
                    }
                }
            }
        }
        }
    }

    private String[] searchForFormInThisPage(WebDriver driver, String url) {
        url = SiteImplementation.sanitization(url, false);
        System.out.println("Test if exist form in: " + url);
        String[] actions = new String[3];
        actions[0] = "";
        actions[1] = "";
        actions[2] = "";
        if (!existForm.containsKey(url)) {
            try {
                driver.get(url);
                driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                String currentUrl = driver.getCurrentUrl();
                System.out.println("Searching for Form with password in real url: " + currentUrl);
                boolean exist = false;
                List<WebElement> elements = driver.findElements(By.tagName("form"));
                for (WebElement el : elements) {
                    //   .// significa cerca nei figli (di form) che sono input di tipo password
                    if (!el.findElements(By.xpath(".//input[@type='password']")).isEmpty()) {
                        exist = true;
                        actions[0] = currentUrl.trim();
                        actions[1] = el.getAttribute("action").trim();
                        actions[2] = url.trim();
                    }
                }
                System.out.println(exist ? "\t Found Form" : "\t Not Found Form");
                if (exist) {
                    existForm.put(url, actions);
                } else {
                    exist = false;
                    System.out.println("Searching for JSForm in real url: " + currentUrl);
                    List<WebElement> password = driver.findElements(By.xpath("//input[@type='password']"));
                    if (!password.isEmpty()) {
                        exist = true;
                        actions[0] = currentUrl.trim(); //sito dove ho trovato il form
                        actions[1] = "javascript"; // url action
                        actions[2] = url.trim(); //sito da cui provenivo o su cui ho fatto click
                        existForm.put(url, actions);
                    }
                    System.out.println(exist ? "\t Found JSForm" : "\t Not Found JSForm");
                }
            } catch (TimeoutException e) {
                System.out.println("Timeout reached: " + e.getMessage());
                //driver.navigate().back();
            }
        } else {
            if (existForm.get(url) != null) {
                actions = existForm.get(url);
                System.out.println("Already exist in map: " + url);
            }

        }
        return actions;
    }

    /*private String[] searchForFormInThisPage(WebDriver driver, String url) {
        String[] actions = existFormPassword(driver, url);
        if (actions[0].trim().isEmpty()) {
            actions = existJSPassword(driver, url);
            if (actions[0].trim().isEmpty()) {
                actions = new String[3];
                actions[0] = "";
                actions[1] = "";
                actions[2] = "";
            }
        }
        return actions;
    }

    //if there is a login form return an array with the site serving the form and the action of the form
    private String[] existFormPassword(WebDriver driver, String url) {
        System.out.println("Test if exist form in: " + url);
        String[] actions = new String[3];
        actions[0] = "";
        actions[1] = "";
        actions[2] = "";
        if (!existForm.containsKey(url)) {
            driver.get(url);
            //String currentSite = driver.getCurrentUrl();
            System.out.println("Searching for Form with password in real url: " + driver.getCurrentUrl());
            boolean exist = false;
            List<WebElement> elements = driver.findElements(By.tagName("form"));
            for (WebElement el : elements) {
                //   .// significa cerca nei figli (di form) che sono input di tipo password
                if (!el.findElements(By.xpath(".//input[@type='password']")).isEmpty()) {
                    exist = true;
                    actions[0] = driver.getCurrentUrl().trim();
                    actions[1] = el.getAttribute("action").trim();
                    actions[2] = url.trim();
                }
            }
            if (exist) {
                existForm.put(url, actions);
            }
            System.out.println(exist ? "\t Found Form" : "\t Not Found Form");
            //driver.navigate().back();
        } else {
            if (existForm.get(url) != null) {
                actions = existForm.get(url);
            }
            System.out.println("Already exist in map: " + url);
        }
        return actions;
    }

    private String[] existJSPassword(WebDriver driver, String url) {
        System.out.println("Test if exist JSform in: " + url);
        String[] actions = new String[3];
        actions[0] = "";
        actions[1] = "";
        actions[2] = "";
        if (!existJSForm.containsKey(url)) {
            driver.get(url);
            //String currentSite = driver.getCurrentUrl();
            System.out.println("Searching for JSForm in real url: " + driver.getCurrentUrl());
            boolean exist = false;
            List<WebElement> password = driver.findElements(By.xpath("//input[@type='password']"));
            //System.out.println(!password.isEmpty() ? "\t Found" : "\t Not Found");
            if (!password.isEmpty()) {
                exist = true;
                actions[0] = driver.getCurrentUrl().trim(); //sito dove ho trovato il form
                actions[1] = "javascript"; // url action
                actions[2] = url.trim(); //sito da cui provenivo o su cui ho fatto click
                existJSForm.put(url, actions);
            }
            System.out.println(exist ? "\t Found JSForm" : "\t Not Found JSForm");
        } else {
            if (existJSForm.get(url) != null) {
                actions = existJSForm.get(url);
            }
            System.out.println("Already exist in map: " + url);
        }
        return actions;
    }
     */
 /*
    return the list of path containing a login form in wordpress or joomla
     */
    private List<String> searchAndFollowLink(WebDriver driver, Site url) {
        System.out.println("Search for link to follow in: " + url.getRealUrl());
        List<String> linkToLogin = new ArrayList<>();
        try{
        
        driver.get(url.getRealUrl());
        driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
        System.out.println("Get: " + driver.getCurrentUrl());
        List<WebElement> elements = new ArrayList<>();
        boolean notFinished = true;
        List<String> nameLogin = defaultWordsLogin();
        int i = -1;
        while (elements.isEmpty() && notFinished) {
            i++;
            try{
            /*elements.addAll(driver.findElements(By.partialLinkText(nameLogin[i])));//non credo che funzioni questo
            elements.addAll(driver.findElements(By.xpath(".//a[contains(text(), '" + nameLogin[i] + "')]")));
            elements.addAll(driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin[i] + "')]")));
             */
            //System.out.println("Searching for partial link...");
            elements = driver.findElements(By.partialLinkText(nameLogin.get(i)));//non credo che funzioni questo
            //System.out.println("PartialLink matching" + nameLogin.get(i) + ":\t" + elements.size());
            if (elements.isEmpty()) {
                /*System.out.println("Searching for partial link failed");
                System.out.println("Searching for A element...");
                System.out.println("A matching" + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath(".//a[contains(text(), '" + nameLogin.get(i) + "')]")).size());*/
                elements = driver.findElements(By.xpath(".//a[contains(text(), '" + nameLogin.get(i) + "')]"));
            }
            if (elements.isEmpty()) {
                /*System.out.println("Searching for A element failed");
                System.out.println("Searching for Button element...");
                System.out.println("Button matching" + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin.get(i) + "')]")).size());*/
                elements = driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin.get(i) + "')]"));
            }
            for (WebElement el : elements) {
                // se è un bottone clicco, se invece è un link provo a navigarci verso
                if (el.getTagName().equalsIgnoreCase("a")) {
                    String urlHref = el.getAttribute("href");
                    System.out.println("Find A element with href: " + urlHref);
                    if(urlHref.startsWith("http")){
                        if (!linkToLogin.contains(urlHref)) {
                                linkToLogin.add(SiteImplementation.sanitization(urlHref,false));
                            }
                    }
                    else{
                    if (el.isDisplayed() && el.isEnabled()) {
                        try {
                            el.click();
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                            String currentUrl = driver.getCurrentUrl();
                            if (!linkToLogin.contains(currentUrl)) {
                                linkToLogin.add(SiteImplementation.sanitization(currentUrl,false));
                            }
                            System.out.println("After click on A arrived a:  " + currentUrl);
                        } catch (TimeoutException e) {
                            System.out.println("Timeout reached: " + e.getMessage());
                            try{
                            driver.get(url.getRealUrl());
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                            }
                            catch(TimeoutException exe){
                                System.out.println("FAILURE on GET after timeout: "+exe.getMessage());
                                notFinished=false;
                            }
                        }
                    } else {
                        try {
                            if (!urlHref.startsWith("http")) {
                                urlHref = "http://" + urlHref;
                            }
                            driver.navigate().to(urlHref);
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                            String currentUrl = driver.getCurrentUrl();
                            if (!linkToLogin.contains(currentUrl)) {
                                linkToLogin.add(SiteImplementation.sanitization(currentUrl,false));
                            }
                            System.out.println("After click on A arrived a:  " + currentUrl);
                        } catch (TimeoutException e) {
                            System.out.println("Timeout reached: " + e.getMessage());
                            try{
                            driver.get(url.getRealUrl());
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                            }
                            catch(TimeoutException exe){
                                System.out.println("FAILURE on GET after timeout: "+exe.getMessage());
                                notFinished=false;
                            }
                        }
                    }
                    }
                    /*String currentUrl = driver.getCurrentUrl();
                    if (!linkToLogin.contains(currentUrl)) {
                        linkToLogin.add(currentUrl);
                    }
                    System.out.println("After click on A arrived a:  " + currentUrl);
                    driver.navigate().back();
                    driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);*/
                    //((PhantomDriver) driver).waitUntilLoad(2);
                } else {
                    //navigate
                    if (el.getTagName().contains("button")) {
                        //then click
                        if (el.isDisplayed() && el.isEnabled()) {
                            try {
                                el.click();
                                driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                                String currentUrl = driver.getCurrentUrl();
                                if (!linkToLogin.contains(currentUrl)) {
                                    linkToLogin.add(SiteImplementation.sanitization(currentUrl,false));
                                    System.out.println("After click on Button arrived a:  " + currentUrl);
                                }
                                driver.navigate().back();
                                driver.manage().timeouts().pageLoadTimeout(SOGLIA_FOLLOW, TimeUnit.SECONDS);
                            } catch (TimeoutException e) {
                                System.out.println("Timeout reached: " + e.getMessage());
                                try{
                            driver.get(url.getRealUrl());
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                            }
                            catch(TimeoutException exe){
                                System.out.println("FAILURE on GET after timeout: "+exe.getMessage());
                                notFinished=false;
                            }
                            }

                        }
                    }
                }
            }
            }
            catch(StaleElementReferenceException stl){
                System.out.println(stl.getMessage());
                try{
                            driver.get(url.getRealUrl());
                            driver.manage().timeouts().pageLoadTimeout(SOGLIA_GET, TimeUnit.SECONDS);
                            }
                            catch(TimeoutException exe){
                                System.out.println("FAILURE on GET after stale error : "+exe.getMessage());
                                notFinished=false;
                            }
            }
            if (i == nameLogin.size() - 1) {
                notFinished = false;
            }
        }
        System.out.println("Link find in " + url.getUrl() + " to follow:");
        System.out.println("--------------------");
        if (linkToLogin.contains(url.getUrl())) {
            linkToLogin.remove(url.getUrl());
        }
        if (linkToLogin.contains(url.getRealUrl())) {
            linkToLogin.remove(url.getRealUrl());
        }
        List<String> linkToLog = new ArrayList<>(linkToLogin);
        for (String s : linkToLog) {
            if (((SiteImplementation) url).alreadyFindInUrlOrFormLocation(s)) {
                linkToLogin.remove(s);
            }
        }
        for (String s : linkToLogin) {
            System.out.println(s);
        }
        System.out.println("--------------------");
    }
    catch(TimeoutException ex){
        System.out.println("Timeout reached! "+ex.getMessage());
}
        //System.out.println("Refreshing to: " + driver.getCurrentUrl());
        System.out.println();
        return linkToLogin;
    }

    @Override
    public Site getSite() {
        return current;
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
    public void searchFormInThisPattern(List pattern) {
        //provare a fare il ping prima
        if (!current.isUnreachable()) {
            if (pattern.isEmpty()) {
                testAll();
                pattern = testCMS;
            }
            /*for (String s : (List<String>) pattern) {

            }*/
            searchLogin(wb, current, true, (List<String>) pattern);
        }
    }

    @Override
    public void searchFormInLinkedPagesOfHomepage() {
        if (!current.isUnreachable()) {
            System.out.println("START of searching in LINKED page:");
            List<String> listaLink = searchAndFollowLink(wb, current);
            if (!listaLink.isEmpty()) {
                //wb = new PhantomDriver(PhantomDriver.capabilities());
                //wb.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                searchLogin(wb, current, false, listaLink);
            }
        }
    }

    @Override
    public void tryLoginSubmit() {
        if (!current.isUnreachable()) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public List searchForWebmasterContacts() {
        if (!current.isUnreachable()) {
            wb.get(current.getRealUrl());
            //public static final String EMAIL_VERIFICATION = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
            //becca il 99% delle email
            //https://stackoverflow.com/questions/11454798/how-can-i-check-if-some-text-exist-or-not-in-the-page
        }
        return new ArrayList<>();

    }

    /*
    public static List<String> defaultPattern() {
        List<String> listCMS = new ArrayList<>();
        listCMS.add(""); //in homepage
        listCMS.add("/wp-login.php"); //wordpress
        listCMS.add("/administrator"); //joomla
        listCMS.add("/login"); //joomla Plone
        listCMS.add("/user"); //drupal
        listCMS.add("/member"); //Typo3
        return listCMS;
    }
     */
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
        nameLogin.add("Area Riservata");
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
    public void testAll() {
        if (testCMS != null) {
            testWordpress();
            testJoomla();
            testPlone();
            testDrupal();
            testTypo3();
            testHomepage();
        }
    }
}
