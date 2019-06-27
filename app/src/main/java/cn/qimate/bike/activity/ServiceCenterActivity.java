package cn.qimate.bike.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.androidquery.util.Common;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.jock.pickerview.view.view.OptionsPickerView;
import cn.loopj.android.http.RequestParams;
import cn.loopj.android.http.TextHttpResponseHandler;
import cn.qimate.bike.R;
import cn.qimate.bike.adapter.CommonQuestionAdapter;
import cn.qimate.bike.adapter.HistoryRoadAdapter;
import cn.qimate.bike.adapter.HotQuestionAdapter;
import cn.qimate.bike.adapter.QuestionAdapter;
import cn.qimate.bike.core.common.HttpHelper;
import cn.qimate.bike.core.common.SharedPreferencesUrls;
import cn.qimate.bike.core.common.UIHelper;
import cn.qimate.bike.core.common.Urls;
import cn.qimate.bike.core.widget.CustomDialog;
import cn.qimate.bike.core.widget.LoadingDialog;
import cn.qimate.bike.model.CommonQuestionBean;
import cn.qimate.bike.model.GlobalConfig;
import cn.qimate.bike.model.HistoryRoadBean;
import cn.qimate.bike.model.HotQuestionBean;
import cn.qimate.bike.model.QuestionBean;
import cn.qimate.bike.model.ResultConsel;
import cn.qimate.bike.model.UserIndexBean;
import cn.qimate.bike.swipebacklayout.app.SwipeBackActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 客服中心
 * Created by yuanyi on 2019/3/19 0012.
 */

public class ServiceCenterActivity extends SwipeBackActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener,AdapterView.OnItemClickListener {

    private Context context;
    private LoadingDialog loadingDialog;
    private LinearLayout ll_back;
    private TextView title;
    private TextView rightBtn;

    private TextView nameEdit;
    private TextView phoneNum;
    private RelativeLayout rl_name;
    private RelativeLayout rl_phoneNum;
    private RelativeLayout rl_realNameAuth;
    private RelativeLayout rl_studentAuth;

    private OptionsPickerView pvOptions;
    private OptionsPickerView pvOptions1;
    private OptionsPickerView pvOptions2;
    private String sex = "";
    private String school = "";

    private ImageView backImg;
    // List
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView myList;
    private SwipeRefreshLayout swipeRefreshLayout2;
    private ListView myList2;

    private View footerView;
    private View footerViewType01;
    private View footerViewType02;
    private View footerViewType03;
    private View footerViewType04;
    private View footerViewType05;
    private View footerLayout;

    private View footerView2;
    private View footerViewType201;
    private View footerViewType202;
    private View footerViewType203;
    private View footerViewType204;
    private View footerViewType205;
    private View footerLayout2;

    private HotQuestionAdapter myHotAdapter;
    private CommonQuestionAdapter myCommonAdapter;
//    private List<QuestionBean> data;
    private List<HotQuestionBean> hotData;
    private List<CommonQuestionBean> commonData;
    private boolean isRefresh = true;// 是否刷新中
    private boolean isLast = false;
    private int showPage = 1;
    private String starttime = "";
    private String endtime = "";

    private String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_center);
        context = this;
//        data = new ArrayList<>();
        hotData = new ArrayList<>();
        commonData = new ArrayList<>();

//        data = getIntent().getStringExtra("data");

        initView();
    }

    private void initView(){

        loadingDialog = new LoadingDialog(context);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

//        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        backImg = (ImageView) findViewById(R.id.mainUI_title_backBtn);
        title = (TextView) findViewById(R.id.mainUI_title_titleText);
        title.setText("客服中心");
        rightBtn = (TextView) findViewById(R.id.mainUI_title_rightBtn);
        rightBtn.setText("拨打客服电话");

        // list投资列表
        footerView = LayoutInflater.from(context).inflate(R.layout.footer_item, null);
        footerViewType01 = footerView.findViewById(R.id.footer_Layout_type01);// 点击加载更多
        footerViewType02 = footerView.findViewById(R.id.footer_Layout_type02);// 正在加载，请您稍等
        footerViewType03 = footerView.findViewById(R.id.footer_Layout_type03);// 已无更多
        footerViewType04 = footerView.findViewById(R.id.footer_Layout_type04);// 刷新失败，请重试
        footerViewType05 = footerView.findViewById(R.id.footer_Layout_type05);// 暂无数据

        footerLayout = footerView.findViewById(R.id.footer_Layout);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.Layout_swipeParentLayout);
        myList = (ListView)findViewById(R.id.Layout_swipeListView);
        myList.addFooterView(footerView);

