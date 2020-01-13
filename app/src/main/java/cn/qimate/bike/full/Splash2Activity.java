package cn.qimate.bike.full;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import cn.jpush.android.api.JPushInterface;
import cn.loopj.android.http.RequestParams;
import cn.loopj.android.http.TextHttpResponseHandler;
import cn.qimate.bike.R;
import cn.qimate.bike.activity.CrashHandler;
import cn.qimate.bike.activity.MainActivity;
import cn.qimate.bike.base.BaseActivity;
import cn.qimate.bike.core.common.AppManager;
import cn.qimate.bike.core.common.HttpHelper;
import cn.qimate.bike.core.common.Md5Helper;
import cn.qimate.bike.core.common.NetworkUtils;
import cn.qimate.bike.core.common.SharedPreferencesUrls;
import cn.qimate.bike.core.common.Urls;
import cn.qimate.bike.core.widget.CustomDialog;
import cn.qimate.bike.model.BannerBean;
import cn.qimate.bike.model.ResultConsel;
import cn.qimate.bike.util.ToastUtil;

@SuppressLint("NewApi")
public class Splash2Activity extends BaseActivity implements View.OnClickListener{

	public static boolean isForeground = false;

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = new AMapLocationClientOption();
	private ImageView loadingImage;
	private LinearLayout skipLayout;
	private TextView skipTime;
	private String imageUrl;
	private String h5_title;
	private String ad_link;
	private String app_type;
	private String app_id;

	private boolean isStop = false;
	private int num = 5;
	private static long ExitTime = 0;
	private boolean isEnd = false;
	private WebView myWebView;
	private WebView webView;
	private Context context;
	String ss = "";

	private Runnable runnable;

//	private Myhandler myhandler;

	private Handler handler = new MainHandler(this);

	private boolean flag = true;
	private boolean isTz = false;
	private String action_content;
	private boolean isAdv = false;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main_enter2);
		context = this;

		isForeground = true;

//		android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);

		CrashHandler.getInstance().init(this);

		if (SharedPreferencesUrls.getInstance().getBoolean("isStop", true)) {
			SharedPreferencesUrls.getInstance().putString("m_nowMac", "");
		}
		if ("".equals(SharedPreferencesUrls.getInstance().getString("m_nowMac", ""))) {
			SharedPreferencesUrls.getInstance().putBoolean("isStop", true);
			SharedPreferencesUrls.getInstance().putBoolean("switcher", false);
		}

		ToastUtil.showMessage(this, SharedPreferencesUrls.getInstance().getBoolean("isStop", true) + "===" + SharedPreferencesUrls.getInstance().getString("m_nowMac", ""));

		loadingImage = findViewById(R.id.plash_loading_main);
		skipLayout = findViewById(R.id.plash_loading_skipLayout);
		skipTime = findViewById(R.id.plash_loading_skipTime);

		loadingImage.setOnClickListener(this);

		initHttp();
		init();

	}

	@Override
	protected void onResume() {
		isForeground = true;
		super.onResume();
		JPushInterface.onResume(this);

		Log.e("splash===onResume", "===");

//		m_myHandler = new Handler();
//		myhandler = new Myhandler();

		if(isStop == true && isEnd == true){
			isStop = false;
			isEnd = false;

			handler.sendEmptyMessageDelayed(0, 900);
		}

//		m_myHandler.sendEmptyMessage(0);
//		m_myHandler.sendEmptyMessageDelayed(0, 900);
//		m_myHandler.postDelayed(myhandler, 900);
//		m_myHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				time();
//			}
//		}, 900);

	}

	private void init() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int checkPermission = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
			if (checkPermission != PackageManager.PERMISSION_GRANTED) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
				} else {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
				}
				return;
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int checkPermission = this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
			if (checkPermission != PackageManager.PERMISSION_GRANTED) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
					requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
				} else {
					requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
				}
				return;
			}
		}

		// <!-- 写入扩展存储，向扩展卡写入数据，用于写入离线定位数据 -->
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int checkPermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (checkPermission != PackageManager.PERMISSION_GRANTED) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
				} else {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
				}
				return;
			}
		}

		// <!-- 写入扩展存储，向扩展卡写入数据，用于写入离线定位数据 -->
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int checkPermission = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
			if (checkPermission != PackageManager.PERMISSION_GRANTED) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
					requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
				} else {
					requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
				}
				return;
			}
		}

		if (null == locationOption) {
			locationOption = new AMapLocationClientOption();
		}
		initjpush();
		registerMessageReceiver();
		initLocation();

		skipLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Log.e("skipLayout===", "==="+isTz);

				if(!isTz){
					isTz = true;

					try{
						tz();

					}catch (Exception e){

					}
				}


			}
		});





