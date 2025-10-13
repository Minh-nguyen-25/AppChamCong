package com.example.chamcong;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SalaryActivity extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---
    // Khai báo các biến giao diện (EditText, Button) để tương tác với các thành phần trong file layout salary.xml
    EditText edtongsoca, edgiolamthuong, edgiotangca, edluongcathuong, edluongtangca, edtongluong;
    Button btnBack;
    // Khai báo biến dbHelper để quản lý và tương tác với cơ sở dữ liệu SQLite.
    DatabaseHelper dbHelper;

    // --- CÁC HẰNG SỐ TÍNH LƯƠNG ---
    // Việc dùng hằng số giúp code dễ đọc và dễ bảo trì. Nếu cần đổi mức lương, chỉ cần sửa ở một nơi.
    private static final double HOURLY_RATE = 50000; // Mức lương cơ bản mỗi giờ: 50,000 VND.
    private static final double OVERTIME_RATE_MULTIPLIER = 1.5; // Hệ số nhân cho giờ làm thêm (tăng ca), ở đây là 150%.
    private static final double STANDARD_HOURS_PER_SHIFT = 8.0; // Số giờ làm tiêu chuẩn cho một ca. Vượt quá mốc này sẽ được tính là tăng ca.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- KHỞI TẠO ACTIVITY ---
        super.onCreate(savedInstanceState);
        // Nạp giao diện từ file layout res/layout/salary.xml để hiển thị lên màn hình.
        setContentView(R.layout.salary);

        // Khởi tạo đối tượng dbHelper để có thể truy cập vào database.
        dbHelper = new DatabaseHelper(this);

        // --- ÁNH XẠ VIEW ---
        // Kết nối các biến Java đã khai báo ở trên với các thành phần trong file XML thông qua ID của chúng.
        edtongsoca = findViewById(R.id.edtongsoca);
        edgiolamthuong = findViewById(R.id.edgiolamthuong);
        edgiotangca = findViewById(R.id.edgiotangca);
        edluongcathuong = findViewById(R.id.edluongcathuong);
        edluongtangca = findViewById(R.id.edluongtangca);
        edtongluong = findViewById(R.id.edtongluong);
        btnBack = findViewById(R.id.btnBack);

        // Gọi hàm chính để bắt đầu quá trình đọc dữ liệu từ database và tính toán lương.
        loadSalaryData();

        // --- ĐĂNG KÝ SỰ KIỆN CLICK CHO NÚT QUAY LẠI ---
        btnBack.setOnClickListener(v -> {
            // Khi người dùng nhấn vào nút "Back", hàm finish() sẽ được gọi.
            finish(); // Lệnh này dùng để đóng Activity hiện tại và quay trở lại màn hình trước đó (MainActivity).
        });
    }

    /**
     * Đây là hàm chính, thực hiện tất cả logic:
     * 1. Mở kết nối đến database.
     * 2. Truy vấn để lấy tất cả các ca làm việc đã hoàn thành.
     * 3. Lặp qua từng ca để tính toán giờ làm và phân loại chúng.
     * 4. Tính toán lương dựa trên số giờ đã phân loại.
     * 5. Hiển thị kết quả cuối cùng lên giao diện.
     */
    private void loadSalaryData() {
        // Mở database ở chế độ "chỉ đọc" (getReadableDatabase) vì chúng ta chỉ cần lấy dữ liệu ra, không sửa đổi.
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // --- THỰC HIỆN TRUY VẤN DATABASE ---
        // db.query() là hàm để truy vấn dữ liệu từ một bảng.
        Cursor cursor = db.query(DatabaseHelper.TABLE_ATTENDANCES, // Tên bảng cần truy vấn: "shifts".
                new String[]{DatabaseHelper.COLUMN_CHECK_IN_TIME, DatabaseHelper.COLUMN_CHECK_OUT_TIME}, // Các cột cần lấy dữ liệu.
                DatabaseHelper.COLUMN_CHECK_OUT_TIME + " IS NOT NULL", // Điều kiện lọc (WHERE clause): chỉ lấy những hàng có cột check_out_time không rỗng (tức là ca đã hoàn thành).
                null, null, null, null); // Các tham số khác không dùng đến.

        // Khởi tạo các biến để lưu trữ kết quả tính toán tổng hợp.
        int totalShifts = 0; // Tổng số ca đã làm.
        double totalRegularHours = 0; // Tổng số giờ làm thường.
        double totalOvertimeHours = 0; // Tổng số giờ tăng ca.

        // --- XỬ LÝ DỮ LIỆU TỪ CURSOR ---
        // Vòng lặp while sẽ chạy qua từng hàng kết quả mà câu lệnh query trả về.
        // cursor.moveToNext() di chuyển con trỏ đến hàng tiếp theo và trả về 'true' nếu còn hàng để đọc.
        while (cursor.moveToNext()) {
            totalShifts++; // Mỗi lần lặp qua một hàng, tức là một ca làm việc, nên tăng biến đếm tổng số ca lên 1.

            // Lấy dữ liệu thời gian check-in và check-out từ hàng hiện tại của cursor.
            // getColumnIndexOrThrow() sẽ tìm chỉ số của cột dựa vào tên cột.
            long checkIn = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHECK_IN_TIME));
            long checkOut = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHECK_OUT_TIME));

            // Tính khoảng thời gian làm việc của ca đó (kết quả tính ra là mili giây).
            long durationMillis = checkOut - checkIn;

            // Chuyển đổi từ mili giây sang giờ. Phải chia cho số thực (3600000.0) để kết quả là số thập phân chính xác.
            double shiftHours = durationMillis / 3600000.0;

            // --- PHÂN LOẠI GIỜ LÀM ---
            // Kiểm tra xem tổng số giờ của ca này có vượt quá giờ làm tiêu chuẩn (8 giờ) hay không.
            if (shiftHours > STANDARD_HOURS_PER_SHIFT) {
                // Nếu có, cộng 8 giờ vào tổng giờ làm thường.
                totalRegularHours += STANDARD_HOURS_PER_SHIFT;
                // Phần còn lại (vượt quá 8 giờ) được cộng vào tổng giờ tăng ca.
                totalOvertimeHours += (shiftHours - STANDARD_HOURS_PER_SHIFT);
            } else {
                // Nếu không, toàn bộ số giờ của ca này được tính là giờ làm thường.
                totalRegularHours += shiftHours;
            }
        }
        // Sau khi vòng lặp kết thúc, phải đóng cursor để giải phóng tài nguyên và tránh rò rỉ bộ nhớ.
        cursor.close();

        // --- TÍNH TOÁN LƯƠNG ---
        // Tính tổng lương cho giờ làm thường.
        double totalRegularSalary = totalRegularHours * HOURLY_RATE;
        // Tính tổng lương cho giờ tăng ca (lương giờ tăng ca = lương cơ bản * hệ số nhân).
        double totalOvertimeSalary = totalOvertimeHours * (HOURLY_RATE * OVERTIME_RATE_MULTIPLIER);
        // Tổng lương cuối cùng là tổng của hai loại trên.
        double finalTotalSalary = totalRegularSalary + totalOvertimeSalary;

        // --- HIỂN THỊ DỮ LIỆU LÊN GIAO DIỆN ---
        // Dùng phương thức setText() để gán giá trị đã tính toán vào các ô EditText tương ứng.
        edtongsoca.setText(String.valueOf(totalShifts)); // Chuyển số nguyên thành chuỗi.
        // String.format() dùng để định dạng chuỗi. "%.2f" nghĩa là hiển thị số thực với 2 chữ số sau dấu phẩy.
        edgiolamthuong.setText(String.format(Locale.US, "%.2f", totalRegularHours));
        edgiotangca.setText(String.format(Locale.US, "%.2f", totalOvertimeHours));

        // Gọi hàm formatCurrency để định dạng số tiền thành chuỗi có ký hiệu tiền tệ VND.
        edluongcathuong.setText(formatCurrency(totalRegularSalary));
        edluongtangca.setText(formatCurrency(totalOvertimeSalary));
        edtongluong.setText(formatCurrency(finalTotalSalary));
    }

    /**
     * Hàm tiện ích để định dạng một số (kiểu double) thành một chuỗi tiền tệ theo chuẩn Việt Nam.
     * @param amount Số tiền cần định dạng.
     * @return Một chuỗi đã được định dạng đẹp mắt (ví dụ: "50.000 ₫").
     */
    private String formatCurrency(double amount) {
        // Tạo một đối tượng Locale đại diện cho khu vực Việt Nam ('vi' là ngôn ngữ, 'VN' là quốc gia).
        Locale localeVN = new Locale("vi", "VN");
        // Lấy một công cụ định dạng tiền tệ dựa trên khu vực đã chỉ định.
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        // Dùng công cụ đó để định dạng số tiền và trả về kết quả dưới dạng chuỗi.
        return currencyFormatter.format(amount);
    }
}
