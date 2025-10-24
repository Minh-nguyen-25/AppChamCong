package com.example.chamcong;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    // --- THÔNG TIN DB ---
    private static final String DATABASE_NAME = "ChamCong.db";
    private static final int DATABASE_VERSION = 5; // tăng version nếu thay schema

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
    public static final String CL_NGAY = "ngay"; // yyyy-MM-dd
    public static final String CL_CA = "ca"; // '8h30-13h' ...
    public static final String CL_OT = "ot";
    public static final String CL_CHECKIN = "checkIn";   // HH:mm (TEXT)
    public static final String CL_CHECKOUT = "checkOut"; // HH:mm (TEXT)
    public static final String CL_MUON = "checkInMuon";  // phút muộn (INTEGER)
    public static final String CL_SOM = "checkOutSom";   // phút sớm (INTEGER)
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

    private static final SimpleDateFormat HHMM = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // BẢNG NHÂN VIÊN (soDienThoai UNIQUE)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NHANVIEN + " (" +
                NV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NV_HOTEN + " TEXT NOT NULL, " +
                NV_NGAYSINH + " TEXT, " +
                NV_CHUCVU + " TEXT, " +
                NV_MUCLUONG + " REAL NOT NULL, " +
                NV_SDT + " TEXT UNIQUE, " +
                NV_LOAI + " TEXT CHECK(" + NV_LOAI + " IN ('fulltime','parttime')), " +
                NV_MATKHAU + " TEXT NOT NULL)");

        // BẢNG CA LÀM (checkIn/checkOut lưu dạng "HH:mm" TEXT), UNIQUE(manv,ngay,ca)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CALAM + " (" +
                CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CL_MANV + " INTEGER NOT NULL, " +
                CL_NGAY + " TEXT NOT NULL, " +
                CL_CA + " TEXT CHECK(" + CL_CA + " IN ('8h30-13h','13h-17h30','17h30-22h')), " +
                CL_OT + " INTEGER DEFAULT 0, " +
                CL_CHECKIN + " TEXT, " +
                CL_CHECKOUT + " TEXT, " +
                CL_MUON + " INTEGER DEFAULT 0, " +
                CL_SOM + " INTEGER DEFAULT 0, " +
                CL_NGHI + " INTEGER DEFAULT 0, " +
                "UNIQUE(" + CL_MANV + "," + CL_NGAY + "," + CL_CA + "), " +
                "FOREIGN KEY(" + CL_MANV + ") REFERENCES " + TABLE_NHANVIEN + "(" + NV_ID + "))");

        // BẢNG TỔNG HỢP
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TONGHOP + " (" +
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

    // 🟢 Hàm thêm dữ liệu mẫu (giữ dạng checkIn/checkOut là "HH:mm" hoặc NULL)
    private void insertSampleData(SQLiteDatabase db) {
        // 1️⃣ Bảng NhanVien
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_NHANVIEN +
                " (" + NV_HOTEN + ", " + NV_NGAYSINH + ", " + NV_CHUCVU + ", " + NV_MUCLUONG + ", " + NV_SDT + ", " + NV_LOAI + ", " + NV_MATKHAU + ") VALUES " +
                "('Nguyen Van A', '1995-01-10', 'Quản lý', 13000000, '0901234567', 'fulltime', '123456')," +
                "('Tran Thi B', '1998-05-22', 'Nhân viên bán hàng', 40000, '0902234567', 'parttime', '111111')," +
                "('Le Van C', '1990-09-13', 'Bảo vệ', 10000000, '0903234567', 'fulltime', '222222')," +
                "('Pham Thi D', '1997-03-19', 'Thu ngân', 9000000, '0904234567', 'fulltime', '333333')," +
                "('Hoang Van E', '2000-07-01', 'Phục vụ', 35000, '0905234567', 'parttime', '444444')");

        // 2️⃣ Bảng CaLam (mỗi người 1 ca mẫu) - ngày mẫu
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_CALAM +
                " (" + CL_MANV + ", " + CL_NGAY + ", " + CL_CA + ", " + CL_OT + ", " + CL_CHECKIN + ", " + CL_CHECKOUT + ", " + CL_MUON + ", " + CL_SOM + ", " + CL_NGHI + ") VALUES " +
                "(1, '2025-10-20', '8h30-13h', 1, '08:30', '13:00', 0, 0, 0)," +
                "(2, '2025-10-20', '13h-17h30', 0, '13:10', '17:30', 10, 0, 0)," +
                "(3, '2025-10-20', '17h30-22h', 0, '17:30', '22:00', 0, 0, 0)," +
                "(4, '2025-10-20', '8h30-13h', 1, '08:30', '13:30', 0, 0, 0)," +
                "(5, '2025-10-20', '13h-17h30', 0, '13:30', '17:30', 0, 0, 0)");

        // 3️⃣ Bảng TongHop (mẫu)
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_TONGHOP +
                " (" + TH_MANV + ", " + TH_THANG + ", " + TH_GIOLAM + ", " + TH_GIOOT + ", " + TH_MUON + ", " + TH_SOM + ", " + TH_NGAYLAM + ", " + TH_NGAYNGHI + ", " + TH_LUONG + ") VALUES " +
                "(1, '2025-10', 160, 10, 20, 10, 26, 2, 13500000)," +
                "(2, '2025-10', 120, 5, 10, 5, 22, 1, 5000000)," +
                "(3, '2025-10', 170, 0, 0, 0, 26, 0, 10000000)," +
                "(4, '2025-10', 165, 8, 5, 10, 25, 1, 9500000)," +
                "(5, '2025-10', 100, 6, 15, 5, 20, 0, 4800000)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // đơn giản drop + recreate (nâng version sẽ reset data mẫu)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TONGHOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALAM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NHANVIEN);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // ================== HÀM TIỆN ÍCH ==================

    // Lấy thông tin nhân viên theo manv
    public Cursor getNhanVienById(int manv) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NHANVIEN, null, NV_ID + "=?", new String[]{String.valueOf(manv)}, null, null, null);
    }

    // Lấy CaLam cho 1 nhân viên vào 1 ngày (yyyy-MM-dd). Trả Cursor (1 row) hoặc null
    public Cursor getCaLamForDate(int manv, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_CALAM, null, CL_MANV + "=? AND " + CL_NGAY + "=?", new String[]{
                String.valueOf(manv), date
        }, null, null, null);
        return c;
    }

    // Lấy ca hôm nay theo manv (trả String ca hoặc null)
    public String getCaLamHomNay(int manv) {
        String today = YYYYMMDD.format(new Date());
        Cursor c = getCaLamForDate(manv, today);
        if (c != null && c.moveToFirst()) {
            String ca = c.getString(c.getColumnIndexOrThrow(CL_CA));
            c.close();
            return ca;
        }
        if (c != null) c.close();
        return null;
    }

    // Thêm ca (sử dụng try/catch để tránh lỗi UNIQUE)
    public long addCaLam(int manv, String ngay, String ca, int ot, Integer checkInHHMM, Integer checkOutHHMM, int nghiDuocKhong) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CL_MANV, manv);
        cv.put(CL_NGAY, ngay);
        cv.put(CL_CA, ca);
        cv.put(CL_OT, ot);
        if (checkInHHMM != null) {
            // nhận dạng số dạng 830 -> convert to "08:30"
            String s = formatFromIntHM(checkInHHMM);
            cv.put(CL_CHECKIN, s);
        }
        if (checkOutHHMM != null) {
            cv.put(CL_CHECKOUT, formatFromIntHM(checkOutHHMM));
        }
        cv.put(CL_NGHI, nghiDuocKhong);
        return db.insertWithOnConflict(TABLE_CALAM, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // Cập nhật check-in: lưu "HH:mm" và tự tính phút muộn
    public void updateCheckInTime(int id, String timeHHmm) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Lấy ca để biết giờ bắt đầu
        Cursor c = db.query(TABLE_CALAM, new String[]{CL_CA}, CL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        int phutMuon = 0;
        if (c != null && c.moveToFirst()) {
            String ca = c.getString(c.getColumnIndexOrThrow(CL_CA));
            try {
                int startMinutes = caStartMinutes(ca);
                int checkedMinutes = minutesFromHHmm(timeHHmm);
                phutMuon = Math.max(0, checkedMinutes - startMinutes);
            } catch (Exception e) {
                Log.e("DB", "parse ca error", e);
            }
            c.close();
        }

        ContentValues cv = new ContentValues();
        cv.put(CL_CHECKIN, timeHHmm);
        cv.put(CL_MUON, phutMuon);
        db.update(TABLE_CALAM, cv, CL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Cập nhật check-out: lưu "HH:mm" và tự tính phút sớm
    public void updateCheckOutTime(int id, String timeHHmm) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(TABLE_CALAM, new String[]{CL_CA}, CL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        int phutSom = 0;
        if (c != null && c.moveToFirst()) {
            String ca = c.getString(c.getColumnIndexOrThrow(CL_CA));
            try {
                int endMinutes = caEndMinutes(ca);
                int checkedMinutes = minutesFromHHmm(timeHHmm);
                phutSom = Math.max(0, endMinutes - checkedMinutes); // nếu >0 => về sớm
            } catch (Exception e) {
                Log.e("DB", "parse ca error", e);
            }
            c.close();
        }

        ContentValues cv = new ContentValues();
        cv.put(CL_CHECKOUT, timeHHmm);
        cv.put(CL_SOM, phutSom);
        db.update(TABLE_CALAM, cv, CL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ================== Helpers ==================

    // parse "8h30-13h" -> start minutes from midnight (8*60+30)
    private int caStartMinutes(String ca) throws ParseException {
        // ca format like "8h30-13h" or "13h-17h30" or "17h30-22h"
        String left = ca.split("-")[0]; // e.g. "8h30" or "13h"
        return hmStringToMinutes(convertHToHHmm(left));
    }

    private int caEndMinutes(String ca) throws ParseException {
        String right = ca.split("-")[1]; // e.g. "13h" or "17h30"
        return hmStringToMinutes(convertHToHHmm(right));
    }

    // convert "8h30" or "13h" into "08:30" or "13:00"
    private String convertHToHHmm(String s) {
        s = s.trim();
        s = s.replace("h", ":");
        if (!s.contains(":")) s = s + ":00";
        // ensure two-digit hour/minute
        String[] p = s.split(":");
        String hh = p[0].length() == 1 ? "0" + p[0] : p[0];
        String mm = p[1].length() == 1 ? "0" + p[1] : p[1];
        return hh + ":" + mm;
    }

    // "08:30" -> minutes since midnight
    private int minutesFromHHmm(String hhmm) throws ParseException {
        Date d = HHMM.parse(hhmm);
        int hours = d.getHours(); // deprecated but OK for minute calc
        int mins = d.getMinutes();
        return hours * 60 + mins;
    }

    // alternative parsing from "08:30" using split (safer)
    private int hmStringToMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        return hh * 60 + mm;
    }

    private String formatFromIntHM(int hm) {
        // hm like 830 -> "08:30", 1310 -> "13:10"
        int hours = hm / 100;
        int mins = hm % 100;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins);
    }
}
