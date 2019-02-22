package com.example.junhuali.beaconfromrn;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BeaconOnOffActivity extends BaseActivity implements BeaconConsumer, View.OnClickListener {

    private static final String TAG = BeaconOnOffActivity.class.getSimpleName();

    /* Ui State */
    public static final int BEACON_STATE_START = 1;
    public static final int BEACON_STATE_END = 0;
    public static final int BEACON_RUN_DISTANCE = 2;
    public static final int BEACON_RUN_STATUS = 3;

    private TextView mTvTitle, mTvTitle2;
    private Button mStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bc_onoff);

        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mTvTitle2 = (TextView) findViewById(R.id.tvTitle2);
        mStartBtn = (Button) findViewById(R.id.btnStart);

        mStartBtn.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mBeaconLocalManager = BeaconLocalManager.newInstance(this);
        mBeaconManager = mBeaconLocalManager.getBeaconManager();
    }

    @Override
    public void bindBeacon() {
        mBeaconManager.bind(this);
    }

    @Override
    public void unbindBeacon() {
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconLocalManager.setForgroundDelay();
        mBeaconManager.addMonitorNotifier(mMonitorNotifier);
        mBeaconManager.addRangeNotifier(mRangeNotifier);
    }

    private MonitorNotifier mMonitorNotifier = new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
            sendMsg(BEACON_RUN_STATUS, "Enter");
        }

        @Override
        public void didExitRegion(Region region) {
            sendMsg(BEACON_RUN_STATUS, "Exit");
        }

        @Override
        public void didDetermineStateForRegion(int state, Region region) {
        }
    };

    private RangeNotifier mRangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                for (Beacon beacon : beacons) {
                    sendMsg(BEACON_RUN_DISTANCE, String.valueOf(beacon.getDistance()));
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                boolean isStartOn = true;
                if (!mBeaconLocalManager.isBluetoothOn()) {
                    isStartOn = false;
                    showInformationMessage(BeaconOnOffActivity.this, "Enable Bluetooth", "Please enable bluetooth before check in.",
                            false, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == DialogInterface.BUTTON_POSITIVE) {
                                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableIntent, 1);
                                    }
                                }
                            });
                }

                if (isStartOn) {
                    sendMsg(BEACON_STATE_START, null);
                }
                break;
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BEACON_STATE_START:
                    mBeaconLocalManager.startBeaconServiceAll(null);
                    mTvTitle2.setText("meta : 0.0");
                    break;
                case BEACON_STATE_END:
                    mTvTitle.setText("Monitor");
                    mTvTitle2.setText("meta : 0.0");
                    break;
                case BEACON_RUN_DISTANCE:
                    String distance = (String) msg.obj;
                    mTvTitle2.setText("meta : " + distance);
                    break;
                case BEACON_RUN_STATUS:
                    String status = (String) msg.obj;
                    mTvTitle.setText("monitor status : " + status);
                    break;
            }
        }
    };

    private void sendMsg(int what, Object data) {
        Message msg = new Message();
        msg.what = what;
        if (data != null) {
            msg.obj = data;
        }
        mHandler.sendMessage(msg);
    }

    public void showInformationMessage(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, listener);
        builder.setCancelable(cancelable);
        builder.show();
    }
}
