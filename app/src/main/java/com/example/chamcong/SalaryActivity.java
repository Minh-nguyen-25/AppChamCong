package com.example.chamcong;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;

public class SalaryActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    Spinner spnMonth, spnYear;
    EditText edtTongSoCa, edGioLamThuong, edGioTangCa, edLuongCaThuong, edLuongTangCa, edTongLuong;

    long userId; // lấy từ intent (khi login xong)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salary );

        dbHelper = new DatabaseHelper(this);

        // --- ÁNH XẠ VIEW ---
        spnMonth = findViewById(R.id.spnMonth);
        spnYear = findViewById(R.id.spnYear);
        edtTongSoCa = findViewById(R.id.edtongsoca);
        edGioLamThuong = findViewById(R.id.edgiolamthuong);
        edGioTangCa = findViewById(R.id.edgiotangca);
        edLuongCaThuong = findViewById(R.id.edluongcathuong);
        edLuongTangCa = findViewById(R.id.edluongtangca);
        edTongLuong = findViewById(R.id.edtongluong);

        // Lấy userId từ Intent
        userId = getIntent().getLongExtra("USER_ID", -1);

        // Gán adapter cho spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this, R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(monthAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this, R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnYear.setAdapter(yearAdapter);

        // --- GẮN SỰ KIỆN KHI CHỌN THÁNG HOẶC NĂM ---
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSalaryData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        spnMonth.setOnItemSelectedListener(listener);
        spnYear.setOnItemSelectedListener(listener);

        // Tải dữ liệu ban đầu
        loadSalaryData();
    }

    // ⚙️ HÀM LẤY DỮ LIỆU TỪ DB
    @SuppressLint("DefaultLocale")
    private void loadSalaryData() {
        String monthStr = spnMonth.getSelectedItem().toString().replaceAll("\\D+", "");
        String yearStr = spnYear.getSelectedItem().toString();

        // Định dạng tháng thành YYYY-MM để khớp với DB
        String formattedMonth = String.format("%s-%02d", yearStr, Integer.parseInt(monthStr));

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.TH_NGAYLAM + ", " + DatabaseHelper.TH_LUONG + " FROM " + DatabaseHelper.TABLE_TONGHOP + " WHERE " + DatabaseHelper.TH_MANV + "=? AND " + DatabaseHelper.TH_THANG + "=?",
                new String[]{String.valueOf(userId), formattedMonth});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int tongNgayLam = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TH_NGAYLAM));
            @SuppressLint("Range") double tongLuong = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TH_LUONG));

            // Các tính toán khác (có thể thay đổi tùy theo logic của bạn)
            // Giả sử edtTongSoCa hiển thị tổng ngày làm
            double gioThuong = tongNgayLam * 8; 
            double gioTangCa = 0;
            double luongCaThuong = tongLuong; 
            double luongTangCa = 0;

            edtTongSoCa.setText(String.valueOf(tongNgayLam));
            edGioLamThuong.setText(String.format("%.1f", gioThuong));
            edGioTangCa.setText(String.format("%.1f", gioTangCa));
            edLuongCaThuong.setText(String.format("%,.0fđ", luongCaThuong));
            edLuongTangCa.setText(String.format("%,.0fđ", luongTangCa));
            edTongLuong.setText(String.format("%,.0fđ", tongLuong));
        } else {
            Toast.makeText(this, "Không có dữ liệu lương cho tháng đã chọn", Toast.LENGTH_SHORT).show();
            clearSalaryData();
        }
        cursor.close();
    }

    private void clearSalaryData(){
        edtTongSoCa.setText("0");
        edGioLamThuong.setText("0");
        edGioTangCa.setText("0");
        edLuongCaThuong.setText("0");
        edLuongTangCa.setText("0");
        edTongLuong.setText("0");
    }
}
