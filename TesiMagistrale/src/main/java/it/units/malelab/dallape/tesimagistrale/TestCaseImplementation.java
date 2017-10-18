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
import java.util.List;
import org.openqa.selenium.*;

public class TestCaseImplementation implements TestCase {

    public static void main(String[] arg) {
        TestCase first = new TestCaseImplementation("www.trieste6.net");
        first.searchFormInThisPattern(first.listCMS());
        first.searchFormInLinkedPagesOfHomepage();
        ((Site) first.getSite()).setVisitedNow();
        first.quitWebDriver();
    }

    private final Site current;
    private WebDriver wb;
    private List<String> testCMS;

    public TestCaseImplementation(String url) {
        current = new SiteImplementation(url);
        testCMS = new ArrayList<>();        
       wb= (!current.getRealUrl().equalsIgnoreCase("Unreachable")) ? new PhantomDriver(PhantomDriver.capabilities()) : null;
    }

    public TestCaseImplementation(String url, WebDriver webDriv) {
        current = new SiteImplementation(url);
        wb = webDriv;
        testCMS = new ArrayList<>();
    }

    private void searchLogin(WebDriver driver, Site url, boolean cmsTest, List<String> listCMS) {//c'è il problema dei siti che presentano una pagina 404, non so se veramente sono wordpress o no servirebbe wappalyzer
        if (!url.getRealUrl().equalsIgnoreCase("Unreachable")) {
            driver.get(url.getUrl());
            url.setRealUrl(driver.getCurrentUrl());
            String path = url.getRealUrl();
            System.out.println("SearchLogin: " + path);
            if (cmsTest) {
                for (int i = 0; i < listCMS.size(); i++) {
                    listCMS.set(i, path + listCMS.get(i));
                }
            }
            for (String currentString : listCMS) {
                System.out.println(currentString);
                //faccio un ping prima di iniziare tranne per current=""
                if (currentString.isEmpty() || SiteImplementation.isReachable(currentString)) {
                    String[] coppiePageAction = searchForFormInThisPage(driver, currentString);
                    if (!(coppiePageAction[0].isEmpty() && coppiePageAction[1].isEmpty() && coppiePageAction[2].isEmpty())) {
                        System.out.println("Tripla:\n" + coppiePageAction[0] + "\n" + coppiePageAction[1] + "\n" + coppiePageAction[2] + "\n");
                        ((SiteImplementation) url).insertIntoResult(coppiePageAction);
                    }
                }
            }
        }
    }

    private String[] searchForFormInThisPage(WebDriver driver, String url) {//sito della regione non riesce a trovare il form dopo aver seguito il link
        System.out.println("InSearchForForm:" + url);
        String[] actions = existFormPassword(driver, url); //provare a vedere se il problema è dato dall'https
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
        System.out.println("existFormPass:" + url);
        driver.get(url);
        String currentSite = driver.getCurrentUrl();
        System.out.println("Searching for Form with password in: " + currentSite);
        String[] actions = new String[3];
        actions[0] = "";
        actions[1] = "";
        actions[2] = "";
        //boolean exist = false;
        List<WebElement> elements = driver.findElements(By.tagName("form"));
        for (WebElement el : elements) {
            //   .// significa cerca nei figli (di form) che sono input di tipo password
            if (!el.findElements(By.xpath(".//input[@type='password']")).isEmpty()) {
                //exist = true;
                actions[0] = currentSite.trim();
                actions[1] = el.getAttribute("action").trim();
                actions[2] = url.trim();

            }
        }
        //System.out.println(exist ? "\t Found" : "\t Not Found");
        driver.navigate().back();
        return actions;
    }

    private String[] existJSPassword(WebDriver driver, String url) {
        System.out.println("existJSPass:" + url);
        driver.get(url);
        String currentSite = driver.getCurrentUrl();
        String[] actions = new String[3];
        actions[0] = "";
        actions[1] = "";
        actions[2] = "";
        //System.out.println("Searching for JSForm with password in: " + currentSite);
        List<WebElement> password = driver.findElements(By.xpath("//input[@type='password']"));
        //System.out.println(!password.isEmpty() ? "\t Found" : "\t Not Found");
        if (!password.isEmpty()) {
            actions[0] = currentSite.trim(); //sito dove ho trovato il form
            actions[1] = "javascript"; // url action
            actions[2] = url.trim(); //sito da cui provenivo o su cui ho fatto click
        }
        driver.navigate().back();
        //qui viene ritornato l'url del sito da cui si preleva il form in js
        return actions;
    }

