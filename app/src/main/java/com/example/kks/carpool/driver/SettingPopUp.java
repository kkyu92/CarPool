package com.example.kks.carpool.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kks.carpool.R;
import com.example.kks.carpool.google_map;
import com.google.android.gms.stats.internal.G;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SettingPopUp extends Activity {

    private SimpleDateFormat dbFormat_Date = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN);
    private SimpleDateFormat dbFormat_Time = new SimpleDateFormat("HH시 mm분 이후", Locale.KOREAN);

    private SimpleDateFormat format_Date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
    private SimpleDateFormat format_Time = new SimpleDateFormat("HH:mm", Locale.KOREAN);

    private TextView date_set, time_set, people_set;
    private Button date_btn, time_btn, people_btn;
    private RadioGroup start_G, end_G;
    private Button firstSet_btn, setFinish_btn, range_date;

    // 설정 변경하기 부분
    private CalendarPickerView pickerView;
    private TimePickerDialog timePickerDialog;
    private Date today;
    private Calendar nextYear;
    private int count = 1;

    // setResult
    private String startDate, endDate, setTime;
    private int people_c = 0;
    private int start_D, end_D;
    private StringBuffer rangeDate;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting_pop_up);

        // 텍스트뷰
        date_set = findViewById(R.id.date_set_view);
        time_set = findViewById(R.id.time_set_view);
        people_set = findViewById(R.id.people_set_view);

        // 변경 버튼
        date_btn = findViewById(R.id.date_setting);
        time_btn = findViewById(R.id.time_setting);
        people_btn = findViewById(R.id.people_setting);

        // 라디오그룹
        start_G = findViewById(R.id.start_radio);
        end_G = findViewById(R.id.end_radio);

        // 기본설정, 완료 버튼, 날짜범위 완료
        firstSet_btn = findViewById(R.id.first_set);
        setFinish_btn = findViewById(R.id.setting_set);
        range_date = findViewById(R.id.range_date_set);

        // 캘린더 라이브러리
        pickerView = findViewById(R.id.calendar);
        today = new Date();
        nextYear = Calendar.getInstance();
        nextYear.add(Calendar.DAY_OF_MONTH, 30);
        pickerView.init(today, nextYear.getTime())
                .inMode(CalendarPickerView.SelectionMode.RANGE)
                .withSelectedDate(today);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingLoad();
        // 날짜 변경
        date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickerView.setVisibility(View.VISIBLE);
                rangeDate = new StringBuffer();

                pickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(Date date) {
                        Log.e("날자선택:::", "" + date);
                        Calendar calSelected = Calendar.getInstance();
                        calSelected.setTime(date);
                        String selectedDate = dbFormat_Date.format(calSelected.getTime());

                        if (count == 1) {
                            // activity 보여지는 형태
                            rangeDate.append(dbFormat_Date.format(pickerView.getSelectedDate()));
                            rangeDate.append(" ~ ");
                            rangeDate.append(selectedDate);
                            range_date.setVisibility(View.VISIBLE);
                            date_set.setText(rangeDate);
                            Toast.makeText(SettingPopUp.this, "기간 : " + rangeDate, Toast.LENGTH_SHORT).show();

                            // db 넣을 형태
                            startDate = format_Date.format(pickerView.getSelectedDate());
                            endDate = format_Date.format(calSelected.getTime());
                        } else if (count % 2 == 0) { // 짝수
                            rangeDate.append(selectedDate);
                            rangeDate.append(" ~ ");
                            range_date.setVisibility(View.INVISIBLE);
                            Toast.makeText(SettingPopUp.this, "마지막 날짜를 지정해 주세요", Toast.LENGTH_SHORT).show();

                            // db 넣을 형태
                            startDate = format_Date.format(calSelected.getTime());
                        } else { // 홀수
                            if (String.valueOf(rangeDate.delete(rangeDate.length() - 3, rangeDate.length())).equals(selectedDate)) {
                                rangeDate.delete(0, rangeDate.length());
                                rangeDate.append(selectedDate);
                                date_set.setText(rangeDate);
                                range_date.setVisibility(View.VISIBLE);

                                // db 넣을 형태
                                startDate = format_Date.format(calSelected.getTime());
                                endDate = format_Date.format(calSelected.getTime());
                            } else {
                                rangeDate.append(" ~ ");
                                rangeDate.append(selectedDate);
                                date_set.setText(rangeDate);
                                range_date.setVisibility(View.VISIBLE);

                                // db 넣을 형태
                                endDate = format_Date.format(calSelected.getTime());
                            }
                            Toast.makeText(SettingPopUp.this, "기간 : " + rangeDate, Toast.LENGTH_SHORT).show();
                        }

                        Log.e("날자 범위:::", "" + String.valueOf(rangeDate));
                        Log.e("카운트:::", "" + count);
                        count++;
                    }

                    @Override
                    public void onDateUnselected(Date date) {
                        Calendar calSelected = Calendar.getInstance();
                        calSelected.setTime(date);

                        String selectedDate = calSelected.get(Calendar.YEAR) + "년 " + (calSelected.get(Calendar.MONTH) + 1) + "월 " + calSelected.get(Calendar.DAY_OF_MONTH) + "일";
                        Log.e("언제냐 여기가:::", "" + date);
                        Log.e("시작날짜?:::", "" + selectedDate);

                        rangeDate.delete(0, rangeDate.length());
                        Log.e("범위 삭제:::", "" + String.valueOf(rangeDate));
                    }
                });
            }
        });

        // 날짜 범위 완료 버튼
        range_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                range_date.setVisibility(View.INVISIBLE);
                pickerView.setVisibility(View.GONE);
            }
        });

        // 시간변경
        time_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar currentDate = Calendar.getInstance();
                final Calendar date = Calendar.getInstance();

                timePickerDialog = new TimePickerDialog(SettingPopUp.this, AlertDialog.THEME_HOLO_LIGHT,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                date.set(Calendar.MINUTE, minute);

                                time = dbFormat_Time.format(date.getTime());
                                setTime = format_Time.format(date.getTime());
                                Toast.makeText(SettingPopUp.this, time + "로 설정합니다", Toast.LENGTH_SHORT).show();
                                time_set.setText(time);
                            }
                        }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
                timePickerDialog.setIcon(R.drawable.time);
                timePickerDialog.setTitle("설정한 시간 이후로 검색 됩니다");
                timePickerDialog.show();
            }
        });

        // 인원 번경
        people_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] list = new String[]{"1인", "2인", "3인", "상관없음"};
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingPopUp.this, AlertDialog.THEME_HOLO_LIGHT);
                mBuilder.setTitle("탑승인원을 선택하세요");
                mBuilder.setIcon(R.drawable.person_add);
                mBuilder.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        people_set.setText(list[i]);
                        if (i == 0) {
                            people_c = 1;
                        } else if (i == 1) {
                            people_c = 2;
                        } else if (i == 2) {
                            people_c = 3;
                        } else {
                            people_c = 0;
                        }
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
                mDialog.getWindow().setAttributes(params);

                mDialog.show();
            }
        });

        // 출발지 거리설정 라디오 그룹
        start_G.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID) {
                    case R.id.start_1km:
                        start_D = 1;
                        break;
                    case R.id.start_5km:
                        start_D = 5;
                        break;
                    case R.id.start_10km:
                        start_D = 10;
                        break;
                    case R.id.start_all:
                        start_D = 0;
                        break;
                }
            }
        });

        // 도착지 거리설정 라디오 그룹
        end_G.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID) {
                    case R.id.end_1km:
                        end_D = 1;
                        break;
                    case R.id.end_5km:
                        end_D = 5;
                        break;
                    case R.id.end_10km:
                        end_D = 10;
                        break;
                    case R.id.end_all:
                        end_D = 0;
                        break;
                }
            }
        });

        // 초기설정
        firstSet_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingFirst();
            }
        });

        // 설정완료
        setFinish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (time == null) {
                    settingSave(startDate, endDate, time_set.getText().toString(), people_c, start_D, end_D);
                } else {
                    settingSave(startDate, endDate, time, people_c, start_D, end_D);
                }
                Intent intent = new Intent();
                intent.putExtra("startDate", startDate);
                intent.putExtra("endDate", endDate);
                intent.putExtra("setTime", setTime);
                intent.putExtra("setPeople", people_c);
                intent.putExtra("startD", start_D);
                intent.putExtra("endD", end_D);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    // 기본설정
    private void settingFirst() {
        // 날짜 설정 (현재날짜)
        Calendar currentDate = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        now.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH), currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE));
        String nowDate = dbFormat_Date.format(now.getTime());
        // DB 넣는 형태
        startDate = format_Date.format(now.getTime());
        endDate = format_Date.format(now.getTime());
        // Activity 보여주는 형태
        date_set.setText(nowDate);

        // 시간 설정 (현재시간 이후)
        String nowTime = dbFormat_Time.format(now.getTime());
        // DB 넣는 형태
        setTime = format_Time.format(now.getTime());
        // Activity 보여주는 형태
        time_set.setText(nowTime);

        // 탑승인원
        people_set.setText("상관없음");
        people_c = 0;

        // 출발 도착 거리 설정
        start_G.check(R.id.start_5km);
        end_G.check(R.id.end_5km);
        start_D = 5;
        end_D = 5;
    }

    // 필터 기억
    private void settingSave(String sDate, String eDate, String time, int people, int s_radio, int e_radio) {
        SharedPreferences preferences = getSharedPreferences("filter", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (String.valueOf(rangeDate).equals(date_set.getText().toString())) {
            editor.putString("text", String.valueOf(rangeDate));
        } else {
            editor.putString("text", date_set.getText().toString());
        }
        editor.putString("sDate", sDate);
        editor.putString("eDate", eDate);
        editor.putString("time", time);
        editor.putString("setTime", setTime);
        editor.putInt("people", people);
        editor.putInt("sRadio", s_radio);
        editor.putInt("eRadio", e_radio);
        editor.apply();
    }

    // 필터 로드
    private void settingLoad() {
        SharedPreferences preferences = getSharedPreferences("filter", MODE_PRIVATE);
        rangeDate = new StringBuffer(preferences.getString("text", ""));
        String sDate = preferences.getString("sDate", "날짜 지정");
        String eDate = preferences.getString("eDate", "날짜 지정");
        String time = preferences.getString("time", "시간 지정");
        String settime = preferences.getString("setTime", "");
        int people = preferences.getInt("people", 0);
        int s_radio = preferences.getInt("sRadio", 5);
        int e_radio = preferences.getInt("eRadio", 5);

        date_set.setText(rangeDate);
        startDate = sDate;
        endDate = eDate;
        time_set.setText(time);
        setTime = settime;
        if (people == 0) {
            people_set.setText("상관없음");
        } else {
            people_set.setText(String.valueOf(people) + "인");
        }
        start_D = s_radio;
        end_D = e_radio;

        if (start_D == 1) {
            start_G.check(R.id.start_1km);
        } else if (start_D == 5) {
            start_G.check(R.id.start_5km);
        } else if (start_D == 10) {
            start_G.check(R.id.start_10km);
        } else {
            start_G.check(R.id.start_all);
        }

        if (end_D == 1) {
            end_G.check(R.id.end_1km);
        } else if (end_D == 5) {
            end_G.check(R.id.end_5km);
        } else if (end_D == 10) {
            end_G.check(R.id.end_10km);
        } else {
            end_G.check(R.id.end_all);
        }
    }

}
