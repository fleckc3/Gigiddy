package sda.oscail.edu.gigiddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Toast;

public class Profile extends AppCompatActivity {

    private String receiverUserId;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //set back button to mainactivity
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        receiverUserId = getIntent().getExtras().get("user_id").toString();

        Toast.makeText(this, "UID: " + receiverUserId, Toast.LENGTH_SHORT).show();
    }
}
