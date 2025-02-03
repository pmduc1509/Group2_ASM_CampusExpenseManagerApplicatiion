package com.example.group2_asm_campusexpensemanagerapplicatiion;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.UserDatabase;
import com.example.group2_asm_campusexpensemanagerapplicatiion.Models.User;

public class SignInActivity extends AppCompatActivity {
    private Button btnLogin;
    private TextView txtSignUp;
    private EditText editUser, editPassword;
    private UserDatabase userDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnLogin = findViewById(R.id.btnSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);
        editUser = findViewById(R.id.edtUser);
        editPassword = findViewById(R.id.edtPass);
        userDatabase = new UserDatabase(SignInActivity.this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = editUser.getText().toString().trim();
                String pass = editPassword.getText().toString().trim();
                if (TextUtils.isEmpty(user)) {
                    editUser.setError("Username cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    editPassword.setError("Password cannot be empty");
                    return;
                }

                User data = userDatabase.getInfoUser(user, pass);
                if (data != null && data.getEmail() != null && data.getId() > 0) {
                    // Đăng nhập thành công, chuyển đến ProfileFragment với thông tin người dùng
                    Intent intent = new Intent(SignInActivity.this, MenuActivity.class);
                    intent.putExtra("username", user); // Truyền tên người dùng
                    intent.putExtra("email", data.getEmail()); // Truyền email người dùng
                    intent.putExtra("phone", data.getPhone()); // Truyền phone người dùng
                    startActivity(intent);
                    finish(); // Đảm bảo không quay lại màn hình đăng nhập
                } else {
                    Toast.makeText(SignInActivity.this, "Account Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Thiết lập clickable cho "Sign up"
        String signUpText = "Don't have an account? Sign up";
        SpannableString spannableString = new SpannableString(signUpText);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Chuyển đến RegisterAccountActivity khi nhấn vào "Sign up"
                Intent intent = new Intent(SignInActivity.this, RegisterAccountActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED); // Đổi màu chữ thành đỏ
                ds.setUnderlineText(false); // Bỏ gạch chân
            }
        };

        int start = signUpText.indexOf("Sign up");
        int end = start + "Sign up".length();
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtSignUp.setText(spannableString);
        txtSignUp.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
