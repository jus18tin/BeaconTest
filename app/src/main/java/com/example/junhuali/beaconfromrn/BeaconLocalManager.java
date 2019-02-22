package com.example.junhuali.beaconfromrn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class BeaconLocalManager {

    private static final String TAG = "BeaconLocalManager";
    /* レギオン ID */
    private static final String REGION_ID = "jp.classmethod.testregion";
    /* レギオン対象 UUID */
    public static final String REGION_UUID = "D456894A-02F0-4CB0-8258-81C187DF45C3";
    /* Beacon Format */
    private static final String IBEACON_FORMAT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    /* Wait 最小値 android version 8.0*/
    private static final long MIN_WAIT_TIME = 60 * 1000 * 15;
    /* Scan 最大値 android version 8.0*/
    private static final long MIN_BACKGROUND_SCAN = 60 * 1000 * 10;
    /* アプリ起動中のスキャンディレイ(設定した時間が過ぎるとRanging値を貰える) */
    private static final long MIN_FORGROUND_SCAN = 5 * 1000;
    /* Backgroundモニターニングする時宣言する*/
    private RegionBootstrap regionBootstrap;
    /* BeaconManager */
    private BeaconManager mBeaconManager = null;
    private Context mContext = null;
    public static BeaconLocalManager instance = null;

    /**
     * Construtor BeaconLocalManager
     *
     * @param _context
     */
    private BeaconLocalManager(Context _context) {
        this.mContext = _context;
        init();
    }

    /**
     * Singleton newInstance
     *
     * @param _context
     */
    public static BeaconLocalManager newInstance(Context _context) {
        if (instance == null) {
            instance = new BeaconLocalManager(_context);
        }
        return instance;
    }

    /**
     * BeaconManagerを初期化する
     */
    private void init() {
        this.mBeaconManager = BeaconManager.getInstanceForApplication(mContext);
        addParser(IBEACON_FORMAT);
        if (BuildConfig.DEBUG) {
            mBeaconManager.setDebug(true);
        }
        mBeaconManager.setBackgroundMode(true);
    }

    /**
     * ランギングのレギオンを返す
     */
    private Region getRangingRegion(String uuid) {
        return createRegion(REGION_ID, (uuid != null && uuid.length() > 0) ? uuid : REGION_UUID);
    }

    /**
     * モニターニングのレギオンを返す
     */
    private Region getMonitorRegion(String uuid) {
        int minor = 2;
        int major = 1;
        return createRegion(REGION_ID,
                (uuid != null && uuid.length() > 0) ? uuid : REGION_UUID,
                String.valueOf(minor).equals("-1") ? "" : String.valueOf(minor),
                String.valueOf(major).equals("-1") ? "" : String.valueOf(major));
    }

    /**
     * AltBeacon Formatを設定する
     *
     * @param beaconFormat
     */
    private void addParser(String beaconFormat) {
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(beaconFormat));
    }

    /**
     * バックモニターニングクリア
     */
    private void disableBackgroundMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }

    /**
     * バックでモニター二ングを監視
     * <p>
     * アプリが起動しない時とビーコンの範囲でEnter/Exitを監視
     *
     * @param bootstrapNotifier
     */
    public void enableBackgroundMonitoring(BootstrapNotifier bootstrapNotifier) {
        disableBackgroundMonitoring();
        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(bootstrapNotifier, region);
    }

    /**
     * モニターニングを開始
     * <p>
     * アプリが起動中とビーコンの範囲でEnter/Exitを監視
     */
    public void startMonitoring(String uuid) {
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(getMonitorRegion(uuid));
        } catch (Exception e) {
            Logger.e(TAG, "startMonitoring, error: ", e);
        }
    }

    /**
     * モニターニングを中止
     *
     * @history not working..
     */
    public void stopMonitoring(String uuid) {
        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(getMonitorRegion(uuid));
        } catch (Exception e) {
            Logger.e(TAG, "stopMonitoring, error: ", e);
        }
    }

    /**
     * ランギングンを開始
     * <p>
     * アプリが起動中とビーコンの範囲で{meter,uuid,address,name}情報を貰える
     */
    public void startRanging(String uuid) {
        try {
            mBeaconManager.startRangingBeaconsInRegion(getRangingRegion(uuid));
        } catch (Exception e) {
            Logger.e(TAG, "startRanging, error: ", e);
        }
    }

    /**
     * ランギングンを中止
     *
     * @history Not working..
     */
    public void stopRanging(String uuid) {
        try {
            mBeaconManager.stopRangingBeaconsInRegion(getRangingRegion(uuid));
        } catch (Exception e) {
            Logger.e(TAG, "stopRanging, error: ", e);
        }
    }

    /**
     * ビーコンサービスを開始
     */
    public void startBeaconServiceAll(String uuid) {
        startMonitoring(uuid);
        startRanging(uuid);
    }

    /**
     * アプリを終了した時ビーコンサービスを全部中止
     */
    public void stopBeaconServiceAll() {
        mBeaconManager.removeAllMonitorNotifiers();
        mBeaconManager.removeAllRangeNotifiers();
    }

    /**
     * ランギングのレギオンバタンを作成
     */
    public Region createRegion(String regionId, String beaconUuid) {
        Identifier id1 = (beaconUuid == null) ? null : Identifier.parse(beaconUuid);
        return new Region(regionId, id1, null, null);
    }

    /**
     * モニターニングのレギオンバタンを作成
     */
    public Region createRegion(String regionId, String beaconUuid, String minor, String major) {
        Identifier id1 = (beaconUuid == null) ? null : Identifier.parse(beaconUuid);
        return new Region(
                regionId,
                id1,
                major.length() > 0 ? Identifier.parse(major) : null,
                minor.length() > 0 ? Identifier.parse(minor) : null
        );
    }

    /**
     * ビーコンマネジャーを返す
     */
    public BeaconManager getBeaconManager() {
        return mBeaconManager;
    }

    /**
     * バッグモニターニングのスキャンディレイ
     */
    public void setBackgroundDelay() {
        //scan wait
        mBeaconManager.setBackgroundBetweenScanPeriod(0l);
        //scan cycle
        mBeaconManager.setBackgroundScanPeriod(MIN_BACKGROUND_SCAN);
    }

    /**
     * 　Forgroundのスキャンディレイ
     */
    public void setForgroundDelay() {
        mBeaconManager.setForegroundBetweenScanPeriod(0l);
        //アプリが起動中にScanPeriod値後でregionからメートルを貰える。
        mBeaconManager.setForegroundScanPeriod(MIN_FORGROUND_SCAN);
        try {
            mBeaconManager.updateScanPeriods();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bluetooth On/Offを返す
     *
     * @return boolean
     */
    public boolean isBluetoothOn() {
        BluetoothManager btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();
        return btAdapter != null && btAdapter.isEnabled();
    }

}
