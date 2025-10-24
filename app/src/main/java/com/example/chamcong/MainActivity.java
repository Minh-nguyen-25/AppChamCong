package com.example.chamcong;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tvName, tvBirthday, tvPosition, tvSalary, tvShiftToday;
    Button btnCheckIn, btnCheckOut, btnRegisterShift, btnViewShifts, btnViewSalary;

    DatabaseHelper db;
    int manv;
    int todayCaId = -1;
    String todayCaString = null; // e.g. "8h30-13h"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

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

        manv = getIntent().getIntExtra("MANV", -1);
        if (manv == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadNhanVien();
        loadCaHomNay();

        btnCheckIn.setOnClickListener(v -> doCheckIn());
        btnCheckOut.setOnClickListener(v -> doCheckOut());

        btnRegisterShift.setOnClickListener(v -> startActivity(new Intent(this, RegisterShiftActivity.class)));
        btnViewShifts.setOnClickListener(v -> startActivity(new Intent(this, WorkScheduleActivity.class)));
        btnViewSalary.setOnClickListener(v -> startActivity(new Intent(this, SalaryActivity.class)));
    }

    private void loadNhanVien() {
        Cursor c = db.getNhanVienById(manv);
        if (c != null && c.moveToFirst()) {

            tvName.setText("Họ tên: " + c.getString(c.getColumnIndexOrThrow(DatabaseHelper.NV_HOTEN)));
            tvBirthday.setText("Ngày sinh: " + c.getString(c.getColumnIndexOrThrow(DatabaseHelper.NV_NGAYSINH)));
            tvPosition.setText("Chức vụ: " + c.getString(c.getColumnIndexOrThrow(DatabaseHelper.NV_CHUCVU)));

            // ✅ LẤY LƯƠNG + FORMAT TIỀN VIỆT
            double salary = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.NV_MUCLUONG));
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedSalary = formatter.format(salary).replace("₫", "VNĐ");
            tvSalary.setText("Mức lương: " + formattedSalary);

            c.close();
        }
    }

    private void loadCaHomNay() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor c = db.getCaLamForDate(manv, today);
        if (c != null && c.moveToFirst()) {
            todayCaId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CL_ID));
            todayCaString = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CL_CA));
            String checkIn = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKIN));
            String checkOut = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKOUT));
            tvShiftToday.setText("Ca hôm nay: " + todayCaString +
                    (checkIn != null ? "\n(Đã check-in: " + checkIn + ")" : "") +
                    (checkOut != null ? "\n(Đã check-out: " + checkOut + ")" : ""));
            c.close();
        } else {
            tvShiftToday.setText("Hôm nay chưa đăng ký ca");
            if (c != null) c.close();
        }
    }

    private String nowHHmm() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private void doCheckIn() {
        if (todayCaId == -1) {
            Toast.makeText(this, "Bạn chưa có ca làm hôm nay!", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = nowHHmm();
        db.updateCheckInTime(todayCaId, time);

        loadCaHomNay();

        Cursor c = db.getCaLamForDate(manv, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        if (c != null && c.moveToFirst()) {
            int muon = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CL_MUON));
            if (muon > 0) {
                Toast.makeText(this, "Đã Check In lúc " + time + " — Trễ " + muon + " phút", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Đã Check In lúc " + time, Toast.LENGTH_SHORT).show();
            }
            c.close();
        }
    }

    private void doCheckOut() {
        if (todayCaId == -1) {
            Toast.makeText(this, "Bạn chưa có ca làm hôm nay!", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = nowHHmm();
        db.updateCheckOutTime(todayCaId, time);

        loadCaHomNay();

        Cursor c = db.getCaLamForDate(manv, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        if (c != null && c.moveToFirst()) {
            int som = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CL_SOM));
            if (som > 0) {
                Toast.makeText(this, "Đã Check Out lúc " + time + " — Về sớm " + som + " phút", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Đã Check Out lúc " + time, Toast.LENGTH_SHORT).show();
            }
            c.close();
        }
    }
}
