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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import org.bson.Document;

public interface Site {

    public void setRealUrl(String url_visited);
/*
    public void setVisitedNow();
    public void setVisitedWhen(boolean value, Timestamp now);

    public void setVisited(boolean value);

    public boolean isVisited();
*/
    public boolean isUnreachable();

    public String getUrl();
    public void setTASKID(String task_id);
    public String getTASKID();
    public void insertTest(Test t);
    public List<Test> getListTests();
    public String getRealUrl();
    public String toJSONString();
    public Document toDocument();
}
