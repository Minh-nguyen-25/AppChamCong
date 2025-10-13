package com.example.chamcong;


import static com.example.chamcong.DatabaseHelper.COLUMN_FULL_NAME;
import static com.example.chamcong.DatabaseHelper.COLUMN_PASSWORD;
import static com.example.chamcong.DatabaseHelper.COLUMN_POSITION;
import static com.example.chamcong.DatabaseHelper.COLUMN_USERNAME;
import static com.example.chamcong.DatabaseHelper.TABLE_USERS;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---
    // Khai báo các thành phần giao diện người dùng (UI) mà chúng ta sẽ tương tác.
    EditText etPhone;       // Ô để người dùng nhập số điện thoại.
    EditText etPassword;    // Ô để người dùng nhập mật khẩu.
    Button btnLogin;        // Nút để người dùng nhấn để đăng nhập.

    // Khai báo đối tượng DatabaseHelper.
    // Đây là "cầu nối" để giao tiếp với cơ sở dữ liệu SQLite của bạn.
    DatabaseHelper db;

    /**
     * Phương thức onCreate() được gọi khi Activity được tạo lần đầu tiên.
     * Đây là nơi để khởi tạo giao diện và các biến cần thiết.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gọi phương thức onCreate của lớp cha (AppCompatActivity). Đây là một lệnh bắt buộc.
        super.onCreate(savedInstanceState);

        // Nạp file layout "activity_login.xml" làm giao diện cho Activity này.
        // Lệnh này kết nối file Java với file thiết kế XML.
        setContentView(R.layout.activity_login);

        // --- KHỞI TẠO CÁC ĐỐI TƯỢNG ---

        // Khởi tạo đối tượng DatabaseHelper. 'this' tham chiếu đến chính LoginActivity.
        // Cần truyền Context (ngữ cảnh) để DatabaseHelper biết nó đang hoạt động ở đâu.
        db = new DatabaseHelper(this);

        // --- ÁNH XẠ VIEW ---
        // Kết nối các biến đã khai báo ở trên với các View cụ thể trong file layout XML
        // thông qua ID của chúng.
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // --- ĐĂNG KÝ SỰ KIỆN CLICK CHO NÚT ĐĂNG NHẬP ---
        // setOnClickListener là một "bộ lắng nghe". Nó sẽ chờ người dùng nhấn vào btnLogin.
        // Khi người dùng nhấn, đoạn mã bên trong v -> { ... } sẽ được thực thi.
        btnLogin.setOnClickListener(v -> {

            // Lấy dữ liệu người dùng đã nhập từ EditText và loại bỏ khoảng trắng thừa ở đầu/cuối.
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // --- KIỂM TRA DỮ LIỆU ĐẦU VÀO ---
            // Kiểm tra xem người dùng đã nhập đủ thông tin chưa, và số điện thoại có đúng 10 ký tự không.
            if (phone.isEmpty() || password.isEmpty() || phone.length() != 10) {
                // Nếu thông tin không hợp lệ, hiển thị một thông báo ngắn (Toast).
                Toast.makeText(this, "Vui lòng nhập đầy đủ và đúng định dạng SĐT", Toast.LENGTH_SHORT).show();
                // Dừng hàm tại đây, không xử lý tiếp.
                return;
            }

            // --- XÁC THỰC NGƯỜI DÙNG VÀ LẤY ID ---
            // Gọi hàm mới checkUserAndGetId() từ DatabaseHelper.
            // Hàm này trả về ID của người dùng nếu thành công, hoặc -1 nếu thất bại.
            long userId = db.checkUserAndGetId(phone, password);

            // --- XỬ LÝ KẾT QUẢ ĐĂNG NHẬP ---
            if (userId != -1) { // Nếu userId khác -1 (đăng nhập thành công)
                // Hiển thị thông báo thành công.
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Tạo một Intent để chuyển từ màn hình LoginActivity sang MainActivity.
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                // *** DÒNG QUAN TRỌNG NHẤT ***
                // Đính kèm ID của người dùng vào Intent để gửi sang cho MainActivity.
                // "USER_ID" là một "nhãn" để MainActivity có thể tìm và lấy giá trị này.
                intent.putExtra("USER_ID", userId);

                // Thực hiện việc chuyển màn hình.
                startActivity(intent);
                // Đóng LoginActivity lại để người dùng không thể quay lại bằng nút Back.
                finish();

            } else { // Nếu userId là -1 (đăng nhập thất bại)
                // Hiển thị thông báo lỗi.
                Toast.makeText(this, "Sai số điện thoại hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
        
    }

}
