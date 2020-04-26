package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * The GroupChat Activity creates a custom chat where multiple users can send and receive chat messages.
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 21/04/2020
 *  - ref: https://www.youtube.com/watch?v=4RL85tdhCEU&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=18
 */
public class GroupChat extends AppCompatActivity {
    private static final String TAG = "GroupChat";

    // View fields declared
    private Toolbar toolbar;
    private ImageButton sendMessaegBtn;
    private EditText messageInput;

    // Variables for showing chat messsages declared
    private final List<GroupMessage> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter messageAdapter;
    private RecyclerView groupMessageList;

    // String variables for chat and user specific info
    private String currentChatName;
    private String currentUID, currentUserName, currentDate, currentTime;

    // Firebase db variables/references declared
    private FirebaseAuth mAuth;
    private DatabaseReference dbUserRef, dbGroupRef, dbGroupMessageKey;

    /**
     * The onCreate() method creates the view for the group chat selected.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //Initialise chat name, current user, firebaseAuth and firebase db reference
        currentChatName = getIntent().getExtras().get("chat_name").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        dbUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Initialise fields for message recyclerview and adapter
        messageAdapter = new GroupMessageAdapter(messageList);
        groupMessageList = findViewById(R.id.group_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
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

        //initialise toolbar with current chat name and chat view input and send message button
        toolbar = findViewById(R.id.group_chat_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentChatName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        sendMessaegBtn = findViewById(R.id.send_message_btn);
        messageInput = findViewById(R.id.input_message);

        // alert for group chat name user is in
        Toast.makeText(this, currentChatName, Toast.LENGTH_SHORT).show();

        userInfo();

        // send message logic called to save in DB
        sendMessaegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDB();
                messageInput.setText("");
            }
        });
    }

    /**
     * This method allows the user to go back to the fragment they were on before they accessed the chat requests activity.
     * @param item gets the home button pressed
     * @return item selected
     *  - ref: https://stackoverflow.com/questions/31491093/how-to-go-back-to-previous-fragment-from-activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The onStart() method helps set the GroupChat focus on the most recent messages.
     * In addition it grabs all the messages saved in the DB under the group chat clicked on.
     * The messageAdapter then takes the GroupMessages object and uses the GroupMessageAdapter.class to display the
     * chat messages.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // ref: https://stackoverflow.com/questions/26580723/how-to-scroll-to-the-bottom-of-a-recyclerview-scrolltoposition-doesnt-work
        groupMessageList.scrollToPosition(messageList.size() - 1);

        // checks for group chats saved in db
        dbGroupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // Uses the GroupMessage model class to retrieve the group chat messages
                // adds them t the messageList array and notifies the adapter
                GroupMessage messages = dataSnapshot.getValue(GroupMessage.class);
                messageList.add(messages);
                Log.d(TAG, "//////////////////////////------------------building the messageList: " + messageList);
                messageAdapter.notifyDataSetChanged();
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

    /**
     * The saveMessagetoDB() method sends the message input to the group chat which saves the message to the DB.
     *   - ref: https://www.youtube.com/watch?v=st0zRArsw9A&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=20
     */
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

    /**
     * The userInfo() method gets info of the user who sent message from the Users DB
     */
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
                // handle canceled error here
            }
        });
    }
}

