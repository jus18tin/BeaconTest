package com.example.junhuali.beaconfromrn;

import android.support.v7.app.AppCompatActivity;

import org.altbeacon.beacon.BeaconManager;

public abstract class BaseActivity extends AppCompatActivity {

    private static String TAG = "BaseActivity";

    protected BeaconManager mBeaconManager;
    protected BeaconLocalManager mBeaconLocalManager = null;
    protected static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.setMonitorActivity(this);
        bindBeacon();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.setMonitorActivity(null);
        unbindBeacon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeBeaconService();
    }

    public void bindBeacon(){}

    public void unbindBeacon(){}

    public void removeBeaconService(){}
}
