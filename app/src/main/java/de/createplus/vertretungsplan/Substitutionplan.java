package de.createplus.vertretungsplan;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Max Nuglisch on 02.02.2017.
 */
public class Substitutionplan {
    String urlArchive = "http://gymnasium-wuerselen.de/untis/";
    String url;
    String User;
    String password;
    String username;
    List<SPlevel> SP;

    int day; // 1 = Today    2 = Tomorrow
    String info; //Date of the Plan

    public Substitutionplan(int index, int day, String User, String username, String password){
        url = urlArchive + User + "/f" + day + "/" + "subst_" + IntToFixString(index, 3) + ".htm";
        this.username = username;
        this.password = password;
        this.User = User;
        this.day = day;
    }

    public void update() throws IOException {
        //System.out.println("URL: " + url);
        //System.out.println("Downloading page...");
        String base64login = new String(Base64.encodeBase64((username + ":" + password).getBytes())); // creating an encoded login
        Document doc = Jsoup.connect(url).header("Authorization", "Basic " + base64login).get(); //loading page.

        String htmlraw = doc.html(); // get raw html
        String[] htmlsplit = htmlraw.split("\\n"); // split raw html -> lines of the file in an array
        info = htmlsplit[88].replace("<div class=\"mon_title\">","").replace("</div>",""); // get level information


        //System.out.println("Extracting Timetable information...");


        org.jsoup.select.Elements rows = doc.select("tr");
        List<String> planList= new LinkedList<String>();

        for(org.jsoup.nodes.Element row :rows)
        {
            String tmp = "";
            org.jsoup.select.Elements columns = row.select("td");
            for (org.jsoup.nodes.Element column:columns)
            {
                tmp = tmp+" "+column.text();
            }
            planList.add(tmp);
        }

        SP = new LinkedList<SPlevel>();

        for(int i = 0; i < planList.size(); i++){
            String tmp = planList.get(i);
            if(Pattern.matches( " [^ ]+ [^ -]+-[^ -]+-[^ -]+", tmp)||Pattern.matches( " [^ ]+", tmp)){
                SP.add(new SPlevel(tmp));
            }else if (tmp.length() > 0 && SP.size() > 0){
                SP.get(SP.size()-1).listings.add(tmp);
            }
        }
    }

    public void add(Substitutionplan plan){
        List<SPlevel> SPtoAdd = plan.getPlan();
        if(SPtoAdd == null || SP ==null){
            return;
        }
        if(SPtoAdd.get(0).title.equals(SP.get(SP.size()-1))){
            for(int i = 0; i < SPtoAdd.get(0).listings.size(); i++){
                SP.get(SP.size()-1).listings.add(SPtoAdd.get(0).listings.get(i));
            }
            SPtoAdd.remove(0);
        }
        while(SPtoAdd.size() >0){
            SP.add(SPtoAdd.remove(0));
        }
    }

    public String getDate(){
        Matcher m = Pattern.compile("[0-9]{1,2}.[0-9]{1,2}.[0-9][0-9][0-9][0-9]").matcher(info);
        if(m.find()){
            return m.group();
        }
        return null;
    }
    public String getWeek(){
        Matcher m = Pattern.compile(", Woche [AB]").matcher(info);
        if(m.find()){
            return m.group().replace(", Woche ","");
        }
        return null;

    }
    public int getPlanIndex(){
        String tmp;
        Matcher m = Pattern.compile("\\(Seite [0-9]+ \\/ [0-9]+\\)").matcher(info);
        if(m.find()){
            tmp = m.group();
        }else return 0;
        tmp = tmp.replace(" ","");
        return Integer.parseInt(tmp.substring(tmp.indexOf("te")+2,tmp.indexOf("/")));
    }
    public int getMaxPlans(){
        String tmp;
        Matcher m = Pattern.compile("\\(Seite [0-9]+ \\/ [0-9]+\\)").matcher(info);
        if(m.find()){
            tmp = m.group();
        }else return 0;
        tmp = tmp.replace(" ","");
        return Integer.parseInt(tmp.substring(tmp.indexOf("/")+1,tmp.indexOf(")")));
    }

    public List<SPlevel> getPlan() {
        return SP;
    }

    public String getInfo() {
        return info;
    }
    public int getDay() {
        return day;
    }
    public String getUrl() {
        return url;
    }
    public String getUser() {
        return User;
    }

    public SPlevel getEntry(int i){
        return SP.get(i);
    }
    public void print(){
        for(int i = 0; i < SP.size(); i++){
            SP.get(i).print();
        }
    }
    private String IntToFixString(int i, int fix){
        String tmp = ""+i;
        while(tmp.length() < fix) tmp = "0"+tmp;
        return tmp;
    }
}
