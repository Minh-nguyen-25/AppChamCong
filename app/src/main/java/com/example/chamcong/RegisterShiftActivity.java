package com.example.chamcong;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterShiftActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LinearLayout layoutShift;
    private TextView tvSelectedDate;
    private CheckBox shiftMorning, shiftAfternoon, shiftEvening;
    private Button btnConfirmShift, btnRegisterOT;

    private String selectedDate = ""; // dd/MM/yyyy
    private SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    DatabaseHelper db;
    int manv = 1; // 👈 tạm set cứng - sau lấy từ đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_shift);

        db = new DatabaseHelper(this);

        calendarView = findViewById(R.id.calendarView);
        layoutShift = findViewById(R.id.layoutShift);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        shiftMorning = findViewById(R.id.shiftMorning);
        shiftAfternoon = findViewById(R.id.shiftAfternoon);
        shiftEvening = findViewById(R.id.shiftEvening);
        btnConfirmShift = findViewById(R.id.btnConfirmShift);
        btnRegisterOT = findViewById(R.id.btnRegisterOT);

        layoutShift.setVisibility(View.GONE);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth, 0, 0, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);

            if (chosen.getTimeInMillis() <= today.getTimeInMillis()) {
                layoutShift.setVisibility(View.GONE);
                Toast.makeText(this, "Ngày hôm nay đã hết ca có thể đăng kí ", Toast.LENGTH_SHORT).show();
                selectedDate = "";
                return;
            }

            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText("Ngày " + selectedDate);
            layoutShift.setVisibility(View.VISIBLE);

            shiftMorning.setChecked(false);
            shiftAfternoon.setChecked(false);
            shiftEvening.setChecked(false);
        });

        btnConfirmShift.setOnClickListener(v -> saveNormalShift());
        btnRegisterOT.setOnClickListener(v -> saveOTShift());
    }

    private void saveNormalShift() {

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selected = new ArrayList<>();
        if (shiftMorning.isChecked()) selected.add("8h30-13h");
        if (shiftAfternoon.isChecked()) selected.add("13h-17h30");
        if (shiftEvening.isChecked()) selected.add("17h30-22h");

        if (selected.isEmpty()) {
            Toast.makeText(this, "Chưa chọn ca nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selected.size() > 2) {
            Toast.makeText(this, "Chỉ được chọn tối đa 2 ca thường!", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateDB = convertToDBDate(selectedDate);

        for (String ca : selected) {
            db.addCaLam(manv, dateDB, ca, 0, null, null, 0);
        }

        Toast.makeText(this, "Đăng ký ca thường thành công!", Toast.LENGTH_SHORT).show();
    }

    private void saveOTShift() {

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateDB = convertToDBDate(selectedDate);

        // Đếm số ca thường đã có
        int count = db.getCaLamForDate(manv, dateDB).getCount();
        if (count < 2) {
            Toast.makeText(this, "Phải đủ 2 ca thường mới được đăng ký OT!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký OT nếu chưa có
        if (!shiftMorning.isChecked()) {
            db.addCaLam(manv, dateDB, "8h30-13h", 1, null, null, 0);
            shiftMorning.setChecked(true);
            Toast.makeText(this, " Đăng ký OT ca Sáng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!shiftAfternoon.isChecked()) {
            db.addCaLam(manv, dateDB, "13h-17h30", 1, null, null, 0);
            shiftAfternoon.setChecked(true);
            Toast.makeText(this, " Đăng ký OT ca Chiều!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!shiftEvening.isChecked()) {
            db.addCaLam(manv, dateDB, "17h30-22h", 1, null, null, 0);
            shiftEvening.setChecked(true);
            Toast.makeText(this, " Đăng ký OT ca Tối!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Tất cả ca đã được đăng ký!", Toast.LENGTH_SHORT).show();
    }

    private String convertToDBDate(String dmy) { // dd/MM/yyyy → yyyy-MM-dd
        try {
            String[] p = dmy.split("/");
            return p[2] + "-" + (p[1].length()==1?"0"+p[1]:p[1]) + "-" + (p[0].length()==1?"0"+p[0]:p[0]);
        } catch (Exception e) {
            return "";
        }
    }
}
