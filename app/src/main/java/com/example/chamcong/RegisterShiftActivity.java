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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterShiftActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LinearLayout layoutShift;
    private TextView tvSelectedDate;
    private CheckBox shiftMorning, shiftAfternoon, shiftEvening;
    private Button btnConfirmShift, btnRegisterOT;

    private String selectedDate = ""; // dd/MM/yyyy
    private Map<String, List<String>> shiftData = new HashMap<>(); // lưu ca tạm thời

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_shift);

        // Ánh xạ view
        calendarView = findViewById(R.id.calendarView);
        layoutShift = findViewById(R.id.layoutShift);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        shiftMorning = findViewById(R.id.shiftMorning);
        shiftAfternoon = findViewById(R.id.shiftAfternoon);
        shiftEvening = findViewById(R.id.shiftEvening);
        btnConfirmShift = findViewById(R.id.btnConfirmShift);
        btnRegisterOT = findViewById(R.id.btnRegisterOT);

        layoutShift.setVisibility(View.GONE);

        // Sự kiện chọn ngày
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            // chuẩn ngày chọn
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth, 0, 0, 0);
            chosen.set(Calendar.MILLISECOND, 0);

            // chuẩn ngày hôm nay
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // kiểm tra ngày hợp lệ (phải > hôm nay)
            if (chosen.getTimeInMillis() <= today.getTimeInMillis()) {
                layoutShift.setVisibility(View.GONE);
                Toast.makeText(this, "Không thể đăng ký ca cho hôm nay hoặc ngày đã qua!", Toast.LENGTH_SHORT).show();
                selectedDate = "";
                return;
            }

            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText("Ngày " + selectedDate);
            layoutShift.setVisibility(View.VISIBLE);

            // reset checkbox
            shiftMorning.setChecked(false);
            shiftAfternoon.setChecked(false);
            shiftEvening.setChecked(false);

            // nếu đã có dữ liệu ngày này → hiển thị lại
            if (shiftData.containsKey(selectedDate)) {
                List<String> registered = shiftData.get(selectedDate);
                if (registered.contains("Sáng")) shiftMorning.setChecked(true);
                if (registered.contains("Chiều")) shiftAfternoon.setChecked(true);
                if (registered.contains("Tối")) shiftEvening.setChecked(true);
            }
        });

        // Xác nhận ca thường
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
                return;
            }

            if (shifts.size() > 2) {
                Toast.makeText(this, "Chỉ được đăng ký tối đa 2 ca thường!", Toast.LENGTH_SHORT).show();
                return;
            }

            shiftData.put(selectedDate, shifts);
            Toast.makeText(this, "Đăng ký thành công " + shifts + " ngày " + selectedDate, Toast.LENGTH_SHORT).show();
            Log.d("SHIFT_DATA", shiftData.toString());
        });

        // Đăng ký OT
        btnRegisterOT.setOnClickListener(v -> {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> shifts = shiftData.getOrDefault(selectedDate, new ArrayList<>());

            if (shifts.size() < 2) {
                Toast.makeText(this, "Cần đăng ký đủ 2 ca thường trước khi đăng ký OT!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!shifts.contains("Sáng")) {
                shifts.add("Sáng");
                shiftMorning.setChecked(true);
                Toast.makeText(this, "Đăng ký OT ca Sáng thành công!", Toast.LENGTH_SHORT).show();
            } else if (!shifts.contains("Chiều")) {
                shifts.add("Chiều");
                shiftAfternoon.setChecked(true);
                Toast.makeText(this, "Đăng ký OT ca Chiều thành công!", Toast.LENGTH_SHORT).show();
            } else if (!shifts.contains("Tối")) {
                shifts.add("Tối");
                shiftEvening.setChecked(true);
                Toast.makeText(this, "Đăng ký OT ca Tối thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tất cả ca đã được đăng ký!", Toast.LENGTH_SHORT).show();
                return;
            }

            shiftData.put(selectedDate, shifts);
            Log.d("SHIFT_DATA", shiftData.toString());
        });
    }
}
