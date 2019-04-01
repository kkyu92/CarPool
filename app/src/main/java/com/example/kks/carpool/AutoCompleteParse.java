package com.example.kks.carpool;

import android.os.AsyncTask;
import android.util.Log;

import com.example.kks.carpool.autosearch.Poi;
import com.example.kks.carpool.autosearch.TMapSearchInfo;
import com.example.kks.carpool.model.Place;
import com.example.kks.carpool.retro.ApiClient;
import com.example.kks.carpool.retro.ApiInterface;
import com.example.kks.carpool.retro.TmapClient;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.kks.carpool.TmapStart.lat;
import static com.example.kks.carpool.TmapStart.lon;

public class AutoCompleteParse extends AsyncTask<String, Void, ArrayList<SearchEntity>> {
//    public static final String TMAP_API_KEY = "e01eeb5d-4c77-47c3-abe4-57a995dc41ce"; // main
    public static final String TMAP_API_KEY = "a22eb7b1-e7a6-4bcd-a34d-73fa0b4d71aa"; // sub
    private final int SEARCH_COUNT = 20;  // minimum is 20

    // 출발지 장소 리스트
    private ArrayList<SearchEntity> mListData;
    // 도착지 장소 리스트
    private ArrayList<SearchEntity> endListData;

    // 출발지 어뎁터
    private RecyclerViewAdapter mAdapter;
    // 도착지 어뎁터
    //private RVAdapterEndPlace endAdapter;

    //retrofit
    private Retrofit retrofit;
    private ApiInterface apiInterface;

    public AutoCompleteParse(RecyclerViewAdapter adapter) {
        this.mAdapter = adapter;
        mListData = new ArrayList<SearchEntity>();
    }

//    public AutoCompleteParse(RVAdapterEndPlace rvAdapterEndPlace) {
//        this.endAdapter = rvAdapterEndPlace;
//        endListData = new ArrayList<SearchEntity>();
//    }

    @Override
    protected ArrayList<SearchEntity> doInBackground(String... word) {
        return getAutoComplete(word[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<SearchEntity> autoCompleteItems) {
        mAdapter.setData(autoCompleteItems);
        mAdapter.notifyDataSetChanged();

//        endAdapter.setData(autoCompleteItems);
//        endAdapter.notifyDataSetChanged();
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
                        MainActivity.adapter.notifyDataSetChanged();
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
//            URL acUrl = new URL(
//                    "https://apis.skplanetx.com/tmap/pois?areaLMCode=&centerLon=&centerLat=&" +
//                            "count=" + SEARCH_COUNT + "&page=&reqCoordType=&" + "" +
//                            "searchKeyword=" + encodeWord + "&callback=&areaLLCode=&multiPoint=&searchtypCd=&radius=&searchType=&resCoordType=WGS84GEO&version=1"
//            );
            URL acUrl = new URL(
                    "https://api2.sktelecom.com/tmap/pois?version=1&page=1&count=" + SEARCH_COUNT + "&searchKeyword=" + encodeWord +
                            // 받는좌표계 유형(경위도)  검색타입(통합)   R:거리순   검색반경10km  입력(경위도)  검색반경 중심좌표
                            "&resCoordType=WGS84GEO&searchType=all&searchtypCd=R&radius=10&reqCoordType=WGS84GEO&centerLon=" + lon + "&centerLat=" + lat +
                            "&multiPoint=N&callback=application/json&appKey=" + TMAP_API_KEY
            );


//            HttpURLConnection acConn = (HttpURLConnection) acUrl.openConnection();
//            acConn.setRequestProperty("Accept", "application/json");
//            acConn.setRequestProperty("appKey", TMAP_API_KEY);
//            Log.d("HttpURL:::",""+acConn);
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    acConn.getInputStream()));
//
//            String line = reader.readLine();
//            Log.d("String line:::",""+line);
//
//            if (line == null) {
//                mListData.clear();
//                return mListData;
//            }
//
//            reader.close();
//
//            mListData.clear();
//
//            TMapSearchInfo searchPoiInfo = new Gson().fromJson(line, TMapSearchInfo.class);
//            Log.d("TmapSearchInfo:::",""+searchPoiInfo);
//            ArrayList<Poi> poi = searchPoiInfo.getSearchPoiInfo().getPois().getPoi();
//            Log.d("AL poi:::",""+poi);
//            Log.d("poi size:::",""+poi.size());
//            for (int i = 0; i < poi.size(); i++) {
//                String fullAddr = poi.get(i).getUpperAddrName() + " " + poi.get(i).getMiddleAddrName() +
//                        " " + poi.get(i).getLowerAddrName() + " " + poi.get(i).getDetailAddrName();
//
//                mListData.add(new SearchEntity(poi.get(i).getName(), fullAddr));
////                mAdapter.notifyItemInserted(i);
//                Log.d("mListData:::",""+mListData);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return mListData;
    }
}
