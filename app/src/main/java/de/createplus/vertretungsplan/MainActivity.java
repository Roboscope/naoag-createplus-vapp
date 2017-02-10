package de.createplus.vertretungsplan;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.createplus.vertretungsplan.backgroundservices.SPDownloader;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanDataReceiver;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ContentViews currentContent = ContentViews.OVERVIEW;
    public String Plan = "Empty";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Setup Background Process

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
        //findViewById(R.id.fab).setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent mServiceIntent = new Intent(MainActivity.this, UpdatePlanData.class);
                //mServiceIntent.setData(Uri.parse(dataUrl));
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
            currentContent = ContentViews.SETTINGS;
            updateContainerContent();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void updateContainerContent(){
        //reset Container
        RelativeLayout content = (RelativeLayout) findViewById(R.id.main_content);
        while(content.getChildCount()>0){
            content.removeView(content.getChildAt(0));
        }
        findViewById(R.id.fab).setVisibility(View.INVISIBLE);

        //update Container
        content.addView(getLayoutInflater().inflate(currentContent.getId(),content,false), 0);
        if(currentContent == ContentViews.OVERVIEW){
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
        }else if(currentContent == ContentViews.SUBSTITUTIONPLAN){
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            TextView text = (TextView) findViewById(R.id.subplan_textfield);
            SPDatabaseHelper db = new SPDatabaseHelper(this);

            text.setText(db.test());
        }else if(currentContent == ContentViews.TIMETABLE){

        }else if(currentContent == ContentViews.SETTINGS){

        }
    }

    public String getPlan() {
        return Plan;
    }

    public void setPlan(String plan) {
        Plan = plan;
    }
}
