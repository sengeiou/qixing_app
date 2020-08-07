package cn.qimate.bike.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.bumptech.glide.Glide;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.loopj.android.http.RequestParams;
import cn.loopj.android.http.TextHttpResponseHandler;
import cn.qimate.bike.R;
import cn.qimate.bike.base.BaseFragment;
import cn.qimate.bike.base.BaseViewAdapter;
import cn.qimate.bike.base.BaseViewHolder;
import cn.qimate.bike.core.common.HttpHelper;
import cn.qimate.bike.core.common.SharedPreferencesUrls;
import cn.qimate.bike.core.common.UIHelper;
import cn.qimate.bike.core.common.Urls;
import cn.qimate.bike.core.widget.LoadingDialog;
import cn.qimate.bike.model.GlobalConfig;
import cn.qimate.bike.model.RankingListBean;
import cn.qimate.bike.model.RankingUserBean;
import cn.qimate.bike.model.ResultConsel;
import cn.qimate.bike.util.LogUtil;

import static android.app.Activity.RESULT_OK;

@SuppressLint("NewApi")
public class MonthFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener{

    Unbinder unbinder;
    private View v;

    private Context context;
    private Activity activity;

//    @BindView(R.id.Layout_swipeListView)
    ListView listview;
//    @BindView(R.id.Layout_swipeParentLayout)
    SwipeRefreshLayout swipeRefreshLayout;
//    @BindView(R.id.msgText)
//    TextView msgText;

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private boolean mFirstFix = true;
    private LatLng myLocation = null;
    private Circle mCircle;
    private BitmapDescriptor successDescripter;
    private BitmapDescriptor bikeDescripter;
    private Handler handler = new Handler();
    private Marker centerMarker;
    private boolean isMovingMarker = false;

    private List<Marker> bikeMarkerList;
    private boolean isUp = false;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private int isLock = 0;


    private LoadingDialog loadingDialog;
    private Dialog dialog;
    private List<RankingListBean> datas;
    private MyAdapter myAdapter;
    private int curPosition = 0;
    private int showPage = 1;
    private boolean isRefresh = true;// 是否刷新中
    private boolean isLast = false;

    private View footerView;
    private View footerViewType01;
    private View footerViewType02;
    private View footerViewType03;
    private View footerViewType04;
    private View footerViewType05;

    private ImageView iv_type05;
    private TextView tv_type05;

    private View footerLayout;

    private String badtime="2115-02-08 20:20";
    private String codenum="";
    private String totalnum="";

//    private String card_code;
    private String cars;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_month, null);
        unbinder = ButterKnife.bind(this, v);

        return v;
    }


    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        activity = getActivity();

        datas = new ArrayList<>();

        cars = activity.getIntent().getStringExtra("cars");

        initView();

        initHttp();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            while (true){
//
//                try {
//                    Thread.sleep(30*1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                m_myHandler.sendEmptyMessage(1);
//            }
//
//            }
//        }).start();

    }

    protected Handler m_myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message mes) {
            switch (mes.what) {
                case 0:
                    break;
                case 1:
                    resetList();

                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public void resetList(){
        showPage = 1;
        badtime="2115-02-08 20:20";
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
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            //pause
        }else{
            //resume
//            resetList();
        }
    }


    private void initView(){
        loadingDialog = new LoadingDialog(context);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

//        dialog = new Dialog(context, R.style.main_publishdialog_style);
//        View tagView = LayoutInflater.from(context).inflate(R.layout.dialog_deduct_mark, null);
//        dialog.setContentView(tagView);
//        dialog.setCanceledOnTouchOutside(false);

        footerView = LayoutInflater.from(context).inflate(R.layout.footer_item, null);
        footerViewType01 = footerView.findViewById(R.id.footer_Layout_type01);// 点击加载更多
        footerViewType02 = footerView.findViewById(R.id.footer_Layout_type02);// 正在加载，请您稍等
        footerViewType03 = footerView.findViewById(R.id.footer_Layout_type03);// 已无更多
        footerViewType04 = footerView.findViewById(R.id.footer_Layout_type04);// 刷新失败，请重试
        footerViewType05 = footerView.findViewById(R.id.footer_Layout_type05);// 暂无数据
        footerLayout = footerView.findViewById(R.id.footer_Layout);
        footerViewType01.setVisibility(View.GONE);

        iv_type05 = footerView.findViewById(R.id.footer_Layout_iv_type05);
        tv_type05 = footerView.findViewById(R.id.footer_Layout_tv_type05);
        iv_type05.setImageResource(R.drawable.no_order_icon);
        tv_type05.setText("暂无数据");

        swipeRefreshLayout = (SwipeRefreshLayout)getActivity().findViewById(R.id.Layout_swipeParentLayout2);
        listview = (ListView)getActivity().findViewById(R.id.Layout_swipeListView2);
//        listview.addFooterView(footerView);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark), getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light), getResources().getColor(android.R.color.holo_red_light));

//        myList.setOnItemClickListener(this);

        myAdapter = new MyAdapter(context);
        myAdapter.setDatas(datas);
        listview.setAdapter(myAdapter);

        footerLayout.setOnClickListener(this);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                curPosition = position;
//                WindowManager.LayoutParams params1 = dialog.getWindow().getAttributes();
//                params1.width = LinearLayout.LayoutParams.MATCH_PARENT;
//                params1.height = LinearLayout.LayoutParams.MATCH_PARENT;
//                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//                dialog.getWindow().setAttributes(params1);
//                dialog.show();

//                Intent intent = new Intent(context, PayMontCartActivity.class);
//                startActivity(intent);
            }
        });

