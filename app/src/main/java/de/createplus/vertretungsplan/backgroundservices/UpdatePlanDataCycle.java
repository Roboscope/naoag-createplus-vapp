package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.config;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;
import de.createplus.vertretungsplan.databases.TplanDatabaseHelper;
import de.createplus.vertretungsplan.settings.SettingsActivity;


public class UpdatePlanDataCycle extends IntentService {
    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "de.createplus.vertretungsplan.backgroundservices.UpdatePlanData.BROADCAST";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "de.createplus.vertretungsplan.backgroundservices.UpdatePlanData.STATUS";
    }


    public UpdatePlanDataCycle() {
        super("UpdatePlanData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean teacherMode = sharedPref.getBoolean(SettingsActivity.KEY_TEACHERMODE, false);
        if (!teacherMode) {
            if (studentPWtest()) return;
            Log.e("VERTRETUNGSPLAN", "Updating subplan ---- WITH NOTIFICATIONS");

            //LinkedList<String[]> knownchanges = getChanges();

            studentModeUpdate();
            LinkedList<String[]> newchanges = getChanges();

            //LinkedList<String[]> changes = getDiffrences(knownchanges, newchanges);

            pushChangeNotifications(newchanges);

        }
    }

    private String getHour(int hour){
        if(hour == 1 || hour == 2)
            return "1/2";
        if(hour == 3 || hour == 4)
            return "3/4";
        if(hour == 5 || hour == 6)
            return "5/6";
        if(hour == 8 || hour == 9)
            return "8/9";
        if(hour == 10 || hour == 11)
            return "10/11";
        return "";
    }
    private boolean studentPWtest() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String studentUSERNAMEPref = sharedPref.getString(SettingsActivity.KEY_STUDENT_USERNAME, "");
        String studentPASSWORDPref = sharedPref.getString(SettingsActivity.KEY_STUDENT_PASSWORD, "");

        if (studentUSERNAMEPref.length() <= 0 || studentPASSWORDPref.length() <= 0) {
            return true;
        }

        if (!studentUSERNAMEPref.equals(config.studentUsername) || !studentPASSWORDPref.equals(config.studentPasswd)) {
            return true;
        }
        return false;
    }

    public void studentModeUpdate() {
        SPDatabaseHelper SPDbHelper = new SPDatabaseHelper(this);

        int maxplans = SPDownloader.download(1, 1, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
        if (maxplans > 0) {
            SPDbHelper.removeAll();
            for (int i = 1; i <= maxplans; i++) {
                maxplans = SPDownloader.download(i, 1, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
            }
            maxplans = SPDownloader.download(1, 2, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
            for (int i = 2; i <= maxplans; i++) {
                maxplans = SPDownloader.download(i, 2, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
            }
        }

        Intent localIntent;
        if (maxplans > 0) {
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, config.spPlanSuccessReloadMsg);
        } else {
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, config.spPlanFailReloadMsg);
        }

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private LinkedList<String[]> getChanges() {
        LinkedList<String[]> ret = new LinkedList<String[]>();

        MainActivity.updateDate();

        //TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
        TplanDatabaseHelper dbplan = new TplanDatabaseHelper(getApplicationContext());
        LinkedList<String[]> FullTimetable = dbplan.getPlan(getApplicationContext());
        SPDatabaseHelper db = new SPDatabaseHelper(getApplicationContext());
        ThtmlDatabaseHelper dbFORCLASS = new ThtmlDatabaseHelper(getApplicationContext());
        // get today name: MainActivity.days[MainActivity.TodayDay]
        // get today date: MainActivity.TodayDate

        String[] TodayEntries = new String[11];
        for (int i = 0; i < FullTimetable.size(); i++) {
            String[] currentItem = FullTimetable.get(i);
            if (((Integer.parseInt(currentItem[0]) % 2) != 0) == ((MainActivity.TodayWeek % 2) != 0)) {
                String[] tmp = {currentItem[1], currentItem[2], currentItem[3]};
                if (Integer.parseInt(currentItem[1]) == MainActivity.TodayDay + 1) {
                    //Log.e("Day",""+Integer.parseInt(currentItem[1]));
                    TodayEntries[Integer.parseInt(currentItem[2])] = currentItem[4] + " " + currentItem[5] + " " + currentItem[6];
                    /*tmp[0] = COLUMN_NAME_WEEK
                    tmp[1] = COLUMN_NAME_DAY
                    tmp[2] = COLUMN_NAME_HOUR
                    tmp[3] = COLUMN_NAME_COURSEGROUP
                    tmp[4] = COLUMN_NAME_COURSE
                    tmp[5] = COLUMN_NAME_TEACHER
                    tmp[6] = COLUMN_NAME_ROOM*/
                }
            }
        }
        System.out.println(Arrays.toString(TodayEntries));// <-------- Plan of today



        String[] TomorrowEntries = new String[11];
        for (int i = 0; i < FullTimetable.size(); i++) {
            String[] currentItem = FullTimetable.get(i);
            //Log.e("Day",""+Integer.parseInt(currentItem[0])+":"+TomorrowWeek);
            if (((Integer.parseInt(currentItem[0]) % 2) != 0) == ((MainActivity.TomorrowWeek % 2) != 0)) {
                String[] tmp = {currentItem[1], currentItem[2], currentItem[3]};
                //Log.e("Day",""+Integer.parseInt(currentItem[1])+":"+TomorrowDay);
                if (Integer.parseInt(currentItem[1]) == MainActivity.TomorrowDay + 1) {
                    //Log.e("Day",""+Integer.parseInt(currentItem[1]));
                    TomorrowEntries[Integer.parseInt(currentItem[2])] = currentItem[4] + " " + currentItem[5] + " " + currentItem[6];
                }
            }
        }
        System.out.println(Arrays.toString(TomorrowEntries));// <-------- Plan of tomorrow


        //#####################################################################################################################




        for (int i = 1; i < TodayEntries.length; i++) {
            if (TodayEntries[i] != null) {
                String[] tmp = TodayEntries[i].split(" ");

                final LinkedList<String[]> spplan = db.getSPinfo(dbFORCLASS.getSavedClass(), tmp[0], MainActivity.TodayDate, i); // Is there a change for this lesson?
                String[] sp = null;
                int changetypeNotFinal = 0; // 0 = Nichts, 1 = Vertretung, 2 = Raumwechsel, 3 = Entfall, 4 = Unbekannt
                if (spplan.size() > 0) {
                    sp = spplan.get(0);
                    //retin[0] = KIND; retin[1] = HOUR; retin[2] = ROOM; retin[3] = DATE; retin[4] = NEWROOM; retin[5] = TEXT;
                    //Log.e("setupOverview",sp[0].replace(" ","").toUpperCase());
                    String Kind = sp[0].replace(" ", "").toUpperCase();
                    String Text = sp[5].replace(" ", "").toUpperCase();
                    if (Kind.equals("RAUM-VTR.")) {
                        changetypeNotFinal = 2;
                    } else if ((Kind.equals("VERTRETUNG") && (Text.contains("SELBSTST"))) || Kind.equals("ENTFALL")) {
                        changetypeNotFinal = 3;
                    } else if (Kind.equals("VERTRETUNG")) {
                        changetypeNotFinal = 1;
                    } else {
                        changetypeNotFinal = 4;
                    }
                    //Log.e("setupOverview",""+changetypeNotFinal);
                }
                final int changetype = changetypeNotFinal;

                i++;

                if(changetype != 0){
                    String[] obj = {"1", MainActivity.days[MainActivity.TodayDay], MainActivity.TodayDate, };

                    /*tmp[0] = COLUMN_NAME_WEEK
                    tmp[1] = COLUMN_NAME_DAY
                    tmp[2] = COLUMN_NAME_HOUR
                    tmp[3] = COLUMN_NAME_COURSEGROUP
                    tmp[4] = COLUMN_NAME_COURSE
                    tmp[5] = COLUMN_NAME_TEACHER
                    tmp[6] = COLUMN_NAME_ROOM*/
                    //retin[0] = KIND; retin[1] = HOUR; retin[2] = ROOM; retin[3] = DATE; retin[4] = NEWROOM; retin[5] = TEXT;
                    sp[0]=""+changetype;
                    sp[2]=MainActivity.days[MainActivity.TodayDay];
                    ret.add(concatenate(tmp,sp));
                }
            }

        }

        //----------------------------------------------------------------------------

        //TitleTomorrow1.setText(MainActivity.days[MainActivity.TomorrowDay] + ",");
        //TitleTomorrow2.setText(MainActivity.TomorrowDate);


        //Log.e("setupOverview","-------------------------------------");
        for (int i = 1; i < TomorrowEntries.length; i++) {

            if (TomorrowEntries[i] != null) {

                String[] tmp = TomorrowEntries[i].split(" ");


                final LinkedList<String[]> spplan = db.getSPinfo(dbFORCLASS.getSavedClass(), tmp[0], MainActivity.TomorrowDate, i);
                String[] sp = null;

                int changetypeNotFinal = 0; // 0 = Nichts, 1 = Vertretung, 2 = Raumwechsel, 3 = Entfall, 4 = Unbekannt
                if (spplan.size() > 0) {
                    sp = spplan.get(0);
                    //retin[0] = KIND; retin[1] = HOUR; retin[2] = ROOM; retin[3] = DATE; retin[4] = NEWROOM; retin[5] = TEXT;
                    String Kind = sp[0].replace(" ", "").toUpperCase();
                    String Text = sp[5].replace(" ", "").toUpperCase();
                    if (Kind.equals("RAUM-VTR.")) {
                        changetypeNotFinal = 2;
                    } else if ((Kind.equals("VERTRETUNG") && (Text.contains("SELBSTST"))) || Kind.equals("ENTFALL")) {
                        changetypeNotFinal = 3;
                    } else if (Kind.equals("VERTRETUNG")) {
                        changetypeNotFinal = 1;
                    } else {
                        changetypeNotFinal = 4;
                    }
                    // Log.e("setupOverview",""+changetypeNotFinal);
                }
                final int changetype = changetypeNotFinal;

                if (changetype != 0) {
                    sp[0]=""+changetype;
                    sp[2]= MainActivity.days[MainActivity.TomorrowDay];
                    ret.add(concatenate(tmp,sp));
                }

                    i++;
                }

            }

        return ret;
    }

    private void addNotification(String title, String text) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(m, builder.build());
    }

    public <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public void pushChangeNotifications(LinkedList<String[]> changes){
        for(int i = 0; i < changes.size(); i++){ // 0 = Nichts, 1 = Vertretung, 2 = Raumwechsel, 3 = Entfall, 4 = Unbekannt
            String[] toView = changes.get(i);
            String title = "";
            String text = "";
            if(toView[3].equals("3")){
                title = "Entfall: " +toView[5]+" "+getHour(Integer.parseInt(toView[4]))+ " "+toView[0] + " " +toView[1];
                text = "Info: " + toView[8];
            }
            else if(toView[3].equals("1")){
                title = "Vertretung: " +toView[5]+" "+getHour(Integer.parseInt(toView[4]))+ " "+ toView[0] + " " +toView[1];
                text = "Raum: " + toView[7];
                if(toView[8].length()>3) text = text +" Info: " + toView[8];
            }
            else if(toView[3].equals("2")){
                title = "Raumwechsel: "  +toView[5]+" "+getHour(Integer.parseInt(toView[4]))+ " "+ toView[0] + " " +toView[1];
            }

            addNotification(title, text);
            Log.e("PUSH NOTE", Arrays.toString(changes.get(i)));
        }
    }
}
