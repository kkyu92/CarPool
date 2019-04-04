package com.example.kks.carpool.DriverClass;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kks.carpool.R;
import com.example.kks.carpool.model.MyRoute;

import java.util.ArrayList;

public class DriverMyRouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MyRoute> myrouteItems = new ArrayList<>();
    private DriverMyRouteCallback callback;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_list_item, parent, false);
        return new myrouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof myrouteViewHolder) {
            final myrouteViewHolder viewHolder = (myrouteViewHolder) holder;

            Log.e("바인드 뷰 홀더:::", "이름:" + myrouteItems.get(position).getTitle());
            Log.e("바인드 뷰 홀더:::", "출발:" + myrouteItems.get(position).getsPlace());
            Log.e("바인드 뷰 홀더:::", "도착:" + myrouteItems.get(position).getePlace());
            Log.e("바인드 뷰 홀더:::", "이미지뷰:"+myrouteItems.get(position).getgMap());
            final String title = myrouteItems.get(position).getTitle();
            final String s = myrouteItems.get(position).getsPlace();
            final double sLat = myrouteItems.get(position).getsLat();
            final double sLon = myrouteItems.get(position).getsLon();
            final String e = myrouteItems.get(position).getePlace();
            final double eLat = myrouteItems.get(position).geteLat();
            final double eLon = myrouteItems.get(position).geteLon();
            final int idx = myrouteItems.get(position).getIdx();
            final String map = myrouteItems.get(position).getgMap();
            Uri routeMap = Uri.parse("http://54.180.95.149/uploadsMap/"+map);
            Log.e("바인드 뷰 홀더:::", "URI:"+routeMap);
            viewHolder.route_title.setText(title);
            viewHolder.route_start.setText("경로 : "+s);
            viewHolder.route_end.setText(" ~ " + e);
            Glide.with(viewHolder.itemView.getContext()).load(routeMap).into(viewHolder.gmap);

            // 삭제 버튼
            viewHolder.del_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.myRouteDel(title, idx);
                }
            });

            // 수정 버튼
            viewHolder.edit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.myRouteEdit(title, s, e, sLat, sLon, eLat, eLon, idx);
                }
            });

            // 불러오기 (아이템 클릭)
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.showToast(title);
                    callback.myRouteClick(title, s, e, sLat, sLon, eLat, eLon);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return myrouteItems.size();
    }

    public void setCallback(DriverMyRouteCallback callback) {
        this.callback = callback;
    }

    public static class myrouteViewHolder extends RecyclerView.ViewHolder {

        public TextView route_title, route_start, route_end;
        public ImageView gmap;
        public Button edit_btn, del_btn;

        public myrouteViewHolder(View itemView) {
            super(itemView);

            route_title = itemView.findViewById(R.id.route_name);
            route_start = itemView.findViewById(R.id.start_route);
            route_end = itemView.findViewById(R.id.end_route);
            gmap = itemView.findViewById(R.id.route_map);
            edit_btn = itemView.findViewById(R.id.goEdit);
            del_btn = itemView.findViewById(R.id.del_btn);
        }
    }

    public void setData(ArrayList<MyRoute> itemLists) {
        this.myrouteItems = itemLists;
    }
}
