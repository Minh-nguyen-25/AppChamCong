package com.example.chamcong;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // tìm view root an toàn: nếu không có R.id.main thì dùng android.R.id.content
        View root = findViewById(R.id.main);
        if (root == null) {
            root = findViewById(android.R.id.content);
        }

        // Đăng ký listener (nếu cần edge-to-edge). Nếu không cần thì xóa toàn bộ block này.
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
