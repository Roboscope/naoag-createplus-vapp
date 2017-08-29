package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

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
            Log.e("VERTRETUNGSPLAN", "Updating student plan");
            studentModeUpdate();
        }
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

    private LinkedList<String[]> getChanges() {/*
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
                    tmp[6] = COLUMN_NAME_ROOM
                }
            }
        }
        //System.out.println(Arrays.toString(TodayEntries));

        for (int i = 1; i < TodayEntries.length; i++) {
            if (TodayEntries[i] != null) {
                String[] tmp = TodayEntries[i].split(" ");

                final LinkedList<String[]> spplan = db.getSPinfo(dbFORCLASS.getSavedClass(), tmp[0], MainActivity.TodayDate, i);

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


                for (int o = 0; o < tmp.length; o++) {
                    if (tmp[o].equals(" ") || tmp[o].equals("")) o++;

                    if (changetype == 2) {

                        tmp[2] = sp[4];
                        //Log.e("setupOverview",Arrays.toString(sp));
                    }

                }

                i++;

                if(changetype != 0){
                    String[] obj = {"1", MainActivity.days[MainActivity.TodayDay], MainActivity.TodayDate, };

                    /*tmp[0] = COLUMN_NAME_WEEK
                    tmp[1] = COLUMN_NAME_DAY
                    tmp[2] = COLUMN_NAME_HOUR
                    tmp[3] = COLUMN_NAME_COURSEGROUP
                    tmp[4] = COLUMN_NAME_COURSE
                    tmp[5] = COLUMN_NAME_TEACHER
                    tmp[6] = COLUMN_NAME_ROOM
                    //retin[0] = KIND; retin[1] = HOUR; retin[2] = ROOM; retin[3] = DATE; retin[4] = NEWROOM; retin[5] = TEXT;
                    ret.add();
                }
            }

        }

        //----------------------------------------------------------------------------

        TitleTomorrow1.setText(MainActivity.days[MainActivity.TomorrowDay] + ",");
        TitleTomorrow2.setText(MainActivity.TomorrowDate);


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
        //System.out.println(Arrays.toString(TomorrowEntries));
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
                    Log.e("setupOverview", sp[0].replace(" ", "").toUpperCase());
                    Log.e("setupOverview", sp[5].replace(" ", "").toUpperCase());
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

                if ((i + 1) < TomorrowEntries.length && TomorrowEntries[i + 1] != null) {

                    two.setText(i + "");
                    tr.addView(two);

                    for (int o = 0; o < tmp.length; o++) {
                        if (tmp[o].equals(" ") || tmp[o].equals("")) o++;

                        tw.setText(tmp[o].replace(" ", ""));
                        tr.addView(tw);
                    }
                } else {
                    two.setText(i + "/" + (i + 1));
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        if (tmp[o].equals(" ") || tmp[o].equals("")) o++;
                        //Log.e("O", "|" + tmp[o] + "|");
                        if (changetype == 2) {
                            if (o == 2) {
                                tw.setTextColor(getChangeColor(changetype));
                                tmp[2] = sp[4];
                                //Log.e("setupOverview",Arrays.toString(sp));
                            }
                        } else {
                            tw.setTextColor(getChangeColor(changetype));
                        }
                        tw.setText(tmp[o].replace(" ", ""));

                        tr.addView(tw);
                    }
                    if (changetype != 0) {
                        LinearLayout outerLayout = new LinearLayout(this);
                        ImageView picture = new ImageView(this);
                        outerLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onClickOverview(spplan.get(0), changetype);
                            }
                        });
                        LinearLayout.LayoutParams pictureLayoutParams = new LinearLayout.LayoutParams(60, 60);
                        picture.setImageResource(R.drawable.info_button);
                        pictureLayoutParams.setMargins(0, 28, 10, 0);
                        //picture.setPadding(0, 35, 10, 0);

                        outerLayout.addView(picture, pictureLayoutParams);
                        outerLayout.bringToFront();
                        tr.addView(outerLayout);
                    }

                    i++;
                }
                LayoutTomorrow.addView(tr);
            }
        }*/
        return null;
    }


}