    /*
    return the list of path containing a login form in wordpress or joomla
     */
    private List<String> searchAndFollowLink(WebDriver driver, Site url) {
        System.out.println("InSearchAndFollow:" + url.getUrl());
        List<String> linkToLogin = new ArrayList<>();
        driver.get(url.getUrl());
        //System.out.println("SearchAndFollow: " + driver.getCurrentUrl());
        List<WebElement> elements = new ArrayList<>();
        boolean notFinished = true;
        List<String> nameLogin =  defaultWordsLogin();
        //String[] nameLogin = (String[]) defaultWordsLogin().toArray();
        int i = -1;
        while (elements.isEmpty() && notFinished) {
            i++;

            /*elements.addAll(driver.findElements(By.partialLinkText(nameLogin[i])));//non credo che funzioni questo
            elements.addAll(driver.findElements(By.xpath(".//a[contains(text(), '" + nameLogin[i] + "')]")));
            elements.addAll(driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin[i] + "')]")));
             */
            elements = driver.findElements(By.partialLinkText(nameLogin.get(i)));//non credo che funzioni questo
            System.out.println("PartialLink " + nameLogin.get(i) + ":\t" + elements.size());
            System.out.println("A " + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath(".//a[contains(text(), '" + nameLogin.get(i) + "')]")).size());
            System.out.println("Button " + nameLogin.get(i) + ":\t" + driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin.get(i) + "')]")).size());
            if (elements.isEmpty()) {
                elements = driver.findElements(By.xpath(".//a[contains(text(), '" + nameLogin.get(i) + "')]"));
            }
            if (elements.isEmpty()) {
                elements = driver.findElements(By.xpath(".//button[contains(text(), '" + nameLogin.get(i) + "')]"));
            }
            //driver.manage().timeouts().setScriptTimeout(4, TimeUnit.SECONDS);
            for (WebElement el : elements) {
                // se è un bottone clicco, se invece è un link provo a navigarci verso
                if (el.getTagName().equalsIgnoreCase("a")) {
                    linkToLogin.add(el.getAttribute("href"));
                    if (el.isDisplayed() && el.isEnabled()) {
                        el.click();
                    } else {
                        driver.navigate().to(el.getAttribute("href"));
                    }
                    if (!linkToLogin.contains(driver.getCurrentUrl())) {
                        linkToLogin.add(driver.getCurrentUrl());
                    }
                    driver.navigate().back();

                } else {
                    //navigate
                    if (el.getTagName().contains("button")) {
                        //then click
                        if (el.isDisplayed() && el.isEnabled()) {
                            el.click();
                        }
                        if (!linkToLogin.contains(driver.getCurrentUrl())) {
                            linkToLogin.add(driver.getCurrentUrl());
                        }
                        driver.navigate().back();
                    }

                }
            }
            /*
                elements = driver.findElements(By.xpath(".//a[contains(text(), \"Login\")]"));
                System.out.println(elements.size());*/

            if (i == nameLogin.size() - 1) {
                notFinished = false;
            }
        }
        System.out.println("Site to follow:");
        for (String s : linkToLogin) {
            //if (s.endsWith("/")) {
            //s = s.substring(0, s.length() - 1);
            System.out.println(s);
            //}
        }
        return linkToLogin;
    }

    private String whatRuns(String url, WebDriver driver) {
        //https://www.whatruns.com/website/
        String[] a = url.split("/");
        if (a[0].startsWith("http") && !a[1].isEmpty() && a[1].contains(".")) {
            url = a[1];
        } else {
            if (a[1].contains(".")) {
                url = a[1];
            } else {
                url = a[2];
            }
        }
        driver.get("https://www.whatruns.com/website/" + url);
        String result = "";
        String test = driver.findElement(By.xpath("//div[@data-name='CMS']")).findElement(By.className("tech-name")).getText();
        if (!test.isEmpty() && test.length() > 3) {
            result = test;
        }
        /*
        <div class="techs-list" data-name="CMS">
        <div class="tech-name">Wordpress</div>
         */
        driver.navigate().back();
        return result;
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
            for (String s : (List<String>) pattern) {

            }
            searchLogin(wb, current, true, (List<String>) pattern);
        }
    }

    @Override
    public void searchFormInLinkedPagesOfHomepage() {
        if (!current.isUnreachable()) {
            List<String> listaLink = searchAndFollowLink(wb, current);
            searchLogin(wb, current, false, listaLink);
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
        nameLogin.add("Sign in");
        nameLogin.add("Log in");
        nameLogin.add("Login");
        nameLogin.add("Signin");
        nameLogin.add("Accedi");
        nameLogin.add("Entra");
        nameLogin.add("Riservata");
        nameLogin.add("login");
        nameLogin.add("sign");
        nameLogin.add("signin");
        nameLogin.add("admin");
        nameLogin.add("Accesso");
        nameLogin.add("accesso");
        nameLogin.add("accedi");
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
