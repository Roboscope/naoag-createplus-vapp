package de.createplus.vertretungsplan.permissionMngr;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by Max Nuglisch on 19.04.2017.
 */
public abstract class PermissionRequest {
    private String Permission;
    private int PermissionKey;
    private boolean explinationPopUp = false;
    private String explinationText = "";
    private String explinationTitle = "";

    /**
     * @param Permission The Android Permition to request
     */
    public PermissionRequest(String Permission) {
        this.Permission = Permission;
        PermissionKey = 7;
        for (int i = 0; i < Permission.length(); i++) {
            PermissionKey = PermissionKey * 31 + Permission.charAt(i);
        }
        explinationPopUp = false;
    }

    public PermissionRequest(String Permition, String explinationTitle, String explinationText) {
        this.Permission = Permition;
        PermissionKey = 7;
        for (int i = 0; i < Permition.length(); i++) {
            PermissionKey = PermissionKey * 31 + Permition.charAt(i);
        }
        this.explinationText = explinationText;
        this.explinationTitle = explinationTitle;
        explinationPopUp = true;
    }

    protected void request(Activity CurrentActivity) {
        if (explinationPopUp) {
            if (ContextCompat.checkSelfPermission(CurrentActivity, Permission) != PackageManager.PERMISSION_GRANTED) {

                final Activity CURRENTACTIVITY = CurrentActivity;
                AlertDialog.Builder builder = new AlertDialog.Builder(CURRENTACTIVITY);
                builder.setTitle(explinationTitle)
                        .setMessage(explinationText)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showPermissionDialog(CURRENTACTIVITY);
                            }
                        });
                builder.create().show();
            } else {
                onPermissionAccepted();
            }
        } else {
            if (ContextCompat.checkSelfPermission(CurrentActivity, Permission) != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog(CurrentActivity);
            } else {
                onPermissionAccepted();
            }
        }
    }

    public void showPermissionDialog(Activity CurrentActivity) {
        ActivityCompat.requestPermissions(CurrentActivity,
                new String[]{Permission},
                PermissionKey);
    }

    public abstract void onPermissionAccepted();

    public abstract void onPermissionDenied();

    public int getPermissionKey() {
        return PermissionKey;
    }

    public String getPermission() {
        return Permission;
    }

    public void setPermission(String permission) {
        Permission = permission;
    }

    public void setPermissionKey(int permissionKey) {
        PermissionKey = permissionKey;
    }

    public void setExplinationText(String explinationText) {
        this.explinationText = explinationText;
        explinationPopUp = true;
    }

    public void disableExplinationPopUp() {
        explinationPopUp = false;
    }

    public void enableExplinationPopUp() {
        explinationPopUp = false;
    }
}

