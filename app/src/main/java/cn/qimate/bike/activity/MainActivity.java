package cn.qimate.bike.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.bumptech.glide.Glide;
import com.sunshine.blelibrary.config.Config;
import com.sunshine.blelibrary.config.LockType;
import com.sunshine.blelibrary.inter.OnConnectionListener;
import com.sunshine.blelibrary.inter.OnDeviceSearchListener;
import com.sunshine.blelibrary.utils.GlobalParameterUtils;
import com.zxing.lib.scaner.activity.ActivityScanerCode;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.jpush.android.api.JPushInterface;
import cn.loopj.android.http.RequestParams;
import cn.loopj.android.http.TextHttpResponseHandler;
import cn.qimate.bike.R;
import cn.qimate.bike.base.BaseApplication;
import cn.qimate.bike.base.BaseFragmentActivity;
import cn.qimate.bike.core.common.AppManager;
import cn.qimate.bike.core.common.DisplayUtil;
import cn.qimate.bike.core.common.HttpHelper;
import cn.qimate.bike.core.common.SharedPreferencesUrls;
import cn.qimate.bike.core.common.UIHelper;
import cn.qimate.bike.core.common.UpdateManager;
import cn.qimate.bike.core.common.Urls;
import cn.qimate.bike.core.widget.CustomDialog;
import cn.qimate.bike.core.widget.LoadingDialog;
import cn.qimate.bike.lock.utils.ToastUtils;
import cn.qimate.bike.model.CardinfoBean;
import cn.qimate.bike.model.CurRoadBikingBean;
import cn.qimate.bike.model.NearbyBean;
import cn.qimate.bike.model.ResultConsel;
import cn.qimate.bike.service.MyService;
import cn.qimate.bike.util.PublicWay;
import cn.qimate.bike.util.SHA1;
import cn.qimate.bike.util.SystemUtil;
import cn.qimate.bike.util.ToastUtil;
import cn.qimate.bike.util.UtilAnim;
import cn.qimate.bike.util.UtilBitmap;
import cn.qimate.bike.util.UtilScreenCapture;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static cn.qimate.bike.core.common.Urls.schoolrangeList;
import static com.umeng.analytics.AnalyticsConfig.getLocation;

@SuppressLint("NewApi")
public class MainActivity extends BaseFragmentActivity implements OnClickListener,LocationSource,
		AMapLocationListener
		,AMap.OnCameraChangeListener
		,AMap.OnMapTouchListener
{

	static private final int REQUEST_CODE_ASK_PERMISSIONS = 101;
	private final static int SCANNIN_GREQUEST_CODE = 1;
	private LoadingDialog lockLoading;
	private LoadingDialog loadingDialog;
	private LoadingDialog loadingDialog1;
	public static boolean isForeground = false;

	private ImageView leftBtn,rightBtn;
	private ImageView myLocationBtn,scanLock,linkBtn;
    private LinearLayout myLocationLayout,linkLayout;

//	private AMap aMap;
	private MapView mapView;
//	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;

	private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
	private boolean mFirstFix = true;
	private LatLng myLocation = null;
	private Circle mCircle;

	private BitmapDescriptor bikeDescripter;
	private Handler handler = new Handler();
	private Marker centerMarker;
	private boolean isMovingMarker = false;
	private Button authBtn;
	private Button rechargeBtn;
	private int Tag = 0;

	private Dialog dialog;
	private View dialogView;

	private ImageView titleImage;
	private ImageView exImage_1;
	private ImageView exImage_2;
	private ImageView exImage_3;

	private LinearLayout marqueeLayout;
	private ImageView closeBtn;
	private Dialog advDialog;
	private ImageView advImageView;
	private ImageView advCloseBtn;
	private String imageUrl;
	private String ad_link;
	private String app_type;
	private String app_id;

	private List<Marker> bikeMarkerList;
	private boolean isUp = false;
	private LinearLayout refreshLayout;
	private Button cartBtn;
	private LinearLayout slideLayout;
	private int imageWith = 0;
	private ValueAnimator animator = null;

	private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
	private static final int PRIVATE_CODE = 1315;//开启GPS权限

	protected OnLocationChangedListener mListener;
	CustomDialog.Builder customBuilder;
	private CustomDialog customDialog;
	private CustomDialog customDialog2;
	private CustomDialog customDialog3;
	private boolean isConnect = false;
	private int flag = 0;
	private int flag2 = 0;
	boolean isFrist1 = true;
	private int near = 1;

	private TextView title;


	@Override
	@TargetApi(23)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
//
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		WindowManager.LayoutParams winParams = getWindow().getAttributes();
		winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.ui_main);
		context = this;

		isContainsList = new ArrayList<>();
		macList = new ArrayList<>();
		pOptions = new ArrayList<>();


		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(mScreenReceiver, filter);


		mapView = (MapView) findViewById(R.id.mainUI_map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		bikeMarkerList = new ArrayList<>();
		//注册一个广播，这个广播主要是用于在GalleryActivity进行预览时，防止当所有图片都删除完后，再回到该页面时被取消选中的图片仍处于选中状态
		filter = new IntentFilter("data.broadcast.action");
		registerReceiver(broadcastReceiver, filter);
		imageWith = (int)(getWindowManager().getDefaultDisplay().getWidth() * 0.8);
		new Thread(new Runnable() {
			@Override
			public void run() {
				getNetTime();
			}
		}).start();

		initView();
//		openGPSSettings();

		if(SharedPreferencesUrls.getInstance().getBoolean("isStop",true)){
			SharedPreferencesUrls.getInstance().putString("m_nowMac", "");
		}

		m_nowMac = SharedPreferencesUrls.getInstance().getString("m_nowMac", "");

//		ToastUtil.showMessageApp(this, SharedPreferencesUrls.getInstance().getBoolean("isStop",true)+"==="+m_nowMac);

		if(!"".equals(m_nowMac)){

			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				ToastUtil.showMessageApp(context, "您的设备不支持蓝牙4.0");
				finish();
			}
			//蓝牙锁
			BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter();

			if (mBluetoothAdapter == null) {
				ToastUtil.showMessageApp(context, "获取蓝牙失败");
				finish();
				return;
			}

			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 188);
			}else{

				connect();

				if (macList.size() != 0){
					macList.clear();
				}
				UUID[] uuids = {Config.xinbiaoUUID};
				mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
			}
		}else{
			SharedPreferencesUrls.getInstance().putBoolean("isStop",true);
		}


