package com.example.kks.carpool.retro;

import android.media.Image;

import com.example.kks.carpool.autosearch.TMapSearchInfo;
import com.example.kks.carpool.autosearch.TmapRoute;
import com.example.kks.carpool.driver.requestParse;
import com.example.kks.carpool.model.MyRoute;
import com.example.kks.carpool.model.RequestCar;
import com.example.kks.carpool.model.ShowRating;
import com.example.kks.carpool.model.User;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("register.php")
    Call<User> performRegistration(@Field("email") String email, @Field("name") String name, @Field("password") String password);

    @FormUrlEncoded
    @POST("login.php")
    Call<User> performUserLogin(@Field("email") String Email, @Field("password") String UserPassword);

    // 카풀 요청 등록하기
    @FormUrlEncoded
    @POST("requestCar.php")
    Call<RequestCar> performRequestCar(@Field("user") String user, @Field("sLat") double sLat, @Field("sLon") double sLon,
                                       @Field("eLat") double eLat, @Field("eLon") double eLon, @Field("date") String date,
                                       @Field("time") String time, @Field("people") int people, @Field("fare") String fare, @Field("rating") double rating);

    // 카풀 요청 삭제하기
    @FormUrlEncoded
    @POST("DelRequestCar.php")
    Call<RequestCar> performDelRequestCar(@Field("idx") int idx);

    // 카풀요청 리스트 디폴트값
    @FormUrlEncoded
    @POST("requestDriver.php")
    Call<ArrayList<requestParse>> performRequestDriver(@Field("type") int type, @Field("date") String date, @Field("time") String time);

    // 카풀요청 리스트 필터적용
    @FormUrlEncoded
    @POST("requestDriverFilter.php")
    Call<ArrayList<requestParse>> performRequestDriverFilter(@Field("type") int type, @Field("sDate") String sDate, @Field("eDate") String eDate, @Field("setTime") String time,
                                                             @Field("people") int people, @Field("sDistance") int sDistance, @Field("eDistance") int eDistance);

    // 나의 경로 등록
    @FormUrlEncoded
    @POST("insertMyRoute.php")
    Call<MyRoute> performInsertMyRoute(@Field("name") String name, @Field("sLat") double sLat, @Field("sLon") double sLon,
                                       @Field("eLat") double eLat, @Field("eLon") double eLon, @Field("title") String title,
                                       @Field("sPlace") String sPlace, @Field("ePlace") String ePlace, @Field("gMap") String gMap);

    // 나의 경로 수정
    @FormUrlEncoded
    @POST("editMyRoute.php")
    Call<MyRoute> performInsertMyRouteEdit(@Field("name") String name, @Field("sLat") double sLat, @Field("sLon") double sLon,
                                           @Field("eLat") double eLat, @Field("eLon") double eLon, @Field("title") String title,
                                           @Field("sPlace") String sPlace, @Field("ePlace") String ePlace, @Field("idx") int idx,
                                           @Field("map") String map);

    // 나의 경로 삭제
    @FormUrlEncoded
    @POST("delMyRoute.php")
    Call<MyRoute> performInsertMyRouteDel(@Field("idx") int idx);

    // 나의 경로 받아오기
    @FormUrlEncoded
    @POST("getMyRoute.php")
    Call<ArrayList<MyRoute>> performGetMyRoute(@Field("name") String name);


    // 평가등록하기
    @FormUrlEncoded
    @POST("insertRating.php")
    Call<Integer> performAddRating(@Field("user_name") String user_name, @Field("start_place") String start_place, @Field("end_place") String end_place,
                                   @Field("date_time") String date_time, @Field("distance_time") String distance_time, @Field("rating") double rating,
                                   @Field("fare") String fare, @Field("target_id") String target_id, @Field("target_profile") String target_profile,
                                   @Field("rating1") double rating1, @Field("rating2") double rating2, @Field("rating3") double rating3);

    // 평가등록 불러오기 ( 운행내역 / 결제내역 )
    @FormUrlEncoded
    @POST("showMyDrive.php")
    Call<ArrayList<ShowRating>> performShowRating(@Field("user_name") String user_name);

    // 내 점수 불러오기 ( 총점 / 개별 )
    @FormUrlEncoded
    @POST("getMyPoint.php")
    Call<String> performGetMyPoint(@Field("user_name") String user_name);

    @Multipart
    @POST("upload_img.php")
    Call<ResponseBody> uploadImage(@Part("description") RequestBody description, @Part MultipartBody.Part file);

    // "https://api2.sktelecom.com/tmap/pois?version=1&page=1&count=20&searchKeyword=" + encodeWord +
    //                // 받는좌표계 유형(경위도)  검색타입(통합)   R:거리순   검색반경10km  입력(경위도)  검색반경 중심좌표
    //                "&resCoordType=WGS84GEO&searchType=all&searchtypCd=R&radius=10&reqCoordType=WGS84GEO&centerLon="+lon+"&centerLat="+lat+
    //                "&multiPoint=N&callback=application/json&appKey=" + TMAP_API_KEY

    //    @GET("https://api2.sktelecom.com/tmap/pois?version=1&page=1&count=20&searchKeyword={keyword}&resCoordType=WGS84GEO&searchType=all&searchtypCd=R&radius=10&reqCoordType=WGS84GEO" +
    //            "&centerLon={lon}&centerLat={lat}&multiPoint=N&callback=application/json&appKey={appkey}")

    // Tmap 장소 검색 받아오기
    @GET("tmap/pois")
    Call<TMapSearchInfo> performTmapPlace(
            @Query("version") String version
            , @Query("page") String page
            , @Query("count") String count
            , @Query("resCoordType") String resCoordType
            , @Query("searchType") String searchType
            , @Query("searchTypCd") String searchTypCd
            , @Query("radius") String radius
            , @Query("reqCoordType") String reqCoordType
            , @Query("multiPoint") String multiPoint
            , @Query("callback") String callback
            , @Query("searchKeyword") String keyword
            , @Query("centerLon") String lon
            , @Query("centerLat") String lat
            , @Query("appKey") String key);

    // Tmap 자동차 경로 받아오기
    @GET("tmap/routes")
    Call<TmapRoute> performTmapInfo(
            @Query("version") String version
            , @Query("callback") String callback
            , @Query("appKey") String key
            , @Query("endX") String endX
            , @Query("endY") String endY
            , @Query("startX") String startX
            , @Query("startY") String startY
            , @Query("totalValue") String totalValue
    );

    @GET("tmap/staticMap")
    Call<Image> performTmapMiniMap(
            @Query("version") String version
            , @Query("callback") String callback
            , @Query("appKey") String appKey
            , @Query("longitude") String longitude
            , @Query("latitude") String latitude
            , @Query("coordType") String coordType
            , @Query("zoom") String zoom
            , @Query("markers") String markers
            , @Query("format") String format
            , @Query("width") String width
            , @Query("height") String height
    );

}
