package de.createplus.vertretungsplan;
/*
 [CLOSED] THIS FILE IS FINAL.

 login: 246221
 pw: DJCRGi175Ja
 */

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
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
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.*;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.security.Permission;
import java.sql.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicMarkableReference;

import de.createplus.vertretungsplan.backgroundservices.Pair;
import de.createplus.vertretungsplan.backgroundservices.Timetable;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanDataCycle;
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
import de.createplus.vertretungsplan.permissionMngr.PermissionManager;
import de.createplus.vertretungsplan.permissionMngr.PermissionRequest;
import de.createplus.vertretungsplan.settings.SettingsActivity;

import static android.Manifest.permission.INTERNET;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    InterstitialAd mInterstitialAd;
    static private ContentViews currentContent = null;
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
    static public boolean updating = false;
    static public int width = 0;
    static public int height = 0;
    static public SwipeRefreshLayout mSwipeRefreshLayout;
    private ShowcaseView showcaseview;
    private Target SliderBtn, SliderMainAreas, Settings, ClassSetting;
    public static boolean inTut, waitingForSlider, waitingForSwiping, waitingForSettings;
    String tutTextSilderBtn, tutTextSliderMainAreas, tutTextSettings, tutTextClass;
    public int currentShowcase = 0;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        Log.e("Main Screen Hight", "" + height);
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }


        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        //window.setStatusBarColor(ContextCompat.getColor(activity,R.color.my_statusbar_color));
        THIS = this;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defsite = sharedPref.getString(SettingsActivity.KEY_COURSE_DEFAULTSITE, "1");
        boolean updateSupPlan = false;
        if (currentContent == null) {
            switch (defsite) {
                case "1":
                    currentContent = ContentViews.OVERVIEW;
                    updateSupPlan = true;
                    break;
                case "2":
                    currentContent = ContentViews.SUBSTITUTIONPLAN;
                    updateSupPlan = true;
                    break;
                case "3":
                    currentContent = ContentViews.TIMETABLE;
                    break;
            }
        }

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
                if (!updating) {
                    updating = true;
                    Intent mServiceIntent = new Intent(MainActivity.this, UpdatePlanDataCycle.class);
                    MainActivity.this.startService(mServiceIntent);
                    //MainActivity.mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //currentShowcase=2; showcaseview.setShowcase(SliderMainAreas,true);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (inTut && currentShowcase == 1) {
                    setShowCase(3);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (inTut && currentShowcase == 3) {
                    setShowCase(1);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateContainerContent();

        if (!updating && updateSupPlan) {
            updating = true;
            Intent UpdateIntent = new Intent(MainActivity.this, UpdatePlanData.class);
            MainActivity.this.startService(UpdateIntent);
            MainActivity.mSwipeRefreshLayout.setRefreshing(true);
        }
        setupInterstitial();

        // ALTE APP
        if (appInstalledOrNot("de.gymnasium_wuerselen.vertretungsplan")) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("Alte Version gefunden!")
                    .setMessage("Eine alte Version der App ist bereits auf diesem System installiert. Wollen sie diese deinstallieren?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Intent intent = new Intent(Intent.ACTION_DELETE);
                            intent.setData(Uri.parse("package:de.gymnasium_wuerselen.vertretungsplan"));
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            builder.create().show();
        }
        //setupWifi("Penelope", "tjMT8BxYPYyfwCuK1UYL");
        setupPermissions();

        // TUTORIAL
        /*
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Einführung")
                .setMessage("Möchtest du eine kurze Einführung in die Grundfunktionen dieser App erhalten?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Tutorial();
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create().show();*/
    }

    private void setupPermissions() {
        permissionManager = new PermissionManager();
        PermissionRequest request = new PermissionRequest(Manifest.permission.CHANGE_WIFI_STATE, "W-lan einrichten", "Um das Schul-Wlan automatisch einzurichten wird eine Berechtigung benötigt!") {

            @Override
            public void onPermissionAccepted() {

            }

            @Override
            public void onPermissionDenied() {

            }

        };

        permissionManager.requestPermission(request, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        /*
            App-ID: ca-app-pub-1455056966789635~1937474304
            Anzeigenblock-ID: ca-app-pub-1455056966789635/6688448196
         */
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

    public static void updateDate() {

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
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
            return true;
        } else if (id == R.id.action_credits) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            String version = "4.0.4";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            } catch (Exception e) {

            }
            String text = "<br /><b>Vertretungsplanapp</b><br />Version: " + version + "<br /><br />Entwickelt von:<br /><b>Max Nuglisch</b><br /><br />Design & Logo:<br /><b>Jonas Leuchtenberger</b><br /><br />Veröffentlicht von:<br /><b>CreatePlus GmbH.</b><br /><br />Website:<br /><b>createplus.de</b><br />";
            TextView myMsg = new TextView(this);
            myMsg.setTextSize(20);
            myMsg.setText(Html.fromHtml(text));
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            builder.setView(myMsg);
            builder.create().show();
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

        } else if (id == R.id.nav_Credits) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            String version = "4.0.4";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            } catch (Exception e) {

            }
            String text = "<br /><b>Vertretungsplanapp</b><br />Version: " + version + "<br /><br />Entwickelt von:<br /><b>Max Nuglisch</b><br /><br />Design & Logo:<br /><b>Jonas Leuchtenberger</b><br /><br />Veröffentlicht von:<br /><b>CreatePlus GmbH.</b><br /><br />Website:<br /><b>createplus.de</b><br />";
            TextView myMsg = new TextView(this);
            myMsg.setTextSize(20);
            myMsg.setText(Html.fromHtml(text));
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            builder.setView(myMsg);
            builder.create().show();
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
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!updating) {
                        updating = true;
                        Intent mServiceIntent = new Intent(MainActivity.this, UpdatePlanData.class);
                        MainActivity.this.startService(mServiceIntent);
                    }
                }
            });

            //findViewById(R.id.fab).setVisibility(View.VISIBLE);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean teacherMode = sharedPref.getBoolean(SettingsActivity.KEY_TEACHERMODE, false);
            if (teacherMode) {
                TextView TitleToday1 = (TextView) findViewById(R.id.overview_textview_teacher);
                TitleToday1.setText("Die Übersicht ist im Lehrer Modus nicht vorhanden.");
                LinearLayout layout = (LinearLayout) findViewById(R.id.overview_layout);
                layout.setVisibility(View.INVISIBLE);
            } else {
                TextView TitleToday1 = (TextView) findViewById(R.id.overview_textview_teacher);
                TitleToday1.setVisibility(View.INVISIBLE);
                setupOverview();
            }
            /*android.support.v4.widget.SwipeRefreshLayout l = (android.support.v4.widget.SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            l.bringToFront();*/
            //WebView web = (WebView) findViewById(R.id.overview_webview);
            //web.loadData("<html>    <link href=\"https://fonts.googleapis.com/css?family=Source+Sans+Pro\" rel=\"stylesheet\">    <link href=\"https://fonts.googleapis.com/css?family=Open+Sans\" rel=\"stylesheet\">    <style>        .coupon {            width: 250px;            padding: 10px;            text-align: center;            border: 3px dashed #0099ff; }        .t {            font-family: 'Source Sans Pro', sans-serif; }        .z {            font-family: 'Open Sans', sans-serif; }    </style>    <table cellspacing=\"10\" class=\"t\"><tr class=\"z\"><th>Heute: Montag,<br>06.03.2017</th></tr><tr><td><table class=\"coupon\"><tr><td><sup style=\"font-size:10px\">1</sup>/<sub style=\"font-size:10px\">2</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr><tr><td><sup style=\"font-size:10px\">3</sup>/<sub style=\"font-size:10px\">4</sub></td><td style=\"color: #33cc33\">Ge1</td><td style=\"color: #33cc33\">Wem</td><td style=\"color: #33cc33\">403</td></tr><tr><td><sup style=\"font-size:10px\">5</sup>/<sub style=\"font-size:10px\">6</sub></td><td>E3</td><td>Ad</td><td>402</td></tr><tr><td><sup style=\"font-size:10px\">7</sup>/<sub style=\"font-size:10px\">8</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr></table></td></tr><tr class=\"z\"><th><br>Morgen: Dienstag,<br>07.03.2017</th></tr><tr><td><table class=\"coupon\"><tr><td><sup style=\"font-size:10px\">1</sup>/<sub style=\"font-size:10px\">2</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr><tr><td><sup style=\"font-size:10px\">3</sup>/<sub style=\"font-size:10px\">4</sub></td><td style=\"color: #33cc33\">Ge1</td><td style=\"color: #33cc33\">Wem</td><td style=\"color: #33cc33\">403</td></tr><tr><td><sup style=\"font-size:10px\">5</sup>/<sub style=\"font-size:10px\">6</sub></td><td>E3</td><td>Ad</td><td>402</td></tr><tr><td><sup style=\"font-size:10px\">7</sup>/<sub style=\"font-size:10px\">8</sub></td><td>D1</td><td>Kru</td><td>A06</td></tr></table></td></tr></table> </html>\n", "text/html", null);
            //ADS
            loadBanner(0);
        } else if (currentContent == ContentViews.SUBSTITUTIONPLAN) {
            setTitle("Vertretungsplan");
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!updating) {
                        updating = true;
                        Intent mServiceIntent = new Intent(MainActivity.this, UpdatePlanData.class);
                        MainActivity.this.startService(mServiceIntent);
                    }
                }
            });
            updateDate();
            findViewById(R.id.subplan_textfield).setVisibility(View.INVISIBLE);
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);
            SPDatabaseHelper db = new SPDatabaseHelper(this);
            if (CurrentShown == 1) {
                ArrayList<Parent> Plan = db.getPlan(TodayDate, this);
                if (Plan.size() < 1)
                    findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                mExpandableList.setAdapter(new MyCustomAdapter(this, Plan));
                mExpandableList.setVisibility(View.VISIBLE);
            } else {
                ArrayList<Parent> Plan = db.getPlan(TomorrowDate, this);
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

                        ArrayList<Parent> Plan = db.getPlan(TomorrowDate, MainActivity.this);
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

                        ArrayList<Parent> Plan = db.getPlan(TodayDate, MainActivity.this);
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
            //TABLE.loadData(tmp, "text/html", "UTF-8");
            TABLE.loadDataWithBaseURL("fake://fake.de", tmp + "<html><br/><br/><br/><br/><br/></html>", "text/html", "UTF-8", null);
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
        int shownHours = 0;


        int tiltesize = height / 83;//23
        int textsize = height / 106;//18
        if (tiltesize < 20) {
            tiltesize = 20;
        }
        if (textsize < 16) {
            textsize = 16;
        }
        int padding = 20;
        switch (tiltesize) {
            case 18:
                padding = 20;
                break;
            case 17:
                padding = 14;
                break;
            case 16:
                padding = 10;
                break;
        }


        Log.e("setupOverview", tiltesize + "");
        Log.e("setupOverview", textsize + "");
        TableLayout LayoutToday = (TableLayout) findViewById(R.id.overview_tablelayout_inner_A);
        TableLayout LayoutTomorrow = (TableLayout) findViewById(R.id.overview_tablelayout_inner_B);
        //TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
        TplanDatabaseHelper dbplan = new TplanDatabaseHelper(getApplicationContext());
        LinkedList<String[]> FullTimetable = dbplan.getPlan(getApplicationContext());

        TextView TitleToday1 = (TextView) findViewById(R.id.overview_textview_titleA1);
        TextView TitleToday2 = (TextView) findViewById(R.id.overview_textview_titleA2);
        TitleToday1.setText(days[TodayDay] + ",");
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
        //System.out.println(Arrays.toString(TodayEntries));

        for (int i = 1; i < TodayEntries.length; i++) {
            if (TodayEntries[i] != null) {
                TableRow tr = new TableRow(MainActivity.this);
                String[] tmp = TodayEntries[i].split(" ");
                SPDatabaseHelper db = new SPDatabaseHelper(MainActivity.this);
                ThtmlDatabaseHelper dbFORCLASS = new ThtmlDatabaseHelper(this);
                final LinkedList<String[]> spplan = db.getSPinfo(dbFORCLASS.getSavedClass(), tmp[0], TodayDate, i);
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

                if ((i + 1) < TodayEntries.length && TodayEntries[i + 1] != null) {
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(padding, padding, padding, padding);
                    two.setText(i + "");
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        if (tmp[o].equals(" ") || tmp[o].equals("")) o++;
                        TextView tw = new TextView(MainActivity.this);
                        tw.setPadding(padding, padding, padding, padding);
                        tw.setText(tmp[o].replace(" ", ""));
                        tw.setTextSize(textsize);
                        tw.setTextColor(getChangeColor(changetype));
                        tr.addView(tw);
                        shownHours++;
                    }
                } else {


                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(padding, padding, padding, padding);
                    two.setText(i + "/" + (i + 1));
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        if (tmp[o].equals(" ") || tmp[o].equals("")) o++;
                        TextView tw = new TextView(MainActivity.this);
                        tw.setTextColor(Color.BLACK);
                        tw.setPadding(padding, padding, padding, padding);

                        tw.setTextSize(textsize);
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
                        shownHours++;
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

                LayoutToday.addView(tr);
            }

        }

        //----------------------------------------------------------------------------
        TextView TitleTomorrow1 = (TextView) findViewById(R.id.overview_textview_titleB1);
        TextView TitleTomorrow2 = (TextView) findViewById(R.id.overview_textview_titleB2);
        TitleTomorrow1.setText(days[TomorrowDay] + ",");
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
                    TomorrowEntries[Integer.parseInt(currentItem[2])] = currentItem[4] + " " + currentItem[5] + " " + currentItem[6];
                }
            }
        }
        //System.out.println(Arrays.toString(TomorrowEntries));
        //Log.e("setupOverview","-------------------------------------");
        for (int i = 1; i < TomorrowEntries.length; i++) {

            if (TomorrowEntries[i] != null) {
                TableRow tr = new TableRow(MainActivity.this);
                String[] tmp = TomorrowEntries[i].split(" ");
                SPDatabaseHelper db = new SPDatabaseHelper(MainActivity.this);
                ThtmlDatabaseHelper dbFORCLASS = new ThtmlDatabaseHelper(this);
                final LinkedList<String[]> spplan = db.getSPinfo(dbFORCLASS.getSavedClass(), tmp[0], TomorrowDate, i);
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
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(padding, padding, padding, padding);
                    two.setText(i + "");
                    two.setTextSize(textsize);
                    tr.addView(two);

                    for (int o = 0; o < tmp.length; o++) {
                        if (tmp[o].equals(" ") || tmp[o].equals("")) o++;
                        //Log.e("O", "|" + tmp[o] + "|");
                        TextView tw = new TextView(MainActivity.this);
                        tw.setPadding(padding, padding, padding, padding);
                        tw.setText(tmp[o].replace(" ", ""));
                        tw.setTextSize(textsize);
                        tr.addView(tw);
                        shownHours++;
                    }
                } else {
                    TextView two = new TextView(MainActivity.this);
                    two.setPadding(padding, padding, padding, padding);
                    two.setText(i + "/" + (i + 1));
                    two.setTextSize(textsize);
                    tr.addView(two);
                    for (int o = 0; o < tmp.length; o++) {
                        if (tmp[o].equals(" ") || tmp[o].equals("")) o++;
                        TextView tw = new TextView(MainActivity.this);
                        tw.setTextColor(Color.BLACK);
                        tw.setPadding(padding, padding, padding, padding);
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
                        tw.setTextSize(textsize);
                        tr.addView(tw);
                        shownHours++;
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
        }
        if (shownHours == 0) {
            TextView hi = (TextView) findViewById(R.id.overview_textview_teacher);
            hi.setText("Um die Übersicht zu benutzen musst du in den Einstellungen deine Kurse auswählen.");
            hi.setVisibility(View.VISIBLE);
            LinearLayout layout = (LinearLayout) findViewById(R.id.overview_layout);
            layout.setVisibility(View.INVISIBLE);
        }
    }

    private void loadBanner(int i) {

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

    private void onClickOverview(String[] sp, int change) { //retin[0] = KIND; retin[1] = HOUR; retin[2] = ROOM; retin[3] = DATE; retin[4] = NEWROOM; retin[5] = TEXT;change 0 = Nichts, 1 = Vertretung, 2 = Raumwechsel, 3 = Entfall, 4 = Unbekannt
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        String Title = "";
        String Text = "";


        switch (change) {
            case 1:
                Title = "Vertretung:";
                Text = "<b>In Raum:</b> " + sp[4];
                break;
            case 2:
                Title = "Raumwechsel:";
                Text = "<b>Alter Raum:</b> " + sp[2] + "<br/><b>Neuer Raum:</b> " + sp[4];
                break;
            case 3:
                Title = "Entfall:";
                break;
            case 4:
                Title = sp[0];
                Text = "<b>Alter Raum:</b> " + sp[2] + "<br/><b>Neuer Raum:</b> " + sp[4];
                break;
        }
        //Log.e("setupOverview", "|"+sp[5].replace(" ","")+"|");
        if (sp[5].replace(" ", "").length() > 2) {
            Text = Text + "<br/><b>Infos:</b> " + sp[5];
        }
        if (Text.replace(" ", "").equals("")) {
            Text = "Keine weiteren Informationnen vorhanden.";
        }
        dlgAlert.setMessage(Html.fromHtml(Text));
        dlgAlert.setTitle(Title);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private int getChangeColor(int type) {// 0 = Nichts, 1 = Vertretung, 2 = Raumwechsel, 3 = Entfall, 4 = Unbekannt
        switch (type) {
            case 0:
                return Color.DKGRAY;
            case 1:
                return Color.CYAN;
            case 2:
                return Color.BLUE;
            case 3:
                return Color.GREEN;
            case 4:
                return Color.BLACK;
        }
        return Color.DKGRAY;
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    private void checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {

            // TODO This is a new install (or the user cleared the shared preferences)

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    public void Tutorial() {
        SliderBtn = new Target() {
            @Override
            public Point getPoint() {
                return new Point(width / 10, height / 10);
            }
        };

        SliderMainAreas = new Target() {
            @Override
            public Point getPoint() {
                return new Point(width / 10, (int) (height * 0.7));
            }
        };

        Settings = new Target() {
            @Override
            public Point getPoint() {
                return new Point((int) (width * 0.25), (int) (height * 0.72));
            }
        };

        ClassSetting = new Target() {
            @Override
            public Point getPoint() {
                return new Point(50, 70);
            }
        };

        tutTextSilderBtn = "Hauptmenü//Das Hauptmenü kannst du durch diesen Button oder durch wischen nach rechts erreichen.";
        tutTextSliderMainAreas = "Einstellungen//Um die App benutzen zu können muss du erst einige Einstellungen tätigen."; // not Setup
        tutTextSettings = "Einstellungen//Um die App benutzen zu können muss du erst einige Einstellungen tätigen.";
        tutTextClass = "D//4";
        inTut = true;


        showcaseview = new ShowcaseView.Builder(this).setTarget(Target.NONE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*switch (currentShowcase){
                    case 1: currentShowcase=2; showcaseview.setShowcase(SliderMainAreas,true);break;
                    case 2: break;
                    case 3: break;
                }*/
                //showcaseview.setShowcase(Target.NONE,false);
                showcaseview.hide();
            }
        }).setContentTitle("Test").setContentText("Dies ist ein Test").setStyle(R.style.AppTheme_AppBarOverlay).build();
        setShowCase(1);
        showcaseview.setButtonText("Abbrechen");
    }

    public void setShowCase(int i) {// 1=SliderBtn 2=SliderMainAreas
        switch (i) {
            case 1:
                currentShowcase = 1;
                showcaseview.setShowcase(SliderBtn, true);
                showcaseview.setContentText(tutTextSilderBtn.split("//")[1]);
                showcaseview.setContentTitle(tutTextSilderBtn.split("//")[0]);
                break;
            case 2:
                currentShowcase = 2;
                showcaseview.setShowcase(SliderMainAreas, true);
                showcaseview.setContentText(tutTextSliderMainAreas.split("//")[1]);
                showcaseview.setContentTitle(tutTextSliderMainAreas.split("//")[0]);
                break;
            case 3:
                currentShowcase = 3;
                showcaseview.setShowcase(Settings, true);
                showcaseview.setContentText(tutTextSettings.split("//")[1]);
                showcaseview.setContentTitle(tutTextSettings.split("//")[0]);
                break;
        }
    }

    public void setupWifi(String networkSSID, String networkPass) {
        /*

        NOT SAVE 2 USE. CHECK IF WIFI IS ENABLED ELSE DONT RUN.

         */

        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.getApplicationContext().WIFI_SERVICE);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        boolean exsists = false;
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                exsists = true;
                Log.e("WIFI", "Wifi already connected!");
                break;
            }
        }
        if (!exsists) {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";

            conf.wepKeys[0] = "\"" + networkPass + "\"";
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

            conf.preSharedKey = "\"" + networkPass + "\"";

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


            wifiManager.addNetwork(conf);
        }

    }
}
