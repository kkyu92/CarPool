package com.example.kks.carpool.driver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.example.kks.carpool.R;
import com.facebook.login.widget.ProfilePictureView;

import de.hdodenhof.circleimageview.CircleImageView;

public class start_Driver extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, myRouteCallback {

    //Get Intent
    private String image_path, email, name, phone;

    private ProfilePictureView fb_imgView;
    private AQuery aQuery;
    private CircleImageView profile_img;
    private TextView n_name, n_email;

    // 메뉴
    private NavigationView navigationView;

    // 툴바
    public static TextView driver_txt;
    public static ImageView list_setting, myload;

    // 필터 거친 값
    private String sDate, eDate, setTime;
    private int people, sDistance, eDistance;
    private Bundle filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start__driver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new FragmentRequestList()).commit();
            navigationView.setCheckedItem(R.id.request_list);
        }

        // 메뉴창 내정보 부분
        View header = navigationView.getHeaderView(0);
        fb_imgView = header.findViewById(R.id.fb_imgView);
        profile_img = header.findViewById(R.id.nav_imageView);
        n_email = header.findViewById(R.id.nav_email);
        n_name = header.findViewById(R.id.nav_name);
        aQuery = new AQuery(this);

        // 메뉴 툴바 헤더부분 (DRIVER / 설정)
        driver_txt = toolbar.findViewById(R.id.driver_text);
        list_setting = toolbar.findViewById(R.id.list_setting);
        myload = toolbar.findViewById(R.id.load_list);

        // GetIntent (로그인 해서 받아온 정보)
        image_path = getIntent().getStringExtra("profile");
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");

        if (getIntent() != null) {
            n_email.setText(email);
            n_name.setText(name);
//            profile.setText(getIntent().getStringExtra("photo_uri"));
            Log.e("email", "" + email);
            Log.e("name", "" + name);
            Log.e("imagePath", "" + image_path);
            if (image_path != null) {
                if (image_path.contains("kakao")) {
                    Log.e("이미지 경로표시 :: ", "" + image_path);
                    fb_imgView.setVisibility(View.GONE);
                    profile_img.setVisibility(View.VISIBLE);
                    aQuery.id(profile_img).image(image_path); // <-  userProfile.getProfileImagePath() <- 큰 이미지
                } else if (email != null && email.contains("gmail.com")) {
                    fb_imgView.setVisibility(View.GONE);
                    profile_img.setVisibility(View.VISIBLE);
                    aQuery.id(profile_img).image(image_path);
                } else if (email != null && email.contains("naver.com")) {
                    fb_imgView.setVisibility(View.GONE);
                    profile_img.setVisibility(View.VISIBLE);
                    Uri img = Uri.parse(image_path);
                    Glide.with(this).load(img).into(profile_img);
                } else {
                    Log.e("이미지 페북 :: ", "" + image_path);
                    fb_imgView.setVisibility(View.VISIBLE);
                    profile_img.setVisibility(View.GONE);
                    fb_imgView.setProfileId(image_path);
                }
            } else { // 프로필 이미지 = null
                fb_imgView.setVisibility(View.GONE);
                profile_img.setVisibility(View.VISIBLE);
                profile_img.setImageResource(R.drawable.kakao_default_profile_image);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 카풀 요청 리스트 설정하러 가기
        list_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(start_Driver.this, "원하는 요청 정보를 설정하세요", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(start_Driver.this, SettingPopUp.class);
                startActivityForResult(intent, 1114);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.request_list:
                list_setting.setVisibility(View.VISIBLE);
                myload.setVisibility(View.INVISIBLE);
                driver_txt.setText("카풀 리스트");
                FragmentRequestList fragmentRequestList = new FragmentRequestList();
                if (filter != null) { fragmentRequestList.setArguments(filter); }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragmentRequestList).commit();
                break;
            case R.id.my_load:
                list_setting.setVisibility(View.INVISIBLE);
                myload.setVisibility(View.VISIBLE);
                driver_txt.setText("운행경로 설정");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentMyLoad()).commit();
                break;
            case R.id.drive_list:
                list_setting.setVisibility(View.INVISIBLE);
                myload.setVisibility(View.INVISIBLE);
                driver_txt.setText("운행내역");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentDriveList()).commit();
                break;
            case R.id.my_point:
                list_setting.setVisibility(View.INVISIBLE);
                myload.setVisibility(View.INVISIBLE);
                driver_txt.setText("나의 평점");
                navigationView.setCheckedItem(R.id.my_point);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentGetMyPoint()).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 필터 적용한 카풀 요청 리스트 받아오기
        if (requestCode == 1114) {
            sDate = data.getStringExtra("startDate");
            eDate = data.getStringExtra("endDate");
            setTime = data.getStringExtra("setTime");
            people = data.getIntExtra("setPeople", 0);
            sDistance = data.getIntExtra("startD", 5);
            eDistance = data.getIntExtra("endD", 5);

            Log.e("시작날짜:::", "" + sDate);
            Log.e("종료날짜:::", "" + eDate);
            Log.e("시간:::", "" + setTime);
            Log.e("탑승인원:::", "" + people);
            Log.e("출발지와의 거리:::", "" + sDistance);
            Log.e("도착지와의 거리:::", "" + eDistance);

            filter = new Bundle();
            filter.putString("startDate", sDate);
            filter.putString("endDate", eDate);
            filter.putString("setTime", setTime);
            filter.putInt("setPeople", people);
            filter.putInt("startD", sDistance);
            filter.putInt("endD", eDistance);

            navigationView.setCheckedItem(R.id.request_list);
            FragmentRequestList fragmentRequestList = new FragmentRequestList();
            fragmentRequestList.setArguments(filter);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragmentRequestList).commit();

        }
    }

    @Override
    public void showToast(String place) {

    }

    // 나의 경로 아이템 클릭 (출발지 도착지 좌표, 장소명 가져온다)
    @Override
    public void myRouteClick(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon) {

    }

    @Override
    public void myRouteEdit(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon, int idx) {

    }

    @Override
    public void myRouteDel(String title, int idx) {

    }

}
