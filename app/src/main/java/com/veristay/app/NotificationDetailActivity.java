package com.veristay.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.veristay.app.R;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        String title   = getIntent().getStringExtra("notification_title");
        String message = getIntent().getStringExtra("notification_message");
        String type    = getIntent().getStringExtra("notification_type");

        TextView tvTitle   = findViewById(R.id.tv_title);
        TextView tvMessage = findViewById(R.id.tv_message);
        TextView tvType    = findViewById(R.id.tv_type);

        if (tvTitle   != null) tvTitle.setText(title   != null ? title   : "Notification");
        if (tvMessage != null) tvMessage.setText(message != null ? message : "");
        if (tvType    != null) tvType.setText(type     != null ? type     : "general");

        // Back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notification");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}