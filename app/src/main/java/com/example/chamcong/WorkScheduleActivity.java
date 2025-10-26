package com.example.chamcong;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WorkScheduleActivity extends AppCompatActivity {

    private CalendarView lichCalendar;
    private LinearLayout khungCaLam;
    private TextView tvNgayDuocChon;

    private DatabaseHelper troGiupCSDL;
    private int maNhanVien;

    private final SimpleDateFormat dinhDangNgay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dinhDangNgayHienThi = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_schedule);

        // 1. Khởi tạo cơ sở dữ liệu và lấy mã nhân viên
        troGiupCSDL = new DatabaseHelper(this);
        maNhanVien = getIntent().getIntExtra("USER_ID", -1);
        if (maNhanVien == -1) {
            finish(); // Không có mã nhân viên => đóng Activity
            return;
        }

        // 2. Ánh xạ View từ layout
        lichCalendar = findViewById(R.id.calendarView);
        khungCaLam = findViewById(R.id.shifts_container);
        tvNgayDuocChon = findViewById(R.id.tvSelectedDate);

        // 3. Sự kiện chọn ngày trên lịch
        lichCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int nam, int thang, int ngay) {
                Calendar lichDaChon = Calendar.getInstance();
                lichDaChon.set(nam, thang, ngay);
                String ngayDaChon = dinhDangNgay.format(lichDaChon.getTime());
                hienThiCaLamTheoNgay(ngayDaChon);
            }
        });

        // 4. Mặc định hiển thị thông tin hôm nay
        String homNay = dinhDangNgay.format(Calendar.getInstance().getTime());
        hienThiCaLamTheoNgay(homNay);
    }

    /**
     * Lấy dữ liệu và hiển thị danh sách ca làm cho một ngày cụ thể.
     * @param ngayChuoi Ngày cần hiển thị (định dạng yyyy-MM-dd)
     */
    private void hienThiCaLamTheoNgay(String ngayChuoi) {
        // Cập nhật TextView hiển thị ngày được chọn
        try {
            tvNgayDuocChon.setText("Chi tiết cho ngày: " + dinhDangNgayHienThi.format(dinhDangNgay.parse(ngayChuoi)));
        } catch (Exception e) {
            tvNgayDuocChon.setText("Chi tiết cho ngày: " + ngayChuoi);
        }

        // Xóa các view cũ
        khungCaLam.removeAllViews();

        Cursor duLieuCaLam = troGiupCSDL.getCaLamForDate(maNhanVien, ngayChuoi);

        if (duLieuCaLam != null && duLieuCaLam.moveToFirst()) {
            // Duyệt qua từng ca làm trong ngày
            do {
                String gioVao = duLieuCaLam.getString(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKIN));
                String gioRa = duLieuCaLam.getString(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKOUT));
                String tenCa = duLieuCaLam.getString(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_CA));
                int gioTangCa = duLieuCaLam.getInt(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_OT));

                // Tạo view hiển thị từng ca làm
                khungCaLam.addView(taoTheHienThiCaLam(gioVao, gioRa, tenCa, gioTangCa));

            } while (duLieuCaLam.moveToNext());

            duLieuCaLam.close();
        } else {
            // Không có ca làm nào
            TextView tvKhongCoCa = new TextView(this);
            tvKhongCoCa.setText("Không có ca làm trong ngày này.");
            tvKhongCoCa.setGravity(Gravity.CENTER);
            tvKhongCoCa.setPadding(0, 40, 0, 40);
            khungCaLam.addView(tvKhongCoCa);
        }
    }

    /**
     * Tạo và trả về một CardView hiển thị chi tiết ca làm.
     * @return CardView hiển thị ca làm
     */
    private View taoTheHienThiCaLam(String gioVao, String gioRa, String tenCa, int gioTangCa) {
        // Tạo CardView
        CardView theCa = new CardView(this);
        LinearLayout.LayoutParams thamSoThe = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        thamSoThe.setMargins(0, 0, 0, 16);
        theCa.setLayoutParams(thamSoThe);
        theCa.setRadius(8f);
        theCa.setContentPadding(24, 24, 24, 24);
        theCa.setCardBackgroundColor(Color.WHITE);

        // Layout bên trong CardView
        LinearLayout boCucNoiDung = new LinearLayout(this);
        boCucNoiDung.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        boCucNoiDung.setOrientation(LinearLayout.VERTICAL);

        // Thêm các dòng thông tin
        boCucNoiDung.addView(taoTextChiTiet("Ca làm: " + tenCa, true));
        boCucNoiDung.addView(taoTextChiTiet("Giờ vào: " + (gioVao != null ? gioVao : "Chưa chấm công"), false));
        boCucNoiDung.addView(taoTextChiTiet("Giờ ra: " + (gioRa != null ? gioRa : "Chưa chấm công"), false));
        boCucNoiDung.addView(taoTextChiTiet("Giờ tăng ca: " + gioTangCa + " giờ", false));

        theCa.addView(boCucNoiDung);
        return theCa;
    }

    /**
     * Hàm tiện ích để tạo một TextView hiển thị chi tiết.
     */
    private TextView taoTextChiTiet(String noiDung, boolean laTieuDe) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        tv.setText(noiDung);
        tv.setTextColor(Color.BLACK);
        if (laTieuDe) {
            tv.setTextSize(16f);
            tv.setPadding(0, 0, 0, 8);
        } else {
            tv.setTextSize(14f);
        }
        return tv;
    }
}
