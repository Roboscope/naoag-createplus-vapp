package de.createplus.vertretungsplan.backgroundservices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.databases.TgroupsDatabaseHelper;
import de.createplus.vertretungsplan.databases.TplanDatabaseHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by Max Nuglisch on 01.02.2017.
 */

public class Timetable {
    public static String urlArchive = "http://gymnasium-wuerselen.de/untis/Schueler-Stundenplan/";
    String schoolname = "Gymnasium der Stadt Würselen";
    String url;
    String password;
    String username;
    String html;
    public static String varname = "classes";


    //-------  info  -------
    int week; //Iso 8601 week number

    //from plan
    String[][] plan;
    String level; //Stufe
    String date; //Date of the Plan


    public Timetable(int week, String type, int number, String username, String password){
        url = urlArchive + IntToFixString(week,2) + "/" + type + "/" + type + IntToFixString(number,5) + ".htm";
        this.week = week;
        this.password = password;
        this.username = username;
    }

    public void update() throws IOException {
        String base64login = new String(Base64.encodeBase64((username + ":" + password).getBytes())); // creating an encoded login
        Document doc = Jsoup.connect(url).header("Authorization", "Basic " + base64login).get(); //loading page.

        String htmlraw = doc.html(); // get raw html
        html = htmlraw;
        String[] htmlsplit = htmlraw.split("\\n"); // split raw html -> lines of the file in an array

        //get information
        level = htmlsplit[17].replace("<font size=\"5\" face=\"Arial\">","").replace("</font>","").replace(" ",""); // get level information

        int dateStart = htmlraw.indexOf("<td valign=\"bottom\"> <font size=\"4\" face=\"Arial\" color=\"#0000FF\"></font></td>\n" +
                "     </tr>\n" +
                "    </tbody>\n" +
                "   </table>\n" +
                "   <font size=\"3\" face=\"Arial\">");
        int dateEnd = htmlraw.indexOf(schoolname);
        Log.e(TAG,dateStart + ":"+dateEnd + ":" + url);
        date = htmlraw.substring(dateStart,dateEnd); // get date information by getting string from "Periode" to the schoolname
        //Log.e(TAG,html);
        //System.out.println(htmlraw);
        //Matcher m = Pattern.compile(">.+Gymnasium der Stadt Würselen").matcher(htmlraw);
        //date = m.group();


        Whitelist wl = Whitelist.simpleText();
        wl.addTags("b");
        wl.addTags("td");
        wl.addTags("tr");
        Whitelist w2 = Whitelist.simpleText();
        w2.addTags("b");

        List<String> planList = new LinkedList<String>();

        //select rows
        org.jsoup.select.Elements rows = doc.select("tr");

        //going through rows
        for (org.jsoup.nodes.Element row : rows) {
            //System.out.println(":"+row.html());
            //select columns
            //System.out.println(":"+row.html());
            //System.out.println(":"+row.html());
            org.jsoup.select.Elements columns = row.select("td");

            //going through columns
            for (org.jsoup.nodes.Element column : columns) {
                //System.out.println(":"+);
                String outer = column.outerHtml();


                if (column.text().contains(" ")) { //selecting valid information

                    //if field has a blod marked text -> mark it with a % char
                    String tmp = column.text();
                    String tmp2 = Jsoup.clean(column.html(), w2);
                    String tmp3 = Jsoup.clean(column.html(), wl);

                    int end = tmp3.indexOf("</tr>");
                    if (end > 0 && Pattern.matches("<tr><td><b>[^<]+</b></td><td>[^<]+</td>", tmp3.substring(0,end).replace("\n", "").replace(" ", ""))) {
                        String Title = Jsoup.clean(tmp3.substring(0, end), new Whitelist()).replace("   "," ");
                        tmp = tmp.replace(Title,Title+"%");
                    }else if (Pattern.matches("<b>[^ ]+</b>.+", tmp2)) {
                        tmp2 = tmp2.replace("<b>", "");
                        tmp = tmp.substring(0, tmp2.indexOf("</b>")) + "%" + tmp.substring(tmp2.indexOf("</b>"), tmp.length());
                    }

                    if (outer.indexOf("rowspan=\"") > 0) {
                        String size = outer.substring(outer.indexOf("rowspan=\""), outer.indexOf("rowspan=\"") + 10).replace("rowspan=\"", "");
                        if (Integer.parseInt(size) == 4) {
                            tmp = "[SIZE:4]" + tmp;
                        }

                    }
                    planList.add(tmp);
                }
                if (column.text().length() == 0) {//if field is empty -> FREE
                    planList.add("FREE");
                }
                if (Pattern.matches("[0-9][0-9]?", column.text())) { // if field contains an hout indicator -> add it
                    planList.add("Indicator:" + column.text());
                    //System.out.println(":"+column.html());
                }
                if (column.text().equals("Pause")) { // if field contains pause -> add Pause
                    planList.add("PAUSE");
                }
            }
        }
        for (int i = 0; i < planList.size(); i++) {
            System.out.println(planList.get(i));
        }

        // creating array
        plan = new String[12][6];
        plan[0][0] = " ";
        plan[0][1] = "Montag";
        plan[0][2] = "Dienstag";
        plan[0][3] = "Mittwoch";
        plan[0][4] = "Donnerstag";
        plan[0][5] = "Freitag";

        int currentIndicator = 0; // hour indicator
        int currentLine = 0; //line iterator index


        for (int row = 1; row < plan.length; row++) {
            currentIndicator++;

            //go to next Indicator mark
            while (!planList.get(currentLine).equals("Indicator:" + currentIndicator)) currentLine++;
            currentLine++;
            currentLine++;


            plan[row][0] = "" + currentIndicator;


            for (int i = 1; i < plan[0].length; i++) {

                // the next lines only exsists because of the very good system of our school. not.
                /*if((row == 11 || row == 9 ||row == 6 )&& i<plan[0].length-1 && (plan[5][i] == "FREE" || plan[5][i] == null || Pattern.matches("[^ ]+ [^ ]+ [^ ]+ .+", plan[5][i]))){
                    i++;
                    //System.out.println("Done");
                }*/


                // get string from list
                String tmp;
                if (i < planList.size()) {
                    tmp = planList.get(currentLine);
                } else break;

                if (row == 2 || row == 4 || row == 6 || row == 9 || row == 11) {
                    //Log.e("Timetable.update",""+plan[row][0]);
                    //Log.e("TEST", ""+plan[row - 1][i]);
                    while (i < plan[row].length && plan[row - 1][i] != null && plan[row - 1][i].contains("[SIZE:4]")) {
                        //Log.e("TEST", "VERSCHOBEN"+plan[row - 1][i]);
                        i++;
                        //tmp = "Ich wurde verschoben";
                    }
                }

                //if line is a Timetable object
                if (Pattern.matches("[^ ]+ [^ ]+ [^ ]+.+", tmp)) {

                    plan[row][i] = tmp;
                    currentLine++;

                    //if line is FREE or PAUSE -> add it and go 2 line further
                } else if (tmp.equals("PAUSE") || tmp.equals("FREE")) {
                    plan[row][i] = tmp;
                    currentLine++;
                    currentLine++;
                    //if an hour indicator appears -> break the loop
                } else if (tmp.equals("Indicator:" + currentIndicator)) break;

                    //if the information is not usable it just skips it
                else currentLine++;
            }

        }
        for (int x = 1; x < plan.length; x++) {
            for (int i = 1; i < plan[0].length; i++) {
                if( plan[x][i] != null) plan[x][i] = plan[x][i].replace("[SIZE:4]", "");
            }
        }
    }

