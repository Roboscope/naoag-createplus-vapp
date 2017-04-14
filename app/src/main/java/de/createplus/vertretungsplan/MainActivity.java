package de.createplus.vertretungsplan;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.sql.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicMarkableReference;

import de.createplus.vertretungsplan.backgroundservices.Pair;
import de.createplus.vertretungsplan.backgroundservices.Timetable;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanDataReceiver;
import de.createplus.vertretungsplan.backgroundservices.UpdateTimetable;
import de.createplus.vertretungsplan.backgroundservices.UpdateTimetableReceiver;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
//import de.createplus.vertretungsplan.listview.MyCustomAdapter;
import de.createplus.vertretungsplan.databases.TgroupsDatabaseHelper;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;
import de.createplus.vertretungsplan.databases.TplanContract;
import de.createplus.vertretungsplan.databases.TplanDatabaseHelper;
import de.createplus.vertretungsplan.listview.MyCustomAdapter;
import de.createplus.vertretungsplan.listview.Parent;
import de.createplus.vertretungsplan.settings.SettingsActivity;

import static android.Manifest.permission.INTERNET;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    InterstitialAd mInterstitialAd;
    static private ContentViews currentContent = ContentViews.OVERVIEW;
    private AdView mAdView;
    static public Pair TimetableIndex = null;
    static private int CurrentShown = 1;
    static public String TodayDate = "*ERROR*";
    static public String TodayDateString = "*ERROR*";
    static public String TomorrowDate = "*ERROR*";
    static public String TomorrowDateString = "*ERROR*";
    static public int TodayWeek;
    static public int TomorrowWeek;
    static public int TodayDay;
    static public int TomorrowDay;
    static public String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    static public MainActivity THIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        THIS = this;
        updateDate();

        //Update Timetable if needed.
        Context con = getApplicationContext();
        Intent mServiceIntent = new Intent(con, UpdateTimetable.class);
        con.startService(mServiceIntent);

        setupBackgoundtasks();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setup Fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent mServiceIntent = new Intent(MainActivity.this, UpdatePlanData.class);
                MainActivity.this.startService(mServiceIntent);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateContainerContent();
        setupInterstitial();

    }

    private void setupBackgoundtasks() {
        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilterPlanData = new IntentFilter(
                UpdatePlanData.Constants.BROADCAST_ACTION);

        // Instantiates a new DownloadStateReceiver
        UpdatePlanDataReceiver mUpdatePlanDataReceiver =
                new UpdatePlanDataReceiver(this);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mUpdatePlanDataReceiver,
                statusIntentFilterPlanData);

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilterTimetable = new IntentFilter(
                UpdateTimetable.Constants.BROADCAST_ACTION);

        // Instantiates a new DownloadStateReceiver
        UpdateTimetableReceiver mUpdateTimetableReceiver =
                new UpdateTimetableReceiver(this);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mUpdateTimetableReceiver,
                statusIntentFilterTimetable);
    }

    private void setupInterstitial() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1455056966789635/4890940704");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("2C75B378313C32C7D50757BB562FF544")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public void updateDate() {

        Calendar calander = Calendar.getInstance();
        while (!((calander.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY) && (calander.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY))) {
            calander.add(Calendar.DATE, -1);
            //Log.e("DATE", "added to:" + calander.get(Calendar.DAY_OF_WEEK));
        }
        int cDay = calander.get(Calendar.DAY_OF_MONTH);
        int cMonth = calander.get(Calendar.MONTH) + 1;
        int cYear = calander.get(Calendar.YEAR);
        TodayDate = cDay + "." + cMonth + "." + cYear;
        TodayDateString = "Heute: " + days[calander.get(Calendar.DAY_OF_WEEK) - 2] + " " + TodayDate;
        TodayDay = calander.get(Calendar.DAY_OF_WEEK) - 2;

        Calendar forWeek = calander;
        forWeek.setMinimalDaysInFirstWeek(4);
        forWeek.setFirstDayOfWeek(Calendar.MONDAY);
        TodayWeek = forWeek.get(Calendar.WEEK_OF_YEAR);


        //Log.e("DATE", "" + calander.get(Calendar.DAY_OF_WEEK));
        calander.add(Calendar.DATE, 1);
        while (!((calander.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY) && (calander.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY))) {
            calander.add(Calendar.DATE, 1);
            //Log.e("DATE", "added to:" + calander.get(Calendar.DAY_OF_WEEK));
        }

        cDay = calander.get(Calendar.DAY_OF_MONTH);
        cMonth = calander.get(Calendar.MONTH) + 1;
        cYear = calander.get(Calendar.YEAR);
        TomorrowDate = cDay + "." + cMonth + "." + cYear;
        TomorrowDateString = "Morgen: " + days[calander.get(Calendar.DAY_OF_WEEK) - 2] + " " + TomorrowDate;
        TomorrowDay = calander.get(Calendar.DAY_OF_WEEK) - 2;

        forWeek = calander;
        forWeek.setMinimalDaysInFirstWeek(4);
        forWeek.setFirstDayOfWeek(Calendar.MONDAY);
        TomorrowWeek = forWeek.get(Calendar.WEEK_OF_YEAR);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //add content
        if (id == R.id.nav_overwiev) {
            currentContent = ContentViews.OVERVIEW;
            updateContainerContent();
        } else if (id == R.id.nav_substitutionplan) {
            currentContent = ContentViews.SUBSTITUTIONPLAN;
            updateContainerContent();
        } else if (id == R.id.nav_timetable) {
            Context con = getApplicationContext();
            Intent mServiceIntent = new Intent(con, UpdateTimetable.class);
            con.startService(mServiceIntent);
            currentContent = ContentViews.TIMETABLE;
            updateContainerContent();
        } else if (id == R.id.nav_settings) {
            currentContent = ContentViews.ADVERTISMENT;
            updateContainerContent();
        } else if (id == R.id.nav_share) {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void updateContainerContent() {
        //Log.e("UPDATE CONTENT", "Updating to: " + currentContent);
        //reset Container
        RelativeLayout content = (RelativeLayout) findViewById(R.id.main_content);
        while (content.getChildCount() > 0) {
            content.removeView(content.getChildAt(0));
        }
        findViewById(R.id.fab).setVisibility(View.INVISIBLE);

        //update Container
        content.addView(getLayoutInflater().inflate(currentContent.getId(), content, false), 0);

        if (currentContent == ContentViews.OVERVIEW) {
            setTitle("Übersicht");
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            setupOverview();
            //WebView web = (WebView) findViewById(R.id.overview_webview);
            //web.loadData("<html>    <link href=\"https://fonts.googleapis.com/css?family=Source+Sans+Pro\" rel=\"stylesheet\">    <link href=\"https://fonts.googleapis.com/css?family=Open+Sans\" rel=\"stylesheet\">    <style>        .coupon {            width: 250px;            padding: 10px;            text-align: center;            border: 3px dashed #0099ff; }        .t {            font-family: 'Source Sans Pro', sans-serif; }        .z {            font-family: 'Open Sans', sans-serif; }    </style>    <table cellspacing=\"10\" class=\"t\"><tr class=\"z\"><th>Heute: Montag,<br>06.03.2017</th></tr><tr><td><table class=\"coupon\"><tr><td><sup style=\"font-size:10px\">1</sup>/<sub style=\"font-size:10px\">2</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr><tr><td><sup style=\"font-size:10px\">3</sup>/<sub style=\"font-size:10px\">4</sub></td><td style=\"color: #33cc33\">Ge1</td><td style=\"color: #33cc33\">Wem</td><td style=\"color: #33cc33\">403</td></tr><tr><td><sup style=\"font-size:10px\">5</sup>/<sub style=\"font-size:10px\">6</sub></td><td>E3</td><td>Ad</td><td>402</td></tr><tr><td><sup style=\"font-size:10px\">7</sup>/<sub style=\"font-size:10px\">8</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr></table></td></tr><tr class=\"z\"><th><br>Morgen: Dienstag,<br>07.03.2017</th></tr><tr><td><table class=\"coupon\"><tr><td><sup style=\"font-size:10px\">1</sup>/<sub style=\"font-size:10px\">2</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr><tr><td><sup style=\"font-size:10px\">3</sup>/<sub style=\"font-size:10px\">4</sub></td><td style=\"color: #33cc33\">Ge1</td><td style=\"color: #33cc33\">Wem</td><td style=\"color: #33cc33\">403</td></tr><tr><td><sup style=\"font-size:10px\">5</sup>/<sub style=\"font-size:10px\">6</sub></td><td>E3</td><td>Ad</td><td>402</td></tr><tr><td><sup style=\"font-size:10px\">7</sup>/<sub style=\"font-size:10px\">8</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr></table></td></tr></table> </html>\n", "text/html", null);
            //ADS
            loadBanner(0);
        } else if (currentContent == ContentViews.SUBSTITUTIONPLAN) {
            setTitle("Vertretungsplan");
            updateDate();
            findViewById(R.id.subplan_textfield).setVisibility(View.INVISIBLE);
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);
            SPDatabaseHelper db = new SPDatabaseHelper(this);
            if (CurrentShown == 1) {
                ArrayList<Parent> Plan = db.getPlan(TodayDate);
                if (Plan.size() < 1)
                    findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                mExpandableList.setAdapter(new MyCustomAdapter(this, Plan));
                mExpandableList.setVisibility(View.VISIBLE);
            } else {
                ArrayList<Parent> Plan = db.getPlan(TomorrowDate);
                if (Plan.size() < 1)
                    findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                mExpandableList.setAdapter(new MyCustomAdapter(this, Plan));
                mExpandableList.setVisibility(View.VISIBLE);
            }

            //Setup SPPlan Toggle Button
            final Button SW = (Button) findViewById(R.id.sw);

            if (CurrentShown == 1) SW.setText(TodayDateString);
            else SW.setText(TomorrowDateString);

            SW.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SPDatabaseHelper db = new SPDatabaseHelper(MainActivity.this);
                    if (CurrentShown == 1) {
                        CurrentShown = 2;
                        SW.setText(TomorrowDateString);
                        ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);

                        ArrayList<Parent> Plan = db.getPlan(TomorrowDate);
                        mExpandableList.setVisibility(View.INVISIBLE);
                        if (Plan.size() < 1)
                            findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                        else {
                            findViewById(R.id.subplan_textfield).setVisibility(View.INVISIBLE);
                            mExpandableList.setAdapter(new MyCustomAdapter(MainActivity.this, Plan));
                            mExpandableList.setVisibility(View.VISIBLE);
                        }


                    } else {

                        SW.setText(TodayDateString);
                        CurrentShown = 1;
                        ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);

                        ArrayList<Parent> Plan = db.getPlan(TodayDate);
                        mExpandableList.setVisibility(View.INVISIBLE);
                        if (Plan.size() < 1)
                            findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                        else {
                            findViewById(R.id.subplan_textfield).setVisibility(View.INVISIBLE);
                            mExpandableList.setAdapter(new MyCustomAdapter(MainActivity.this, Plan));
                            mExpandableList.setVisibility(View.VISIBLE);
                        }

                    }
                    db.close();
                }
            });
            db.close();


            //ADS
            loadBanner(1);


        } else if (currentContent == ContentViews.TIMETABLE) {
            setTitle("Stundenplan");

            WebView TABLE = (WebView) findViewById(R.id.timetable_table);
            ThtmlDatabaseHelper db = new ThtmlDatabaseHelper(this);
            String tmp = db.getHtml();
            TABLE.loadData(tmp, "text/html", "UTF-8");
            TABLE.loadDataWithBaseURL("fake://fake.de", tmp, "text/html", "UTF-8", null);
            TABLE.getSettings().setBuiltInZoomControls(true);
            TABLE.getSettings().setDisplayZoomControls(false);
            TABLE.setLongClickable(false);


            //ADS
            loadBanner(0);

        } else if (currentContent == ContentViews.ADVERTISMENT) {
            setTitle("Werbung");

            Button b = (Button) findViewById(R.id.sw_show_ad);
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Snackbar.make(findViewById(R.id.main_content), "Deine Werbung ist noch nicht geladen!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                }
            });
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Snackbar.make(findViewById(R.id.main_content), "Deine Werbung ist noch nicht geladen!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_menu_gallery)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void setupOverview() {
        int tiltesize = 23;
        int textsize = 18;
        TableLayout LayoutToday = (TableLayout) findViewById(R.id.overview_tablelayout_inner_A);
        TableLayout LayoutTomorrow = (TableLayout) findViewById(R.id.overview_tablelayout_inner_B);
        TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
        TplanDatabaseHelper dbplan = new TplanDatabaseHelper(getApplicationContext());
        LinkedList<String[]> FullTimetable = dbplan.getPlan();

        TextView TitleToday1 = (TextView) findViewById(R.id.overview_textview_titleA1);
        TextView TitleToday2 = (TextView) findViewById(R.id.overview_textview_titleA2);
        TitleToday1.setText("Heute: " + days[TodayDay] + ",");
        TitleToday2.setText(TodayDate);
        TitleToday1.setTextSize(tiltesize);
        TitleToday2.setTextSize(tiltesize);

        String[] TodayEntries = new String[11];
        for (int i = 0; i < FullTimetable.size(); i++) {
            String[] currentItem = FullTimetable.get(i);
            if (((Integer.parseInt(currentItem[0]) % 2) != 0) == ((TodayWeek % 2) != 0)) {
                String[] tmp = {currentItem[1], currentItem[2], currentItem[3]};
                if (Integer.parseInt(currentItem[1]) == TodayDay + 1) {
                    //Log.e("Day",""+Integer.parseInt(currentItem[1]));
                    TodayEntries[Integer.parseInt(currentItem[2])] = dbgroups.getEnabledCourseOfGroup(currentItem[3]);
                }
            }
        }
        System.out.println(Arrays.toString(TodayEntries));

        for (int i = 1; i < TodayEntries.length - 1; i++) {
            if (TodayEntries[i] != null) {
                TableRow tr = new TableRow(MainActivity.this);
                String[] tmp = TodayEntries[i].split(" ");
                if (TodayEntries[i + 1] != null) {
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(20, 20, 20, 20);
                    two.setText(i + "");
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        TextView tw = new TextView(MainActivity.this);
                        tw.setPadding(20, 20, 20, 20);
                        tw.setText(tmp[o]);
                        tw.setTextSize(textsize);
                        tr.addView(tw);
                    }
                } else {
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(20, 20, 20, 20);
                    two.setText(i + "/" + (i + 1));
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        TextView tw = new TextView(MainActivity.this);
                        tw.setPadding(20, 20, 20, 20);
                        tw.setText(tmp[o]);
                        tw.setTextSize(textsize);
                        tr.addView(tw);
                    }
                    i++;
                }

                LayoutToday.addView(tr);
            }

        }

        //----------------------------------------------------------------------------
        TextView TitleTomorrow1 = (TextView) findViewById(R.id.overview_textview_titleB1);
        TextView TitleTomorrow2 = (TextView) findViewById(R.id.overview_textview_titleB2);
        TitleTomorrow1.setText("Morgen: " + days[TomorrowDay] + ",");
        TitleTomorrow2.setText(TomorrowDate);
        TitleTomorrow1.setTextSize(tiltesize);
        TitleTomorrow2.setTextSize(tiltesize);

        String[] TomorrowEntries = new String[11];
        for (int i = 0; i < FullTimetable.size(); i++) {
            String[] currentItem = FullTimetable.get(i);
            //Log.e("Day",""+Integer.parseInt(currentItem[0])+":"+TomorrowWeek);
            if (((Integer.parseInt(currentItem[0]) % 2) != 0) == ((TomorrowWeek % 2) != 0)) {
                String[] tmp = {currentItem[1], currentItem[2], currentItem[3]};
                //Log.e("Day",""+Integer.parseInt(currentItem[1])+":"+TomorrowDay);
                if (Integer.parseInt(currentItem[1]) == TomorrowDay + 1) {
                    //Log.e("Day",""+Integer.parseInt(currentItem[1]));
                    TomorrowEntries[Integer.parseInt(currentItem[2])] = dbgroups.getEnabledCourseOfGroup(currentItem[3]);
                }
            }
        }
        System.out.println(Arrays.toString(TomorrowEntries));

        for (int i = 1; i < TomorrowEntries.length - 1; i++) {
            if (TomorrowEntries[i] != null) {
                TableRow tr = new TableRow(MainActivity.this);
                String[] tmp = TomorrowEntries[i].split(" ");
                if (TomorrowEntries[i + 1] != null) {
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(20, 20, 20, 20);
                    two.setText(i + "");
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        TextView tw = new TextView(MainActivity.this);
                        tw.setPadding(20, 20, 20, 20);
                        tw.setText(tmp[o]);
                        tw.setTextSize(textsize);
                        tr.addView(tw);
                    }
                } else {
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(20, 20, 20, 20);
                    two.setText(i + "/" + (i + 1));
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        TextView tw = new TextView(MainActivity.this);
                        tw.setPadding(20, 20, 20, 20);
                        tw.setText(tmp[o]);
                        tw.setTextSize(textsize);
                        tr.addView(tw);
                    }
                    i++;
                }

                LayoutTomorrow.addView(tr);
            }

        }
    }

    private void loadBanner(int i) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean adPref = sharedPref.getBoolean(SettingsActivity.KEY_AD_BANNER, true);
        if (adPref) {
            mAdView = (AdView) findViewById(R.id.adBanner);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("2C75B378313C32C7D50757BB562FF544").build();
            mAdView.loadAd(adRequest);
            if (i == 1) {
                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT);

                p.addRule(RelativeLayout.BELOW, R.id.adBanner);
                TextView tw = (TextView) findViewById(R.id.subplan_textfield);
                tw.setLayoutParams(p);

                ExpandableListView exp = (ExpandableListView) findViewById(R.id.expandable_list);
                exp.setLayoutParams(p);
            }
        }

    }
}
