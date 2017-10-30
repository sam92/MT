/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.sql.Timestamp;
import org.bson.Document;


/**
 *
 * @author Samuele
 */
public interface Test {
    public String getName();
    public double getResult();
    public Object getDetails();
    public String getDescription();
    public Timestamp getTimestamp(); 
    public void setTaskID(String id);
    public String getTaskID();
    public String toJSON();
    public Document toDocument();
}
