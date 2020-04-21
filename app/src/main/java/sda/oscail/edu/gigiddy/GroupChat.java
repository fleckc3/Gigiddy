package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //initialise chat view objects
        sendMessaegBtn = findViewById(R.id.send_message_btn);
        messageInput = findViewById(R.id.input_message);
        scrollView = findViewById(R.id.scroll_view);
        showTextMessages = findViewById(R.id.chat_text_display);

        // focus on most recent message at bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        // alert for chat name user is in
        Toast.makeText(this, currentChatName, Toast.LENGTH_SHORT).show();

        userInfo();

        // send message logic called
        sendMessaegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDB();

                messageInput.setText("");
                scrollView.fullScroll(scrollView.FOCUS_DOWN);
            }
        });
    }

    // goes back to fragment that calls this activity
    // ref: https://stackoverflow.com/questions/31491093/how-to-go-back-to-previous-fragment-from-activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ref: https://www.youtube.com/watch?v=uiR0U6Gs9e8&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=21
    @Override
    protected void onStart() {
        super.onStart();

        // checks for group chats saved in db
        dbGroupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // if groups exist in db then display them
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

        // gets message typed in field
        String message = messageInput.getText().toString();
        String messageKey = dbGroupRef.push().getKey();

        // checks if field is empty and alerts user
        if(TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write a message...", Toast.LENGTH_SHORT).show();
        } else {

            // gets date and timestamp for message
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            // gets group db key
            HashMap<String, Object> groupMessageKey = new HashMap<>();
            dbGroupRef.updateChildren(groupMessageKey);

            // gets the reference at that key in db
            dbGroupMessageKey = dbGroupRef.child(messageKey);

            // creates map of message info
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            // adds the message to to the db under that group message
            dbGroupMessageKey.updateChildren(messageInfoMap);
        }
    }

    // gets info of the user who sent message
    private void userInfo() {
        dbUserRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // if user id exist in the group db -> get their name
                if(dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // ref: https://www.youtube.com/watch?v=uiR0U6Gs9e8&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=21
    // displays chat messages from the db
    private void displayChatMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        // iterates over each entry in the db -> sets info in the correct view
        while(iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime= (String) ((DataSnapshot)iterator.next()).getValue();

            // sets the messages and associated info in the group chat view
            showTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");
            scrollView.fullScroll(scrollView.FOCUS_DOWN);
        }

        // sets the scrollable view to the newest message at bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}