//		getCurrentorder(uid, access_token);



		ToastUtil.showMessage(this, SharedPreferencesUrls.getInstance().getString("userName","")+"==="+SharedPreferencesUrls.getInstance().getString("uid","")+"<==>"+SharedPreferencesUrls.getInstance().getString("access_token",""));

		customBuilder = new CustomDialog.Builder(this);
		customBuilder.setType(1).setTitle("温馨提示").setMessage("当前行程已停止计费，客服正在加紧处理，请稍等");
		customDialog = customBuilder.create();

		CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle("温馨提示").setMessage("还车须至校内地图红色区域，或打开手机GPS并重启软件再试")
				.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		customDialog3 = customBuilder.create();

	}

	private void getCurrentorder(String uid, String access_token){
		RequestParams params = new RequestParams();
		params.put("uid",uid);
		params.put("access_token",access_token);
		HttpHelper.post(context, Urls.getCurrentorder, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
						ToastUtil.showMessageApp(context,"数据更新成功");
						if ("[]".equals(result.getData()) || 0 == result.getData().length()){
							ToastUtil.showMessage(context,"mmmmmmmm");

//							ToastUtil.showMessageApp(context,"当前无行程");
//							BaseApplication.getInstance().getIBLE().refreshCache();
//							BaseApplication.getInstance().getIBLE().close();
//							BaseApplication.getInstance().getIBLE().disconnect();
//
//							finish();
//							scrollToFinishActivity();
						}else {
							CurRoadBikingBean bean = JSON.parseObject(result.getData(),CurRoadBikingBean.class);
//							bikeCode = bean.getCodenum();
//							bikeCodeText.setText(bikeCode);
//							time.setText(bean.getSt_time());
							oid = bean.getOid();
							osn = bean.getOsn();
//							password = bean.getPassword();
							type = bean.getType();


							if ("1".equals(bean.getType())){
//								hintText.setText("还车须至校园地图红色覆盖区，关锁并拨乱密码后点击结束！");
//								lookPsdBtn.setText("查看密码");
							}else {
//								hintText.setText("还车须至校园地图红色覆盖区，关锁后距车一米内点击结束！");
								m_nowMac = bean.getMacinfo();

//								ToastUtil.showMessage(context, "###===="+m_nowMac);

//								connect();

//								lookPsdBtn.setText("再次开锁");
//								if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//									ToastUtil.showMessageApp(context, "您的设备不支持蓝牙4.0");
//									finish();
//								}
//								//蓝牙锁
//								BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//
//								mBluetoothAdapter = bluetoothManager.getAdapter();
//
//								if (mBluetoothAdapter == null) {
//									ToastUtil.showMessageApp(context, "获取蓝牙失败");
//									finish();
//									return;
//								}
//
//								if (!mBluetoothAdapter.isEnabled()) {
//									Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//									startActivityForResult(enableBtIntent, 188);
//								}else if(context instanceof MainActivity){
//									connect();
//								}


								if (macList.size() != 0){
									macList.clear();
								}
								UUID[] uuids = {Config.xinbiaoUUID};
								mBluetoothAdapter.startLeScan(uuids,mLeScanCallback);
							}
//							if (isFrist){
//								isFrist = false;
//							}
						}
					} else {
						ToastUtil.showMessageApp(context, result.getMsg());
					}
				} catch (Exception e) {
					ToastUtil.showMessageApp(context, "ee>>>>"+e);
				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}

	BroadcastReceiver mScreenReceiver =  new BroadcastReceiver() {
		private String action = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏

				ToastUtil.showMessage(context, "===on");
				Log.e("main===", "===on");

			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏

				ToastUtil.showMessage(context, "===off");
				Log.e("main===", "===off");

				try {
					registerReceiver(Config.initFilter());
					GlobalParameterUtils.getInstance().setLockType(LockType.MTS);
				} catch (Exception e) {
					ToastUtil.showMessage(context, "eee===="+e);
				}

			} else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁

				ToastUtil.showMessage(context, "===present");
				Log.e("main===", tz+">>>present==="+m_nowMac);

				if(tz==1){
					UIHelper.goToAct(MainActivity.this, FeedbackActivity.class);
					Log.e("main===","main===Feedback");
				}else if(tz==2){
					intent = new Intent(MainActivity.this, HistoryRoadDetailActivity.class);
					intent.putExtra("oid",oid);
					startActivity(intent);
					Log.e("main===","main===HistoryRoadDetail");
				}else if(tz==3){
					UIHelper.goToAct(MainActivity.this, CurRoadBikedActivity.class);
					Log.e("main===","main===CurRoadBiked");
				}


				if(!"".equals(m_nowMac)){

					if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
						ToastUtil.showMessageApp(context, "您的设备不支持蓝牙4.0");
						finish();
					}
					//蓝牙锁
					BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
					mBluetoothAdapter = bluetoothManager.getAdapter();

					Log.e("main===", "present===1");

					if (mBluetoothAdapter == null) {
						ToastUtil.showMessageApp(context, "获取蓝牙失败");
						finish();
						return;
					}

					Log.e("main===", "present===2");

					if (!mBluetoothAdapter.isEnabled()) {
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, 188);
					}else{

						Log.e("main===", "present===3");

						connect();

						if (macList.size() != 0){
							macList.clear();
						}
						UUID[] uuids = {Config.xinbiaoUUID};
						mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
					}

				}

			}

		}

	};

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			getCurrentorder1(SharedPreferencesUrls.getInstance().getString("uid",""), SharedPreferencesUrls.getInstance().getString("access_token",""));

			getFeedbackStatus();

		}
	};

	@Override
	protected void onResume() {
		isForeground = true;
		super.onResume();


		if ((isContainsList.contains(true) || macList.size() > 0) && !"1".equals(type)){
			near = 0;
		}else{
			near = 1;
		}

		JPushInterface.onResume(this);
		mapView.onResume();
		if (aMap != null) {
			setUpMap();
		}

		context = this;

		if(flag==1){
			flag = 0;
			return;
		}

		uid = SharedPreferencesUrls.getInstance().getString("uid","");
		access_token = SharedPreferencesUrls.getInstance().getString("access_token","");

		m_nowMac = SharedPreferencesUrls.getInstance().getString("m_nowMac", "");
		oid = SharedPreferencesUrls.getInstance().getString("oid", "");
		osn = SharedPreferencesUrls.getInstance().getString("osn", "");
		type = SharedPreferencesUrls.getInstance().getString("type", "");

		ToastUtil.showMessageApp(this, oid+">>>"+osn+">>>"+type+">>>main===onResume==="+SharedPreferencesUrls.getInstance().getBoolean("isStop",true)+">>>"+m_nowMac);
		Log.e("main===", "main====onResume");


		closeBroadcast();
		try {
//            registerReceiver(broadcastReceiver1, Config.initFilter());
			registerReceiver(Config.initFilter());
			GlobalParameterUtils.getInstance().setLockType(LockType.MTS);
		} catch (Exception e) {
			ToastUtil.showMessage(this, "eee===="+e);
		}

		getFeedbackStatus();



		String uid = SharedPreferencesUrls.getInstance().getString("uid","");
		String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
		String specialdays = SharedPreferencesUrls.getInstance().getString("specialdays","");
		if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)){
			authBtn.setVisibility(View.VISIBLE);
			authBtn.setText("您还未登录，点我快速登录");
			authBtn.setEnabled(true);
			cartBtn.setVisibility(View.GONE);
			refreshLayout.setVisibility(View.GONE);
			rechargeBtn.setVisibility(View.GONE);
		}else {
//			getCurrentorder2(uid,access_token);

			refreshLayout.setVisibility(View.VISIBLE);
			if (SharedPreferencesUrls.getInstance().getString("iscert","") != null && !"".equals(SharedPreferencesUrls.getInstance().getString("iscert",""))){
				switch (Integer.parseInt(SharedPreferencesUrls.getInstance().getString("iscert",""))){
					case 1:
						authBtn.setEnabled(true);
						authBtn.setVisibility(View.VISIBLE);
						authBtn.setText("您还未认证，点我快速认证");
						break;
					case 2:
						getCurrentorder1(uid,access_token);
						break;
					case 3:
						authBtn.setEnabled(true);
						authBtn.setVisibility(View.VISIBLE);
						authBtn.setText("认证被驳回，请重新认证");
						break;
					case 4:
						authBtn.setEnabled(false);
						authBtn.setVisibility(View.VISIBLE);
						authBtn.setText("认证审核中");
						break;
				}
			}else {
				authBtn.setVisibility(View.GONE);
			}
			if ("0.00".equals(SharedPreferencesUrls.getInstance().getString("money", ""))
					|| "0".equals(SharedPreferencesUrls.getInstance().getString("money", "")) || SharedPreferencesUrls.getInstance().getString("money", "") == null ||
					"".equals(SharedPreferencesUrls.getInstance().getString("money", ""))){
				rechargeBtn.setVisibility(View.VISIBLE);
			}else {
				rechargeBtn.setVisibility(View.GONE);
			}
			if (("0".equals(specialdays) || specialdays == null || "".equals(specialdays))
					&& ("0".equals(specialdays) || specialdays == null || "".equals(specialdays))){
				cartBtn.setVisibility(View.GONE);
			}else {
				cartBtn.setVisibility(View.VISIBLE);
				cartBtn.setText("免费"+specialdays+"天,每次前一个小时免费,点击续费");
			}
		}
	}


	@Override
	protected void onPause() {
		isForeground = false;
		if (loadingDialog != null && loadingDialog.isShowing()){
			loadingDialog.dismiss();
		}
		if (lockLoading != null && lockLoading.isShowing()){
			lockLoading.dismiss();
		}
		super.onPause();

		JPushInterface.onPause(this);
		mapView.onPause();
		deactivate();
//		mFirstFix = false;

		ToastUtil.showMessage(this, "main====onPause");
		Log.e("main===", "main====onPause");



//		closeBroadcast();

	}



	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		if (loadingDialog != null && loadingDialog.isShowing()){
			loadingDialog.dismiss();
		}
		super.onDestroy();

		ToastUtil.showMessage(context, "main===onDestroy");

		mapView.onDestroy();
		if(null != mlocationClient){
			mlocationClient.onDestroy();
		}
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
			broadcastReceiver = null;
		}

