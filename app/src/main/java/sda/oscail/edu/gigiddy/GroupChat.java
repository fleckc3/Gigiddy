package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


// ref: https://www.youtube.com/watch?v=4RL85tdhCEU&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=18
public class GroupChat extends AppCompatActivity {

    private static final String TAG = "GroupChat";
    
    private Toolbar toolbar;
    private ImageButton sendMessaegBtn;
    private EditText messageInput;
    private ScrollView scrollView;

    private final List<GroupMessage> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter messageAdapter;
    private RecyclerView groupMessageList;

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

        // Initialise fields for message recyclerview and adapter
        messageAdapter = new GroupMessageAdapter(messageList);
        groupMessageList = findViewById(R.id.group_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        groupMessageList.setLayoutManager(linearLayoutManager);
        groupMessageList.setAdapter(messageAdapter);

        // ref: https://stackoverflow.com/questions/32506759/how-to-push-recyclerview-up-when-keyboard-appear
        // moves content in recycler view up when keyboard is activated
        groupMessageList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                groupMessageList.scrollToPosition(messageList.size()-1);
            }
        });

        //db reference to group chat name selected
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

        // alert for chat name user is in
        Toast.makeText(this, currentChatName, Toast.LENGTH_SHORT).show();

        userInfo();

        // send message logic called
        sendMessaegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDB();

                messageInput.setText("");
               // scrollView.fullScroll(scrollView.FOCUS_DOWN);
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

        // ref: https://stackoverflow.com/questions/26580723/how-to-scroll-to-the-bottom-of-a-recyclerview-scrolltoposition-doesnt-work
        groupMessageList.scrollToPosition(messageList.size() - 1);

        // checks for group chats saved in db
        dbGroupRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                GroupMessage messages = dataSnapshot.getValue(GroupMessage.class);

                messageList.add(messages);
                Log.d(TAG, "//////////////////////////------------------building the messageList: " + messageList);
                messageAdapter.notifyDataSetChanged();

                // if groups exist in db then display them
//                if(dataSnapshot.exists()) {
//                    displayChatMessages(dataSnapshot);
//                }
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
            messageInfoMap.put("id", currentUID);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            // adds the message to to the db under that group message
            dbGroupMessageKey.updateChildren(messageInfoMap);
        }
        groupMessageList.scrollToPosition(messageList.size() - 1);
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

//    // ref: https://www.youtube.com/watch?v=uiR0U6Gs9e8&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=21
//    // displays chat messages from the db
//    private void displayChatMessages(DataSnapshot dataSnapshot) {
//        Iterator iterator = dataSnapshot.getChildren().iterator();
//
//        // iterates over each entry in the db -> sets info in the correct view
//        while(iterator.hasNext()) {
//            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatTime= (String) ((DataSnapshot)iterator.next()).getValue();
//
//            // sets the messages and associated info in the group chat view
//
//            if(chatName.equals(currentUserName)) {
//                otherUserName.setVisibility(View.INVISIBLE);
//                otherDateTime.setVisibility(View.INVISIBLE);
//                otherSenderText.setVisibility(View.INVISIBLE);
//
//                currentUserText.setVisibility(View.VISIBLE);
//                currentUserDateTime.setVisibility(View.VISIBLE);
//                currentUserText.setText(chatMessage);
//                currentUserDateTime.setText(chatDate + " - " + chatTime);
//
//            } else {
//                currentUserText.setVisibility(View.INVISIBLE);
//                currentUserDateTime.setVisibility(View.INVISIBLE);
//
//                otherUserName.setVisibility(View.VISIBLE);
//                otherDateTime.setVisibility(View.VISIBLE);
//                otherSenderText.setVisibility(View.VISIBLE);
//                otherUserName.setText(chatName);
//                otherDateTime.setText(chatDate + " - " + chatTime);
//                otherSenderText.setText(chatMessage);
//            }
//
//            scrollView.fullScroll(scrollView.FOCUS_DOWN);
//        }
//
//        // sets the scrollable view to the newest message at bottom
//        scrollView.post(new Runnable() {
//            @Override
//            public void run() {
//                scrollView.fullScroll(View.FOCUS_DOWN);
//            }
//        });
//    }
}

//        otherUserName = findViewById(R.id.receiver_user_name);
//        otherDateTime = findViewById(R.id.receiver_message_date_time);
//        otherSenderText = findViewById(R.id.receiver_message_text);
//        currentUserDateTime = findViewById(R.id.sender_message_date_time);
//        currentUserText = findViewById(R.id.sender_message_text);