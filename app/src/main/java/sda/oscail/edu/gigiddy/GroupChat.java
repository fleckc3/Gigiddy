package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


// ref: https://www.youtube.com/watch?v=4RL85tdhCEU&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=18
public class GroupChat extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessaegBtn;
    private EditText messageInput;
    private ScrollView scrollView;
    private TextView showTextMessages;

    private String currentChatName;
    private String currentUID, currentUserName, currentDate, currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference dbUserRef, dbGroupRef, dbGroupMessageKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //Initialise chat name, current user, firbaseAuth and firebase db reference
        currentChatName = getIntent().getExtras().get("chat_name").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        dbUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //db reference to chat name selected
        dbGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentChatName);

        //initialise toolbar with current chat name
        toolbar = findViewById(R.id.group_chat_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentChatName);

        //initialise chat view objects
        sendMessaegBtn = findViewById(R.id.send_message_btn);
        messageInput = findViewById(R.id.input_message);
        scrollView = findViewById(R.id.scroll_view);
        showTextMessages = findViewById(R.id.chat_text_display);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        Toast.makeText(this, currentChatName, Toast.LENGTH_SHORT).show();

        userInfo();

        sendMessaegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDB();

                messageInput.setText("");
                scrollView.fullScroll(scrollView.FOCUS_DOWN);
            }
        });
    }

    // ref: https://www.youtube.com/watch?v=uiR0U6Gs9e8&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=21
    @Override
    protected void onStart() {
        super.onStart();
        dbGroupRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    displayChatMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //ref: https://www.youtube.com/watch?v=st0zRArsw9A&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=20
    private void saveMessageToDB() {
        String message = messageInput.getText().toString();
        String messageKey = dbGroupRef.push().getKey();
        if(TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write a message...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            dbGroupRef.updateChildren(groupMessageKey);

            dbGroupMessageKey = dbGroupRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            dbGroupMessageKey.updateChildren(messageInfoMap);

        }
    }

    private void userInfo() {
        dbUserRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //ref: https://www.youtube.com/watch?v=uiR0U6Gs9e8&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=21
    private void displayChatMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime= (String) ((DataSnapshot)iterator.next()).getValue();

            showTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");

            scrollView.fullScroll(scrollView.FOCUS_DOWN);

        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
