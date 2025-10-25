package com.example.chamcong;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Màn hình hiển thị chi tiết ca làm việc của nhân viên.
 * Người dùng có thể chọn ngày để xem thông tin chấm công.
 */
public class WorkScheduleActivity extends AppCompatActivity {

    // --- Các thành phần giao diện ---
    private CalendarView lich;
    private LinearLayout khungChiTietLamViec;
    private TextView tvNgay, tvGioVao, tvGioRa, tvCaLam, tvTongGioLam, tvGioTangCaTrongNgay;

    // --- Biến nghiệp vụ ---
    private DatabaseHelper troGiupCSDL;
    private int maNhanVien;

    // Định dạng ngày chuẩn yyyy-MM-dd để truy vấn
    private final SimpleDateFormat dinhDangNgay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_schedule);

        // --- 1. Khởi tạo DatabaseHelper ---
        troGiupCSDL = new DatabaseHelper(this);

        // --- 2. Lấy mã nhân viên từ Intent ---
        maNhanVien = getIntent().getIntExtra("USER_ID", -1);
        if (maNhanVien == -1) {
            finish();
            return;
        }

        // --- 3. Ánh xạ View ---
        lich = findViewById(R.id.calendarView);
        khungChiTietLamViec = findViewById(R.id.layoutWorkDetail);
        tvNgay = findViewById(R.id.tvDate);
        tvGioVao = findViewById(R.id.tvCheckIn);
        tvGioRa = findViewById(R.id.tvCheckOut);
        tvCaLam = findViewById(R.id.tvShift);
        tvTongGioLam = findViewById(R.id.tvTotalHours);
        tvGioTangCaTrongNgay = findViewById(R.id.tvOvertimeHoursDay);

        // Ban đầu ẩn phần chi tiết
        khungChiTietLamViec.setVisibility(View.GONE);

        // --- 4. Bắt sự kiện chọn ngày trên lịch ---
        lich.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int nam, int thang, int ngayTrongThang) {
                Calendar lichTam = Calendar.getInstance();
                lichTam.set(nam, thang, ngayTrongThang);
                String ngayChon = dinhDangNgay.format(lichTam.getTime());

                // Hiển thị thông tin ca làm của ngày được chọn
                hienThiThongTinCaLam(ngayChon);
            }
        });

        // --- 5. Mặc định hiển thị thông tin hôm nay ---
        String homNay = dinhDangNgay.format(Calendar.getInstance().getTime());
        hienThiThongTinCaLam(homNay);
    }

    /**
     * Hiển thị chi tiết ca làm việc cho ngày cụ thể.
     *
     * @param ngayChuoi Chuỗi ngày dạng yyyy-MM-dd
     */
    private void hienThiThongTinCaLam(String ngayChuoi) {
        Cursor duLieu = troGiupCSDL.getCaLamForDate(maNhanVien, ngayChuoi);

        if (duLieu != null && duLieu.moveToFirst()) {
            // --- Nếu có dữ liệu ---
            khungChiTietLamViec.setVisibility(View.VISIBLE);

            String gioVao = duLieu.getString(duLieu.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKIN));
            String gioRa = duLieu.getString(duLieu.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKOUT));
            String caLam = duLieu.getString(duLieu.getColumnIndexOrThrow(DatabaseHelper.CL_CA));
            int gioTangCa = duLieu.getInt(duLieu.getColumnIndexOrThrow(DatabaseHelper.CL_OT));

            tvNgay.setText("Ngày: " + ngayChuoi);
            tvGioVao.setText("Giờ vào: " + (gioVao != null ? gioVao : "Chưa chấm công"));
            tvGioRa.setText("Giờ ra: " + (gioRa != null ? gioRa : "Chưa chấm công"));
            tvCaLam.setText("Ca làm: " + caLam);
            tvGioTangCaTrongNgay.setText("Giờ tăng ca: " + gioTangCa + " giờ");

            // --- Tính tổng giờ làm ---
            if (gioVao != null && gioRa != null) {
                try {
                    long thoiGianLam = new SimpleDateFormat("HH:mm", Locale.getDefault())
                            .parse(gioRa).getTime() -
                            new SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .parse(gioVao).getTime();

                    double tongGioLam = (double) thoiGianLam / (1000 * 60 * 60);
                    tvTongGioLam.setText(String.format(Locale.getDefault(), "Số giờ làm: %.2f giờ", tongGioLam));
                } catch (Exception e) {
                    tvTongGioLam.setText("Số giờ làm: Lỗi tính toán");
                }
            } else {
                tvTongGioLam.setText("Số giờ làm: N/A");
            }

            duLieu.close();
        } else {
            // --- Nếu không có dữ liệu ---
            khungChiTietLamViec.setVisibility(View.VISIBLE); // Hiện khung để báo không có ca
            tvNgay.setText("Ngày: " + ngayChuoi);
            tvGioVao.setText("Giờ vào: Không có ca làm");
            tvGioRa.setText("Giờ ra: Không có ca làm");
            tvCaLam.setText("Ca làm: Không có ca làm");
            tvTongGioLam.setText("Số giờ làm: N/A");
            tvGioTangCaTrongNgay.setText("Giờ tăng ca: N/A");
        }
    }
}
