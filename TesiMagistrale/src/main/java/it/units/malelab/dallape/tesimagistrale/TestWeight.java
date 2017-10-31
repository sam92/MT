/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import org.openqa.selenium.WebDriver;

/**
 *
 * @author Samuele
 */
public interface TestWeight extends Test{
    public WebDriver getWebDriver();
    public void setWebDriver(WebDriver wb);
    public void quitWebDriver();
    public void setThreshold(double t);
}
