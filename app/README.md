# BeaconTest


## 機能

### monitorning
> アプリ起動開始・終了両方(入ると出る)のを監視。

### ranging
> アプリ起動中のみ情報を貰える(beacon name, address, uuid, meter)。


## 設定

### ranging ディレイ
<pre>
//再スキャン待ち時間。
BeaconManager.setForegroundBetweenScanPeriod(0l);

//アプリが起動中にScanPeriod値後で
//regionからメートルを貰える。
BeaconManager.setForegroundScanPeriod(5000l);
</pre>

### monitorning ディレイ
<pre>
//再スキャン待ち時間。
mBeaconManager.setBackgroundBetweenScanPeriod(0l);
//バックでアプリ起動中とか起動してない時スキャン時間 10m
mBeaconManager.setBackgroundScanPeriod(600000l);
</pre>

#### アプリ起動してない状況のバックスキャン
> 1. Android Oreo(8.0)からはsetBackgroundBetweenScanPeriodに最小値で15minを設定必要がある。
> 2. setBackgroundBetweenScanPeriodに最小値で設定してもOS的で増える時間が発生する。
> 3. 15min設定しても+10~50minまで増える時がある。
> 4. 反応しない時がある。