//		Countdown();
	}

	private void tz(){


		if(!isStop && !isEnd){

			isStop = true;
			isEnd = true;

			Log.e("splash===init", isAdv+"==="+getVersion()+"==="+SharedPreferencesUrls.getInstance().getBoolean("isFirst", true)+"==="+SharedPreferencesUrls.getInstance().getInt("version", 0));



			synchronized(ss){

				skipLayout.setEnabled(false);
//				UIHelper.goToAct(context, MainActivity.class);

				Intent intent = new Intent(context, MainActivity.class);
				if(isAdv){
					intent.putExtra("h5_title", h5_title);
					intent.putExtra("action_content", action_content);
				}

				startActivity(intent);

				ToastUtil.showMessage(this,  "===111" );

				finishMine();

//				if ((!SharedPreferencesUrls.getInstance().getBoolean("isFirst", true) && getVersion() == SharedPreferencesUrls.getInstance().getInt("version", 0))) {
//					UIHelper.goToAct(context, MainActivity.class);
//
//					ToastUtil.showMessage(this,  "===111" );
//				} else {
//					SharedPreferencesUrls.getInstance().putBoolean("isFirst", false);
//					SharedPreferencesUrls.getInstance().putInt("version", getVersion());
//					UIHelper.goToAct(context, EnterActivity.class);
//
//					ToastUtil.showMessage(this,  "===222" );
//				}
			}

//			UIHelper.goToAct(context, InterstitialActivity.class);

		}


	}


//	private synchronized void tz(){
//
//		if(!isStop && !isEnd){
//
//			isStop = true;
//			isEnd = true;
//
////			handler.removeMessages(0);
//
//			if ((!SharedPreferencesUrls.getInstance().getBoolean("isFirst", true) && getVersion() == SharedPreferencesUrls.getInstance().getInt("version", 0))) {
//				UIHelper.goToAct(context, MainFragment.class);
//			} else {
//				SharedPreferencesUrls.getInstance().putBoolean("isFirst", false);
//				SharedPreferencesUrls.getInstance().putInt("version", getVersion());
//				UIHelper.goToAct(context, EnterActivity.class);
//			}
//
//			finishMine();
//
//		}
//	}



	@Override
	protected void onPause() {
		isForeground = false;
		super.onPause();
		JPushInterface.onPause(this);

//		try {
//			if (internalReceiver != null) {
//				unregisterReceiver(internalReceiver);
//				internalReceiver = null;
//			}
//		} catch (Exception e) {
//			ToastUtil.showMessage(this, "eee===="+e);
//		}
	}

	@Override
	protected void onDestroy() {
		isForeground = false;
		super.onDestroy();

//		handler.removeCallbacksAndMessages(null);

		unregisterReceiver(mMessageReceiver);

		stopLocation();

		isStop = true;
		isEnd = true;

		handler.removeMessages(0);

//		if (runnable != null) {
//			handler.removeCallbacks(runnable);
//		}


//		m_myHandler.removeCallbacks(myhandler);

	}

//	@Override
//	public void deactivate() {
//		mListener = null;
//		if (mlocationClient != null) {
//			mlocationClient.stopLocation();
//			mlocationClient.onDestroy();
//		}
//		mlocationClient = null;
//	}


	private static class MainHandler extends Handler {
//		class MainHandler extends Handler {
		WeakReference<Splash2Activity> softReference;

		public MainHandler(Splash2Activity activity) {
			softReference = new WeakReference<Splash2Activity>(activity);
		}

		@Override
		public void handleMessage(Message mes) {
			Splash2Activity splashActivity = softReference.get();

			switch (mes.what) {
				case 0:
//					time();
					splashActivity.time();
					break;
				default:
					break;
			}
		}
	}

