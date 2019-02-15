package com.example.junhuali.beaconfromrn;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    private static final String LOG_TAG = "BeaconsAndroidModule";
    private Context mApplicationContext;
    private BeaconManager mBeaconManager;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        this.mBeaconManager = BeaconManager.getInstanceForApplication(this);
        addParser("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24");

        mBeaconManager.bind(this);
    }

    public void addParser(String parser) {
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(parser));
    }

    public void startRanging(String regionId, String beaconUuid) {
        Log.d(LOG_TAG, "=====startRanging, rangingRegionId: " + regionId + ", rangingBeaconUuid: " + beaconUuid);
        try {
            Region region = createRegion(regionId, beaconUuid);
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception e) {
            Log.e(LOG_TAG, "=====startRanging, error: ", e);
        }
    }

    public void startMonitoring(String regionId, String beaconUuid, int minor, int major) {
        Log.d(LOG_TAG, "startMonitoring, monitoringRegionId: " + regionId + ", monitoringBeaconUuid: " + beaconUuid + ", minor: " + minor + ", major: " + major);
        try {
            Region region = createRegion(
                    regionId,
                    beaconUuid,
                    String.valueOf(minor).equals("-1") ? "" : String.valueOf(minor),
                    String.valueOf(major).equals("-1") ? "" : String.valueOf(major)
            );
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (Exception e) {
            Log.e(LOG_TAG, "startMonitoring, error: ", e);
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.v(LOG_TAG, "=====onBeaconServiceConnect");

        // deprecated since v2.9 (see github: https://github.com/AltBeacon/android-beacon-library/releases/tag/2.9)
        // mBeaconManager.setMonitorNotifier(mMonitorNotifier);
        // mBeaconManager.setRangeNotifier(mRangeNotifier);

        startMonitoring("jp.classmethod.testregion", "D456894A-02F0-4CB0-8258-81C187DF45C3", 2, 1);
        startRanging("jp.classmethod.testregion", "D456894A-02F0-4CB0-8258-81C187DF45C3");

        mBeaconManager.addMonitorNotifier(mMonitorNotifier);
        mBeaconManager.addRangeNotifier(mRangeNotifier);
    }

    private MonitorNotifier mMonitorNotifier = new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
            Log.d(LOG_TAG, "===== didEnterRegion, region: " + region.toString());
        }

        @Override
        public void didExitRegion(Region region) {
            Log.d(LOG_TAG, "===== didExitRegion, region: " + region.toString());
        }

        @Override
        public void didDetermineStateForRegion(int i, Region region) {
            Log.d(LOG_TAG, "===== didDetermineStateForRegion, region: " + region.toString());
        }
    };

    private RangeNotifier mRangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                Log.d(LOG_TAG, "=====rangingConsumer didRangeBeaconsInRegion, beacons: " + beacons.toString());
                Log.d(LOG_TAG, "=====rangingConsumer didRangeBeaconsInRegion, region: " + region.toString());
//            sendEvent(mReactContext, "beaconsDidRange", createRangingResponse(beacons, region));
            }
        }
    };

    private Region createRegion(String regionId, String beaconUuid) {
        Identifier id1 = (beaconUuid == null) ? null : Identifier.parse(beaconUuid);
        return new Region(regionId, id1, null, null);
    }

    private Region createRegion(String regionId, String beaconUuid, String minor, String major) {
        Identifier id1 = (beaconUuid == null) ? null : Identifier.parse(beaconUuid);
        return new Region(
                regionId,
                id1,
                major.length() > 0 ? Identifier.parse(major) : null,
                minor.length() > 0 ? Identifier.parse(minor) : null
        );
    }
}
