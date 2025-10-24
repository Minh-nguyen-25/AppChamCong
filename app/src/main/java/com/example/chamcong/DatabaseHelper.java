package com.example.chamcong;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // --- THÔNG TIN DB ---
    private static final String DATABASE_NAME = "ChamCong.db";
    private static final int DATABASE_VERSION = 4; // ✅ Tăng version lên để tránh lỗi downgrade

    // --- BẢNG NHÂN VIÊN ---
    public static final String TABLE_NHANVIEN = "NhanVien";
    public static final String NV_ID = "manv";
    public static final String NV_HOTEN = "hoTen";
    public static final String NV_NGAYSINH = "ngaySinh";
    public static final String NV_CHUCVU = "chucVu";
    public static final String NV_MUCLUONG = "mucLuong";
    public static final String NV_SDT = "soDienThoai";
    public static final String NV_LOAI = "loai";
    public static final String NV_MATKHAU = "matKhau";

    // --- BẢNG CA LÀM ---
    public static final String TABLE_CALAM = "CaLam";
    public static final String CL_ID = "id";
    public static final String CL_MANV = "manv";
    public static final String CL_NGAY = "ngay";
    public static final String CL_CA = "ca";
    public static final String CL_OT = "ot";
    public static final String CL_CHECKIN = "checkIn";
    public static final String CL_CHECKOUT = "checkOut";
    public static final String CL_MUON = "checkInMuon";
    public static final String CL_SOM = "checkOutSom";
    public static final String CL_NGHI = "nghiDuocKhong";

    // --- BẢNG TỔNG HỢP ---
    public static final String TABLE_TONGHOP = "TongHop";
    public static final String TH_ID = "id";
    public static final String TH_MANV = "manv";
    public static final String TH_THANG = "thang";
    public static final String TH_GIOLAM = "gioLamThuong";
    public static final String TH_GIOOT = "gioTangCa";
    public static final String TH_MUON = "phutMuon";
    public static final String TH_SOM = "phutSom";
    public static final String TH_NGAYLAM = "ngayLam";
    public static final String TH_NGAYNGHI = "ngayNghiCoLuong";
    public static final String TH_LUONG = "luong";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // BẢNG NHÂN VIÊN
        db.execSQL("CREATE TABLE " + TABLE_NHANVIEN + " (" +
                NV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NV_HOTEN + " TEXT NOT NULL, " +
                NV_NGAYSINH + " TEXT, " +
                NV_CHUCVU + " TEXT, " +
                NV_MUCLUONG + " REAL NOT NULL, " +
                NV_SDT + " TEXT, " +
                NV_LOAI + " TEXT CHECK(" + NV_LOAI + " IN ('fulltime','parttime')), " +
                NV_MATKHAU + " TEXT NOT NULL)");

        // BẢNG CA LÀM
        db.execSQL("CREATE TABLE " + TABLE_CALAM + " (" +
                CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CL_MANV + " INTEGER NOT NULL, " +
                CL_NGAY + " TEXT NOT NULL, " +
                CL_CA + " TEXT CHECK(" + CL_CA + " IN ('8h30-13h','13h-17h30','17h30-22h')), " +
                CL_OT + " INTEGER DEFAULT 0, " +
                CL_CHECKIN + " INTEGER, " +
                CL_CHECKOUT + " INTEGER, " +
                CL_MUON + " INTEGER DEFAULT 0, " +
                CL_SOM + " INTEGER DEFAULT 0, " +
                CL_NGHI + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + CL_MANV + ") REFERENCES " + TABLE_NHANVIEN + "(" + NV_ID + "))");

        // BẢNG TỔNG HỢP
        db.execSQL("CREATE TABLE " + TABLE_TONGHOP + " (" +
                TH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TH_MANV + " INTEGER NOT NULL, " +
                TH_THANG + " TEXT NOT NULL, " +
                TH_GIOLAM + " REAL DEFAULT 0, " +
                TH_GIOOT + " REAL DEFAULT 0, " +
                TH_MUON + " INTEGER DEFAULT 0, " +
                TH_SOM + " INTEGER DEFAULT 0, " +
                TH_NGAYLAM + " INTEGER DEFAULT 0, " +
                TH_NGAYNGHI + " INTEGER DEFAULT 0, " +
                TH_LUONG + " REAL DEFAULT 0, " +
                "FOREIGN KEY(" + TH_MANV + ") REFERENCES " + TABLE_NHANVIEN + "(" + NV_ID + "))");
        insertSampleData(db);
    }

    // 🟢 Hàm thêm dữ liệu mẫu
    private void insertSampleData(SQLiteDatabase db) {
        // 1️⃣ Bảng NhanVien
        db.execSQL("INSERT INTO " + TABLE_NHANVIEN +
                " (" + NV_HOTEN + ", " + NV_NGAYSINH + ", " + NV_CHUCVU + ", " + NV_MUCLUONG + ", " + NV_SDT + ", " + NV_LOAI + ", " + NV_MATKHAU + ") VALUES " +
                "('Nguyen Van A', '1995-01-10', 'Quản lý', 13000000, '0901234567', 'fulltime', '123456')," +
                "('Tran Thi B', '1998-05-22', 'Nhân viên bán hàng', 40000, '0902234567', 'parttime', '111111')," +
                "('Le Van C', '1990-09-13', 'Bảo vệ', 10000000, '0903234567', 'fulltime', '222222')," +
                "('Pham Thi D', '1997-03-19', 'Thu ngân', 9000000, '0904234567', 'fulltime', '333333')," +
                "('Hoang Van E', '2000-07-01', 'Phục vụ', 35000, '0905234567', 'parttime', '444444')");

        // 2️⃣ Bảng CaLam (mỗi người 1 ca mẫu)
        db.execSQL("INSERT INTO " + TABLE_CALAM +
                " (" + CL_MANV + ", " + CL_NGAY + ", " + CL_CA + ", " + CL_OT + ", " + CL_CHECKIN + ", " + CL_CHECKOUT + ", " + CL_MUON + ", " + CL_SOM + ", " + CL_NGHI + ") VALUES " +
                "(1, '2025-10-20', '8h30-13h', 1, 830, 1300, 0, 0, 0)," +
                "(2, '2025-10-20', '13h-17h30', 0, 1310, 1730, 10, 0, 0)," +
                "(3, '2025-10-20', '17h30-22h', 0, 1730, 2200, 0, 0, 0)," +
                "(4, '2025-10-20', '8h30-13h', 1, 830, 1330, 0, 0, 0)," +
                "(5, '2025-10-20', '13h-17h30', 0, 1330, 1730, 0, 0, 0)");

        // 3️⃣ Bảng TongHop
        db.execSQL("INSERT INTO " + TABLE_TONGHOP +
                " (" + TH_MANV + ", " + TH_THANG + ", " + TH_GIOLAM + ", " + TH_GIOOT + ", " + TH_MUON + ", " + TH_SOM + ", " + TH_NGAYLAM + ", " + TH_NGAYNGHI + ", " + TH_LUONG + ") VALUES " +
                "(1, '2025-10', 160, 10, 20, 10, 26, 2, 13500000)," +
                "(2, '2025-10', 120, 5, 10, 5, 22, 1, 5000000)," +
                "(3, '2025-10', 170, 0, 0, 0, 26, 0, 10000000)," +
                "(4, '2025-10', 165, 8, 5, 10, 25, 1, 9500000)," +
                "(5, '2025-10', 100, 6, 15, 5, 20, 0, 4800000)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TONGHOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALAM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NHANVIEN);
        onCreate(db);
    }

    // ✅ Thêm hàm này để tránh lỗi khi version giảm
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TONGHOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALAM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NHANVIEN);
        onCreate(db);
    }

    // ================== HÀM CHỨC NĂNG ==================

    public long addNhanVien(String hoTen, String ngaySinh, String chucVu, double mucLuong,
                            String soDienThoai, String loai, String matKhau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NV_HOTEN, hoTen);
        cv.put(NV_NGAYSINH, ngaySinh);
        cv.put(NV_CHUCVU, chucVu);
        cv.put(NV_MUCLUONG, mucLuong);
        cv.put(NV_SDT, soDienThoai);
        cv.put(NV_LOAI, loai);
        cv.put(NV_MATKHAU, matKhau);
        return db.insert(TABLE_NHANVIEN, null, cv);
    }

    public long addCaLam(int manv, String ngay, String ca, int ot, int nghiDuocKhong) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_MANV, manv);
        cv.put(CL_NGAY, ngay);
        cv.put(CL_CA, ca);
        cv.put(CL_OT, ot);
        cv.put(CL_NGHI, nghiDuocKhong);
        return db.insert(TABLE_CALAM, null, cv);
    }

    public void updateCheckIn(int id, long time, int phutMuon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_CHECKIN, time);
        cv.put(CL_MUON, phutMuon);
        db.update(TABLE_CALAM, cv, CL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateCheckOut(int id, long time, int phutSom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_CHECKOUT, time);
        cv.put(CL_SOM, phutSom);
        db.update(TABLE_CALAM, cv, CL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public double tinhLuong(int manv, String loai, double mucLuong,
                            double gioLamThuong, double gioTangCa,
                            int phutMuon, int phutSom, int ngayLam, int ngayNghiCoLuong) {

        double luong = 0;

        if (loai.equalsIgnoreCase("fulltime")) {
            double luongNgay = mucLuong / 26.0;
            double luongGio = luongNgay / 8.0;
            luong = (ngayLam / 26.0) * mucLuong
                    + (gioTangCa * luongGio * 1.5)
                    + (ngayNghiCoLuong * luongNgay)
                    - ((phutMuon + phutSom) * 1000);
        } else {
            luong = (gioLamThuong * mucLuong)
                    + (gioTangCa * mucLuong * 1.5)
                    - ((phutMuon + phutSom) * 1000);
        }

        return luong;
    }
}
