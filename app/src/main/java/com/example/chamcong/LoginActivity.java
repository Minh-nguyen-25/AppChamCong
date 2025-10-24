package com.example.chamcong;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etPhone, etPassword;
    Button btnLogin;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            long manv = checkUserAndGetId(phone, password);
            if (manv != -1) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("MANV", (int) manv);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Sai số điện thoại hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // trả về manv hoặc -1
    private long checkUserAndGetId(String phone, String password) {
        long id = -1;
        Cursor cursor = db.getReadableDatabase().query(
                DatabaseHelper.TABLE_NHANVIEN,
                new String[]{DatabaseHelper.NV_ID},
                DatabaseHelper.NV_SDT + "=? AND " + DatabaseHelper.NV_MATKHAU + "=?",
                new String[]{phone, password},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getLong(0); // lấy cột đầu (NV_ID)
            cursor.close();
        }
        return id;
    }
}