//		if (broadcastReceiver2 != null) {
//			unregisterReceiver(broadcastReceiver2);
//			broadcastReceiver2 = null;
//		}

		closeBroadcast();


		if (!"1".equals(type)){
			if (mLeScanCallback != null && mBluetoothAdapter != null) {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				mLeScanCallback = null;
			}
		}

	}

	private void closeBroadcast(){
		try {
			if (internalReceiver != null) {
				unregisterReceiver(internalReceiver);
				internalReceiver = null;
			}

			ToastUtil.showMessage(this, "main====closeBroadcast==="+internalReceiver);


		} catch (Exception e) {
			ToastUtil.showMessage(this, "eee===="+e);
		}
	}

	private void getFeedbackStatus(){
		RequestParams params = new RequestParams();
		params.put("telphone",SharedPreferencesUrls.getInstance().getString("userName",""));
		HttpHelper.get(context, Urls.getFeedbackStatus, params, new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
//						ToastUtil.showMessageApp(context,"数据更新成功==="+SharedPreferencesUrls.getInstance().getBoolean("isStop",true));

						if("2".equals(result.data) && !SharedPreferencesUrls.getInstance().getBoolean("isStop",true)){
							customDialog.show();
						}else{
							customDialog.dismiss();
						}


					} else {
						ToastUtil.showMessageApp(context, result.getMsg());
					}
				} catch (Exception e) {
				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ToastUtil.showMessage(this, resultCode+"===="+requestCode);


		if(resultCode == RESULT_OK){
			switch (requestCode) {


				case 188:
					connect();

					if (macList.size() != 0){
						macList.clear();
					}
					UUID[] uuids = {Config.xinbiaoUUID};
					mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);

					break;

				default:
					break;

			}
		}else{
			switch (requestCode) {
				case PRIVATE_CODE:
					openGPSSettings();
					break;

				case 188:
					ToastUtil.showMessageApp(this, "需要打开蓝牙");
					AppManager.getAppManager().AppExit(context);
					break;
				default:
					break;
			}
		}
	}

	private boolean checkGPSIsOpen() {
		boolean isOpen;
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		isOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		return isOpen;
	}


	private void openGPSSettings() {

		if (checkGPSIsOpen()) {
		} else {

			CustomDialog.Builder customBuilder = new CustomDialog.Builder(MainActivity.this);
			customBuilder.setTitle("温馨提示").setMessage("您需要在设置的定位模式里打开GPS！")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finishMine();
						}
					})
					.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, PRIVATE_CODE);
						}
					});
			customBuilder.create().show();

		}
	}



	private void initView() {

//        openGPS(false);

//		ToastUtil.showMessageApp(this, "gps===="+isOpen());

//		LocationManager alm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
//			Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
//		}else{
//
//			Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
//
//			Intent intent = new Intent();
//			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//			startActivityForResult(intent, PRIVATE_CODE);
//
////            Intent GPSIntent = new Intent();
////            GPSIntent.setClassName("com.android.settings","com.android.settings.widget.SettingsAppWidgetProvider");
////            GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
////            GPSIntent.setData(Uri.parse("custom:3"));
////            try {
////                PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//
//		}

//		Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//		startActivityForResult(intent,0);

//		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PERMISSION_GRANTED) {
//			Settings.Secure.setLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER, true);
//		}



//		LocationManager lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
//		boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//		if (ok) {//开了定位服务
//			if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
//				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PERMISSION_GRANTED) {// 没有权限，申请权限。
//					ToastUtil.showMessageApp(this, "====权限");
//
////					ActivityCompat.requestPermissions(this, LOCATIONGPS, BAIDU_READ_PHONE_STATE);
//
//				} else {
//					getLocation();//getLocation为定位方法
//				}
//			} else {
//				getLocation();//getLocation为定位方法
//			}
//		} else {
//			ToastUtil.showMessageApp(this, "系统检测到未开启GPS定位服务,请开启");
//			Intent intent = new Intent();
//			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//			startActivityForResult(intent, PRIVATE_CODE);
//		}


//		try {
//			Intent gpsIntent = new Intent();
//			gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
//			gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
//			gpsIntent.setData(Uri.parse("custom:3"));
//			PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
//		}catch (Exception e) {
//			ToastUtil.showMessageApp(this, "eee===="+e);
//		}

		openGPSSettings();

//		Settings.Secure.setLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER, true);

		if (Build.VERSION.SDK_INT >= 23) {
			int checkPermission = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
			if (checkPermission != PackageManager.PERMISSION_GRANTED) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
							REQUEST_CODE_ASK_PERMISSIONS);
				} else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setTitle("温馨提示").setMessage("您需要在设置里打开位置权限！")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							MainActivity.this.requestPermissions(
									new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
									REQUEST_CODE_ASK_PERMISSIONS);
						}
					});
					customBuilder.create().show();
				}
				return;
			}
		}


		loadingDialog = new LoadingDialog(this);
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);

		lockLoading = new LoadingDialog(this);
		lockLoading.setCancelable(false);
		lockLoading.setCanceledOnTouchOutside(false);

		loadingDialog1 = new LoadingDialog(this);
		loadingDialog1.setCancelable(false);
		loadingDialog1.setCanceledOnTouchOutside(false);

		dialog = new Dialog(this, R.style.Theme_AppCompat_Dialog);
		View dialogView = LayoutInflater.from(this).inflate(R.layout.ui_frist_view, null);
		dialog.setContentView(dialogView);
		dialog.setCanceledOnTouchOutside(false);

		advDialog = new Dialog(this, R.style.Theme_AppCompat_Dialog);
		View advDialogView = LayoutInflater.from(this).inflate(R.layout.ui_adv_view, null);
		advDialog.setContentView(advDialogView);
		advDialog.setCanceledOnTouchOutside(false);

		marqueeLayout = findViewById(R.id.mainUI_marqueeLayout);

		titleImage = (ImageView)dialogView.findViewById(R.id.ui_fristView_title);
		exImage_1 = (ImageView)dialogView.findViewById(R.id.ui_fristView_exImage_1);
		exImage_2 = (ImageView)dialogView.findViewById(R.id.ui_fristView_exImage_2);
		exImage_3 = (ImageView)dialogView.findViewById(R.id.ui_fristView_exImage_3);
		closeBtn = (ImageView)dialogView.findViewById(R.id.ui_fristView_closeBtn);

		advImageView = (ImageView)advDialogView.findViewById(R.id.ui_adv_image);
		advCloseBtn = (ImageView)advDialogView.findViewById(R.id.ui_adv_closeBtn);

		LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) advImageView.getLayoutParams();
		params4.height = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.8);
		advImageView.setLayoutParams(params4);

		title = (TextView) findViewById(R.id.mainUI_title);
		leftBtn = (ImageView) findViewById(R.id.mainUI_leftBtn);
		rightBtn = (ImageView) findViewById(R.id.mainUI_rightBtn);
        myLocationLayout =  (LinearLayout) findViewById(R.id.mainUI_myLocationLayout);
        linkLayout = (LinearLayout) findViewById(R.id.mainUI_linkServiceLayout);
		myLocationBtn = (ImageView) findViewById(R.id.mainUI_myLocation);
		scanLock = (ImageView) findViewById(R.id.mainUI_scanCode_lock);
		linkBtn = (ImageView) findViewById(R.id.mainUI_linkService_btn);
		authBtn = (Button)findViewById(R.id.mainUI_authBtn);
		cartBtn = (Button)findViewById(R.id.mainUI_cartBtn);
		rechargeBtn = (Button)findViewById(R.id.mainUI_rechargeBtn);
		refreshLayout = (LinearLayout) findViewById(R.id.mainUI_refreshLayout);
		slideLayout = findViewById(R.id.mainUI_slideLayout);
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setMyLocationButtonEnabled(false);
		aMap.getUiSettings()
				.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);// 设置地图logo显示在右下方
		CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(18);// 设置缩放监听
		aMap.moveCamera(cameraUpdate);
		successDescripter = BitmapDescriptorFactory.fromResource(R.drawable.icon_usecarnow_position_succeed);
		bikeDescripter = BitmapDescriptorFactory.fromResource(R.drawable.bike_icon);
		setUpLocationStyle();

		aMap.setOnMapTouchListener(MainActivity.this);

		leftBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		marqueeLayout.setOnClickListener(this);
		myLocationBtn.setOnClickListener(this);
        myLocationLayout.setOnClickListener(this);
        linkLayout.setOnClickListener(this);
		scanLock.setOnClickListener(this);
		linkBtn.setOnClickListener(this);
		authBtn.setOnClickListener(this);
		rechargeBtn.setOnClickListener(this);
		refreshLayout.setOnClickListener(this);
		advImageView.setOnClickListener(this);
		advCloseBtn.setOnClickListener(this);
		cartBtn.setOnClickListener(this);
		slideLayout.setOnClickListener(this);

		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleImage.getLayoutParams();
		params.height = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.16);
		titleImage.setLayoutParams(params);

		LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) exImage_1.getLayoutParams();
		params1.height = (imageWith - DisplayUtil.dip2px(context,20)) * 2 / 5;
		exImage_1.setLayoutParams(params1);

		LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) exImage_2.getLayoutParams();
		params2.height = (imageWith - DisplayUtil.dip2px(context,20)) * 2 / 5;
		exImage_2.setLayoutParams(params2);

		LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) exImage_3.getLayoutParams();
		params3.height = (imageWith - DisplayUtil.dip2px(context,20)) * 2 / 5;
		exImage_3.setLayoutParams(params3);

		if (SharedPreferencesUrls.getInstance().getBoolean("ISFRIST",true)){
			SharedPreferencesUrls.getInstance().putBoolean("ISFRIST",false);
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = (int) (display.getWidth() * 0.8); // 设置宽度0.6
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
			dialog.getWindow().setAttributes(lp);
			dialog.show();
		}else {
			initHttp();
		}
		exImage_1.setOnClickListener(myOnClickLister);
		exImage_2.setOnClickListener(myOnClickLister);
		closeBtn.setOnClickListener(myOnClickLister);

	}

	private OnClickListener myOnClickLister = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ui_fristView_exImage_1:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    UIHelper.goWebViewAct(context,"使用说明",Urls.bluecarisee);
                    break;
                case R.id.ui_fristView_exImage_2:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    UIHelper.goWebViewAct(context,"使用说明",Urls.useHelp);
                    break;
                case R.id.ui_fristView_closeBtn:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    };

	/**
	 * 获取广告
	 * */
	private void initHttp(){
		RequestParams params = new RequestParams();
		params.put("adsid","11");
		if (SharedPreferencesUrls.getInstance().getString("uid","") != null &&
				!"".equals(SharedPreferencesUrls.getInstance().getString("uid",""))){
			params.put("uid",SharedPreferencesUrls.getInstance().getString("uid",""));
		}
		if (SharedPreferencesUrls.getInstance().getString("access_token","") != null &&
				!"".equals(SharedPreferencesUrls.getInstance().getString("access_token",""))){
			params.put("access_token",SharedPreferencesUrls.getInstance().getString("access_token",""));
		}
		HttpHelper.get(context, Urls.getIndexAd, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
						JSONArray jsonArray = new JSONArray(result.getData());
						for (int i = 0; i < jsonArray.length();i++){
							imageUrl = jsonArray.getJSONObject(i).getString("ad_file");
							ad_link = jsonArray.getJSONObject(i).getString("ad_link");
							app_type = jsonArray.getJSONObject(i).getString("app_type");
							app_id = jsonArray.getJSONObject(i).getString("app_id");
							ad_link = jsonArray.getJSONObject(i).getString("ad_link");

						}
						if (!SharedPreferencesUrls.getInstance().getBoolean("ISFRIST",false)){
							if (imageUrl != null && !"".equals(imageUrl)){
								WindowManager windowManager = getWindowManager();
								Display display = windowManager.getDefaultDisplay();
								WindowManager.LayoutParams lp = advDialog.getWindow().getAttributes();
								lp.width = (int) (display.getWidth() * 0.8);
								lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
								advDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
								advDialog.getWindow().setAttributes(lp);
								advDialog.show();
								// 加载图片
								Glide.with(context).load(imageUrl).into(advImageView);
							}
						}
					}
				}catch (Exception e){
				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}

	private void addChooseMarker() {
		// 加入自定义标签
		MarkerOptions centerMarkerOption = new MarkerOptions().position(myLocation).icon(successDescripter);
		centerMarker = aMap.addMarker(centerMarkerOption);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				CameraUpdate update = CameraUpdateFactory.zoomTo(18);
				aMap.animateCamera(update, 1000, new AMap.CancelableCallback() {
					@Override
					public void onFinish() {
						aMap.setOnCameraChangeListener(MainActivity.this);
					}

					@Override
					public void onCancel() {

					}
				});
			}
		}, 1000);
	}

	private void setMovingMarker() {
		if (isMovingMarker)
			return;

		isMovingMarker = true;
		centerMarker.setIcon(successDescripter);
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		if (centerMarker != null) {
			setMovingMarker();
		}
	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
		if (isUp){
			initNearby(cameraPosition.target.latitude,cameraPosition.target.longitude);
			if (centerMarker != null) {
				animMarker();
			}
		}
	}

	@Override
	public void onTouch(MotionEvent motionEvent) {
		if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
				motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE
				|| motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
			isUp = true;
		}else {
			isUp = false;
		}
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}

	@Override
	public void onClick(View view) {
		String uid = SharedPreferencesUrls.getInstance().getString("uid","");
		String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
		switch (view.getId()){
			case R.id.mainUI_leftBtn:
//				endBtn(context);

				UIHelper.goToAct(MainActivity.this, ActionCenterActivity.class);
//				finish();

//				UIHelper.goToAct(context, Main2Activity.class);
//				UIHelper.goToAct(context, CurRoadBikingActivity.class);
				break;
			case R.id.mainUI_rightBtn:
				if (SharedPreferencesUrls.getInstance().getString("uid","") == null || "".equals(
						SharedPreferencesUrls.getInstance().getString("access_token",""))){
					UIHelper.goToAct(context,LoginActivity.class);
					ToastUtil.showMessageApp(context,"请先登录你的账号");
					return;
				}
				UIHelper.goToAct(context, PersonAlterActivity.class);
				break;
			case R.id.mainUI_marqueeLayout:

				break;
            case R.id.mainUI_myLocationLayout:
			case R.id.mainUI_myLocation:
				if (myLocation != null) {
					CameraUpdate update = CameraUpdateFactory.changeLatLng(myLocation);
					aMap.animateCamera(update);
				}
				break;
			case R.id.mainUI_scanCode_lock:


				if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)){
					ToastUtil.showMessageApp(context,"请先登录账号");
					UIHelper.goToAct(context,LoginActivity.class);
					return;
				}
				if (SharedPreferencesUrls.getInstance().getString("iscert","") != null && !"".equals(SharedPreferencesUrls.getInstance().getString("iscert",""))){
					switch (Integer.parseInt(SharedPreferencesUrls.getInstance().getString("iscert",""))){
						case 1:
							ToastUtil.showMessageApp(context,"您还未认证,请先认证");
							UIHelper.goToAct(context,RealNameAuthActivity.class);
							break;
						case 2:
							getCurrentorder2(uid,access_token);
							break;
						case 3:
							ToastUtil.showMessageApp(context,"认证被驳回，请重新认证");
							UIHelper.goToAct(context,RealNameAuthActivity.class);
							break;
						case 4:
							ToastUtil.showMessageApp(context,"认证审核中");
							break;
					}
				}else {
					ToastUtil.showMessage(context,"您还未认证,请先认证");
				}
				break;
            case R.id.mainUI_linkServiceLayout:
			case R.id.mainUI_linkService_btn:
				initmPopupWindowView();
				break;
			case R.id.mainUI_authBtn:
				if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)){
					UIHelper.goToAct(context,LoginActivity.class);
				}else {
					if ("2".equals(SharedPreferencesUrls.getInstance().getString("iscert",""))){
						switch (Tag){
							case 0:
								closeBroadcast();

								UIHelper.goToAct(context, CurRoadBikingActivity.class);
								break;
							case 1:
								UIHelper.goToAct(context,CurRoadBikedActivity.class);
								break;
							default:
								break;
						}
					}else {
						UIHelper.goToAct(context,RealNameAuthActivity.class);
					}
				}
				break;
			case R.id.mainUI_rechargeBtn:
				UIHelper.goToAct(context,MyPurseActivity.class);
				break;
			case R.id.mainUI_refreshLayout:
				RefreshLogin();
				if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)){
					UIHelper.goToAct(context,LoginActivity.class);
				}else {
					new MyAsyncTask().execute();
					if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)) {
						authBtn.setVisibility(View.VISIBLE);
						authBtn.setText("您还未登录，点我快速登录");
						authBtn.setEnabled(true);
					} else {
						if (SharedPreferencesUrls.getInstance().getString("iscert", "") != null && !"".equals(SharedPreferencesUrls.getInstance().getString("iscert", ""))) {
							switch (Integer.parseInt(SharedPreferencesUrls.getInstance().getString("iscert", ""))) {
								case 1:
									authBtn.setEnabled(true);
									authBtn.setVisibility(View.VISIBLE);
									authBtn.setText("您还未认证，点我快速认证");
									break;
								case 2:
									getCurrentorder1(uid, access_token);
									break;
								case 3:
									authBtn.setEnabled(true);
									authBtn.setVisibility(View.VISIBLE);
									authBtn.setText("认证被驳回，请重新认证");
									break;
								case 4:
									authBtn.setEnabled(false);
									authBtn.setVisibility(View.VISIBLE);
									authBtn.setText("认证审核中");
									break;
							}
						} else {
							authBtn.setVisibility(View.GONE);
						}
					}
					if ("0.00".equals(SharedPreferencesUrls.getInstance().getString("money", ""))||
							"0".equals(SharedPreferencesUrls.getInstance().getString("money", "")) ||
							SharedPreferencesUrls.getInstance().getString("money", "") == null ||
							"".equals(SharedPreferencesUrls.getInstance().getString("money", ""))){
						rechargeBtn.setVisibility(View.VISIBLE);
					}else {
						rechargeBtn.setVisibility(View.GONE);
					}
				}
				break;
			case R.id.ui_adv_image:
				UIHelper.bannerGoAct(context,app_type,app_id,ad_link);
				break;
			case R.id.ui_adv_closeBtn:
				if (advDialog != null && advDialog.isShowing()) {
					advDialog.dismiss();
				}
				break;
			case R.id.mainUI_cartBtn:
				UIHelper.goToAct(context,PayMontCartActivity.class);
				break;
			case R.id.mainUI_slideLayout:
				UIHelper.goWebViewAct(context,"停车须知",Urls.phtml5 + uid);
				break;
			default:
				break;
		}
	}



	private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (loadingDialog != null && !loadingDialog.isShowing()) {
				loadingDialog.setTitle("正在刷新");
				loadingDialog.show();
			}
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			ToastUtil.showMessage(context, "刷新成功");
		}
	}

	public void initmPopupWindowView(){

		// 获取自定义布局文件的视图
		View customView = getLayoutInflater().inflate(R.layout.pop_menu, null, false);
		// 创建PopupWindow宽度和高度
		RelativeLayout pop_win_bg = (RelativeLayout) customView.findViewById(R.id.pop_win_bg);
		ImageView iv_popup_window_back = (ImageView) customView.findViewById(R.id.popupWindow_back);
		// 获取截图的Bitmap
		Bitmap bitmap = UtilScreenCapture.getDrawing(this);
		if (bitmap != null) {
			// 将截屏Bitma放入ImageView
			iv_popup_window_back.setImageBitmap(bitmap);
			// 将ImageView进行高斯模糊【25是最高模糊等级】【0x77000000是蒙上一层颜色，此参数可不填】
			UtilBitmap.blurImageView(this, iv_popup_window_back, 10,0xAA000000);
		} else {
			// 获取的Bitmap为null时，用半透明代替
			iv_popup_window_back.setBackgroundColor(0x77000000);
		}
		// 打开弹窗
		UtilAnim.showToUp(pop_win_bg, iv_popup_window_back);
		// 创建PopupWindow宽度和高度
		final PopupWindow popupwindow = new PopupWindow(customView, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, true);
		/**
		 * 设置动画效果 ,从上到下加载方式等，不设置自动的下拉，最好 [动画效果不好，不加实现下拉效果，不错]
		 */
		popupwindow.setAnimationStyle(R.style.PopupAnimation);
		popupwindow.setOutsideTouchable(false);

		LinearLayout feedbackLayout = (LinearLayout)customView.findViewById(R.id.pop_menu_feedbackLayout);
		LinearLayout helpLayout = (LinearLayout)customView.findViewById(R.id.pop_menu_helpLayout);
		final LinearLayout callLayout = (LinearLayout)customView.findViewById(R.id.pop_menu_callLayout);
		TextView cancleBtn = (TextView)customView.findViewById(R.id.pop_menu_cancleBtn);

		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()){
					case R.id.pop_menu_feedbackLayout:
						UIHelper.goToAct(context,FeedbackActivity.class);
						break;
					case R.id.pop_menu_helpLayout:
						WindowManager windowManager = getWindowManager();
						Display display = windowManager.getDefaultDisplay();
						WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
						lp.width = (int) (display.getWidth() * 0.8); // 设置宽度0.6
						lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
						dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
						dialog.getWindow().setAttributes(lp);
						dialog.show();
						break;
					case R.id.pop_menu_callLayout:
						if (Build.VERSION.SDK_INT >= 23) {
							int checkPermission = MainActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE);
							if (checkPermission != PERMISSION_GRANTED) {
								if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
									requestPermissions(new String[] { Manifest.permission.CALL_PHONE }, 0);
								} else {
									CustomDialog.Builder customBuilder = new CustomDialog.Builder(MainActivity.this);
									customBuilder.setTitle("温馨提示").setMessage("您需要在设置里打开拨打电话权限！")
											.setNegativeButton("取消", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													dialog.cancel();
												}
											}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											MainActivity.this.requestPermissions(
													new String[] { Manifest.permission.CALL_PHONE }, 0);
										}
									});
									customBuilder.create().show();
								}
								return;
							}
						}
						CustomDialog.Builder customBuilder = new CustomDialog.Builder(MainActivity.this);
						customBuilder.setTitle("温馨提示").setMessage("确认拨打" + "0519-86999222" + "吗?")
								.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								Intent intent=new Intent();
								intent.setAction(Intent.ACTION_CALL);
								intent.setData(Uri.parse("tel:" + "0519-86999222"));
								startActivity(intent);
							}
						});
						customBuilder.create().show();
						break;
					case R.id.pop_menu_cancleBtn:

						break;
				}
				popupwindow.dismiss();
			}
		};

		feedbackLayout.setOnClickListener(listener);
		helpLayout.setOnClickListener(listener);
		callLayout.setOnClickListener(listener);
		cancleBtn.setOnClickListener(listener);

		popupwindow.showAtLocation(customView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
	}


	protected void getCurrentorder2(String uid, String access_token){
		RequestParams params = new RequestParams();
		params.put("uid",uid);
		params.put("access_token",access_token);
		HttpHelper.post(context, Urls.getCurrentorder, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
						if ("[]".equals(result.getData()) || 0 == result.getData().length()){
							SharedPreferencesUrls.getInstance().putBoolean("isStop",true);
							cardCheck();
						}else {
							if (loadingDialog != null && loadingDialog.isShowing()){
								loadingDialog.dismiss();
							}
							CurRoadBikingBean bean = JSON.parseObject(result.getData(),CurRoadBikingBean.class);

							if ("1".equals(bean.getStatus())){
								SharedPreferencesUrls.getInstance().putBoolean("isStop",false);
								if (loadingDialog != null && loadingDialog.isShowing()){
									loadingDialog.dismiss();
								}
								closeBroadcast();
								UIHelper.goToAct(context, CurRoadBikingActivity.class);
							}else {
								SharedPreferencesUrls.getInstance().putBoolean("isStop",true);
								if (loadingDialog != null && loadingDialog.isShowing()){
									loadingDialog.dismiss();
								}
								UIHelper.goToAct(context,CurRoadBikedActivity.class);
							}
						}
					} else {
						ToastUtil.showMessage(context,result.getMsg());
					}
				} catch (Exception e) {
				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}
	/**
	 *
	 * 保险接口
	 * */
	private void cardCheck() {

		String uid = SharedPreferencesUrls.getInstance().getString("uid", "");
		String access_token = SharedPreferencesUrls.getInstance().getString("access_token", "");
		if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)) {
			ToastUtils.show("请先登录您的账号");
			UIHelper.goToAct(context, LoginActivity.class);
		} else {
			RequestParams params = new RequestParams();
			params.put("uid", uid);
			params.put("access_token", access_token);
			HttpHelper.get(context, Urls.useinfo, params, new TextHttpResponseHandler() {
				@Override
				public void onStart() {
					if (loadingDialog1 != null && !loadingDialog1.isShowing()) {
						loadingDialog1.setTitle("正在提交");
						loadingDialog1.show();
					}
				}
				@Override
				public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
					if (loadingDialog1 != null && loadingDialog1.isShowing()) {
						loadingDialog1.dismiss();
					}
					UIHelper.ToastError(context, throwable.toString());
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, String responseString) {
					try {
						ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
						if (result.getFlag().equals("Success")) {
							CardinfoBean bean = JSON.parseObject(result.getData(), CardinfoBean.class);
							if (!"2".equals(bean.getCardcheck())){

								CustomDialog.Builder customBuilder = new CustomDialog.Builder(MainActivity.this);
								customBuilder.setTitle("温馨提示").setMessage("为了您的骑行安全，请上传身份证完善保险信息")
										.setNegativeButton("去上传", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												Intent intent1 = new Intent(context,InsureanceActivity.class);
												intent1.putExtra("isBack",true);
												context.startActivity(intent1);
												dialog.cancel();
											}
										}).setPositiveButton("直接用车", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										if (Build.VERSION.SDK_INT >= 23) {
											int checkPermission = MainActivity.this.checkSelfPermission(Manifest.permission.CAMERA);
											if (checkPermission != PERMISSION_GRANTED) {
												flag = 1;

												if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
													requestPermissions(new String[] { Manifest.permission.CAMERA }, 100);
												} else {
													CustomDialog.Builder customBuilder1 = new CustomDialog.Builder(MainActivity.this);
													customBuilder1.setTitle("温馨提示").setMessage("您需要在设置里打开相机权限！")
															.setNegativeButton("取消", new DialogInterface.OnClickListener() {
																public void onClick(DialogInterface dialog, int which) {
																	dialog.cancel();
																}
															}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
																	dialog.cancel();
																	MainActivity.this.requestPermissions(
																			new String[] { Manifest.permission.CAMERA },
																			100);
															}
														});
													customBuilder1.create().show();
												}
												if (loadingDialog1 != null && loadingDialog1.isShowing()){
													loadingDialog1.dismiss();
												}
												return;
											}
										}
										try {

											closeBroadcast();

											Intent intent = new Intent();
											intent.setClass(MainActivity.this, ActivityScanerCode.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											startActivityForResult(intent, SCANNIN_GREQUEST_CODE);



										} catch (Exception e) {
											UIHelper.showToastMsg(context, "相机打开失败,请检查相机是否可正常使用", R.drawable.ic_error);
										}
										dialog.cancel();
										if (loadingDialog1 != null && loadingDialog1.isShowing()){
											loadingDialog1.dismiss();
										}
									}
								});
								customDialog2 = customBuilder.create();
								customDialog2.show();
							}else {
								if (Build.VERSION.SDK_INT >= 23) {
									int checkPermission = MainActivity.this.checkSelfPermission(Manifest.permission.CAMERA);
									if (checkPermission != PERMISSION_GRANTED) {
										if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
											requestPermissions(new String[] { Manifest.permission.CAMERA }, 100);
										} else {
											CustomDialog.Builder customBuilder1 = new CustomDialog.Builder(MainActivity.this);
											customBuilder1.setTitle("温馨提示").setMessage("您需要在设置里打开相机权限！")
													.setNegativeButton("取消", new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
															dialog.cancel();
														}
													}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													dialog.cancel();
													MainActivity.this.requestPermissions(
															new String[] { Manifest.permission.CAMERA },
															100);
												}
											});
											customBuilder1.create().show();
										}
										if (loadingDialog1 != null && loadingDialog1.isShowing()){
											loadingDialog1.dismiss();
										}
										return;
									}
								}
								if (loadingDialog1 != null && loadingDialog1.isShowing()){
									loadingDialog1.dismiss();
								}
								try {
									closeBroadcast();

									Intent intent = new Intent();
									intent.setClass(MainActivity.this, ActivityScanerCode.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivityForResult(intent, SCANNIN_GREQUEST_CODE);


								} catch (Exception e) {
									UIHelper.showToastMsg(context, "相机打开失败,请检查相机是否可正常使用", R.drawable.ic_error);
								}
							}
						} else {
							ToastUtils.show(result.getMsg());
						}
					} catch (Exception e) {
					}
					if (loadingDialog1 != null && loadingDialog1.isShowing()) {
						loadingDialog1.dismiss();
					}
				}
			});
		}
	}
	/**
	 *
	 * 附近车接口
	 *
	 * */
	private void initNearby(double latitude, double longitude){
		RequestParams params = new RequestParams();
		params.put("latitude",latitude);
		params.put("longitude",longitude);
		HttpHelper.get(context, Urls.nearby, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {

				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
						JSONArray array = new JSONArray(result.getData());
						for (Marker marker : bikeMarkerList){
							if (marker != null){
								marker.remove();
							}
						}
						if (!bikeMarkerList.isEmpty() || 0 != bikeMarkerList.size()){
							bikeMarkerList.clear();
						}
						if (0 == array.length()){
							ToastUtils.show("附近没有自行车");
						}else {
							for (int i = 0; i < array.length(); i++){
								NearbyBean bean = JSON.parseObject(array.getJSONObject(i).toString(), NearbyBean.class);
								// 加入自定义标签
								MarkerOptions bikeMarkerOption = new MarkerOptions().position(new LatLng(
										Double.parseDouble(bean.getLatitude()),Double.parseDouble(bean.getLongitude()))).icon(bikeDescripter);
								Marker bikeMarker = aMap.addMarker(bikeMarkerOption);
								bikeMarkerList.add(bikeMarker);
							}
						}
					} else {
						ToastUtils.show(result.getMsg());
					}
				} catch (Exception e) {

				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			try{
				CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
				customBuilder.setTitle("温馨提示").setMessage("确认退出吗?")
						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						AppManager.getAppManager().AppExit(context);
					}
				});
				customBuilder.create().show();
				return true;
			}catch (Exception e){

			}
		}
		return super.onKeyDown(keyCode, event);
	}


	private void getCurrentorder1(String uid, String access_token){
		RequestParams params = new RequestParams();
		params.put("uid",uid);
		params.put("access_token",access_token);
		HttpHelper.post(context, Urls.getCurrentorder, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
						if ("2".equals(SharedPreferencesUrls.getInstance().getString("iscert",""))){
							if ("[]".equals(result.getData()) || 0 == result.getData().length()){
								authBtn.setEnabled(false);
								authBtn.setVisibility(View.GONE);
							}else {
								CurRoadBikingBean bean = JSON.parseObject(result.getData(),CurRoadBikingBean.class);
								if ("1".equals(bean.getStatus())){
									SharedPreferencesUrls.getInstance().putBoolean("isStop",false);
									authBtn.setText("您有一条进行中的行程，点我查看");
									Tag = 0;
								}else {
									SharedPreferencesUrls.getInstance().putBoolean("isStop",true);
									authBtn.setText("您有一条未支付的行程，点我查看");
									Tag = 1;
								}
								authBtn.setVisibility(View.VISIBLE);
								authBtn.setEnabled(true);
							}
						}
					} else {
						ToastUtils.show(result.getMsg());
					}
				} catch (Exception e) {
				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}


	private void setUpLocationStyle() {
		// 自定义系统定位蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked));
		myLocationStyle.strokeWidth(0);
		myLocationStyle.strokeColor(R.color.main_theme_color);
		myLocationStyle.radiusFillColor(Color.TRANSPARENT);
		aMap.setMyLocationStyle(myLocationStyle);
	}


	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
