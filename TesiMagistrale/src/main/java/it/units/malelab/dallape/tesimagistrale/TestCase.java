/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.List;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author Samuele
 */
public interface TestCase<T> {

    public void searchFormInThisPattern(List<String> pattern);
    public void searchFormInLinkedPagesOfHomepage();
    public void tryLoginSubmit();
    public List<String> searchForWebmasterContacts();
    public T getSite();
    public WebDriver getWebDriver();
    public void setWebDriver(WebDriver wb);
    public void quitWebDriver();
    public void testWordpress();
    public void testJoomla();
    public void testPlone();
    public void testDrupal();
    public void testTypo3();
    public void testHomepage();
    public void testAll();
    public List<String> listCMS();
    
}

