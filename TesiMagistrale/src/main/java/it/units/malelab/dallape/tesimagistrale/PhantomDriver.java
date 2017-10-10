/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.logging.Level;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

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
    public static Capabilities capabilities(){
        Capabilities caps = DesiredCapabilities.phantomjs();//new DesiredCapabilities();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        ((DesiredCapabilities) caps).setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        ((DesiredCapabilities) caps).setJavascriptEnabled(true);
        ((DesiredCapabilities) caps).setCapability("takesScreenshot", false);
        ((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "webSecurityEnabled", false);

        //((DesiredCapabilities) caps).setCapability("phantomjs.page.settings.acceptSslCerts", true);
        //((DesiredCapabilities) caps).setCapability("phantomjs.page.settings.loadImages", false);
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

    
}
