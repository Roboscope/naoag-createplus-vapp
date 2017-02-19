package de.createplus.vertretungsplan;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicMarkableReference;

import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanDataReceiver;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
//import de.createplus.vertretungsplan.listview.MyCustomAdapter;
import de.createplus.vertretungsplan.listview.MyCustomAdapter;
import de.createplus.vertretungsplan.listview.Parent;
import de.createplus.vertretungsplan.settings.SettingsActivity;

import static android.Manifest.permission.INTERNET;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static private ContentViews currentContent = ContentViews.SUBSTITUTIONPLAN;


    static private int CurrentShown = 1;
    static public String TodayDate = "*ERROR*";
    static public String TodayDateString = "*ERROR*";
    static public String TomorrowDate = "*ERROR*";
    static public String TomorrowDateString = "*ERROR*";
    static final private int INTERNET_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar calander = Calendar.getInstance();

        int cDay = calander.get(Calendar.DAY_OF_MONTH);
        int cMonth = calander.get(Calendar.MONTH) + 1;
        int cYear = calander.get(Calendar.YEAR);
        TodayDate = cDay + "." + cMonth + "." + cYear;
        Log.e("DATE", "" + calander.get(Calendar.DAY_OF_WEEK));
        calander.add(Calendar.DATE, 1);
        while (!(calander.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY) && (calander.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY)){
            calander.add(Calendar.DATE, 1);
            Log.e("DATE", "added to:" + calander.get(Calendar.DAY_OF_WEEK));
        }

        cDay = calander.get(Calendar.DAY_OF_MONTH);
        cMonth = calander.get(Calendar.MONTH) + 1;
        cYear = calander.get(Calendar.YEAR);
        TomorrowDate = cDay + "." + cMonth + "." + cYear;


        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                UpdatePlanData.Constants.BROADCAST_ACTION);

        // Instantiates a new DownloadStateReceiver
        UpdatePlanDataReceiver mUpdatePlanDataReceiver =
                new UpdatePlanDataReceiver(this);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mUpdatePlanDataReceiver,
                statusIntentFilter);


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
            currentContent = ContentViews.TIMETABLE;
            updateContainerContent();
        } else if (id == R.id.nav_settings) {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_share) {

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
            findViewById(R.id.fab).setVisibility(View.VISIBLE);


        } else if (currentContent == ContentViews.SUBSTITUTIONPLAN) {
            findViewById(R.id.subplan_textfield).setVisibility(View.INVISIBLE);
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);
            SPDatabaseHelper db = new SPDatabaseHelper(this);
            if (CurrentShown == 1) {
                ArrayList<Parent> Plan = db.getPlan(TodayDate);
                if(Plan.size() < 1) findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                mExpandableList.setAdapter(new MyCustomAdapter(this, Plan));
                mExpandableList.setVisibility(View.VISIBLE);
            } else {
                ArrayList<Parent> Plan = db.getPlan(TomorrowDate);
                if(Plan.size() < 1) findViewById(R.id.subplan_textfield).setVisibility(View.VISIBLE);
                mExpandableList.setAdapter(new MyCustomAdapter(this, Plan));
                mExpandableList.setVisibility(View.VISIBLE);
            }

            //Setup SPPlan Toggle Button
            final Button SW = (Button) findViewById(R.id.sw);

            if (CurrentShown == 1) SW.setText(TodayDate);
            else SW.setText(TomorrowDate);

            SW.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (CurrentShown == 1) {
                        CurrentShown = 2;
                        SW.setText(TomorrowDate);
                        updateContainerContent();
                    } else {

                        SW.setText(TodayDate);
                        CurrentShown = 1;
                        updateContainerContent();
                    }
                }
            });
            db.close();


        } else if (currentContent == ContentViews.TIMETABLE) {

        } else if (currentContent == ContentViews.SETTINGS) {

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
}
