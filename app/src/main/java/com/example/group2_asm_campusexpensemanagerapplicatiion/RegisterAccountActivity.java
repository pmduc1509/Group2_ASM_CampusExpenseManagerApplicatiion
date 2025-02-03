package com.example.group2_asm_campusexpensemanagerapplicatiion;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group2_asm_campusexpensemanagerapplicatiion.Database.UserDatabase;

public class RegisterAccountActivity extends AppCompatActivity {
    private EditText edtUsername, edtPassword, edtEmail, edtPhone;
    private Button btnSignUp;
    private TextView txtBackToSignIn;
    private UserDatabase userDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        edtUsername = findViewById(R.id.edtUserAccount);
        edtPassword = findViewById(R.id.edtPasswordAccount);
        edtEmail = findViewById(R.id.edtEmailAccount);
        edtPhone = findViewById(R.id.edtPhoneAccount);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtBackToSignIn = findViewById(R.id.txtBackToSignIn);
        userDatabase = new UserDatabase(RegisterAccountActivity.this);

        // Thêm InputFilter cho edtPhone để giới hạn số lượng ký tự và chỉ cho phép nhập số
        edtPhone.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(11) });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        setupBackToSignInLink();
    }

    private void signUp() {
        String user = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        // Kiểm tra tên tài khoản
        if (TextUtils.isEmpty(user)) {
            edtUsername.setError("Username cannot be empty");
            return;
        }
        if (userDatabase.checkUsernameExists(user)) {
            Toast.makeText(RegisterAccountActivity.this, "Username already exists", Toast.LENGTH_LONG).show();
            return;
        }

        // Kiểm tra mật khẩu
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password cannot be empty");
            return;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(RegisterAccountActivity.this, "Password must contain at least one uppercase letter and one special character", Toast.LENGTH_LONG).show();
            return;
        }

        // Kiểm tra email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email cannot be empty");
            return;
        }
        if (!email.endsWith("@gmail.com")) {
            edtEmail.setError("Email must end with @gmail.com");
            return;
        }
        if (userDatabase.checkEmailExists(email)) {
            Toast.makeText(RegisterAccountActivity.this, "Email already exists", Toast.LENGTH_LONG).show();
            return;
        }

        // Kiểm tra số điện thoại
        if (TextUtils.isEmpty(phone)) {
            edtPhone.setError("Phone number cannot be empty");
            return;
        }
        if (phone.length() < 10 || phone.length() > 11) {
            edtPhone.setError("Phone number must be 10 to 11 digits");
            return;
        }
        if (!phone.matches("\\d{10,11}")) {
            edtPhone.setError("Phone number must only contain digits");
            return;
        }
        if (userDatabase.checkPhoneExists(phone)) {
            Toast.makeText(RegisterAccountActivity.this, "Phone number already exists", Toast.LENGTH_LONG).show();
            return;
        }

        // Thực hiện đăng ký
        try {
            long insert = userDatabase.addNewUser(user, password, email, phone);
            if (insert == -1) {
                Toast.makeText(RegisterAccountActivity.this, "Insert Failure", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RegisterAccountActivity.this, "Insert Successfully", Toast.LENGTH_LONG).show();
                // Chuyển đến màn hình đăng nhập sau khi đăng ký thành công
                Intent intent = new Intent(RegisterAccountActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // Đảm bảo rằng màn hình đăng ký không còn trong ngăn xếp hoạt động
            }
        } catch (Exception e) {
            Toast.makeText(RegisterAccountActivity.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidPassword(String password) {
        // Kiểm tra mật khẩu có chữ viết hoa và ký tự đặc biệt không
        return password.matches(".*[A-Z].*") && password.matches(".*[^a-zA-Z0-9].*");
    }

    private void setupBackToSignInLink() {
        String text = "Already have an account? Sign In";
        SpannableString spannableString = new SpannableString(text);

        // Tạo một ClickableSpan cho chữ "Sign In"
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Quay lại màn hình đăng nhập khi nhấn vào "Sign In"
                Intent intent = new Intent(RegisterAccountActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // Đảm bảo rằng màn hình đăng ký không còn trong ngăn xếp hoạt động
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED); // Đổi màu chữ thành đỏ
                ds.setUnderlineText(false); // Bỏ gạch chân
            }
        };

        // Áp dụng màu sắc cho chữ "Sign In"
        int start = text.indexOf("Sign In");
        int end = start + "Sign In".length();
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtBackToSignIn.setText(spannableString);
        txtBackToSignIn.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