//	    super.activate(listener);
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mlocationClient.setLocationListener(this);
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			mLocationOption.setInterval(2 * 1000);
			//设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mlocationClient.startLocation();
		}
	}



	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
//		super.onLocationChanged(amapLocation);


//		title.setText(SharedPreferencesUrls.getInstance().getBoolean("isStop",true)+"==4="+isContainsList.contains(true)+"==="+macList.size()+"》》》"+near);

		if (mListener != null && amapLocation != null) {

			if((referLatitude == amapLocation.getLatitude()) && (referLongitude == amapLocation.getLongitude())) return;

//			title.setText(amapLocation.getLatitude()+"==="+amapLocation.getLongitude());
			Log.e("main===1",amapLocation.getLatitude()+"==="+amapLocation.getLongitude());

			if (amapLocation != null && amapLocation.getErrorCode() == 0) {

//				title.setText(amapLocation.getErrorCode()+">>>"+amapLocation.getLongitude());
				Log.e("main===2",amapLocation.getErrorCode()+">>>"+amapLocation.getLongitude());

				if (0.0 != amapLocation.getLatitude() && 0.0 != amapLocation.getLongitude()){
					String latitude = SharedPreferencesUrls.getInstance().getString("biking_latitude","");
					String longitude = SharedPreferencesUrls.getInstance().getString("biking_longitude","");
					if (latitude != null && !"".equals(latitude) && longitude != null && !"".equals(longitude)){
						if (AMapUtils.calculateLineDistance(new LatLng(
								Double.parseDouble(latitude),Double.parseDouble(longitude)
						),new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude())) > 10){

							SharedPreferencesUrls.getInstance().putString("biking_latitude",""+amapLocation.getLatitude());
							SharedPreferencesUrls.getInstance().putString("biking_longitude",""+amapLocation.getLongitude());
							addMaplocation(amapLocation.getLatitude(),amapLocation.getLongitude());
						}
					}
					if (mListener != null) {
						mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
					}
					referLatitude = amapLocation.getLatitude();
					referLongitude = amapLocation.getLongitude();
					myLocation = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
					if (mFirstFix) {
						mFirstFix = false;
						schoolrangeList();

						addChooseMarker();
						addCircle(myLocation, amapLocation.getAccuracy());//添加定位精度圆
						initNearby(amapLocation.getLatitude(),amapLocation.getLongitude());

//						aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
						aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
					} else {
//						centerMarker.remove();
//						mCircle.remove();

						centerMarker.setPosition(myLocation);
						mCircle.setCenter(myLocation);

						if (!isContainsList.isEmpty() || 0 != isContainsList.size()){
							isContainsList.clear();
						}
						for ( int i = 0; i < pOptions.size(); i++){
							isContainsList.add(pOptions.get(i).contains(myLocation));
						}
					}

//					aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

//					if(isConnect){
//						BaseApplication.getInstance().getIBLE().getLockStatus();
//					}

					if(!SharedPreferencesUrls.getInstance().getBoolean("isStop",true)){
						if ((isContainsList.contains(true) || macList.size() > 0) && !"1".equals(type) && near==1){
							ToastUtil.showMessage(context,"main---》》》里");
//							endBtn(context);
							BaseApplication.getInstance().getIBLE().getLockStatus();
						}else if (((!isContainsList.contains(true) && macList.size() <= 0) || "1".equals(type)) && near==0){
							ToastUtil.showMessage(context,"main---》》》外");
//							endBtn(context);
							BaseApplication.getInstance().getIBLE().getLockStatus();
						}
					}


					if ((isContainsList.contains(true) || macList.size() > 0) && !"1".equals(type)){
						near = 0;
					}else{
						near = 1;
					}

//					BaseApplication.getInstance().getIBLE().getConnectStatus();
//					connect();

//					addChooseMarker();
//					addCircle(myLocation, amapLocation.getAccuracy());//添加定位精度圆
				}else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setTitle("温馨提示").setMessage("您需要在设置里打开位置权限！")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
									finish();
//									scrollToFinishActivity();
								}
							}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							MainActivity.this.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE_ASK_PERMISSIONS);
						}
					});
					customBuilder.create().show();
				}


