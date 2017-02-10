package de.createplus.vertretungsplan.substitutionplan;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Max Nuglisch on 02.02.2017.
 */
public class SPlevel {
    public String title;
    public List<String> listings = new LinkedList<String>();

    public SPlevel(String title){this.title = title;}

    public void print(){
        System.out.println("-----------------------------------------------");
        System.out.println(title);
        for(int i = 0; i < listings.size(); i++){
            System.out.println(listings.get(i));
        }
    }

    public String getText(){
        String tmp = "\n-----------------------------------------------\n"+title;
        for(int i = 0; i < listings.size(); i++){
            tmp= tmp + "\n" + listings.get(i);
        }
        return tmp;
    }
}
