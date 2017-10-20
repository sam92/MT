/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Samuele
 */
public class PhantomDriver extends PhantomJSDriver{
    
    public PhantomDriver(){
        super();
    }
    public PhantomDriver(Capabilities cap){
        super(cap);
    }
    public static void main(String[] arg){
        PhantomDriver driver=new PhantomDriver(capabilities());
       //driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
       try{
        driver.get("https://www.trieste6.net/wordpres");
        //List<String> b=driver.searchAndFollowLink(driver, "https://www.trieste6.net/wordpres");
        //p.waitUntilLoad(40);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        String a=driver.getCurrentUrl();
        System.out.println(a);
        driver.get("https://www.trieste6.net/wordpres/user");
        driver.manage().timeouts().pageLoadTimeout(2, TimeUnit.MINUTES);
        //System.out.println(driver.executePhantomJS("window.location.href"));
        //p.waitUntilLoad(40);
        //driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        //try execute document.location.href
        a=driver.getCurrentUrl();
        System.out.println(a);
        driver.manage().deleteAllCookies();
                driver.get("https://www.trieste6.net/wordpres/user");
                driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        //p.waitUntilLoad(40);
        //driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        a=driver.getCurrentUrl();
        System.out.println(a);
        
                driver.get("https://www.trieste6.net/wordpres/administrator");
                driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        //p.waitUntilLoad(40);
        //driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        a=driver.getCurrentUrl();
        System.out.println(a);
        
                driver.get("https://www.trieste6.net/wordpres/member");
                driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        //p.waitUntilLoad(40);
        //driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        a=driver.getCurrentUrl();
        System.out.println(a);
       }
       catch(TimeoutException e){
           System.out.println("Timeout reached: "+e.getMessage());
                                driver.get("https://www.trieste6.net/wordpres/member");
                                driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
       }
        driver.quit();
    }
    public static Capabilities capabilities(){
        Capabilities caps = DesiredCapabilities.phantomjs();//new DesiredCapabilities();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        ((DesiredCapabilities) caps).setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        ((DesiredCapabilities) caps).setJavascriptEnabled(true);
        ((DesiredCapabilities) caps).setCapability("takesScreenshot", false);
        ((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "webSecurityEnabled", false);
        ((DesiredCapabilities) caps).setAcceptInsecureCerts(true);
        ((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "C://Users//Samuele//Desktop//phantomjs-2.1.1-windows//phantomjs-2.1.1-windows//bin//phantomjs.exe");
        ((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{"--web-security=false", "--ssl-protocol=any", "--ignore-ssl-errors=true", "--load-images=false"});
        ((DesiredCapabilities) caps).setCapability("acceptSslCerts", true);
        ((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", false);
        ((DesiredCapabilities) caps).setAcceptInsecureCerts(true);
        return caps;
    }

    public WebDriver resetWebDriver(){
        this.quit();
        return new PhantomDriver(capabilities());
        
    }
    public void waitUntilLoad(long nr){
        WebDriverWait wait = new WebDriverWait(this, nr);
    wait.until(new ExpectedCondition<Boolean>() {
        @Override
        public Boolean apply(WebDriver wdriver) {
            return ((JavascriptExecutor) wdriver).executeScript(
                "return document.readyState"
            ).equals("complete");
        }
    });
    }
   /* 
    WebDriver driver_;

public void waitForPageLoad() {

    Wait<WebDriver> wait = new WebDriverWait(driver_, 30);
    wait.until(new Function<WebDriver, Boolean>() {
        @Override
        public Boolean apply(WebDriver driver) {
            System.out.println("Current Window State       : "
                + String.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState")));
            return String
                .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                .equals("complete");
        }
    });
}*/
        /*
    public void waitForPageLoaded(WebDriver driver)
{
    ExpectedCondition<Boolean> expectation = new
ExpectedCondition<Boolean>() 
    {
        public Boolean apply(WebDriver driver)
        {
            return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
        }
    };
    Wait<WebDriver> wait = new WebDriverWait(driver,30);
    try
    {
        wait.until(expectation);
    }
    catch(Throwable error)
    {
        assertFalse("Timeout waiting for Page Load Request to complete.",true);
    }
}
    */
}