//				if (mListener != null) {
//					mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
//				}
//				myLocation = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
//				if (mFirstFix){
//					mFirstFix = false;
//					addChooseMarker();
//					addCircle(myLocation, amapLocation.getAccuracy());//添加定位精度圆
//					initNearby(amapLocation.getLatitude(),amapLocation.getLongitude());
//				} else {
//					centerMarker.setPosition(myLocation);
//					mCircle.setCenter(myLocation);
//				}
//				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
				//保存经纬度到本地
				SharedPreferencesUrls.getInstance().putString("latitude",""+amapLocation.getLatitude());
				SharedPreferencesUrls.getInstance().putString("longitude",""+amapLocation.getLongitude());
			}
		}
	}

	private void addMaplocation(double latitude,double longitude){
		String uid = SharedPreferencesUrls.getInstance().getString("uid","");
		String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
		if (uid != null && !"".equals(uid) && access_token != null && !"".equals(access_token)){
			RequestParams params = new RequestParams();
			params.put("uid",uid);
			params.put("access_token",access_token);
			params.put("oid",oid);
			params.put("osn",osn);
			params.put("latitude",latitude);
			params.put("longitude",longitude);
			HttpHelper.post(context, Urls.addMaplocation, params, new TextHttpResponseHandler() {
				@Override
				public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, String responseString) {
					try {
						ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
						if (result.getFlag().equals("Success")) {
							if (myLocation != null){
								SharedPreferencesUrls.getInstance().putString("biking_latitude",""+myLocation.latitude);
								SharedPreferencesUrls.getInstance().putString("biking_longitude",""+myLocation.longitude);
							}
						}
					}catch (Exception e){

					}
				}
			});
		}
	}

	private void schoolrangeList(){
		RequestParams params = new RequestParams();
		HttpHelper.get(context, schoolrangeList, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在加载");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {
						JSONArray jsonArray = new JSONArray(result.getData());
						if (!isContainsList.isEmpty() || 0 != isContainsList.size()){
							isContainsList.clear();
						}
						for (int i = 0; i < jsonArray.length(); i++) {
							List<LatLng> list = new ArrayList<>();
							for (int j = 0; j < jsonArray.getJSONArray(i).length(); j ++){
								JSONObject jsonObject = jsonArray.getJSONArray(i).getJSONObject(j);
								LatLng latLng = new LatLng(Double.parseDouble(jsonObject.getString("latitude")),
										Double.parseDouble(jsonObject.getString("longitude")));
								list.add(latLng);
							}
							Polygon polygon = null;
							PolygonOptions pOption = new PolygonOptions();
							pOption.addAll(list);
							polygon = aMap.addPolygon(pOption.strokeWidth(2)
									.strokeColor(Color.argb(160, 255, 0, 0))
									.fillColor(Color.argb(160, 255, 0, 0)));
							pOptions.add(polygon);
							isContainsList.add(polygon.contains(myLocation));
						}
					}else {
						ToastUtil.showMessageApp(context,result.getMsg());
					}
				}catch (Exception e){
				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		});
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}

	/**
	 * 添加Circle
	 * @param latlng  坐标
	 * @param radius  半径
	 */
	private void addCircle(LatLng latlng, double radius) {
		CircleOptions options = new CircleOptions();
		options.strokeWidth(1f);
		options.fillColor(FILL_COLOR);
		options.strokeColor(STROKE_COLOR);
		options.center(latlng);
		options.radius(radius);
		mCircle = aMap.addCircle(options);
	}


	private void animMarker() {
		isMovingMarker = false;
		if (animator != null) {
			animator.start();
			return;
		}
		animator = ValueAnimator.ofFloat(mapView.getHeight() / 2, mapView.getHeight() / 2 - 30);
		animator.setInterpolator(new DecelerateInterpolator());
		animator.setDuration(150);
		animator.setRepeatCount(1);
		animator.setRepeatMode(ValueAnimator.REVERSE);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Float value = (Float) animation.getAnimatedValue();
				centerMarker.setPositionByPixels(mapView.getWidth() / 2, Math.round(value));
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				centerMarker.setIcon(successDescripter);
			}
		});
		animator.start();
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case 0:
				if (grantResults[0] == PERMISSION_GRANTED) {
					// Permission Granted
					if (permissions[0].equals(Manifest.permission.CALL_PHONE)){
						Intent intent=new Intent();
						intent.setAction(Intent.ACTION_CALL);
						intent.setData(Uri.parse("tel:" + "0519-86999222"));
						startActivity(intent);
					}
				}else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setTitle("温馨提示").setMessage("您需要在设置里打开电话权限！")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							}).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Intent localIntent = new Intent();
							localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
							localIntent.setData(Uri.fromParts("package", getPackageName(), null));
							startActivity(localIntent);
							finishMine();
						}
					});
					customBuilder.create().show();
				}
				break;
			case 100:
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				if (customDialog2 != null && customDialog2.isShowing()){
					customDialog2.dismiss();
				}

				if (grantResults[0] == PERMISSION_GRANTED) {
					// Permission Granted
					if (permissions[0].equals(Manifest.permission.CAMERA)){
						try {
							closeBroadcast();

							Intent intent = new Intent();
							intent.setClass(MainActivity.this, ActivityScanerCode.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivityForResult(intent, SCANNIN_GREQUEST_CODE);

						} catch (Exception e) {
							UIHelper.showToastMsg(context, "相机打开失败,请检查相机是否可正常使用", R.drawable.ic_error);
						}
					}
				}else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setTitle("温馨提示").setMessage("您需要在设置里允许获取相机权限！")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							}).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Intent localIntent = new Intent();
							localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
							localIntent.setData(Uri.fromParts("package", getPackageName(), null));
							startActivity(localIntent);
							finishMine();
						}
					});
					customBuilder.create().show();
				}
				break;
			case REQUEST_CODE_ASK_PERMISSIONS:
				if (grantResults[0] == PERMISSION_GRANTED) {
					// Permission Granted
					if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
						if (aMap == null) {
							aMap = mapView.getMap();
							setUpMap();
						}
						aMap.getUiSettings().setZoomControlsEnabled(false);
						aMap.getUiSettings().setMyLocationButtonEnabled(false);
						aMap.getUiSettings()
								.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);// 设置地图logo显示在右下方
						CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(18);// 设置缩放监听
						aMap.moveCamera(cameraUpdate);
						setUpLocationStyle();
					}
				} else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setTitle("温馨提示").setMessage("您需要在设置里允许获取定位权限！")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
									finishMine();
								}
							}).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							Intent localIntent = new Intent();
							localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
							localIntent.setData(Uri.fromParts("package", getPackageName(), null));
							startActivity(localIntent);
							finishMine();
						}
					});
					customBuilder.create().show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}




	//获取网络时间
	private void getNetTime() {
		URL url = null;//取得资源对象
		final DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		try {
//			url = new URL("http://www.baidu.com");
			url = new URL("http://www.ntsc.ac.cn");//中国科学院国家授时中心
			//url = new URL("http://www.bjtime.cn");
			URLConnection uc = url.openConnection();//生成连接对象
			uc.connect(); //发出连接
			long ld = uc.getDate(); //取得网站日期时间
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(ld);
			final String format = formatter.format(calendar.getTime());
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (SharedPreferencesUrls.getInstance().getString("date","") != null &&
							!"".equals(SharedPreferencesUrls.getInstance().getString("date",""))){
						if (!format.equals(SharedPreferencesUrls.getInstance().getString("date",""))){
							UpdateManager.getUpdateManager().checkAppUpdate(context, true);
							SharedPreferencesUrls.getInstance().putString("date",""+format);
						}
					}else {
						// 版本更新
						UpdateManager.getUpdateManager().checkAppUpdate(context, true);
						SharedPreferencesUrls.getInstance().putString("date",""+format);
					}
				}
			});
		} catch (Exception e) {
			String date = formatter.format(new Date());
			if (SharedPreferencesUrls.getInstance().getString("date","") != null &&
					!"".equals(SharedPreferencesUrls.getInstance().getString("date",""))){
				if (!date.equals(SharedPreferencesUrls.getInstance().getString("date",""))){
					UpdateManager.getUpdateManager().checkAppUpdate(context, true);
					SharedPreferencesUrls.getInstance().putString("date",""+date);
				}
			}else {
				// 版本更新
				UpdateManager.getUpdateManager().checkAppUpdate(context, true);
				SharedPreferencesUrls.getInstance().putString("date",""+date);
			}
			e.printStackTrace();
		}
	}


	protected void handleReceiver(Context context, Intent intent) {
		// 广播处理
		if (intent == null) {
			return;
		}

		String action = intent.getAction();
		String data = intent.getStringExtra("data");
		switch (action) {
			case BluetoothAdapter.ACTION_STATE_CHANGED:

				ToastUtil.showMessage(context,"main===蓝牙CHANGED");
				int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
				switch(blueState){

					case BluetoothAdapter.STATE_TURNING_ON:
						break;

					case BluetoothAdapter.STATE_ON:
						break;

					case BluetoothAdapter.STATE_TURNING_OFF:
						ToastUtil.showMessage(context,"main===TURNING_OFF");
						break;

					case BluetoothAdapter.STATE_OFF:
						ToastUtil.showMessage(context,"main===OFF");
						break;
				}

				break;
			case Config.TOKEN_ACTION:
				isConnect = true;

				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						BaseApplication.getInstance().getIBLE().getBattery();
					}
				}, 500);
				if (null != lockLoading && lockLoading.isShowing()) {
					lockLoading.dismiss();
				}
