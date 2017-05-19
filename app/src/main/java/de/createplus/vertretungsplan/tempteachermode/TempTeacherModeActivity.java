package de.createplus.vertretungsplan.tempteachermode;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.backgroundservices.Pair;
import de.createplus.vertretungsplan.backgroundservices.Timetable;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanDataReceiver;
import de.createplus.vertretungsplan.backgroundservices.UpdateTempTeacherPlan;
import de.createplus.vertretungsplan.backgroundservices.UpdateTempTeacherPlanReceiver;
import de.createplus.vertretungsplan.config;

/**
 * Created by Max Nuglisch LOKAL on 15.05.2017.
 */

public class TempTeacherModeActivity extends AppCompatActivity {
    public static String teacher = "";
    public static TempTeacherModeActivity THIS;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_timetable);
        THIS = this;


        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilterPlanData = new IntentFilter(
                UpdateTempTeacherPlan.Constants.BROADCAST_ACTION);

        // Instantiates a new DownloadStateReceiver
        UpdateTempTeacherPlanReceiver mUpdatePlanDataReceiver =
                new UpdateTempTeacherPlanReceiver(this);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mUpdatePlanDataReceiver,
                statusIntentFilterPlanData);

        Intent mServiceIntent = new Intent(this, UpdateTempTeacherPlan.class);
        this.startService(mServiceIntent);
    }

    public void setContent(String html){
        WebView TABLE = (WebView) findViewById(R.id.timetable_table);
        TABLE.loadDataWithBaseURL("fake://fake.de", html, "text/html", "UTF-8", null);
        TABLE.getSettings().setBuiltInZoomControls(true);
        TABLE.getSettings().setDisplayZoomControls(false);
        TABLE.setLongClickable(false);
    }
}
