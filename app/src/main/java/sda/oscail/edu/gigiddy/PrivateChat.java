package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChat extends AppCompatActivity {

    private static final String TAG = "PrivateChat";
    private String messageReceiverId, messageReceiverName, otherUserProfileImage, senderUserId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private ImageButton sendMessageBtn;
    private EditText messageToUserInput;
    private Toolbar privateChatToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRootRef;

    // fields for the messages adapter
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;



    // ref: https://www.youtube.com/watch?v=9siwOvkQb5c&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=47
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        // Initialise fields
        userImage = findViewById(R.id.private_chat_profile_image);
        userName = findViewById(R.id.private_chat_name);
        sendMessageBtn = findViewById(R.id.send_message_btn);
        messageToUserInput = findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        // Firebase refs
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getUid();
        dbRootRef = FirebaseDatabase.getInstance().getReference();

        // Get user data from members intent
        messageReceiverId = getIntent().getExtras().get("user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();
        otherUserProfileImage = getIntent().getExtras().get("user_image").toString();

        // set custom action bar
        privateChatToolbar = findViewById(R.id.private_chat_bar);
        setSupportActionBar(privateChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // sets the custom bar layout with name and username
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.private_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        Log.d(TAG, " PRIVATE CHAT /////////////////////////////////// ------------------------------------------ " +otherUserProfileImage);

        // sets the profile image of user
        Glide.with(this)
                .load(otherUserProfileImage)
                .placeholder(R.drawable.profile_image)
                .into(userImage);
        // set user data in custom toolbar fields
        userName.setText(messageReceiverName);

        // send message logic called
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
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

    @Override
    protected void onStart() {
        super.onStart();

        dbRootRef.child("Messages").child(senderUserId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);


                        messagesList.add(messages);

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

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "/////////////////////// --------------------- IM ON PAAAAAAUUUUSSSSEEEE");
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "/////////////////////// --------------------- IM REEEEEESUUUUUMMMMIIINNNGGG");

    }

    // ref: https://www.youtube.com/watch?v=pI9g8zUEqKI&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=48
    private void sendMessage() {


        String messageText = messageToUserInput.getText().toString();

        if(messageText.isEmpty()) {
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
        } else {

            // string db refs to sender and reciever for messages
            String messageSenderRef = "Messages/" + senderUserId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + senderUserId;

            // db ref for key to these specific messages
            DatabaseReference userMessageKeyRef = dbRootRef.child("Messages")
                    .child(senderUserId).child(messageReceiverId).push();

            // grabs key created above
            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderUserId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            dbRootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(PrivateChat.this, "Message sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PrivateChat.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    messageToUserInput.setText("");
                }
            });



        }
    }
}
