package com.brachialste.earthquakemonitor.reports;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.view.CircleImageView;

import java.util.List;

/**
 * Created by brachialste on 23/02/15.
 */
public class DataAdapter extends
        RecyclerView.Adapter<DataAdapter.DataViewHolder>{

    // Debug
    private static final String TAG = "DataAdapter";

    private List<DataBeanInfo> dataInfoList;
    private Context context;

    public DataAdapter(Context context, List<DataBeanInfo> dataInfoList) {
        this.context = context;
        this.dataInfoList = dataInfoList;
    }

    @Override
    public int getItemCount() {
        return dataInfoList.size();
    }

    @Override
    public void onBindViewHolder(DataViewHolder reporteViewHolder, int i) {
        DataBeanInfo dataBeanInfo = dataInfoList.get(i);
        // es nuevo
        reporteViewHolder.vMagnitude.setText(dataBeanInfo.magnitude);
        reporteViewHolder.vMagnitude.setTypeface(null, Typeface.BOLD);
        reporteViewHolder.vDepth.setText(dataBeanInfo.depth);
        reporteViewHolder.vPlace.setText(dataBeanInfo.place);
        reporteViewHolder.vLocation.setText(dataBeanInfo.location);
        reporteViewHolder.vDateHour.setText(dataBeanInfo.date);
        reporteViewHolder.vImage.setBorderColor(context.getResources().getColor(dataBeanInfo.color));
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.data_card, viewGroup, false);
        return new DataViewHolder(itemView);
    }

    public DataBeanInfo getItem(int position) {
        return dataInfoList.get(position);
    }

    /**
     * Holder
     */
    public static class DataViewHolder extends RecyclerView.ViewHolder{

        public TextView vMagnitude;
        public TextView vDepth;
        public TextView vPlace;
        public TextView vLocation;
        public TextView vDateHour;
        public CircleImageView vImage;

        public DataViewHolder(View v) {
            super(v);
            vMagnitude =  (TextView) v.findViewById(R.id.data_magnitude);
            vDepth =  (TextView) v.findViewById(R.id.data_depth);
            vPlace =  (TextView) v.findViewById(R.id.data_place);
            vLocation =  (TextView) v.findViewById(R.id.data_location);
            vDateHour =  (TextView) v.findViewById(R.id.data_date);
            vImage = (CircleImageView) v.findViewById(R.id.data_indicator_img);
        }
    }
}