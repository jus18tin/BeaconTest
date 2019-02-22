package com.example.junhuali.beaconfromrn;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.ArrayList;
import java.util.List;

import static com.example.junhuali.beaconfromrn.BeaconLocalManager.REGION_UUID;

public class MyApplication extends Application implements BootstrapNotifier {

    private static final String TAG = "BeaconsAndroidModule";

    public static Activity monitoringActivity = null;

    private static final String PUSH_CHANNEL_NAME = "IBEACON_CHANNEL";
    private static final String PUSH_CHANNEL_ID = "1";
    private static final int PUSH_GROUP_ID = 0;
    private static String PUSH_GROUP_PATH = "";

    private static int mNotificationId = 1;
    private static String mPrevNotificationType = "";

    private static List<String> beacons = new ArrayList<String>();

    public void onCreate() {
        super.onCreate();

        //バックスキャンのディレイを設定する
        BeaconLocalManager.newInstance(this).setBackgroundDelay();

        //バックスキャンを登録する
        BeaconLocalManager.newInstance(this).enableBackgroundMonitoring(this);

        //バッテリーセーブする
        new BackgroundPowerSaver(this);

        //通知のグルプを設定する
        PUSH_GROUP_PATH = getPackageName();

        //ダミービーコンUUIDを登録する
        beacons.add(REGION_UUID.toLowerCase());
    }

    @Override
    public void didEnterRegion(Region region) {
        try {
            if (onCheckBecons(region)) {
                if (monitoringActivity != null) {
                    sendNotification("  Running.. Activity : Enter", region);
                } else {
                    sendNotification("  Not yet run Activity : Enter", region);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didExitRegion(Region region) {
        try {
            if (onCheckBecons(region)) {
                if (monitoringActivity != null) {
                    sendNotification(" Running.. Activity : Exit", region);
                } else {
                    sendNotification(" Not yet run Activity : Exit", region);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
//        Logger.d(TAG, "==Application:Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE (" + state + ")"));
    }

    /**
     * Activityの状態を保存する
     * */
    public static void setMonitorActivity(Activity activity) {
        monitoringActivity = activity;
    }

    /**
     *  ビーコンリストと取ったビーコンと比較して返す
     */
    private boolean onCheckBecons(Region region) {
        try {
            if (region != null && region.getId1() != null) {
                if (beacons != null && beacons.size() > 0) {
                    for (String beaconUuid : beacons) {
                        if (beaconUuid.equals(region.getId1().toString())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 通知を作成
     */
    private void sendNotification(String type, Region region) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!type.equals(mPrevNotificationType)) {
            mNotificationId++;
            mPrevNotificationType = type;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(PUSH_CHANNEL_ID, PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);

            // 通知-設定する
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PUSH_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setGroup(PUSH_GROUP_PATH)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setContentTitle("iBeacon oreo" + type)
                    .setContentText("UUID : " + region.getId1().toString())
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVibrate(new long[]{0});
            }

            NotificationManagerCompat.from(this).notify(mNotificationId, builder.build());
            startForegroundService(notifyIntent);

        } else {
            // 通知-設定する
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PUSH_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setContentTitle("iBeacon !oreo" + type)
                    .setGroup(PUSH_GROUP_PATH)
                    .setContentText("UUID : " + region.getId1().toString())
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVibrate(new long[]{0});
            }
            //notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
            NotificationManagerCompat.from(this).notify(mNotificationId, builder.build());
        }

        // 通知ーGrouping
        NotificationCompat.Builder builderGroup = new NotificationCompat.Builder(this, PUSH_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setGroup(PUSH_GROUP_PATH)
                .setContentTitle("iBeacon Group")
                .setWhen(System.currentTimeMillis())
                .setGroupSummary(true);
        NotificationManagerCompat.from(this).notify(PUSH_GROUP_ID, builderGroup.build());

        try {
            BeaconLocalManager.newInstance(this).getBeaconManager().updateScanPeriods();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

