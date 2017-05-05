package de.createplus.vertretungsplan.backgroundservices;

import android.content.Context;
import android.util.Log;
import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.substitutionplan.SPlevel;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;


public class SPDownloader {
    private static final String URL_ARCHIVE = "http://gymnasium-wuerselen.de/untis/";

    public static int download(int index, int day, String user, String username, String password, Context context) {
        String info = "";
        try {
            String url = URL_ARCHIVE + user + "/f" + day + "/" + "subst_" + IntToFixString(index, 3) + ".htm";
            String base64login = new String(Base64.encodeBase64((username + ":" + password).getBytes())); // creating an encoded login
            Document doc = Jsoup.connect(url).header("Authorization", "Basic " + base64login).get(); //loading page.

            SPDatabaseHelper SPDbHelper = new SPDatabaseHelper(context);

            String htmlraw = doc.html(); // get raw html
            String[] htmlsplit = htmlraw.split("\\n"); // split raw html -> lines of the file in an array
            info = htmlsplit[88].replace("<div class=\"mon_title\">", "").replace("</div>", ""); // get level information


            org.jsoup.select.Elements rows = doc.select("tr");
            List<String> planList = new LinkedList<String>();
            String title = "";
            Calendar cal = Calendar.getInstance();
            for (org.jsoup.nodes.Element row : rows) {

                int i = 0;
                String[] data = new String[8];
                org.jsoup.select.Elements columns = row.select("td");
                for (org.jsoup.nodes.Element column : columns) {
                    //Log.e(TAG,column.html());
                    if (column.outerHtml().contains("class=\"list inline_header\"")) {
                        title = column.text();
                    } else {
                        //if(i < 7){
                            data[i] = column.text();
                            i++;
                        //}
                    }
                }
                if (data[6] != null) {

                    data[0] = info.split("\\.")[0].replace(" ","") + "." + info.split("\\.")[1].replace(" ","") + ".";
                    //Log.e("VERTRETUNGSPLAN", title + Arrays.toString(data) + "INFO:" + info);
                    SPDbHelper.addLine(title, data, info);
                }/*else if(data[1] == null){
                    title = data[0];
                }*/

            }
        } catch (IOException e) {
            Log.e("VERTRETUNGSPLAN", e.toString());
            return -1;
        }return getMaxPlans(info);


    }

    public static String getDate(String info) {
        Matcher m = Pattern.compile("[0-9]{1,2}.[0-9]{1,2}.[0-9][0-9][0-9][0-9]").matcher(info);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    public static String getWeek(String info) {
        Matcher m = Pattern.compile(", Woche [AB]").matcher(info);
        if (m.find()) {
            return m.group().replace(", Woche ", "");
        }
        return null;

    }

    public static int getPlanIndex(String info) {
        String tmp;
        Matcher m = Pattern.compile("\\(Seite [0-9]+ \\/ [0-9]+\\)").matcher(info);
        if (m.find()) {
            tmp = m.group();
        } else return 0;
        tmp = tmp.replace(" ", "");
        return Integer.parseInt(tmp.substring(tmp.indexOf("te") + 2, tmp.indexOf("/")));
    }

    public static int getMaxPlans(String info) {
        String tmp;
        Matcher m = Pattern.compile("\\(Seite [0-9]+ \\/ [0-9]+\\)").matcher(info);
        if (m.find()) {
            tmp = m.group();
        } else return 1;
        tmp = tmp.replace(" ", "");
        return Integer.parseInt(tmp.substring(tmp.indexOf("/") + 1, tmp.indexOf(")")));
    }


    private static String IntToFixString(int i, int fix) {
        String tmp = "" + i;
        while (tmp.length() < fix) tmp = "0" + tmp;
        return tmp;
    }
}
