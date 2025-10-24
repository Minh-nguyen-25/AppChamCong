package com.example.chamcong;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tvName, tvBirthday, tvPosition, tvSalary, tvShiftToday;
    Button btnCheckIn, btnCheckOut, btnRegisterShift, btnViewShifts, btnViewSalary;

    DatabaseHelper dbHelper;
    int manv; // mã nhân viên đăng nhập
    String loai;
    double mucLuong;
    int calamId = -1; // id ca làm trong ngày

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // 🟢 Lấy mã nhân viên từ Intent (truyền từ LoginActivity)
        manv = getIntent().getIntExtra("MANV", -1);
        if (manv == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin nhân viên!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ view
        tvName = findViewById(R.id.tvName);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvPosition = findViewById(R.id.tvPosition);
        tvSalary = findViewById(R.id.tvSalary);
        tvShiftToday = findViewById(R.id.tvShiftToday);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnRegisterShift = findViewById(R.id.btnRegisterShift);
        btnViewShifts = findViewById(R.id.btnViewShifts);
        btnViewSalary = findViewById(R.id.btnViewSalary);

        // Hiển thị thông tin nhân viên
        loadEmployeeInfo();

        // Kiểm tra ca làm hôm nay
        checkTodayShift();

        // Nút Check In
        btnCheckIn.setOnClickListener(v -> handleCheckIn());

        // Nút Check Out
        btnCheckOut.setOnClickListener(v -> handleCheckOut());

        // Nút chuyển Activity
        btnRegisterShift.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterShiftActivity.class).putExtra("MANV", manv))
        );
        btnViewShifts.setOnClickListener(v ->
                startActivity(new Intent(this, WorkScheduleActivity.class).putExtra("MANV", manv))
        );
        btnViewSalary.setOnClickListener(v ->
                startActivity(new Intent(this, SalaryActivity.class).putExtra("MANV", manv))
        );
    }

    // ======================= HIỂN THỊ THÔNG TIN =======================
    private void loadEmployeeInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NHANVIEN +
                " WHERE " + DatabaseHelper.NV_ID + "=?", new String[]{String.valueOf(manv)});
        if (cursor.moveToFirst()) {
            String hoTen = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_HOTEN));
            String ngaySinh = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_NGAYSINH));
            String chucVu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_CHUCVU));
            mucLuong = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_MUCLUONG));
            loai = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_LOAI));

            tvName.setText("Họ tên: " + hoTen);
            tvBirthday.setText("Ngày sinh: " + ngaySinh);
            tvPosition.setText("Chức vụ: " + chucVu);
            tvSalary.setText("Mức lương: " + mucLuong + (loai.equals("parttime") ? "đ/giờ" : "đ/tháng"));
        }
        cursor.close();
    }

    // ======================= KIỂM TRA CA HÔM NAY =======================
    private void checkTodayShift() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CALAM +
                        " WHERE " + DatabaseHelper.CL_MANV + "=? AND " + DatabaseHelper.CL_NGAY + "=?",
                new String[]{String.valueOf(manv), today});

        if (cursor.moveToFirst()) {
            calamId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CL_ID));
            String ca = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CL_CA));
            Integer checkIn = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKIN));
            Integer checkOut = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKOUT));

            tvShiftToday.setText("Ca hôm nay: " + ca);

            // ✅ Cập nhật trạng thái nút
            if (checkIn != 0) {
                btnCheckIn.setText("Đã Check In");
                btnCheckIn.setEnabled(false);
            }
            if (checkOut != 0) {
                btnCheckOut.setText("Đã Check Out");
                btnCheckOut.setEnabled(false);
            }
        } else {
            tvShiftToday.setText("Hôm nay chưa có ca làm!");
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(false);
        }
        cursor.close();
    }

    // ======================= CHECK IN =======================
    private void handleCheckIn() {
        if (calamId == -1) {
            Toast.makeText(this, "Không tìm thấy ca làm hôm nay!", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        int time = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
        dbHelper.updateCheckIn(calamId, time, 0);

        btnCheckIn.setText("Đã Check In");
        btnCheckIn.setEnabled(false);
        Toast.makeText(this, "Check In lúc: " + time, Toast.LENGTH_SHORT).show();
    }

    // ======================= CHECK OUT =======================
    private void handleCheckOut() {
        if (calamId == -1) {
            Toast.makeText(this, "Không tìm thấy ca làm hôm nay!", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        int time = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
        dbHelper.updateCheckOut(calamId, time, 0);

        btnCheckOut.setText("Đã Check Out");
        btnCheckOut.setEnabled(false);
        Toast.makeText(this, "Check Out lúc: " + time, Toast.LENGTH_SHORT).show();
    }
}