    public static Pair getTimtableIndex(String username, String password) throws IOException{

        String url = urlArchive + "frames/navbar.htm";
        String base64login = new String(Base64.encodeBase64((username + ":" + password).getBytes())); // creating an encoded login
        Document doc = Jsoup.connect(url).header("Authorization", "Basic " + base64login).get(); //loading page.

        String htmlraw = doc.html(); // get raw html
        Matcher m = Pattern.compile("var "+varname+" = \\[.+\\];").matcher(htmlraw);
        String classes = "";
        if (m.find()) {
             classes = m.group().replace("var "+varname+" = [", "").replace("];","").replace("\"","");
        }

        String[] classesSplit = classes.split(",");

        m = Pattern.compile("<select name=\"week\" class=\"selectbox\" [^&]+<\\/select>").matcher(htmlraw);
        String weeks = "";
        if (m.find()) {
            weeks = m.group().replace("<select name=\"week\" class=\"selectbox\" onchange=\"doDisplayTimetable(NavBar, topDir);\"> ","").replace("<option value=","").replace("</select>","");
        }
        String[] weeksSplit = weeks.split("</option> ");
        //Log.e("Week",weeksSplit[0]+"|"+weeksSplit[1]);

        return new Pair(classesSplit,weeksSplit);
    }

