
package com.example.chamcong;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên và phiên bản của Database
    private static final String DATABASE_NAME = "ChamCong.db";
    private static final int DATABASE_VERSION = 3;

    // --- BẢNG USERS ---
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_POSITION = "position";

    // --- BẢNG SCHEDULES (Lịch làm việc đã đăng ký) ---
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String COLUMN_SCHEDULE_ID = "_id";
    public static final String COLUMN_SCHEDULE_NAME = "schedule_name";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_WORK_DATE = "work_date";


    // --- BẢNG ATTENDANCES (Lịch sử chấm công thực tế) ---
    public static final String TABLE_ATTENDANCES = "attendances";
    public static final String COLUMN_ATTENDANCE_ID = "_id";
    public static final String COLUMN_ATT_USER_ID = "user_id"; // Foreign Key
    public static final String COLUMN_ATT_SCHEDULE_ID = "schedule_id"; // Foreign Key, nullable
    public static final String COLUMN_CHECK_IN_TIME = "check_in_time";
    public static final String COLUMN_CHECK_OUT_TIME = "check_out_time";
    public static final String COLUMN_TOTAL_SALARY = "total_salary";
    public static final String COLUMN_STATUS = "status";


    // --- CÂU LỆNH TẠO BẢNG ---

    // Tạo bảng Users
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COLUMN_PASSWORD + " TEXT NOT NULL, " +
            COLUMN_FULL_NAME + " TEXT, " +
            COLUMN_POSITION + " TEXT);";



    // Tạo bảng Schedules
    private static final String CREATE_TABLE_SCHEDULES = "CREATE TABLE " + TABLE_SCHEDULES + " (" +
            COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SCHEDULE_NAME + " TEXT NOT NULL, " +
            COLUMN_START_TIME + " TEXT NOT NULL, " +
            COLUMN_END_TIME + " TEXT NOT NULL, " +
            COLUMN_WORK_DATE + " TEXT NOT NULL);";

    // Tạo bảng Attendances
    private static final String CREATE_TABLE_ATTENDANCES = "CREATE TABLE " + TABLE_ATTENDANCES + " (" +
            COLUMN_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ATT_USER_ID + " INTEGER NOT NULL, " +
            COLUMN_ATT_SCHEDULE_ID + " INTEGER, " +
            COLUMN_CHECK_IN_TIME + " INTEGER, " +
            COLUMN_CHECK_OUT_TIME + " INTEGER, " +
            COLUMN_TOTAL_SALARY + " REAL, " +
            COLUMN_STATUS + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_ATT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
            "FOREIGN KEY(" + COLUMN_ATT_SCHEDULE_ID + ") REFERENCES " + TABLE_SCHEDULES + "(" + COLUMN_SCHEDULE_ID + "));";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Thực thi các câu lệnh để tạo bảng
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_SCHEDULES);
        db.execSQL(CREATE_TABLE_ATTENDANCES);

        // Thêm các người dùng mặc định để kiểm tra đăng nhập
        addDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Khi nâng cấp database, xóa các bảng cũ đi và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Thêm các người dùng mặc định
    private void addDefaultUsers(SQLiteDatabase db) {
        // --- Người dùng 1 ---
        ContentValues values1 = new ContentValues();
        values1.put(COLUMN_USERNAME, "0123456789");
        values1.put(COLUMN_PASSWORD, "12345");
        values1.put(COLUMN_FULL_NAME, "Test User");
        values1.put(COLUMN_POSITION, "Nhân viên");
        db.insert(TABLE_USERS, null, values1);

        // --- Người dùng 2 (Tài khoản bạn yêu cầu) ---
        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_USERNAME, "0356897098");
        values2.put(COLUMN_PASSWORD, "12345");
        values2.put(COLUMN_FULL_NAME, "Nhân Viên Mới");
        values2.put(COLUMN_POSITION, "Nhân viên");
        db.insert(TABLE_USERS, null, values2);
    }

    // ======================== CHỖ TÔI ĐÃ SỬA ========================
    /**
     * Kiểm tra thông tin đăng nhập và lấy ID của người dùng.
     * Thay vì trả về true/false, hàm này trả về ID của người dùng nếu thành công.
     * @param username Tên đăng nhập (SĐT)
     * @param password Mật khẩu
     * @return ID (kiểu long) của người dùng nếu đăng nhập thành công, -1 nếu thất bại.
     */
    public long checkUserAndGetId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1; // Giá trị mặc định nếu không tìm thấy user

        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        // Nếu con trỏ có thể di chuyển đến hàng đầu tiên, nghĩa là tìm thấy người dùng
        if (cursor.moveToFirst()) {
            // Lấy ID từ cột COLUMN_USER_ID.
            // getColumnIndexOrThrow sẽ báo lỗi nếu tên cột không tồn tại, giúp phát hiện lỗi sớm.
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }

        // Đóng con trỏ và database để giải phóng tài nguyên
        cursor.close();
        db.close();

        // Trả về ID người dùng, hoặc -1 nếu không tìm thấy
        return userId;
    }
    // ===============================================================
}