//	protected Handler handler = new Handler(new Handler.Callback() {
//		@Override
//		public boolean handleMessage(Message mes) {
//			switch (mes.what) {
//				case 0:
//					time();
//					break;
//
//				default:
//					break;
//			}
//			return false;
//		}
//	});

	private void time(){

		if (num != 0) {
			skipLayout.setVisibility(View.VISIBLE);
			skipTime.setText("" + (--num) + "s");

			if(num==1){
//				skipLayout.setVisibility(View.GONE);
				tz();
			}

		} else {
//			skipLayout.setVisibility(View.GONE);
//
//			tz();

//			if (!isStop) {
//
//				isStop = true;
//				isEnd = true;

//				synchronized (this) {
//
//					if ((!SharedPreferencesUrls.getInstance().getBoolean("isFirst", true) && getVersion() == SharedPreferencesUrls.getInstance().getInt("version", 0))) {
//						UIHelper.goToAct(context, Main2Activity.class);
//					} else {
//						SharedPreferencesUrls.getInstance().putBoolean("isFirst", false);
//						SharedPreferencesUrls.getInstance().putInt("version", getVersion());
//						UIHelper.goToAct(context, EnterActivity.class);
//					}
//
//					finishMine();
//				}
//			}

		}
		if (!isEnd && !isStop) {
//			m_myHandler.sendEmptyMessage(0);
			handler.sendEmptyMessageDelayed(0, 900);
//			m_myHandler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					time();
//				}
//			}, 900);
		}
	}


	@Override
	public void onClick(View view) {
//		String uid = SharedPreferencesUrls.getInstance().getString("uid","");
//		String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
		switch (view.getId()){
			case R.id.plash_loading_main:

				isAdv = true;

				tz();

//				isStop = true;

				Log.e("sa===onClick", "ui_adv==="+app_type+"==="+h5_title+"==="+action_content);
//
//				UIHelper.goWebViewAct(context, h5_title, action_content);

				break;



			default:
				break;
		}
	}





//	@Override
//	public void onBackPressed() {
//	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.e("main===WebView", myWebView+"===");
//
//		if(myWebView!=null){
//			myWebView.goBack();
////			return true;
//		}else{
//			if(System.currentTimeMillis()-ExitTime > 2000){
//				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
//				ExitTime=System.currentTimeMillis();
////				return false;
//			}else{
////				return super.onKeyDown(keyCode, event);
//			}
//		}
//
//		return super.onKeyDown(keyCode, event);
//
//	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AppManager.getAppManager().AppExit(context);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initHttp() {
		Log.e("sa===banner", "===");

		HttpHelper.get2(context, Urls.banner + 1, new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				onStartCommon("正在加载");
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.e("sa===banner=fail", "===" + throwable.toString());
				onFailureCommon(throwable.toString());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, final String responseString) {
				m_myHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							Log.e("sa===banner0", responseString + "===");

							ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);

							JSONArray ja_banners = new JSONArray(new JSONObject(result.getData()).getString("banners"));

							Log.e("sa===banner1", ja_banners.length() + "===" + result.data);

							if(ja_banners.length()==0){
								tz();
							}else{
								for (int i = 0; i < ja_banners.length(); i++) {
									BannerBean bean = JSON.parseObject(ja_banners.get(i).toString(), BannerBean.class);

									Log.e("sa===banner2", bean.getImage_url()+"===");

									imageUrl = bean.getImage_url();
									h5_title = bean.getH5_title();

									action_content = bean.getAction_content();
									if(action_content.contains("?")){
										action_content += "&token="+access_token;
									}else{
										action_content += "?token="+access_token;
									}

//                                imagePath.add(imageUrl);

									if (imageUrl == null || "".equals(imageUrl)) {
//								loadingImage.setBackgroundResource(R.drawable.enter_bg);
									} else {
										// 加载图片
										Glide.with(context).load(imageUrl).into(loadingImage);
									}

									skipLayout.setVisibility(View.VISIBLE);
									handler.sendEmptyMessageDelayed(0, 900);

								}
							}