    public void print(){
        for(int i = 0; i<plan.length; i++)
        {
            for(int j = 0; j<plan[0].length; j++)
            {
                System.out.print(" | "+plan[i][j]);
            }
            System.out.println();
            System.out.println();
        }
    }

    public String toString(){
        String tmp = "";
        for(int i = 0; i<plan.length; i++)
        {
            for(int j = 0; j<plan[0].length; j++)
            {
                tmp = tmp + " | "+plan[i][j];
            }
            tmp = tmp +"\n\n";
        }
        return tmp;
    }




    public String getUrl(){return url;}
    public String getUsername(){return username;}
    public String[][] getPlan(){return plan;}
    public String getLevel() {return level;}
    public int getWeek() {return week;}
    public String getDate() {return date;}
    public String getHtml() {return html;}

    public void setUrl(String url) {this.url = url;}
    public void setPassword(String password) {this.password = password;}
    public void setPlan(String[][] plan) {this.plan = plan;}
    public void setUsername(String username) {this.username = username;}
    public void setWeek(int week) {this.week = week;}

    private String IntToFixString(int i, int fix){
        String tmp = ""+i;
        while(tmp.length() < fix) tmp = "0"+tmp;
        return tmp;
    }

    public void addToSQL(Context context){
        TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(context);
        TplanDatabaseHelper dbplan = new TplanDatabaseHelper(context);


        for(int day = 1; day <6; day++){
            for(int hour = 1; hour < 12; hour++){
                String tmp = plan[hour][day];
                if(tmp != null && tmp != "FREE" && tmp != "PAUSE"){
                    String[] parts = tmp.split("%");// LK08%COURSES
                    if(Pattern.matches("[^ %]+% [^ ]+ [^ ]+", tmp)){// E% AD 506
                        if(!dbgroups.doesExist(parts[0])){
                            dbgroups.addLine(parts[0],true);
                        }
                        String tmpin[] = parts[1].split(" ");// AD 506
                        dbplan.addLine(Integer.parseInt(date.split("[\\(\\)]")[3]), day, hour, parts[0] ,parts[0],tmpin[1],tmpin[2]);
                    }
                    Matcher m = Pattern.compile(" [^ ]+ [^ ]+ [^ ]+").matcher(parts[1]);
                    while(m.find()){
                        String tmpin[] = m.group().split(" ");// E3 AD 506
                        if(!dbgroups.doesExist(tmpin[1])){
                            dbgroups.addLine(tmpin[1],false);
                        }
                        dbplan.addLine(Integer.parseInt(date.split("[\\(\\)]")[3]), day, hour, tmpin[1] ,parts[0],tmpin[2],tmpin[3]);
                    }
                }
            }
        }
    }
}
