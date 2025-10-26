package com.example.chamcong;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Màn hình hiển thị chi tiết lương cho nhân viên.
 * Tự động tính lương trong tháng dựa vào dữ liệu chấm công.
 */
public class SalaryActivity extends AppCompatActivity {

    // --- Các hằng số tính lương ---
    private static final double TIEN_PHAT_DI_MUON = 50000; // Phạt 50,000đ/lần đi muộn
    private static final double HE_SO_TANG_CA = 1.5;       // Lương tăng ca nhân 1.5
    private static final int GIO_CHUAN_THANG_FULLTIME = 208; // 8 giờ/ngày * 26 ngày

    // --- Các thành phần giao diện ---
    TextView tvTieuDeLuong;
    EditText edTongSoCa, edGioLamThuong, edGioTangCa, edSoLanDiMuon,
            edTienPhat, edLuongCaThuong, edLuongTangCa, edTongLuong;

    // --- Biến nghiệp vụ ---
    DatabaseHelper dbTroGiup;
    int maNhanVien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salary);

        // --- 1. Khởi tạo và ánh xạ ---
        dbTroGiup = new DatabaseHelper(this);

        tvTieuDeLuong = findViewById(R.id.tvSalaryTitle);
        edTongSoCa = findViewById(R.id.edtongsoca);
        edGioLamThuong = findViewById(R.id.edgiolamthuong);
        edGioTangCa = findViewById(R.id.edgiotangca);
        edSoLanDiMuon = findViewById(R.id.edsolandiMuon);
        edTienPhat = findViewById(R.id.edtienphat);
        edLuongCaThuong = findViewById(R.id.edluongcathuong);
        edLuongTangCa = findViewById(R.id.edluongtangca);
        edTongLuong = findViewById(R.id.edtongluong);

        // --- 2. Lấy mã nhân viên từ Intent ---
        maNhanVien = getIntent().getIntExtra("USER_ID", -1);
        if (maNhanVien == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- 3. Tính toán lương ---
        tinhVaHienThiLuong();
    }

    /**
     * Hàm chính: tính và hiển thị lương.
     */
    private void tinhVaHienThiLuong() {
        Calendar lich = Calendar.getInstance(); // Lấy thời gian hiện tại
        int nam = lich.get(Calendar.YEAR);      // Lấy năm hiện tại
        int thang = lich.get(Calendar.MONTH) + 1; // Lấy tháng hiện tại (lưu ý: tháng bắt đầu từ 0)


        tvTieuDeLuong.setText(String.format(Locale.getDefault(), "Tổng hợp lương tháng %d/%d", thang, nam));

        String thangNamChon = String.format(Locale.getDefault(), "%d-%02d", nam, thang);

        // --- Bước 2: Lấy thông tin nhân viên ---
        Cursor duLieuNV = dbTroGiup.getNhanVienById(maNhanVien);
        if (duLieuNV == null || !duLieuNV.moveToFirst()) {
            Toast.makeText(this, "Không thể tìm thấy thông tin nhân viên.", Toast.LENGTH_SHORT).show();
            if (duLieuNV != null) duLieuNV.close();
            return;
        }

        String loaiNV = duLieuNV.getString(duLieuNV.getColumnIndexOrThrow(DatabaseHelper.NV_LOAI));
        double mucLuong = duLieuNV.getDouble(duLieuNV.getColumnIndexOrThrow(DatabaseHelper.NV_MUCLUONG));
        duLieuNV.close();

        // --- Bước 3: Lấy dữ liệu chấm công ---
        Cursor duLieuCaLam = dbTroGiup.getReadableDatabase().rawQuery(
                "SELECT * FROM CaLam WHERE MANV = ? AND strftime('%Y-%m', Ngay) = ?",
                new String[]{String.valueOf(maNhanVien), thangNamChon}
        );

        int tongSoCa = 0;
        int soLanDiMuon = 0;
        double tongPhutLamThuong = 0;
        double tongGioTangCa = 0;

        if (duLieuCaLam != null && duLieuCaLam.moveToFirst()) {
            tongSoCa = duLieuCaLam.getCount();
            do {
                if (duLieuCaLam.getInt(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_MUON)) > 0) {
                    soLanDiMuon++;
                }
                tongGioTangCa += duLieuCaLam.getDouble(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_OT));

                String gioVao = duLieuCaLam.getString(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKIN));
                String gioRa = duLieuCaLam.getString(duLieuCaLam.getColumnIndexOrThrow(DatabaseHelper.CL_CHECKOUT));

                if (gioVao != null && gioRa != null) {
                    tongPhutLamThuong += (chuyenDoiPhut(gioRa) - chuyenDoiPhut(gioVao));
                }
            } while (duLieuCaLam.moveToNext());
        }
        if (duLieuCaLam != null) duLieuCaLam.close();

        double tongGioLamThuong = tongPhutLamThuong / 60.0;

        // --- Bước 4: Tính lương ---
        double luongMotGio=mucLuong;
        double luongThuong;
        double luongTangCa;

        if ("parttime".equalsIgnoreCase(loaiNV)) {
            luongThuong = tongGioLamThuong * luongMotGio;
        } else {
            luongMotGio = mucLuong / GIO_CHUAN_THANG_FULLTIME;
            luongThuong = tongGioLamThuong * luongMotGio;
        }

        luongTangCa = tongGioTangCa * luongMotGio * HE_SO_TANG_CA;
        double tienPhat = soLanDiMuon * TIEN_PHAT_DI_MUON;
        double tongLuong = luongThuong + luongTangCa - tienPhat;

        // --- Bước 5: Hiển thị ---
        NumberFormat dinhDangTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        edTongSoCa.setText(String.valueOf(tongSoCa));
        edGioLamThuong.setText(String.format(Locale.US, "%.1f", tongGioLamThuong));
        edGioTangCa.setText(String.format(Locale.US, "%.1f", tongGioTangCa));
        edSoLanDiMuon.setText(String.valueOf(soLanDiMuon));
        edTienPhat.setText(dinhDangTien.format(tienPhat));
        edLuongCaThuong.setText(dinhDangTien.format(luongThuong));
        edLuongTangCa.setText(dinhDangTien.format(luongTangCa));
        edTongLuong.setText(dinhDangTien.format(tongLuong));
    }

    /**
     * Chuyển chuỗi "HH:mm" sang tổng số phút.
     * Ví dụ: "08:30" → 510 phút.
     */
    private double chuyenDoiPhut(String thoiGian) {
        if (thoiGian == null || !thoiGian.contains(":")) {
            return 0;
        }
        try {
            String[] tach = thoiGian.split(":");
            int gio = Integer.parseInt(tach[0]);
            int phut = Integer.parseInt(tach[1]);
            return gio * 60 + phut;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
