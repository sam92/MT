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
public interface TestForm extends Test{

    public void searchFormInThesePaths(List<String> pattern);
    public void searchFormInLinkedPagesOfHomepage();
    public WebDriver getWebDriver();
    public void setWebDriver(WebDriver wb);
    public void quitWebDriver();
    public List<String> listCMS();
}