//        try{
//            JSONArray jsonArray = new JSONArray(cars);
////            List carList = new ArrayList();
////            String cars="";
//
//            for (int i = 0; i < jsonArray.length(); i++) {
//
//                RankingListBean bean = new RankingListBean();
////                bean.setCar_number(jsonArray.getJSONObject(i).getString("car_number"));
////                bean.setCar_type(jsonArray.getJSONObject(i).getString("lock_name"));
////                bean.setCar_status(jsonArray.getJSONObject(i).getInt("car_status"));
////                datas.add(bean);
//            }
//
//            myAdapter.notifyDataSetChanged();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }





    private void initHttp(){
        String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
        if (access_token == null || "".equals(access_token)){
            Toast.makeText(context,"请先登录账号", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams();
        params.put("type", 2);    //榜单类型 1周榜 2月榜 3季榜
//        params.put("page",showPage);    //当前页码
//        params.put("per_page", GlobalConfig.PAGE_SIZE);
        HttpHelper.get(context, Urls.user_rank_cycling, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                if (loadingDialog != null && !loadingDialog.isShowing()) {
                    loadingDialog.setTitle("正在加载");
                    loadingDialog.show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                UIHelper.ToastError(context, throwable.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    LogUtil.e("mf===1","==="+responseString);

                    JSONObject jsonObject = new JSONObject(responseString);


                    LogUtil.e("mf===2", jsonObject + "===" + jsonObject.getString("user"));

//                    RankingUserBean bean = JSON.parseObject(jsonObject.getString("user"), RankingUserBean.class);
//
//                    tv_update_time.setText("更新："+jsonObject.getString("update_time"));
//                    String avatar = bean.getAvatar();
//                    if(avatar==null || "".equals(avatar)){
//                        iv_header.setImageResource(R.drawable.head_icon);
//                    }else{
//                        Glide.with(context).load(avatar).into(iv_header);
//                    }
//                    tv_nickname.setText(bean.getNickname());
//                    tv_rank.setText("NO."+bean.getRank());
//                    tv_message.setText(bean.getMessage());


                    JSONArray array = new JSONArray(jsonObject.getString("ranks"));
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

                    if (array.length() == 0) {
                        footerViewType05.setVisibility(View.VISIBLE);
                    }else{
                        footerViewType05.setVisibility(View.GONE);
                    }

                    LogUtil.e("mf===3","==="+array.length());

                    for (int i = 0; i < array.length();i++){
                        RankingListBean rbean = JSON.parseObject(array.getJSONObject(i).toString(), RankingListBean.class);

//                        LogUtil.e("wf===3","==="+rbean.getCounts());

                        datas.add(rbean);
                    }

                    myAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    swipeRefreshLayout.setRefreshing(false);
//                    isRefresh = false;
//                    setFooterVisibility();
                }
                if (loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private class MyAdapter extends BaseViewAdapter<RankingListBean> {

        private LayoutInflater inflater;

        public MyAdapter(Context context){
            super(context);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.item_month_detail, null);
            }

            ImageView iv_rank = BaseViewHolder.get(convertView, R.id.iv_rank);
            TextView tv_rank = BaseViewHolder.get(convertView, R.id.tv_rank);
            ImageView iv_avatar = BaseViewHolder.get(convertView, R.id.iv_avatar);
            TextView tv_nickname = BaseViewHolder.get(convertView, R.id.tv_nickname);
            TextView tv_counts = BaseViewHolder.get(convertView, R.id.tv_counts);

            RankingListBean bean = getDatas().get(position);

            int rank = bean.getRank();

            if(rank<=3){
                iv_rank.setVisibility(View.VISIBLE);
                tv_rank.setVisibility(View.GONE);

                iv_rank.setImageResource(rank==1?R.drawable.gold_medal_icon:rank==2?R.drawable.silver_medal_icon:R.drawable.bronze_medal_icon);
            }else{
                iv_rank.setVisibility(View.GONE);
                tv_rank.setVisibility(View.VISIBLE);

                tv_rank.setText(""+rank);
            }

            String avatar = bean.getAvatar();
            if(avatar==null || "".equals(avatar)){
                iv_avatar.setImageResource(R.drawable.head_icon);
            }else{
                Glide.with(context).load(avatar).into(iv_avatar);
            }
            tv_nickname.setText(bean.getNickname());
            tv_counts.setText(""+bean.getCounts());


            return convertView;
        }
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


    @Override
    public void onResume() {
        super.onResume();
        isRefresh = true;
    }



    @Override
    public void onClick(View v) {
        String uid = SharedPreferencesUrls.getInstance().getString("uid","");
        String access_token = SharedPreferencesUrls.getInstance().getString("access_token","");
        switch (v.getId()){

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

    @Override
    public void onPause() {
        super.onPause();
    }
    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtil.e("requestCode===", "==="+requestCode);

        switch (requestCode) {

            case 1:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("QR_CODE");
                } else {
					Toast.makeText(context, "扫描取消啦!", Toast.LENGTH_SHORT).show();
                }

                LogUtil.e("requestCode===1", "==="+resultCode);
                break;

            default:
                break;

        }
    }


}
