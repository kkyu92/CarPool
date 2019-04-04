package com.example.kks.carpool.RiderClass;

import android.os.AsyncTask;
import android.util.Log;

import com.example.kks.carpool.AutoSearch.Poi;
import com.example.kks.carpool.AutoSearch.TMapSearchInfo;
import com.example.kks.carpool.Retrofit.ApiInterface;
import com.example.kks.carpool.Retrofit.TmapClient;
import com.example.kks.carpool.model.SearchEntity;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.kks.carpool.RiderClass.TmapStart.lat;
import static com.example.kks.carpool.RiderClass.TmapStart.lon;

public class AutoCompleteParse extends AsyncTask<String, Void, ArrayList<SearchEntity>> {
//    public static final String TMAP_API_KEY = "e01eeb5d-4c77-47c3-abe4-57a995dc41ce"; // main
    public static final String TMAP_API_KEY = "a22eb7b1-e7a6-4bcd-a34d-73fa0b4d71aa"; // sub
    private final int SEARCH_COUNT = 20;  // minimum is 20

    // 출발지 장소 리스트
    private ArrayList<SearchEntity> mListData;
    // 도착지 장소 리스트
    private ArrayList<SearchEntity> endListData;

    // 출발지 어뎁터
    private PlaceSearchAdapter mAdapter;
    // 도착지 어뎁터
    //private RVAdapterEndPlace endAdapter;

    //retrofit
    private Retrofit retrofit;
    private ApiInterface apiInterface;

    public AutoCompleteParse(PlaceSearchAdapter adapter) {
        this.mAdapter = adapter;
        mListData = new ArrayList<SearchEntity>();
    }

    @Override
    protected ArrayList<SearchEntity> doInBackground(String... word) {
        return getAutoComplete(word[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<SearchEntity> autoCompleteItems) {
        mAdapter.setData(autoCompleteItems);
        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<SearchEntity> getAutoComplete(String word) {
        apiInterface = TmapClient.getApiClient().create(ApiInterface.class);
        Call<TMapSearchInfo> call = apiInterface.performTmapPlace("1", "1", "20", "WGS84GEO", "all", "R", "10", "WGS84GEO", "N", "application/json", word, lon, lat, TMAP_API_KEY);
//        Log.e("encoded::::", ""+encodeWord);
        Log.e("!encoded::::", "" + word);
        call.enqueue(new Callback<TMapSearchInfo>() {
            @Override
            public void onResponse(Call<TMapSearchInfo> call, Response<TMapSearchInfo> response) {
                Log.d("레트로핏::::", "성공" + response.body());
                Log.d("레트로핏::::", "성공" + call);
//                mListData.clear();
                TMapSearchInfo tMapSearchInfo = response.body();

                if (tMapSearchInfo != null) {
                    ArrayList<Poi> poi = tMapSearchInfo.getSearchPoiInfo().getPois().getPoi();
                    Log.d("AL poi:::", "(레트로핏)" + poi);
                    Log.d("poi size:::", "(레트로핏)" + poi.size());

                    for (int i = 0; i < poi.size(); i++) {
                        String fullAddr = poi.get(i).getUpperAddrName() + " " + poi.get(i).getMiddleAddrName() +
                                " " + poi.get(i).getLowerAddrName() + " " + poi.get(i).getFirstNo() + "-" + poi.get(i).getSecondNo() + " 번지";

                        mListData.add(new SearchEntity(poi.get(i).getName(), fullAddr, poi.get(i).getFrontLat(), poi.get(i).getFrontLon()));
                        RiderSearchPlace.adapter.notifyDataSetChanged();
                        Log.d("start_ListData:::", "" + mListData);

                    }
                }
            }

            @Override
            public void onFailure(Call<TMapSearchInfo> call, Throwable t) {
                Log.d("레트로핏::::", "실패");
            }
        });
        try {
            String encodeWord = URLEncoder.encode(word, "UTF-8");
            URL acUrl = new URL(
                    "https://api2.sktelecom.com/tmap/pois?version=1&page=1&count=" + SEARCH_COUNT + "&searchKeyword=" + encodeWord +
                            // 받는좌표계 유형(경위도)  검색타입(통합)   R:거리순   검색반경10km  입력(경위도)  검색반경 중심좌표
                            "&resCoordType=WGS84GEO&searchType=all&searchtypCd=R&radius=10&reqCoordType=WGS84GEO&centerLon=" + lon + "&centerLat=" + lat +
                            "&multiPoint=N&callback=application/json&appKey=" + TMAP_API_KEY
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
        return mListData;
    }
}
