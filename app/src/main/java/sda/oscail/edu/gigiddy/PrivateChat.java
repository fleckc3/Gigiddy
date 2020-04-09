package sda.oscail.edu.gigiddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class PrivateChat extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        messageReceiverId = getIntent().getExtras().get("user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();

        Toast.makeText(this, messageReceiverId, Toast.LENGTH_LONG).show();
        Toast.makeText(this, messageReceiverName, Toast.LENGTH_SHORT).show();
    }
}
