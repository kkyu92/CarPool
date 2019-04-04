package com.example.kks.carpool.DriverClass;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kks.carpool.R;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<RequestItem> requestItems = new ArrayList<>();
    private RequestCallback callback;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new requestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof requestViewHolder) {
            final requestViewHolder viewHolder = (requestViewHolder) holder;

            Log.e("바인드 뷰 홀더:::", "거리:"+requestItems.get(position).getStart_distance());
            Log.e("바인드 뷰 홀더:::", "인원:"+requestItems.get(position).getPeople_count());
            Log.e("바인드 뷰 홀더:::", "요금:"+requestItems.get(position).getFare());
            Log.e("바인드 뷰 홀더:::", "별점:"+requestItems.get(position).getRating());
            Log.e("바인드 뷰 홀더:::", "시작점:"+requestItems.get(position).getStart_point());
            Log.e("바인드 뷰 홀더:::", "도착점:"+requestItems.get(position).getEnd_point());
            Log.e("바인드 뷰 홀더:::", "날자:"+requestItems.get(position).getStart_date());
            Log.e("바인드 뷰 홀더:::", "시간:"+requestItems.get(position).getStart_time());
            Log.e("바인드 뷰 홀더:::", "idx:"+requestItems.get(position).getIDX());

            final String name = requestItems.get(position).getName();
            final String idx = requestItems.get(position).getIDX();
            String dis = requestItems.get(position).getStart_distance();
            final String people = requestItems.get(position).getPeople_count();
            final String fare = requestItems.get(position).getFare();
            final RatingBar rating = requestItems.get(position).getRating();
            String start_point = requestItems.get(position).getStart_point();
            String end_point = requestItems.get(position).getEnd_point();
            final String date = requestItems.get(position).getStart_date();
            final String time = requestItems.get(position).getStart_time();
            final double sLat = requestItems.get(position).getsLat();
            final double sLon = requestItems.get(position).getsLon();
            final double eLat = requestItems.get(position).geteLat();
            final double eLon = requestItems.get(position).geteLon();

            viewHolder.start_distance.setText(requestItems.get(position).getStart_distance());
            viewHolder.people_count.setText(requestItems.get(position).getPeople_count());
            viewHolder.fare.setText(requestItems.get(position).getFare());
            viewHolder.rating.setRating(requestItems.get(position).getRating().getRating());
            viewHolder.start_point.setText(requestItems.get(position).getStart_point());
            viewHolder.end_point.setText(requestItems.get(position).getEnd_point());
            viewHolder.start_date.setText(requestItems.get(position).getStart_date());
            viewHolder.start_time.setText(requestItems.get(position).getStart_time());

            // 카풀 요청 아이템 클릭
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.requestItemClick(sLat, sLon, eLat, eLon, date, time, people, fare, rating, name, idx);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return requestItems.size();
    }

    public void setCallback(RequestCallback callback) {
        this.callback = callback;
    }

    public static class requestViewHolder extends RecyclerView.ViewHolder {

        public TextView start_distance, start_point, end_point, people_count, fare, start_date, start_time;
        public RatingBar rating;

        public requestViewHolder(View itemView) {
            super(itemView);

            start_distance = itemView.findViewById(R.id.start_distance);
            start_point = itemView.findViewById(R.id.start_place);
            end_point = itemView.findViewById(R.id.end_place);
            people_count = itemView.findViewById(R.id.people_c);
            fare = itemView.findViewById(R.id.fare);
            start_date = itemView.findViewById(R.id.start_date);
            start_time = itemView.findViewById(R.id.start_time);
            rating = itemView.findViewById(R.id.rating);

        }
    }

    public void setData(ArrayList<RequestItem> itemLists) {
        this.requestItems = itemLists;
    }
}