//        footerLayout.setVisibility(View.GONE);
//        isLast = true;
//        footerViewType01.setVisibility(View.GONE);
//        footerViewType02.setVisibility(View.GONE);
//        footerViewType03.setVisibility(View.GONE);
//        footerViewType04.setVisibility(View.GONE);
//        footerViewType05.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark), getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light), getResources().getColor(android.R.color.holo_red_light));


        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("onItemClick===1", "==="+ myHotAdapter.getDatas().get(position).getChildrens());

                String s = myHotAdapter.getDatas().get(position).getChildrens();

                if ("[]".equals(s)){
                    UIHelper.goWebViewAct(context, myHotAdapter.getDatas().get(position).getTitle(), myHotAdapter.getDatas().get(position).getH5_url());
                }else{
                    Intent intent = new Intent(context, ServiceCenter2Activity.class);
                    intent.putExtra("title", myHotAdapter.getDatas().get(position).getTitle());
                    intent.putExtra("data", s);
                    startActivity(intent);
                }
            }
        });
        if(hotData.isEmpty()){
            initHttp();
        }

        myHotAdapter = new HotQuestionAdapter(context);
        myHotAdapter.setDatas(hotData);
        myList.setAdapter(myHotAdapter);
        setListViewHeight(myList);

        footerView2 = LayoutInflater.from(context).inflate(R.layout.footer_item, null);
        footerViewType201 = footerView2.findViewById(R.id.footer_Layout_type01);// 点击加载更多
        footerViewType202 = footerView2.findViewById(R.id.footer_Layout_type02);// 正在加载，请您稍等
        footerViewType203 = footerView2.findViewById(R.id.footer_Layout_type03);// 已无更多
        footerViewType204 = footerView2.findViewById(R.id.footer_Layout_type04);// 刷新失败，请重试
        footerViewType205 = footerView2.findViewById(R.id.footer_Layout_type05);// 暂无数据

        footerLayout2 = footerView2.findViewById(R.id.footer_Layout);

        swipeRefreshLayout2 = (SwipeRefreshLayout)findViewById(R.id.Layout_swipeParentLayout2);
        myList2 = (ListView)findViewById(R.id.Layout_swipeListView2);
        myList2.addFooterView(footerView2);

        swipeRefreshLayout2.setOnRefreshListener(this);
        swipeRefreshLayout2.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark), getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light), getResources().getColor(android.R.color.holo_red_light));

        myList2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("onItemClick===", "==="+ myCommonAdapter.getDatas().get(position).getChildrens());

                String s = myCommonAdapter.getDatas().get(position).getChildrens();

                if ("[]".equals(s)){
                    UIHelper.goWebViewAct(context, myCommonAdapter.getDatas().get(position).getTitle(), myCommonAdapter.getDatas().get(position).getH5_url());
                }else{
                    Intent intent = new Intent(context, ServiceCenter2Activity.class);
                    intent.putExtra("title", myCommonAdapter.getDatas().get(position).getTitle());
                    intent.putExtra("data", s);
                    startActivity(intent);
                }

            }
        });
//        if(commonData.isEmpty()){
//            initHttp();
//        }
//        initHttp();


        myCommonAdapter = new CommonQuestionAdapter(context);
        myCommonAdapter.setDatas(commonData);
        myList2.setAdapter(myCommonAdapter);


        backImg.setOnClickListener(this);
        rightBtn.setOnClickListener(this);
        footerLayout.setOnClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//        UIHelper.goWebViewAct(context,"停车须知",Urls.phtml5 + uid);
