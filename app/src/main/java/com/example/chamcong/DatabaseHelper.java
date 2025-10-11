
package com.example.chamcong;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên và phiên bản của Database
    private static final String DATABASE_NAME = "ChamCongPro.db";
    private static final int DATABASE_VERSION = 1;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Khi nâng cấp database, xóa các bảng cũ đi và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
