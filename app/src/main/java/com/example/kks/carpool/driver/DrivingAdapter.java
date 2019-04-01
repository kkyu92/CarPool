package com.example.kks.carpool.driver;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kks.carpool.R;
import com.example.kks.carpool.model.DrivingItem;

import java.util.ArrayList;

public class DrivingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DrivingItem> drivingItems = new ArrayList<>();
    private requestCallback callback;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driving_item, parent, false);
        return new drivingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof drivingViewHolder) {
            final drivingViewHolder viewHolder = (drivingViewHolder) holder;

            Log.e("바인드 뷰 홀더:::", "상대방 이름:" + drivingItems.get(position).getTarget_name());
            Log.e("바인드 뷰 홀더:::", "출발지:" + drivingItems.get(position).getStart_place());
            Log.e("바인드 뷰 홀더:::", "도착지:" + drivingItems.get(position).getEnd_place());
            Log.e("바인드 뷰 홀더:::", "출발시간:" + drivingItems.get(position).getDate_time());
            Log.e("바인드 뷰 홀더:::", "소요시간:" + drivingItems.get(position).getDistance_time());
            Log.e("바인드 뷰 홀더:::", "평점:" + drivingItems.get(position).getRating());
            Log.e("바인드 뷰 홀더:::", "요금:" + drivingItems.get(position).getFare());
            Log.e("바인드 뷰 홀더:::", "idx:" + drivingItems.get(position).getIdx());

            String target_name = drivingItems.get(position).getTarget_name();
            String target_profile = drivingItems.get(position).getTarget_profile();
            String start_place = drivingItems.get(position).getStart_place();
            String end_place = drivingItems.get(position).getEnd_place();
            String date_time = drivingItems.get(position).getDate_time();
            String distance_time = drivingItems.get(position).getDistance_time();
            String rating = drivingItems.get(position).getRating();
            String fare = drivingItems.get(position).getFare();
            String idx = drivingItems.get(position).getIdx();

            viewHolder.target_id.setText(target_name);
            viewHolder.start_place.setText(start_place);
            viewHolder.end_place.setText(end_place);
            viewHolder.date_time.setText(date_time);
            viewHolder.distance_time.setText(distance_time);
            viewHolder.fare.setText(fare);
            Log.e("이미지 경로표시 :: ", "" + target_profile);
            Glide.with(viewHolder.itemView.getContext()).load(target_profile).apply(new RequestOptions().circleCrop()).into(viewHolder.target_profile);


            // 운행내역 아이템 클릭
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewHolder.check == 0) {
                        viewHolder.start.setVisibility(View.VISIBLE);
                        viewHolder.end.setVisibility(View.VISIBLE);
                        viewHolder.time.setVisibility(View.VISIBLE);
                        viewHolder.start_place.setVisibility(View.VISIBLE);
                        viewHolder.end_place.setVisibility(View.VISIBLE);
                        viewHolder.distance_time.setVisibility(View.VISIBLE);
                        viewHolder.target_id.setVisibility(View.VISIBLE);
                        viewHolder.target_profile.setVisibility(View.VISIBLE);
                        viewHolder.more.setVisibility(View.GONE);
                        viewHolder.check = 1;
                    } else {
                        viewHolder.start.setVisibility(View.GONE);
                        viewHolder.end.setVisibility(View.GONE);
                        viewHolder.time.setVisibility(View.GONE);
                        viewHolder.start_place.setVisibility(View.GONE);
                        viewHolder.end_place.setVisibility(View.GONE);
                        viewHolder.distance_time.setVisibility(View.GONE);
                        viewHolder.target_id.setVisibility(View.GONE);
                        viewHolder.target_profile.setVisibility(View.GONE);
                        viewHolder.more.setVisibility(View.VISIBLE);
                        viewHolder.check = 0;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return drivingItems.size();
    }

    public void setCallback(requestCallback callback) {
        this.callback = callback;
    }

    public static class drivingViewHolder extends RecyclerView.ViewHolder {

        public TextView start, end, time;
        public TextView start_place, end_place, date_time, distance_time, fare, target_id;
        public ImageView target_profile, more;
        public int check;

        public drivingViewHolder(View itemView) {
            super(itemView);

            start = itemView.findViewById(R.id.textView13);
            end = itemView.findViewById(R.id.textView14);
            time = itemView.findViewById(R.id.time);
            start_place = itemView.findViewById(R.id.start_place);
            end_place = itemView.findViewById(R.id.end_place);
            date_time = itemView.findViewById(R.id.date_time);
            distance_time = itemView.findViewById(R.id.distance_time);
            fare = itemView.findViewById(R.id.fare);
            target_id = itemView.findViewById(R.id.target_id);
            target_profile = itemView.findViewById(R.id.target_profile);
            more = itemView.findViewById(R.id.more);
            check = 0;
        }
    }

    public void setData(ArrayList<DrivingItem> itemLists) {
        this.drivingItems = itemLists;
    }
}
