package sda.oscail.edu.gigiddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

public class GroupChat extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessaegBtn;
    private EditText messageInput;
    private ScrollView scrollView;
    private TextView showTextMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar = findViewById(R.id.group_chat_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Group Name");

        sendMessaegBtn = findViewById(R.id.send_message_btn);
        messageInput = findViewById(R.id.input_message);
        scrollView = findViewById(R.id.scroll_view);
        showTextMessages = findViewById(R.id.chat_text_display);




    }
}