//					isStop = true;
				ToastUtil.showMessageApp(context,"main===设备连接成功");


				break;
			case Config.BATTERY_ACTION:
//				ToastUtil.showMessage(context,"####===2");

//				BaseApplication.getInstance().getIBLE().getConnectStatus();

//				if(BaseApplication.getInstance().getIBLE().getConnectStatus()){
//					BaseApplication.getInstance().getIBLE().getLockStatus();
//				}

				if(isConnect){
					BaseApplication.getInstance().getIBLE().getLockStatus();
				}


				break;
			case Config.OPEN_ACTION:
				ToastUtil.showMessage(context,"####===3");
				break;
			case Config.CLOSE_ACTION:
				ToastUtil.showMessage(context,"####===4");
				break;
			case Config.LOCK_STATUS_ACTION:

				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				if (lockLoading != null && lockLoading.isShowing()){
					lockLoading.dismiss();
				}

				if (TextUtils.isEmpty(data)) {

					ToastUtil.showMessageApp(context,"main====锁已关闭");
					Log.e("main===","main===锁已关闭");

					//锁已关闭


					if (!isContainsList.contains(true) && macList.size() <= 0){
						customDialog3.show();
					}else{
						submit(uid, access_token);
					}



				} else {
					//锁已开启
					ToastUtil.showMessageApp(context,"main====您还未上锁，请给车上锁后还车");
				}
				break;
			case Config.LOCK_RESULT:

				PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
				boolean screenOn = pm.isScreenOn();
				if (!screenOn) {
					// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
					PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
					wl.acquire();
//					wl.acquire(10000); // 点亮屏幕
					wl.release(); // 释放
				}

				// 屏幕解锁
				KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
				KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
