package com.example.kks.carpool;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.example.kks.carpool.autosearch.TmapMiniMap;
import com.example.kks.carpool.autosearch.TmapRoute;
import com.example.kks.carpool.retro.ApiInterface;
import com.example.kks.carpool.retro.TmapClient;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.AutoCompleteParse.TMAP_API_KEY;

/**
 * Created by KJH on 2017-11-06.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 맵 이미지 가져오기
    private ApiInterface apiInterface;
    private AQuery aQuery;
    int check = 100;

    // 장소 아이템 리스트
    private ArrayList<com.example.kks.carpool.SearchEntity> itemLists = new ArrayList<>();
    private com.example.kks.carpool.RecyclerViewAdapterCallback callback;

    private int place_point = 0;

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView address;
        public ImageButton favorite, favorite_full;
        public Button set_place;
        public ImageView miniMap;

        public CustomViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            address = (TextView) itemView.findViewById(R.id.tv_address);
            favorite = itemView.findViewById(R.id.favorite_btn);
            favorite_full = itemView.findViewById(R.id.favorite_full_btn);
            set_place = itemView.findViewById(R.id.set_place_btn);
            miniMap = itemView.findViewById(R.id.mini_map);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final int ItemPosition = position;

        if (holder instanceof CustomViewHolder) {
            final CustomViewHolder viewHolder = (CustomViewHolder) holder;

            viewHolder.title.setText(itemLists.get(position).getTitle());
            viewHolder.address.setText(itemLists.get(position).getAddress());

            viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.favorite.setVisibility(View.INVISIBLE);
                    viewHolder.favorite_full.setVisibility(View.VISIBLE);
                }
            });

            viewHolder.favorite_full.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.favorite.setVisibility(View.VISIBLE);
                    viewHolder.favorite_full.setVisibility(View.INVISIBLE);
                }
            });

            if (place_point == 1) {
                viewHolder.set_place.setText("출발");
            } else if (place_point == 2) {
                viewHolder.set_place.setText("도착");
            }
            viewHolder.set_place.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewHolder.set_place.getText().toString().equals("출발")) {
                        String title = itemLists.get(ItemPosition).getTitle();
                        String lat = itemLists.get(ItemPosition).getLat();
                        String lon = itemLists.get(ItemPosition).getLon();
                        callback.startPlace(title, lat, lon);
                    } else {
                        String title = itemLists.get(ItemPosition).getTitle();
                        String lat = itemLists.get(ItemPosition).getLat();
                        String lon = itemLists.get(ItemPosition).getLon();
                        callback.endPlace(title, lat, lon);
                    }
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (check == ItemPosition) { // ------------------------------------------------ 눌렀던 장소 한번 더 누름
                        viewHolder.miniMap.setVisibility(View.GONE);
                        check = 100;
                    } else if (check == 100) { //--------------------------------------------------- 검색후 처음 누름 or 초기화
                        String placeName = itemLists.get(ItemPosition).getTitle();
                        callback.showToast(placeName);
                        String lon = itemLists.get(ItemPosition).getLon();
                        String lat = itemLists.get(ItemPosition).getLat();
                        String image_path = "https://api2.sktelecom.com/tmap/staticMap?appKey=e01eeb5d-4c77-47c3-abe4-57a995dc41ce&" +
                                "longitude=" + lon + "&latitude=" + lat + "&coordType=WGS84GEO&zoom=17&markers=" + lon + "," + lat + "&format=PNG&width=512&height=512&callback=application/json";

                        aQuery = new AQuery(v);
                        aQuery.id(viewHolder.miniMap).image(image_path); // <- 미니맵지도
                        viewHolder.miniMap.setVisibility(View.VISIBLE);
                        check = ItemPosition;
                    } else { // -------------------------------------------------------------------- 눌려있는데 다른장소 클릭

                        //check 위치한 아이템의 이미지뷰 GONE 처리 하면 되는데


                        String placeName = itemLists.get(ItemPosition).getTitle();
                        callback.showToast(placeName);
                        String lon = itemLists.get(ItemPosition).getLon();
                        String lat = itemLists.get(ItemPosition).getLat();
                        String image_path = "https://api2.sktelecom.com/tmap/staticMap?appKey=e01eeb5d-4c77-47c3-abe4-57a995dc41ce&" +
                                "longitude=" + lon + "&latitude=" + lat + "&coordType=WGS84GEO&zoom=17&markers=" + lon + "," + lat + "&format=PNG&width=512&height=512&callback=application/json";

                        aQuery = new AQuery(v);
                        aQuery.id(viewHolder.miniMap).image(image_path); // <- 미니맵지도
                        viewHolder.miniMap.setVisibility(View.VISIBLE);
                        check = ItemPosition;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemLists.size();
    }

    public void setData(ArrayList<com.example.kks.carpool.SearchEntity> itemLists) {
        this.itemLists = itemLists;
    }

    public void setCallback(com.example.kks.carpool.RecyclerViewAdapterCallback callback) {
        this.callback = callback;
    }

    public void clear() {
        itemLists.clear();
    }

    public void filter(String keyword, int point) {
        itemLists.clear();
        if (keyword.length() >= 2) {
            if (point == 1) {
                place_point = 1;
                try {
                    AutoCompleteParse parser = new AutoCompleteParse(this);
                    itemLists.addAll(parser.execute(keyword).get());
                    notifyDataSetChanged();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                place_point = 2;
                try {
                    AutoCompleteParse parser = new AutoCompleteParse(this);
                    itemLists.addAll(parser.execute(keyword).get());
                    notifyDataSetChanged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
