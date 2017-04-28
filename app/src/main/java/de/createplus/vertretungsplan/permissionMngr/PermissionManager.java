package de.createplus.vertretungsplan.permissionMngr;

/**
 * Created by Max Nuglisch on 21.04.2017.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Max Nuglisch on 19.04.2017.
 */

public class PermissionManager {
    List<PermissionRequest> requests = new ArrayList<PermissionRequest>();

    /**
     * @param request The Request to send.
     * @param CurrentActivity The Activity to show request
     */
    public void requestPermission(PermissionRequest request, Activity CurrentActivity) {
        requests.add(request);
        request.request(CurrentActivity);
    }

    /** The Method that is called by the Activity.
     *  Place this one in the @Override public void onRequestPermissionsResult(...){...}
     *  And transfer the Variables.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        for(int i = 0; i < requests.size(); i++){
            if(requestCode == requests.get(i).getPermissionKey()){
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requests.get(i).onPermissionAccepted();
                } else {
                    requests.get(i).onPermissionDenied();
                }
            }
        }
    }
}