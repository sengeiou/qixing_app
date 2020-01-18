package cn.qimate.bike.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.apache.http.Header;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import cn.loopj.android.http.RequestParams;
import cn.loopj.android.http.TextHttpResponseHandler;
import cn.qimate.bike.R;
import cn.qimate.bike.adapter.ActionCenterAdapter;
import cn.qimate.bike.core.common.HttpHelper;
import cn.qimate.bike.core.common.SharedPreferencesUrls;
import cn.qimate.bike.core.common.UIHelper;
import cn.qimate.bike.core.common.Urls;
import cn.qimate.bike.model.ActionCenterBean;
import cn.qimate.bike.model.GlobalConfig;
import cn.qimate.bike.model.ResultConsel;
import cn.qimate.bike.model.UserBean;
import cn.qimate.bike.swipebacklayout.app.SwipeBackActivity;

/**
 * Created by Administrator1 on 2017/2/10.
 */

public class ActionCenterActivity extends SwipeBackActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {

    private Context context = this;
    private ImageView backImg;
    private TextView title;
    // List
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView myList;

    private View footerView;
    private View footerViewType01;
    private View footerViewType02;
    private View footerViewType03;
    private View footerViewType04;
    private View footerViewType05;

    private View footerLayout;

    private ActionCenterAdapter myAdapter;
    private List<ActionCenterBean> datas;
    private boolean isRefresh = true;// 是否刷新中
    private boolean isLast = false;
    private int showPage = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_action_center);
        datas = new ArrayList<>();
        initView();
    }

    private void initView() {

        backImg = (ImageView) findViewById(R.id.mainUI_title_backBtn);
        title = (TextView) findViewById(R.id.mainUI_title_titleText);
        title.setText("活动中心");

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

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark), getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light), getResources().getColor(android.R.color.holo_red_light));

        myList.setOnItemClickListener(this);
        if(datas.isEmpty()){
            initHttp();
        }

        myAdapter = new ActionCenterAdapter(context);
        myAdapter.setDatas(datas);
        myList.setAdapter(myAdapter);

        backImg.setOnClickListener(this);
        footerLayout.setOnClickListener(this);

//        user();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        UIHelper.goWebViewAct(context,"活动中心",Urls.activityDetail+"&id="+myAdapter.getDatas().get(position).getId());
        Intent intent = new Intent(context, ActionWebviewActivity.class);
        intent.putExtra("title","活动中心");
        intent.putExtra("link",Urls.activityDetail+"&id="+myAdapter.getDatas().get(position).getId());
        intent.putExtra("imageUrl",Urls.host+myAdapter.getDatas().get(position).getAc_thumb());
        intent.putExtra("shareTitle",myAdapter.getDatas().get(position).getTitle());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        isRefresh = true;
        if(datas.size()!=0){
            myAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onRefresh() {
        showPage = 1;
        if (!isRefresh) {
            if(datas.size()!=0){
                myAdapter.getDatas().clear();
                myAdapter.notifyDataSetChanged();
            }
            isRefresh = true;
            initHttp();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.mainUI_title_backBtn:
                scrollToFinishActivity();
                break;

            case R.id.footer_Layout:
                if (!isLast) {
                    showPage += 1;
                    initHttp();
                    myAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }
    private void initHttp(){

        RequestParams params = new RequestParams();
        params.put("page",showPage);
        params.put("pagesize",GlobalConfig.PAGE_SIZE);
        HttpHelper.get(context, Urls.activityList, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                setFooterType(1);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, final Throwable throwable) {
                m_myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UIHelper.ToastError(context, throwable.toString());
                        swipeRefreshLayout.setRefreshing(false);
                        isRefresh = false;
                        setFooterType(3);
                        setFooterVisibility();
                    }
                });

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final String responseString) {
                m_myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ResultConsel result = JSON.parseObject(responseString, ResultConsel.class);
                            if ("Success".equals(result.getFlag())){
                                JSONArray array = new JSONArray(result.getData());
                                for (int i = 0; i < array.length(); i++) {
                                    ActionCenterBean bean = JSON.parseObject(array.getJSONObject(i).toString(), ActionCenterBean.class);
                                    datas.add(bean);
                                }
                                if (array.length() == 0 && showPage == 1) {
                                    setFooterType(4);// 暂无数据
                                    return;
                                } else if (array.length() < GlobalConfig.PAGE_SIZE && showPage == 1 && array.length() >= 1) {
                                    setFooterType(5);
                                } else if (array.length() < GlobalConfig.PAGE_SIZE) {
                                    setFooterType(2);// 数据已全部加载
                                } else if (array.length() == GlobalConfig.PAGE_SIZE) {
                                    setFooterType(0);// 点击加载更多
                                }
                            } else {
                                Toast.makeText(context,result.getMsg(),Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                        } finally {
                            isRefresh = false;
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            scrollToFinishActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
}
