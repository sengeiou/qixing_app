package cn.qimate.bike.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.qimate.bike.R;
import cn.qimate.bike.base.BaseViewAdapter;
import cn.qimate.bike.base.BaseViewHolder;
import cn.qimate.bike.model.CommonQuestionBean;
import cn.qimate.bike.model.HotQuestionBean;

/**
 * Created by Administrator1 on 2017/2/13.
 */

public class CommonQuestionAdapter extends BaseViewAdapter<CommonQuestionBean> {

    private LayoutInflater inflater;

    public CommonQuestionAdapter(Context context) {
        super(context);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.item_question_common, null);
        }

        View line = BaseViewHolder.get(convertView,R.id.item_question_line);
        TextView title = BaseViewHolder.get(convertView,R.id.item_question_title);

        CommonQuestionBean bean = getDatas().get(position);
        if (position == getDatas().size() -1){
            line.setVisibility(View.GONE);
        }else {
            line.setVisibility(View.VISIBLE);
        }


        Log.e("QuestionAdapter===", "==="+bean.getTitle());

//        header.setImageResource(R.drawable.his_road_icon);
//        money.setText("￥"+bean.getPrices());
//        bikeCode.setText(bean.getCodenum());
        title.setText(bean.getTitle());

        return convertView;
    }
}
