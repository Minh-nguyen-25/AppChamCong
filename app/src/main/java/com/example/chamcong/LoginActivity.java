package com.example.chamcong;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        this.deleteDatabase("ChamCong.db");

        db = new DatabaseHelper(this);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty() || phone.length() != 10) {
                Toast.makeText(this, "Vui lòng nhập đúng định dạng số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = checkUserAndGetId(phone, password);
            if (userId != -1) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Sai số điện thoại hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kiểm tra người dùng theo số điện thoại & mật khẩu.
     * Trả về id nếu đúng, -1 nếu sai.
     */
    private long checkUserAndGetId(String phone, String password) {
        SQLiteDatabase database = db.getReadableDatabase();

        Cursor cursor = database.rawQuery(
                "SELECT " + DatabaseHelper.NV_ID + " FROM " + DatabaseHelper.TABLE_NHANVIEN + " WHERE " + DatabaseHelper.NV_SDT + " = ? AND " + DatabaseHelper.NV_MATKHAU + " = ?",
                new String[]{phone, password}
        );

        long id = -1;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(DatabaseHelper.NV_ID);
            if (index >= 0) id = cursor.getLong(index);
        }

        cursor.close();
        database.close();
        return id;
    }

}
