package com.example.kks.carpool.LoginSignup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.kks.carpool.R;
import com.example.kks.carpool.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Signup extends AppCompatActivity {

    private EditText Email, UserName, UserPassword;
    private Button signup_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Email = findViewById(R.id.email_txt);
        UserName = findViewById(R.id.name_txt);
        UserPassword = findViewById(R.id.pass_txt);

        signup_btn = findViewById(R.id.signup_btn);
    }

    protected void onResume() {
        super.onResume();

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performRegistration();
            }
        });
    }

    public void performRegistration() {
        final String email = Email.getText().toString();
        final String name = UserName.getText().toString();
        final String password = UserPassword.getText().toString();

        Call<User> call = Login.apiInterface.performRegistration(email, name, password);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (email.equals("") || name.equals("") || password.equals("")) {
                    Login.prefConfig.displayToast("빈칸 없이 입력해 주세요!");
                } else if (response.body() != null) {
                    if (response.body().getResponse().equals("ok")) {
                        Login.prefConfig.displayToast("가입 성공!!!");
                        Intent intent = new Intent(Signup.this, Login.class);
                        intent.putExtra("sign_name", name);
                        startActivity(intent);
                        finish();
                    } else if (response.body().getResponse().equals("exist")) {
                        Login.prefConfig.displayToast("이미 존재하는 아이디 입니다...");
                    } else if (response.body().getResponse().equals("error")) {
                        Login.prefConfig.displayToast("에러에러에러.....");
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
        Email.setText("");
        UserName.setText("");
        UserPassword.setText("");
    }
}