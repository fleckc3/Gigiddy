package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

// ref: https://www.youtube.com/watch?v=fatkPOq4AlA&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=33
public class Profile extends AppCompatActivity {

    private String receiverUserId, currentState, senderUserId, senderUserName;
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageBtn, cancelRequestBtn;

    private DatabaseReference dbUserRef, dbChatReqRef, dbContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase db ref for users and chat requests.
        // Firebase auth user instance
        dbUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbChatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        dbContactsRef = FirebaseDatabase.getInstance().getReference().child(("Contacts"));
        mAuth = FirebaseAuth.getInstance();

        // Initialise fields
        profileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageBtn = findViewById(R.id.message_req_btn);
        cancelRequestBtn = findViewById(R.id.decline_req_btn);

        // set toolbar and back button to mainactivity
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // gets the id of user clicked from the findMembers intent
        receiverUserId = getIntent().getExtras().get("user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        //
        dbUserRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    senderUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Sets the state of the relationship with user to new
        currentState = "new";
        getUserInfo();
    }

    // gets the info of the profile clicked on
    private void getUserInfo() {

        dbUserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // checks the db ref exists and user has image
                // sets user fields with correct info
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Glide.with(Profile.this)
                            .load(userImage)
                            .placeholder(R.drawable.profile_image)
                            .into(profileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    // gets info about friends request
                    manageChatRequests();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // ref: https://www.youtube.com/watch?v=4M5pWsrdTS4&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=34
    // Gets info from db on whether current user and the user profile selected are friends or pending a request
    private void manageChatRequests() {

        // looks up if this info is in db or not
        dbChatReqRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                // if it is in db then it gets the state
                if(dataSnapshot.hasChild(receiverUserId)) {

                    // request/relationship status
                    String requestType = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    // toggles buttons based off the request types
                    if(requestType.equals("sent")) {
                        currentState = "request_sent";
                        sendMessageBtn.setText("Cancel Friend Request");
                    } else if(requestType.equals("received")) {
                        currentState = "request_received";
                        sendMessageBtn.setText("Accept Friend Request");

                        cancelRequestBtn.setVisibility(View.VISIBLE);
                        cancelRequestBtn.setEnabled(true);

                        // cancel friend request
                        cancelRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatReq();
                            }
                        });
                    }
                } else {

                    // checks if they are friends already
                    dbContactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    // if friends, show button fro removing them from friends list
                                    if(dataSnapshot.hasChild(receiverUserId)) {
                                        currentState = "friends";
                                        sendMessageBtn.setText("Remove Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Checks to see if profile is the current user profile and hides the send message button
        if(!senderUserId.equals(receiverUserId)) {
            sendMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageBtn.setEnabled(false);
                    
                    if(currentState.equals("new")) {
                        sendChatReq();
                    }

                    if(currentState.equals("request_sent")) {
                        cancelChatReq();
                    }

                    if(currentState.equals("request_received")) {
                        acceptChatReq();
                    }
                    
                    if(currentState.equals("friends")){
                        removeSpecificContact();
                    }


                }
            });
        } else {
            sendMessageBtn.setVisibility(View.INVISIBLE);
        }
    }

    // ref: https://www.youtube.com/watch?v=4M5pWsrdTS4&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=34
    // sends chat request when send message button is clicked in profile
    private void sendChatReq() {

        String setUserName = userProfileName.getText().toString();
        final String userName = senderUserName;

        HashMap<String, Object> contactsMap = new HashMap<>();
        contactsMap.put("request_type", "sent");
        contactsMap.put("name", setUserName);

        //saves the request status in db for user who sends request
        dbChatReqRef.child(senderUserId).child(receiverUserId)
                .updateChildren(contactsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // if update successfull..
                        if(task.isSuccessful()) {

                            // creates map of info to set in db
                            HashMap<String, Object> contactsMap = new HashMap<>();
                            contactsMap.put("request_type", "received");
                            contactsMap.put("name", userName);

                            // saves the received status of request with user receiving the chat request
                            dbChatReqRef.child(receiverUserId).child(senderUserId)
                                    .updateChildren(contactsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            // if update successfull....
                                            if(task.isSuccessful()) {
                                                sendMessageBtn.setEnabled(true);
                                                currentState = "request_sent";
                                                sendMessageBtn.setText("Cancel Friend Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // ref: https://www.youtube.com/watch?v=6SFaWDda2ps&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=35
    // Called when cancel chat request button clicked. Removes the sent and recieved values in db.
    // Resets currentState to new, hides the cancel btn, and enables the send message button again
    private void cancelChatReq() {

        // gets the value saved for sender and receiver request
        dbChatReqRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // if remove value successful...
                        if(task.isSuccessful()) {

                            // ...remove request for receiver
                            dbChatReqRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            // if remove successful...
                                            if(task.isSuccessful()) {

                                                // update back to neutral realtionship values
                                                sendMessageBtn.setEnabled(true);
                                                currentState = "new";
                                                sendMessageBtn.setText("Send Friend Request");

                                                // cancel button
                                                cancelRequestBtn.setVisibility(View.INVISIBLE);
                                                cancelRequestBtn.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    // ref: https://www.youtube.com/watch?v=h-P6hHdjmZk&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=37
    // accept chat request, set each user as a contact of one another in new db node
    // remove chat request between them since they are contacts now
    private void acceptChatReq() {

        // grabs name of sender
        String setUserName = userProfileName.getText().toString();
        final String userName = senderUserName;

        // creates map of info to be updated in db: contact = saved(aka friends), and the contacts name
        HashMap<String, Object> contactsMap = new HashMap<>();
        contactsMap.put("Contacts", "Saved");
        contactsMap.put("name", setUserName);

        // update sender node in contacts db
        dbContactsRef.child(senderUserId).child(receiverUserId)
                .updateChildren(contactsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // if udpate successful....
                        if(task.isSuccessful()) {

                            // ...create map object to update for receiever in db: contacts = saved(aka friends), and contacts name
                            HashMap<String, Object> contactsMap = new HashMap<>();
                            contactsMap.put("Contacts", "Saved");
                            contactsMap.put("name", userName);

                            // update receiver node in contacts db
                            dbContactsRef.child(receiverUserId).child(senderUserId)
                                    .updateChildren(contactsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            // if update successful....
                                            if(task.isSuccessful()) {

                                                // ....remove sender node in chat requests db. friends now so no request info needed anymore
                                                dbChatReqRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                // if remove successful....
                                                                if(task.isSuccessful()) {

                                                                    // ...remove receiver node in chat requests db. friends now so no request info needed anymore
                                                                    dbChatReqRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    // if remove successful....
                                                                                    if(task.isSuccessful()) {

                                                                                        // update info to reflect new status: friends
                                                                                        sendMessageBtn.setEnabled(true);
                                                                                        currentState = "friends";
                                                                                        sendMessageBtn.setText("Remove Contact");

                                                                                        // hide cancel button
                                                                                        cancelRequestBtn.setVisibility(View.INVISIBLE);
                                                                                        cancelRequestBtn.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // ref: https://www.youtube.com/watch?v=O-P8mqtL4Sc&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=38
    // remove the sending user and receiver user as contacts from contact db
    private void removeSpecificContact() {

        // remove sender/receiver relationship
        dbContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // if remove successful...
                        if(task.isSuccessful()) {

                            // remove receiver/sender user relationship from contact db
                            dbContactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            // if remove successful...
                                            if(task.isSuccessful()) {

                                                // update info to refelct new relationship status: not friends
                                                sendMessageBtn.setEnabled(true);
                                                currentState = "new";
                                                sendMessageBtn.setText("Send Friend Request");

                                                // hide cancel button
                                                cancelRequestBtn.setVisibility(View.INVISIBLE);
                                                cancelRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
