package com.example.chamcong;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---
    Button btnCheckIn, btnCheckOut, btnRegisterShift, btnViewSalary;
    DatabaseHelper dbHelper; // Đối tượng "cầu nối" đến database

    // Biến để lưu ID của người dùng đang đăng nhập.
    // ID này được gửi từ LoginActivity sau khi đăng nhập thành công.
    private long currentUserId;

    // Hằng số để tính lương, giúp code dễ đọc và dễ bảo trì.
    private static final double HOURLY_RATE = 50000; // Lương 50,000 VND/giờ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- KHỞI TẠO ---
        dbHelper = new DatabaseHelper(this);

        // Lấy dữ liệu được gửi từ LoginActivity.
        // "USER_ID" là key (khóa) mà chúng ta đã định nghĩa ở LoginActivity.
        // -1 là giá trị mặc định nếu không tìm thấy key "USER_ID".
        currentUserId = getIntent().getLongExtra("USER_ID", -1);

        // Kiểm tra xem có lấy được ID người dùng hợp lệ không.
        if (currentUserId == -1) {
            // Nếu không có ID, tức là có lỗi trong luồng đăng nhập.
            // Hiển thị lỗi và đóng Activity để tránh các thao tác sai trên database.
            Toast.makeText(this, "Lỗi: Không xác định được người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish(); // Đóng MainActivity.
            return; // Dừng thực thi phương thức onCreate.
        }

        // --- ÁNH XẠ VIEW ---
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnRegisterShift = findViewById(R.id.btnRegisterShift);
        btnViewSalary = findViewById(R.id.btnViewSalary);


        // --- THIẾT LẬP SỰ KIỆN CLICK ---

        // Sự kiện khi nhấn nút Check-in
        btnCheckIn.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase(); // Mở database ở chế độ đọc

            // Câu lệnh truy vấn để kiểm tra xem người dùng HIỆN TẠI có ca làm nào đang mở không.
            // "Đang mở" nghĩa là đã check-in nhưng chưa check-out (check_out_time IS NULL).
            Cursor cursor = db.query(DatabaseHelper.TABLE_ATTENDANCES, // Truy vấn bảng chấm công
                    new String[]{DatabaseHelper.COLUMN_ATTENDANCE_ID}, // Chỉ cần lấy cột ID để kiểm tra sự tồn tại
                    // <<< SỬA LỖI TẠI ĐÂY >>>: Phải dùng cột user_id của bảng attendances (COLUMN_ATT_USER_ID)
                    DatabaseHelper.COLUMN_ATT_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_CHECK_OUT_TIME + " IS NULL",
                    new String[]{String.valueOf(currentUserId)}, // Giá trị cho dấu "?" ở trên.
                    null, null, null);

            // Nếu con trỏ có thể di chuyển đến hàng đầu tiên, nghĩa là tìm thấy một bản ghi thỏa mãn điều kiện.
            if (cursor.moveToFirst()) {
                Toast.makeText(MainActivity.this, "Bạn đã check-in rồi!", Toast.LENGTH_SHORT).show();
                cursor.close(); // Đóng con trỏ để giải phóng tài nguyên.
                return; // Dừng lại, không làm gì thêm.
            }
            cursor.close(); // Luôn đóng con trỏ sau khi dùng xong.

            // Nếu không có ca nào đang mở, tiến hành thêm ca làm mới.
            db = dbHelper.getWritableDatabase(); // Mở database ở chế độ ghi.
            ContentValues values = new ContentValues();
            // Đưa dữ liệu vào: ID người dùng và thời gian check-in (thời gian hiện tại).
            // <<< SỬA LỖI TẠI ĐÂY >>>: Phải dùng cột user_id của bảng attendances (COLUMN_ATT_USER_ID)
            values.put(DatabaseHelper.COLUMN_ATT_USER_ID, currentUserId);
            values.put(DatabaseHelper.COLUMN_CHECK_IN_TIME, System.currentTimeMillis());

            // Thêm một hàng mới vào bảng attendances.
            long newRowId = db.insert(DatabaseHelper.TABLE_ATTENDANCES, null, values);

            // db.insert trả về ID của hàng mới nếu thành công, hoặc -1 nếu thất bại.
            if (newRowId != -1) {
                Toast.makeText(MainActivity.this, "Check-in thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Lỗi khi check-in!", Toast.LENGTH_SHORT).show();
            }
        });


        // Sự kiện khi nhấn nút Check-out
        btnCheckOut.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long attendanceId = -1; // Biến lưu ID của ca làm việc cần check-out.
            long checkInTime = -1;  // Biến lưu thời gian đã check-in.

            // Tìm ca làm việc đang mở của người dùng hiện tại để check-out.
            Cursor cursor = db.query(DatabaseHelper.TABLE_ATTENDANCES,
                    // Lần này cần lấy cả ID của ca và thời gian check-in để tính lương.
                    new String[]{DatabaseHelper.COLUMN_ATTENDANCE_ID, DatabaseHelper.COLUMN_CHECK_IN_TIME},
                    // <<< SỬA LỖI TẠI ĐÂY >>>: Phải dùng cột user_id của bảng attendances (COLUMN_ATT_USER_ID)
                    DatabaseHelper.COLUMN_ATT_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_CHECK_OUT_TIME + " IS NULL",
                    new String[]{String.valueOf(currentUserId)},
                    null, null, null);

            // Nếu tìm thấy ca làm đang mở...
            if (cursor.moveToFirst()) {
                attendanceId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ATTENDANCE_ID));
                checkInTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHECK_IN_TIME));
            } else {
                // Nếu không tìm thấy, nghĩa là người dùng chưa check-in.
                Toast.makeText(MainActivity.this, "Bạn chưa check-in!", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
            cursor.close();

            // --- TÍNH TOÁN LƯƠNG ---
            long checkOutTime = System.currentTimeMillis(); // Lấy thời gian hiện tại làm mốc check-out.
            long durationMillis = checkOutTime - checkInTime; // Tổng thời gian làm việc (tính bằng mili giây).
            // Chuyển đổi mili giây sang giờ. Chia cho số thực 3600000.0 để có kết quả chính xác.
            double hoursWorked = durationMillis / 3600000.0;
            double shiftSalary = hoursWorked * HOURLY_RATE; // Tính lương của ca này.

            // --- CẬP NHẬT DATABASE ---
            ContentValues values = new ContentValues();
            // Đưa dữ liệu cần cập nhật vào: thời gian check-out và tổng lương của ca.
            values.put(DatabaseHelper.COLUMN_CHECK_OUT_TIME, checkOutTime);
            values.put(DatabaseHelper.COLUMN_TOTAL_SALARY, shiftSalary);

            // Thực hiện cập nhật bản ghi trong bảng attendances.
            int rowsAffected = db.update(DatabaseHelper.TABLE_ATTENDANCES, // Tên bảng
                    values, // Dữ liệu mới
                    DatabaseHelper.COLUMN_ATTENDANCE_ID + " = ?", // Điều kiện WHERE: cập nhật đúng ca làm việc
                    new String[]{String.valueOf(attendanceId)}); // Giá trị cho dấu "?".

            // db.update trả về số hàng đã bị ảnh hưởng. Nếu > 0 là thành công.
            if (rowsAffected > 0) {
                Toast.makeText(MainActivity.this, String.format("Check-out thành công! Lương ca này: %.0f VND", shiftSalary), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Lỗi khi check-out!", Toast.LENGTH_SHORT).show();
            }
        });


        // Sự kiện nhấn nút Đăng ký ca
        btnRegisterShift.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterShiftActivity.class);
            startActivity(intent);
        });


        // Sự kiện nhấn nút Xem lương
        btnViewSalary.setOnClickListener(V -> {
            Intent intent = new Intent(this, SalaryActivity.class);
            // Gửi ID của người dùng hiện tại sang SalaryActivity
            // để màn hình lương biết cần hiển thị lương của ai.
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });
    }
}
