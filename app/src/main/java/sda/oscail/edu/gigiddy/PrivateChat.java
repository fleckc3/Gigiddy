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

/**
 * The PrivateChat activity creates the 1:1 chat activity for users who are friends with each other.
 * This activity receives the information on the user from the member fragment. This user info is
 * used to show their name and profile image
 *    - Adapted from: https://www.youtube.com/watch?v=9siwOvkQb5c&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=47
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 10/03/2020
 */
public class PrivateChat extends AppCompatActivity {
    private static final String TAG = "PrivateChat";

    // private chat variables declared
    private String messageReceiverId, messageReceiverName, otherUserProfileImage, senderUserId;
    private TextView userName;
    private CircleImageView userImage;
    private ImageButton sendMessageBtn;
    private EditText messageToUserInput;
    private Toolbar privateChatToolbar;

    // Firebase auth and db references
    private FirebaseAuth mAuth;
    private DatabaseReference dbRootRef;

    // fields for the messages adapter
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    /**
     * The onCreate() method creates the activity view and initialises the view objects. It also coordinates
     * with the MessageAdapter to get the messages sent and recieved from the users and displays them
     * appropriately.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        // Initialise fields
        userImage = findViewById(R.id.private_chat_profile_image);
        userName = findViewById(R.id.private_chat_name);
        sendMessageBtn = findViewById(R.id.send_message_btn);
        messageToUserInput = findViewById(R.id.input_message);

        // Sets the message adapter on the message list with linear layout manager
        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        // ref: https://stackoverflow.com/questions/32506759/how-to-push-recyclerview-up-when-keyboard-appear
        // moves content in recycler view up when keyboard is activated
        userMessagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
               userMessagesList.scrollToPosition(messagesList.size()-1);
            }
        });

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

        // sets the custom bar layout with name and userprofile image
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.private_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        Log.d(TAG, " PRIVATE CHAT /////////////////////////////////// ------------------------------------------ " + otherUserProfileImage);

        // Sets the profile image and user name in the chat bar with information passed from the
        // member fragment
        Glide.with(this)
                .load(otherUserProfileImage)
                .placeholder(R.drawable.profile_image)
                .into(userImage);
        userName.setText(messageReceiverName);

        // send message logic called
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    /**
     *  The onOptionsItemSelected() method allows user to go back to fragment that started this activity
     *    - Adapted from: https://stackoverflow.com/questions/31491093/how-to-go-back-to-previous-fragment-from-activity
     * @param item is the back button
     * @return menu item selected
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
     * The onsTart() method here serves to grab the messages currently in the DB and add them to
     * the messagesList so they can be appropriately formatted in the chat recyclerView.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // ref: https://stackoverflow.com/questions/26580723/how-to-scroll-to-the-bottom-of-a-recyclerview-scrolltoposition-doesnt-work
        userMessagesList.scrollToPosition(messagesList.size() - 1);

        // Gets the messages saved between current user and their friend they clicked on in member fragment
        dbRootRef.child("Messages").child(senderUserId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        // uses the Messages model class to get the message information and pass it to the messageList
                        // the adpater is then notified to update with the information
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        Log.d(TAG, "//////////////////////////------------------- message list: " + messagesList);
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
     * The sendMessage() method saves the message in the DB twice. Once for the current user with a
     * reference on who they sent the message to. And a second time for the receiving user with a reference
     * to the current user AKA the sender.
     */
    private void sendMessage() {
        // gets the messaeg input from user
        String messageText = messageToUserInput.getText().toString();

        // alerts user if the input is empty
        if(messageText.isEmpty()) {
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
        } else {

            // These two strings create the message references in the Messages DB for each user
            String messageSenderRef = "Messages/" + senderUserId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + senderUserId;

            // db ref for key to these specific messages
            DatabaseReference userMessageKeyRef = dbRootRef.child("Messages")
                    .child(senderUserId).child(messageReceiverId).push();

            // grabs key created above
            String messagePushID = userMessageKeyRef.getKey();

            // creates the message information stored for each message entry
            // under the String references set above.
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderUserId);

            // message body text is added with the strign reference and id to the messageBodyDetails map
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            // updates the Messaged DB with messages sent between users
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

            // ref: https://stackoverflow.com/questions/26580723/how-to-scroll-to-the-bottom-of-a-recyclerview-scrolltoposition-doesnt-work
            userMessagesList.scrollToPosition(messagesList.size() - 1);
        }
    }
}