//                            mBanner.setBannerTitles(imageTitle);
//                            mBanner.setImages(imagePath).setOnBannerListener(MainActivity.this).start();

						} catch (Exception e) {
//                            memberEvent(context.getClass().getName()+"_"+e.getStackTrace()[0].getLineNumber()+"_"+e.getMessage());

							if (loadingDialog != null && loadingDialog.isShowing()) {
								loadingDialog.dismiss();
							}
						}

					}
				});
			}
		});
	}


	/**
	 * 初始化定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation() {
		if (NetworkUtils.getNetWorkType(context) != NetworkUtils.NONETWORK) {
			//初始化client
			locationClient = new AMapLocationClient(this.getApplicationContext());
			//设置定位参数
			locationClient.setLocationOption(getDefaultOption());
			// 设置定位监听
			locationClient.setLocationListener(locationListener);
			startLocation();

//			PostDeviceInfo(0, 0);
		} else {
			Toast.makeText(context, "暂无网络连接，请连接网络", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption() {
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(20 * 1000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
		mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
		return mOption;
	}

	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation loc) {
			if (null != loc) {
//				Toast.makeText(context,"===="+loc.getLongitude(),Toast.LENGTH_SHORT).show();

				Log.e("onLocationChanged===", loc.getLongitude()+"==="+loc.getLongitude());

				if (0.0 != loc.getLongitude() && 0.0 != loc.getLongitude() ) {

					if(flag){
						flag = false;
						PostDeviceInfo(loc.getLatitude(), loc.getLongitude());
					}

				} else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(Splash2Activity.this);
					customBuilder.setType(3).setTitle("温馨提示").setMessage("您需要在设置里打开定位权限！")
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
					return;
				}
			} else {
				Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();
				finishMine();
			}
		}
	};

	/**
	 * 开始定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void startLocation() {
		if (null != locationClient) {
			// 设置定位参数
			locationClient.setLocationOption(locationOption);
			// 启动定位
			locationClient.startLocation();
		}

	}

	/**
	 * 停止定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void stopLocation() {
		// 停止定位
		if (null != locationClient) {
			locationClient.stopLocation();
		}

	}

	/**
	 * 销毁定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
//	private void destroyLocation() {
//		if (null != locationClient) {
//			/**
//			 * 如果AMapLocationClient是在当前Activity实例化的，
//			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
//			 */
//			locationClient.onDestroy();
//			locationClient = null;
//			locationOption = null;
//		}
//	}




	// 初始化极光
	private void initjpush() {
		JPushInterface.init(getApplicationContext()); // 初始化 JPush
	}




	// for receive customer msg from jpush server
	private MessageReceiver mMessageReceiver;
	public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";

	public void registerMessageReceiver() {
		mMessageReceiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		registerReceiver(mMessageReceiver, filter);
	}

	public class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
				String messge = intent.getStringExtra(KEY_MESSAGE);
				String extras = intent.getStringExtra(KEY_EXTRAS);
				StringBuilder showMsg = new StringBuilder();
				showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
				if (extras != null && !"".equals(extras)) {
					showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
				}
			}
		}
	}



	/**
	 * 获取版本号
	 *
	 * @return 当前应用的版本号
	 */
	public int getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			int version = info.versionCode;

			Log.e("getVersion===", "==="+version);

			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	// 提交设备信息到appinfo
	private void PostDeviceInfo(double latitude, double longitude) {
		if (NetworkUtils.getNetWorkType(context) != NetworkUtils.NONETWORK) {
			try {
				TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
					return;
				}
				String UUID = tm.getDeviceId();
//				String UUID = java.util.UUID.randomUUID().toString();

				String system_version = Build.VERSION.RELEASE;
				String device_model = new Build().MODEL;
				RequestParams params = new RequestParams();
				Md5Helper Md5Helper = new Md5Helper();
				String verify = Md5Helper.encode("7mateapp" + UUID);

				Log.e("PostDeviceInfo===0", latitude+"==="+longitude+"==="+system_version+"==="+device_model+"==="+new Build().MANUFACTURER+"==="+UUID);

				params.put("verify", verify);
				params.put("system_name", "Android");
				params.put("system_version", system_version);
				params.put("device_model", device_model);
				params.put("device_user", new Build().MANUFACTURER + device_model);
				params.put("longitude", ""+longitude);
				params.put("latitude", ""+latitude);
				params.put("UUID", UUID);
				HttpHelper.post(context, Urls.DevicePostUrl, params, new TextHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, String responseString) {
						try {
							Log.e("PostDeviceInfo===", "==="+responseString);

							ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
							if (result.getFlag().toString().equals("Success")) {

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

					}
				});
//				mThread.start();
			} catch (Exception e) {
//				showDialog();
				return;
			}
		}else{
			Toast.makeText(context,"暂无网络连接，请连接网络",Toast.LENGTH_SHORT).show();
			return;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case 0:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//						int checkPermission = this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
//						if (checkPermission != PackageManager.PERMISSION_GRANTED) {
//							if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
//								requestPermissions(new String[] { Manifest.permission.READ_PHONE_STATE }, 100);
//							} else {
//								SplashActivity.this.requestPermissions(
//										new String[] { Manifest.permission.READ_PHONE_STATE }, 100);
//								return;
//							}
//						}
//					}
					init();
				} else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setType(3).setTitle("温馨提示").setMessage("您需要在设置里打开存储空间权限！")
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
				return;
			case 1:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					init();
				} else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setType(3).setTitle("温馨提示").setMessage("您需要在设置里打开存储空间权限！")
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
				return;
			case 100:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// <!-- 写入扩展存储，向扩展卡写入数据，用于写入离线定位数据 -->
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//						int checkPermission = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
//						if (checkPermission != PackageManager.PERMISSION_GRANTED) {
//							if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//								requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
//										Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
//							} else {
//								SplashActivity.this.requestPermissions(
//										new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
//												Manifest.permission.WRITE_EXTERNAL_STORAGE },
//										0);
//								return;
//							}
//						}
//					}
					init();
				} else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setType(3).setTitle("温馨提示").setMessage("您需要在设置里允许设备信息权限！")
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
				return;
			case 101:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					init();
				}else {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
					customBuilder.setType(3).setTitle("温馨提示").setMessage("您需要在设置里定位权限！")
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
				return;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}


}