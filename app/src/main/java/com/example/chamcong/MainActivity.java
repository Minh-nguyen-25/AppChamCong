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
    Button btnCheckIn, btnCheckOut, btnRegisterShift, btnViewShifts, btnViewSalary, btndangxuat;

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
        btndangxuat = findViewById(R.id.btndangxuat);

        manv = getIntent().getIntExtra("MANV", -1);
        if (manv == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadNhanVien();
        loadCaHomNay();
        loadHistory(); // hiển thị lịch sử hôm nay

        btnCheckIn.setOnClickListener(v -> doCheckIn());
        btnCheckOut.setOnClickListener(v -> doCheckOut());

        btnRegisterShift.setOnClickListener(v -> startActivity(new Intent(this, RegisterShiftActivity.class)));
        btnViewShifts.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkScheduleActivity.class);
            intent.putExtra("USER_ID", manv);
            startActivity(intent);
        });
        btnViewSalary.setOnClickListener(v -> {
            Intent intent = new Intent(this, SalaryActivity.class);
            intent.putExtra("USER_ID", manv);
            startActivity(intent);
        });
        btndangxuat.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
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
        todayCaId = -1;
        todayCaString = null;

        if (c != null && c.moveToFirst()) {
            // lấy row đầu tiên (thường sẽ chỉ có 1 ca cho 1 nhân viên 1 ngày)
            todayCaId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CL_ID));
            todayCaString = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CL_CA));
            String checkIn = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKIN));
            String checkOut = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKOUT));

            String base = "Ca hôm nay: " + todayCaString;
            if (checkIn != null) base += "\n(Đã check-in: " + checkIn + ")";
            if (checkOut != null) base += "\n(Đã check-out: " + checkOut + ")";
            tvShiftToday.setText(base);

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
        // Cập nhật trường checkIn trong CaLam (vẫn lưu latest vào CaLam)
        db.updateCheckInTime(todayCaId, time);
        // Lưu lịch sử (mỗi lần bấm sẽ lưu 1 record)
        db.saveCheckHistory(manv, "in", time);

        loadCaHomNay();
        loadHistory();

        // show thông báo: lấy phút muộn từ bảng CaLam
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
        // Cập nhật trường checkOut trong CaLam
        db.updateCheckOutTime(todayCaId, time);
        // Lưu lịch sử
        db.saveCheckHistory(manv, "out", time);

        loadCaHomNay();
        loadHistory();

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

    // Hiển thị lịch sử check-in/out của hôm nay (chỉ hôm nay)
    private void loadHistory() {
        Cursor h = db.getTodayHistory(manv);
        StringBuilder sb = new StringBuilder();

        // Hiện phần thông tin ca hiện tại (nếu có)
        if (todayCaString != null) {
            sb.append("Ca hôm nay: ").append(todayCaString).append("\n");
        } else {
            sb.append("Hôm nay chưa đăng ký ca\n");
        }

        // Thêm phần lịch sử
        sb.append("Lịch sử hôm nay:\n");
        if (h != null && h.moveToFirst()) {
            do {
                String loai = h.getString(h.getColumnIndexOrThrow(DatabaseHelper.CH_LOAI));
                String gio = h.getString(h.getColumnIndexOrThrow(DatabaseHelper.CH_GIO));
                String label = loai.equals("in") ? "Check-in" : "Check-out";
                sb.append(label).append(" lúc ").append(gio).append("\n");
            } while (h.moveToNext());
            h.close();
        } else {
            if (h != null) h.close();
        }

        tvShiftToday.setText(sb.toString());
    }
}
