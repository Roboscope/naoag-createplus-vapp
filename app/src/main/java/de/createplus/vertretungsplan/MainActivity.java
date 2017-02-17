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
import java.util.Calendar;

import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanDataReceiver;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
//import de.createplus.vertretungsplan.listview.MyCustomAdapter;
import de.createplus.vertretungsplan.settings.SettingsActivity;

import static android.Manifest.permission.INTERNET;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static private ContentViews currentContent = ContentViews.SUBSTITUTIONPLAN;


    static private int CurrentShown = 1;
    static public String TodayDate = "*ERROR*";
    static public String TomorrowDate = "*ERROR*";
    static final private int INTERNET_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        askForPermission(INTERNET, INTERNET_REQUEST_CODE);

        while(){

        }

        Calendar calander = Calendar.getInstance();

        int cDay = calander.get(Calendar.DAY_OF_MONTH);
        int cMonth = calander.get(Calendar.MONTH) + 1;
        int cYear = calander.get(Calendar.YEAR);
        TodayDate = cDay + "." + cMonth + "." + cYear;

        calander.add(Calendar.DATE, 1);

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

        SPDatabaseHelper SPDbHelper = new SPDatabaseHelper(this);


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

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                //Internet
                case 1:
                    Intent IneternetIntent = new Intent(Intent.INETRNET);
                    callIntent.setData(Uri.parse("tel:" + "{This is a telephone number}"));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        startActivity(callIntent);
                    }
                    break;
            }

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
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
/*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.nav_overwiev).setChecked(false);
        menu.findItem(R.id.nav_timetable).setChecked(false);
        menu.findItem(R.id.nav_substitutionplan).setChecked(false);
        menu.findItem(R.id.nav_settings).setChecked(false);

        switch(currentContent){
            case OVERVIEW: menu.findItem(R.id.nav_overwiev).setChecked(true);
                break;
            case TIMETABLE: menu.findItem(R.id.nav_timetable).setChecked(true);
                break;
            case SUBSTITUTIONPLAN: menu.findItem(R.id.nav_substitutionplan).setChecked(true);
                break;
            case SETTINGS: menu.findItem(R.id.nav_settings).setChecked(true);
                break;
        }
        return true;
    }*/

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
            currentContent = ContentViews.SETTINGS;
            updateContainerContent();
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
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
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);
            //SPDatabaseHelper db = new SPDatabaseHelper(this);
            /*if(CurrentShown == 1){
                mExpandableList.setAdapter(new MyCustomAdapter(this, db.getPlan(TodayDate)));
                mExpandableList.setVisibility(View.VISIBLE);
            }else {
                mExpandableList.setAdapter(new MyCustomAdapter(this, db.getPlan(TomorrowDate)));
                mExpandableList.setVisibility(View.VISIBLE);
            }*/

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

            //TextView text = (TextView) findViewById(R.id.subplan_textfield);
            //SPDatabaseHelper db = new SPDatabaseHelper(this);

            //text.setText(db.test());
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
