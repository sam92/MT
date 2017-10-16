/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.dallape.tesimagistrale;

import java.util.List;

/**
 *
 * @author Samuele
 */
public class Conditions {
    private List<String> sites;
    private List<String> test;
    private boolean reanalyze;
    private String taskID;
    private String NAME_COLLECTION;
    private String NAME_STATE;
    private Thread t;
    public Conditions(){}
    public Conditions(List<String> sites,List<String> test, boolean reanalyze,String taskID,String NAME_COLLECTION,String NAME_STATE, Thread t){
        this.NAME_COLLECTION=NAME_COLLECTION;
        this.NAME_STATE=NAME_STATE;
        this.reanalyze=reanalyze;
        this.sites=sites;
        this.taskID=taskID;
        this.test=test;
        this.t=t;
    }
    public Conditions(List<String> sites,List<String> test, boolean reanalyze,String taskID,String NAME_COLLECTION,String NAME_STATE){
        this.NAME_COLLECTION=NAME_COLLECTION;
        this.NAME_STATE=NAME_STATE;
        this.reanalyze=reanalyze;
        this.sites=sites;
        this.taskID=taskID;
        this.test=test;
    }
    public void setSites(List<String> sites){
        this.sites=sites;
    }
        public void setTests(List<String> test){
        this.test=test;
    }
    public void setReanalyze(boolean reanalyze){
        this.reanalyze=reanalyze;
    }
    public void setTaskID(String taskID){
        this.taskID=taskID;
    }
    public void setNameCollection(String nameCollection){
        this.NAME_COLLECTION=nameCollection;
    }
    public void setNameState(String nameState){
        this.NAME_STATE=nameState;
    }
    public List<String> getSites(){
        return sites;
    }
        public List<String> getTests(){
         return test;
    }
    public boolean getReanalyze(){
        return reanalyze;
    }
    public String getTaskID(){
        return taskID;
    }
    public String getNameCollection(){
        return NAME_COLLECTION;
    }
    public String getNameState(){
        return NAME_STATE;
    }
    public Thread getThread(){
        return t.isAlive() ? t : null;
    }
    public void setThread(Thread t){
        this.t=t;
    }
    
}