//        ResultConsel result = JSON.parseObject(myAdapter.getDatas().get(position).getChildrens(), ResultConsel.class);

//        Log.e("onItemClick===", "==="+ myAdapter.getDatas().get(position).getChildrens());
//
//        String s = myAdapter.getDatas().get(position).getChildrens();
//
//        if ("[]".equals(s)){
//            UIHelper.goWebViewAct(context, myAdapter.getDatas().get(position).getTitle(), myAdapter.getDatas().get(position).getH5_url());
//        }else{
//            Intent intent = new Intent(context, ServiceCenter2Activity.class);
//            intent.putExtra("data", s);
//            startActivity(intent);
//        }

    }

    private void setListViewHeight(ListView listView){
        ListAdapter listAdapter = listView.getAdapter(); //得到ListView 添加的适配器
        if(listAdapter == null){
            return;
        }

        View itemView = listAdapter.getView(0, null, listView); //获取其中的一项
        //进行这一项的测量，为什么加这一步，具体分析可以参考 https://www.jianshu.com/p/dbd6afb2c890这篇文章
        itemView.measure(0,0);
        int itemHeight = itemView.getMeasuredHeight(); //一项的高度
        int itemCount = listAdapter.getCount();//得到总的项数
        LinearLayout.LayoutParams layoutParams = null; //进行布局参数的设置

        Log.e("setListViewHeight===", itemHeight+"==="+itemCount);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT ,itemHeight*(itemCount-1));
        listView.setLayoutParams(layoutParams);

        swipeRefreshLayout.setLayoutParams(layoutParams);
    }

    private void setListViewHeight2(ListView listView){
        ListAdapter listAdapter = listView.getAdapter(); //得到ListView 添加的适配器
        if(listAdapter == null){
            return;
        }

        View itemView = listAdapter.getView(0, null, listView); //获取其中的一项
        //进行这一项的测量，为什么加这一步，具体分析可以参考 https://www.jianshu.com/p/dbd6afb2c890这篇文章
        itemView.measure(0,0);
        int itemHeight = itemView.getMeasuredHeight(); //一项的高度
        int itemCount = listAdapter.getCount();//得到总的项数
        LinearLayout.LayoutParams layoutParams = null; //进行布局参数的设置

        Log.e("setListViewHeight2===", itemHeight+"==="+itemCount);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT ,itemHeight*(itemCount-1));
        listView.setLayoutParams(layoutParams);

        swipeRefreshLayout2.setLayoutParams(layoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        isRefresh = true;
        if(hotData.size()!=0){
            myHotAdapter.notifyDataSetChanged();
        }
        if(commonData.size()!=0){
            myCommonAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onRefresh() {
        showPage = 1;
        if (!isRefresh) {
            if(hotData.size()!=0){
                myHotAdapter.getDatas().clear();
                myHotAdapter.notifyDataSetChanged();
            }
            if(commonData.size()!=0){
                myCommonAdapter.getDatas().clear();
                myCommonAdapter.notifyDataSetChanged();
            }
            isRefresh = true;
            initHttp();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout2.setRefreshing(false);
        }
    }

    private void initHttp(){

        Log.e("initHttp===", "==="+Urls.question);

//        [{"childrens":[],"content":"<p>电单车还不了车~</p>","h5_url":"http://www.7mate.cn/Home/Question/show.html?id=6","title":"电单车还不了车"},{"childrens":[],"content":"<p>单车还不了车！！！</p>","h5_url":"http://www.7mate.cn/Home/Question/show.html?id=7","title":"单车还不了车"}]


        HttpHelper.get(context, Urls.question, null, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                setFooterType(1);
                setFooterType2(1);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                UIHelper.ToastError(context, throwable.toString());
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout2.setRefreshing(false);
                isRefresh = false;
                setFooterType(3);
                setFooterVisibility();

                setFooterType2(3);
                setFooterVisibility2();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
                    if ("Success".equals(result.getFlag())) {
//                        JSONArray array = new JSONArray(result.getData());
                        JSONObject jsonObject = new JSONObject(result.getData());
                        QuestionBean questionBean = JSON.parseObject(result.getData(), QuestionBean.class);

                        Log.e("initHttp===Success", questionBean.getCommon_problem()+"==="+questionBean.getHot_problem());

                        JSONArray array = new JSONArray(questionBean.getHot_problem());
                        JSONArray array2 = new JSONArray(questionBean.getCommon_problem());

                        if (array.length() == 0 && showPage == 1) {
                            footerLayout.setVisibility(View.VISIBLE);
                            setFooterType(4);
                            return;
                        } else if (array.length() < GlobalConfig.PAGE_SIZE && showPage == 1) {
                            footerLayout.setVisibility(View.GONE);
                            setFooterType(5);
                        } else if (array.length() < GlobalConfig.PAGE_SIZE) {
                            footerLayout.setVisibility(View.VISIBLE);
                            setFooterType(2);
                        } else if (array.length() >= 10) {
                            footerLayout.setVisibility(View.VISIBLE);
                            setFooterType(0);
                        }

                        if (array2.length() == 0 && showPage == 1) {
                            footerLayout.setVisibility(View.VISIBLE);
                            setFooterType2(4);
                            return;
                        } else if (array2.length() < GlobalConfig.PAGE_SIZE && showPage == 1) {
                            footerLayout.setVisibility(View.GONE);
                            setFooterType2(5);
                        } else if (array2.length() < GlobalConfig.PAGE_SIZE) {
                            footerLayout.setVisibility(View.VISIBLE);
                            setFooterType2(2);
                        } else if (array2.length() >= 10) {
                            footerLayout.setVisibility(View.VISIBLE);
                            setFooterType2(0);
                        }

                        Log.e("initHttp===Success2", array.length()+"==="+array2.length());

                        for (int i = 0; i < array.length(); i++) {
                            HotQuestionBean bean = JSON.parseObject(array.getJSONObject(i).toString(), HotQuestionBean.class);
                            Log.e("initHttp===Success", "333==="+bean);

                            hotData.add(bean);
                        }

                        for (int i = 0; i < array2.length(); i++) {
                            CommonQuestionBean bean = JSON.parseObject(array2.getJSONObject(i).toString(), CommonQuestionBean.class);
                            Log.e("initHttp===Success", "333==="+bean);

                            commonData.add(bean);
                        }

                        setListViewHeight(myList);
                        setListViewHeight2(myList2);

//                        Log.e("initHttp===Success", "==="+hotQuestionBean.getTitle());

                    } else {
                        Toast.makeText(context,result.getMsg(),Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {

                    Log.e("initHttp===e", "==="+e.toString());

                } finally {
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout2.setRefreshing(false);
                    isRefresh = false;
                    setFooterVisibility();
                    setFooterVisibility2();


                }
            }
        });
    }

    private void setFooterType(int type) {
        switch (type) {
            case 0:
                isLast = false;
                footerViewType01.setVisibility(View.VISIBLE);
                footerViewType02.setVisibility(View.GONE);
                footerViewType03.setVisibility(View.GONE);
                footerViewType04.setVisibility(View.GONE);
                footerViewType05.setVisibility(View.GONE);
                break;
            case 1:
                isLast = false;
                footerViewType01.setVisibility(View.GONE);
                footerViewType02.setVisibility(View.VISIBLE);
                footerViewType03.setVisibility(View.GONE);
                footerViewType04.setVisibility(View.GONE);
                footerViewType05.setVisibility(View.GONE);
                break;
            case 2:
                isLast = true;
                footerViewType01.setVisibility(View.GONE);
                footerViewType02.setVisibility(View.GONE);
                footerViewType03.setVisibility(View.VISIBLE);
                footerViewType04.setVisibility(View.GONE);
                footerViewType05.setVisibility(View.GONE);
                break;
            case 3:
                isLast = false;
                // showPage -= 1;
                footerViewType01.setVisibility(View.GONE);
                footerViewType02.setVisibility(View.GONE);
                footerViewType03.setVisibility(View.GONE);
                footerViewType04.setVisibility(View.VISIBLE);
                footerViewType05.setVisibility(View.GONE);
                break;
            case 4:
                isLast = true;
                footerViewType01.setVisibility(View.GONE);
                footerViewType02.setVisibility(View.GONE);
                footerViewType03.setVisibility(View.GONE);
                footerViewType04.setVisibility(View.GONE);
                footerViewType05.setVisibility(View.VISIBLE);
                break;
            case 5:
                isLast = true;
                footerViewType01.setVisibility(View.GONE);
                footerViewType02.setVisibility(View.GONE);
                footerViewType03.setVisibility(View.GONE);
                footerViewType04.setVisibility(View.GONE);
                footerViewType05.setVisibility(View.GONE);
                break;
        }
    }

    private void setFooterVisibility() {
        if (footerView.getVisibility() == View.GONE) {
            footerView.setVisibility(View.VISIBLE);
        }
    }

    private void setFooterType2(int type) {
        switch (type) {
            case 0:
                isLast = false;
                footerViewType201.setVisibility(View.VISIBLE);
                footerViewType202.setVisibility(View.GONE);
                footerViewType203.setVisibility(View.GONE);
                footerViewType204.setVisibility(View.GONE);
                footerViewType205.setVisibility(View.GONE);
                break;
            case 1:
                isLast = false;
                footerViewType201.setVisibility(View.GONE);
                footerViewType202.setVisibility(View.VISIBLE);
                footerViewType203.setVisibility(View.GONE);
                footerViewType204.setVisibility(View.GONE);
                footerViewType205.setVisibility(View.GONE);
                break;
            case 2:
                isLast = true;
                footerViewType201.setVisibility(View.GONE);
                footerViewType202.setVisibility(View.GONE);
                footerViewType203.setVisibility(View.VISIBLE);
                footerViewType204.setVisibility(View.GONE);
                footerViewType205.setVisibility(View.GONE);
                break;
            case 3:
                isLast = false;
                // showPage -= 1;
                footerViewType201.setVisibility(View.GONE);
                footerViewType202.setVisibility(View.GONE);
                footerViewType203.setVisibility(View.GONE);
                footerViewType204.setVisibility(View.VISIBLE);
                footerViewType205.setVisibility(View.GONE);
                break;
            case 4:
                isLast = true;
                footerViewType201.setVisibility(View.GONE);
                footerViewType202.setVisibility(View.GONE);
                footerViewType203.setVisibility(View.GONE);
                footerViewType204.setVisibility(View.GONE);
                footerViewType205.setVisibility(View.VISIBLE);
                break;
            case 5:
                isLast = true;
                footerViewType201.setVisibility(View.GONE);
                footerViewType202.setVisibility(View.GONE);
                footerViewType203.setVisibility(View.GONE);
                footerViewType204.setVisibility(View.GONE);
                footerViewType205.setVisibility(View.GONE);
                break;
        }
    }

    private void setFooterVisibility2() {
        if (footerView2.getVisibility() == View.GONE) {
            footerView2.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        switch (v.getId()){
            case R.id.mainUI_title_backBtn:
                scrollToFinishActivity();
                break;

            case R.id.mainUI_title_rightBtn:
                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = checkSelfPermission(Manifest.permission.CALL_PHONE);
                    if (checkPermission != PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                            requestPermissions(new String[] { Manifest.permission.CALL_PHONE }, 0);
                        } else {
                            CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
                            customBuilder.setTitle("温馨提示").setMessage("您需要在设置里打开拨打电话权限！")
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }).setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    requestPermissions(
                                            new String[] { Manifest.permission.CALL_PHONE }, 0);
                                }
                            });
                            customBuilder.create().show();
                        }
                        return;
                    }
                }
                CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
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

//            case R.id.rl_name:
//                UIHelper.goToAct(context, ChangePhoneNumActivity.class);
//                break;
//
//            case R.id.rl_phoneNum:
//                UIHelper.goToAct(context, ChangePhoneNumActivity.class);
//                break;
//
//            case R.id.rl_realNameAuth:
//                UIHelper.goToAct(context, RealNameAuthActivity.class);
//                break;

//            case R.id.rl_studentAuth:
//                UIHelper.goToAct(context, ChangePhoneNumActivity.class);
//                break;

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            scrollToFinishActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
