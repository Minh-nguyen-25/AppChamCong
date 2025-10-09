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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterShiftActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LinearLayout layoutShift;
    private TextView tvSelectedDate;
    private CheckBox shiftMorning, shiftAfternoon, shiftEvening;
    private Button btnConfirmShift, btnRegisterOT;

    private String selectedDate = "";

    // Lưu danh sách ca đã đăng ký: key = ngày, value = list ca
    private Map<String, List<String>> shiftData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_shift);

        // ánh xạ view
        calendarView = findViewById(R.id.calendarView);
        layoutShift = findViewById(R.id.layoutShift);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        shiftMorning = findViewById(R.id.shiftMorning);
        shiftAfternoon = findViewById(R.id.shiftAfternoon);
        shiftEvening = findViewById(R.id.shiftEvening);
        btnConfirmShift = findViewById(R.id.btnConfirmShift);
        btnRegisterOT = findViewById(R.id.btnRegisterOT);

        // sự kiện chọn ngày trong lịch
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText("Ngày " + selectedDate);
            layoutShift.setVisibility(View.VISIBLE);

            // reset checkbox mỗi khi chọn ngày mới
            shiftMorning.setChecked(false);
            shiftAfternoon.setChecked(false);
            shiftEvening.setChecked(false);

            // nếu ngày đã có dữ liệu thì tick lại
            if (shiftData.containsKey(selectedDate)) {
                List<String> registered = shiftData.get(selectedDate);
                if (registered.contains("Sáng")) shiftMorning.setChecked(true);
                if (registered.contains("Chiều")) shiftAfternoon.setChecked(true);
                if (registered.contains("Tối")) shiftEvening.setChecked(true);
            }
        });

        // sự kiện bấm nút xác nhận ca thường
        btnConfirmShift.setOnClickListener(v -> {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> shifts = new ArrayList<>();
            if (shiftMorning.isChecked()) shifts.add("Sáng");
            if (shiftAfternoon.isChecked()) shifts.add("Chiều");
            if (shiftEvening.isChecked()) shifts.add("Tối");

            if (shifts.isEmpty()) {
                Toast.makeText(this, "Chưa chọn ca nào!", Toast.LENGTH_SHORT).show();
            } else if (shifts.size() > 2) {
                Toast.makeText(this, "Chỉ được đăng ký tối đa 2 ca thường!", Toast.LENGTH_SHORT).show();
            } else {
                shiftData.put(selectedDate, shifts);
                Toast.makeText(this, "Đăng ký thành công: " + shifts + " ngày " + selectedDate, Toast.LENGTH_SHORT).show();
                Log.d("SHIFT_DATA", shiftData.toString());
            }
        });

        // sự kiện bấm nút đăng ký OT
        btnRegisterOT.setOnClickListener(v -> {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> shifts = shiftData.getOrDefault(selectedDate, new ArrayList<>());

            // kiểm tra: phải đủ 2 ca thường mới cho đăng ký OT
            if (shifts.size() < 2) {
                Toast.makeText(this, "Cần đăng ký đủ 2 ca thường trước khi đăng ký OT!", Toast.LENGTH_SHORT).show();
                return;
            }

            // thêm ca OT còn lại
            if (!shifts.contains("Sáng")) {
                shifts.add("Sáng");
                shiftMorning.setChecked(true);
                Toast.makeText(this, "Đăng ký OT: ca Sáng ngày " + selectedDate, Toast.LENGTH_SHORT).show();
            } else if (!shifts.contains("Chiều")) {
                shifts.add("Chiều");
                shiftAfternoon.setChecked(true);
                Toast.makeText(this, "Đăng ký OT: ca Chiều ngày " + selectedDate, Toast.LENGTH_SHORT).show();
            } else if (!shifts.contains("Tối")) {
                shifts.add("Tối");
                shiftEvening.setChecked(true);
                Toast.makeText(this, "Đăng ký OT: ca Tối ngày " + selectedDate, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tất cả ca trong ngày đã được đăng ký!", Toast.LENGTH_SHORT).show();
            }

            shiftData.put(selectedDate, shifts);
            Log.d("SHIFT_DATA", shiftData.toString());
        });
    }
}