//				KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
				// 屏幕锁定
//				keyguardLock.reenableKeyguard();
				keyguardLock.disableKeyguard(); // 解锁



				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				if (lockLoading != null && lockLoading.isShowing()){
					lockLoading.dismiss();
				}


				ToastUtil.showMessageApp(context,"main===恭喜您，您已成功上锁");
                Log.e("main===","main===恭喜您，您已成功上锁");

				endBtn(context);

				break;
		}
	}

	protected void submit(String uid, String access_token){

		Log.e("base===",uid+"==="+access_token+"==="+oid+"==="+referLatitude+"==="+referLongitude);

		RequestParams params = new RequestParams();
		params.put("uid", uid);
		params.put("access_token", access_token);
		params.put("oid", oid);
		params.put("latitude", referLatitude);
		params.put("longitude", referLongitude);
		if (macList.size() > 0){
			params.put("xinbiao",macList.get(0));
		}
		HttpHelper.post(context, Urls.backBikescan, params, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				if (loadingDialog != null && !loadingDialog.isShowing()) {
					loadingDialog.setTitle("正在提交");
					loadingDialog.show();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				UIHelper.ToastError(context, throwable.toString());
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				Log.e("base===","结束用车:"+responseString);
				try {
					ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
					if (result.getFlag().equals("Success")) {

						SharedPreferencesUrls.getInstance().putString("type","");
						SharedPreferencesUrls.getInstance().putString("m_nowMac","");
						SharedPreferencesUrls.getInstance().putString("oid","");
						SharedPreferencesUrls.getInstance().putString("osn","");
						SharedPreferencesUrls.getInstance().putString("type","");
						SharedPreferencesUrls.getInstance().putBoolean("isStop",true);
						SharedPreferencesUrls.getInstance().putString("biking_latitude","");
						SharedPreferencesUrls.getInstance().putString("biking_longitude","");

						if ("1".equals(result.getData())){
							ToastUtil.showMessageApp(context, result.getMsg());
							if ("已为您免单,欢迎反馈问题".equals(result.getMsg())){

								ToastUtil.showMessage(context,"context==="+context);

								if(context instanceof CurRoadStartActivity){
									CurRoadStartActivity.isEnd = true;
									CurRoadStartActivity.instance.finish();
								}

								tz = 1;
								UIHelper.goToAct(context, FeedbackActivity.class);
//								UIHelper.goToAct(context, Main2Activity.class);
//                              scrollToFinishActivity();

								Log.e("base===","base===Feedback");
							}else {
								tz = 2;
								Intent intent = new Intent(context, HistoryRoadDetailActivity.class);
								intent.putExtra("oid",oid);
								startActivity(intent);

								Log.e("base===","base===HistoryRoadDetail==="+oid);
							}
						}else {
							ToastUtil.showMessageApp(context,"恭喜您,还车成功,请支付!");

							tz = 3;
							UIHelper.goToAct(context, CurRoadBikedActivity.class);

//							Intent intent = new Intent(getApplicationContext(),  CurRoadBikedActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//							startActivity(intent);


							Log.e("base===","base===CurRoadBiked");
						}
//                        scrollToFinishActivity();

					}else {
						ToastUtil.showMessageApp(context, "base===="+result.getMsg());
					}
				}catch (Exception e){

				}
				if (loadingDialog != null && loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				if (customDialog3 != null && customDialog3.isShowing()){
					customDialog3.dismiss();
				}
			}
		});
	}

	public void endBtn(final Context context){
		final String uid = SharedPreferencesUrls.getInstance().getString("uid","");
		final String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
		if (uid == null || "".equals(uid) || access_token == null || "".equals(access_token)){
			ToastUtil.showMessageApp(context,"请先登录账号");
			UIHelper.goToAct(context,LoginActivity.class);
		}else {
			ToastUtil.showMessage(context,uid+"==="+access_token);
			ToastUtil.showMessage(context,macList+"==="+isContainsList);
			ToastUtil.showMessage(context,macList.size()+"==="+isContainsList.contains(true));

			if (isContainsList.contains(true)){
				if ("1".equals(type)){
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setTitle("温馨提示").setMessage("还车必须到校内关锁并拨乱数字密码，距车一米内在APP点击结束!")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							}).setPositiveButton("确认", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							submit(uid,access_token);
						}
					});
					customBuilder.create().show();
				}else {
//                    flag = 2;
					if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
						ToastUtil.showMessageApp(context, "您的设备不支持蓝牙4.0");
						finish();
//                        scrollToFinishActivity();
					}
					//蓝牙锁
					if (!BaseApplication.getInstance().getIBLE().isEnable()){
						BaseApplication.getInstance().getIBLE().enableBluetooth();
						return;
					}
					if (BaseApplication.getInstance().getIBLE().getConnectStatus()){

						if (loadingDialog != null && !loadingDialog.isShowing()){
							loadingDialog.setTitle("请稍等");
							loadingDialog.show();
						}

						BaseApplication.getInstance().getIBLE().getLockStatus();
					}else {

						if (lockLoading != null && !lockLoading.isShowing()){
							lockLoading.setTitle("正在连接");
							lockLoading.show();
						}

						m_myHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								ToastUtil.showMessage(context, BaseApplication.getInstance().getIBLE().getConnectStatus()+"==="+BaseApplication.getInstance().getIBLE().getLockStatus());

								if (lockLoading != null && lockLoading.isShowing()){
									lockLoading.dismiss();
								}

								if (!BaseApplication.getInstance().getIBLE().getConnectStatus()){
									CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
									customBuilder.setTitle("连接失败").setMessage("关锁后，请离车1米内重试或在右上角提交")
											.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													dialog.cancel();
												}
											});
									customBuilder.create().show();
								}

							}
						}, 10 * 1000);

						connect();

					}
				}
			}else {
				if (macList.size() > 0 && !"1".equals(type)){
					if (!TextUtils.isEmpty(m_nowMac)) {
						//蓝牙锁
						if (!BaseApplication.getInstance().getIBLE().isEnable()){
							BaseApplication.getInstance().getIBLE().enableBluetooth();
							return;
						}
						if (BaseApplication.getInstance().getIBLE().getConnectStatus()){
							BaseApplication.getInstance().getIBLE().getLockStatus();
						}else {
							if (lockLoading != null && !lockLoading.isShowing()){
								lockLoading.setTitle("正在连接");
								lockLoading.show();
							}
							connect();
						}
					}
				}else {
					customDialog3.show();
				}
			}
		}
	}




}