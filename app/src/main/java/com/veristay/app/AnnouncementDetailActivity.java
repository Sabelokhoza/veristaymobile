package com.veristay.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.veristay.app.R;


public class AnnouncementDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

        String message    = getIntent().getStringExtra("announcement_message");
        String landlord   = getIntent().getStringExtra("announcement_landlord");
        String postedAt   = getIntent().getStringExtra("announcement_posted_at");

        TextView tvMessage  = findViewById(R.id.tv_message);
        TextView tvLandlord = findViewById(R.id.tv_landlord);
        TextView tvDate     = findViewById(R.id.tv_date);

        if (tvMessage  != null) tvMessage.setText(message  != null ? message  : "");
        if (tvLandlord != null) tvLandlord.setText("📢 " + (landlord != null ? landlord : "Landlord"));
        if (tvDate     != null) tvDate.setText(postedAt != null && postedAt.length() >= 10
                ? "Posted: " + postedAt.substring(0, 10) : "");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Announcement");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}