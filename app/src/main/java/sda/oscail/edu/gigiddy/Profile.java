package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
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

/**
 * The Profile Activity is started when the current user clicks on a user in the FindMembers activity.
 * That user's info is then passed to this activity so it can be populated in their profile view for
 * the current user to view. The current user can then see what type of relation sthey have with the
 * user if any  or they can send a friend request.
 *    - Adapted from:  ref: https://www.youtube.com/watch?v=fatkPOq4AlA&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=33
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 07/04/2020
 */
public class Profile extends AppCompatActivity {
    private static final String TAG = "Profile";

    // profile view variables declared
    private String receiverUserId, currentState, senderUserId, senderUserName;
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageBtn, cancelRequestBtn;

    // Firebase auth and db references
    private DatabaseReference dbUserRef, dbChatReqRef, dbContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase db and auth references initialised
        dbUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbChatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        dbContactsRef = FirebaseDatabase.getInstance().getReference().child(("Contacts"));
        mAuth = FirebaseAuth.getInstance();

        // Initialise view fields
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

        // get the current user's name
        dbUserRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    senderUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle db error here
            }
        });

        // Sets the state of the relationship with user to new
        currentState = "new";
        getUserInfo();
    }

    /**
     * the onStart() method checks the relationship status between the current user and the user who's
     * profile they are viewing by calling the checkRelationshipStatus() method.
     */
    @Override
    protected void onStart() {
        super.onStart();
        checkRelationshipStatus();
    }

    /**
     * The checkRelationshipStatus() method checks the realationship staus of the current user and thr user profile
     * they are viewing.
     */
    private void checkRelationshipStatus() {

        // check the chat Requests request between the two
        dbChatReqRef.child(senderUserId).child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {

                    // if no chat request exists between them then check if they are contacts of eachother in the contatcs db
                    dbContactsRef.child(senderUserId).child(receiverUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {

                                // if they arent contacts of each other then set the buttons to allow them to send friend request
                                currentState = "new";
                                sendMessageBtn.setText("Send Friend Request");
                                cancelRequestBtn.setVisibility(View.INVISIBLE);
                                cancelRequestBtn.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // handle db error here
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle db error here
            }
        });
    }

    /**
     * The getUserInfo() method grabs the user info of the profile being viewed
     */
    private void getUserInfo() {

        // Checks user at the reference provided from the previous activity
        dbUserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("image"))) {

                    // gets the user info if snapshot exit and has image value
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    // sets the user info in the view
                    Glide.with(Profile.this)
                            .load(userImage)
                            .placeholder(R.drawable.profile_image)
                            .into(profileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    // gets info about friends request status
                    manageChatRequests();
                } else {

                    // if no image then name and status set in the view
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle db error here
            }
        });
    }

    /**
     * The manageChatsRequests() method gets info from db on whether current user and the user profile
     * selected are friends or have a pending a friend request.
     *    - Adapted from: https://www.youtube.com/watch?v=4M5pWsrdTS4&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=34
     */
    private void manageChatRequests() {

        // looks up if this info is in db or not
        dbChatReqRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)) {

                    // if it is in db then it gets the state/type
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
                    Log.d(TAG, "//////////////////////////////------------ if chat req snapshot does not exist between the users");

                    // checks if they are friends already
                    dbContactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiverUserId)) {
                                        Log.d(TAG, "////////////////////////-------------- I am friends with this person");

                                        // show button for removing them from friends list
                                        currentState = "friends";
                                        sendMessageBtn.setText("Remove Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // handle db error here
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle db error here
            }
        });

        if(!senderUserId.equals(receiverUserId)) {

            // Onclick calls necessary button logic based on current state between the two users
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

        // if profile is the current user then button is hidden
        } else {
            sendMessageBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * The sendChatReq() method sends a chat request when send message button is clicked in profile
     *    - Adapted from: https://www.youtube.com/watch?v=4M5pWsrdTS4&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=34
     */
    private void sendChatReq() {

        // gets the the users name to send chat req to and also the current users name
        String setUserName = userProfileName.getText().toString();
        final String userName = senderUserName;

        // Builds the chat request information to be saved
        HashMap<String, Object> contactsMap = new HashMap<>();
        contactsMap.put("request_type", "sent");
        contactsMap.put("name", setUserName);

        //saves the request status in db for user who sends request
        dbChatReqRef.child(senderUserId).child(receiverUserId)
                .updateChildren(contactsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            // create map to also update the receiver with the same info
                            HashMap<String, Object> contactsMap = new HashMap<>();
                            contactsMap.put("request_type", "received");
                            contactsMap.put("name", userName);

                            // saves the received status of request with user receiving the chat request
                            dbChatReqRef.child(receiverUserId).child(senderUserId)
                                    .updateChildren(contactsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {

                                                // update the button logic to reflect request
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

    /**
     * The cancelChatReq() method cancels chat request button clicked. Removes the sent and received values in db for each user.
     *  It then resets the currentState to new, hides the cancel btn, and enables the send message button again.
     *    - Adapted from: https://www.youtube.com/watch?v=6SFaWDda2ps&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=35
     */
    private void cancelChatReq() {

        // gets the value saved for sender and receiver request and removes it
        dbChatReqRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            // remove the request for the receiver as well
                            dbChatReqRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {

                                                // update back to neutral relationship values
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

    /**
     * The acceptChatReq() method accepts the chat request, sets each user as a contact of one another in the Contacts DB.
     * Then it removes the chat request between them since they are contacts now.
     *    - Adapted from: https://www.youtube.com/watch?v=h-P6hHdjmZk&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=37
     */
    private void acceptChatReq() {

        // grabs name of sender and user to be a contact with
        String setUserName = userProfileName.getText().toString();
        final String userName = senderUserName;

        // creates map of info to be updated in db: contact = saved(aka friends), and the contacts name
        HashMap<String, Object> contactsMap = new HashMap<>();
        contactsMap.put("Contacts", "Saved");
        contactsMap.put("name", setUserName);

        // updates current user and reciever as friends
        dbContactsRef.child(senderUserId).child(receiverUserId)
                .updateChildren(contactsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            // ...create map object to update for receeiver in db: contacts = saved(aka friends), and contacts name
                            HashMap<String, Object> contactsMap = new HashMap<>();
                            contactsMap.put("Contacts", "Saved");
                            contactsMap.put("name", userName);

                            // update receiver as friends with current user
                            dbContactsRef.child(receiverUserId).child(senderUserId)
                                    .updateChildren(contactsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {

                                                // remove the chat request in db for sender and receiver
                                                dbChatReqRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()) {

                                                                    // remove chat request for receiver and sender
                                                                    dbChatReqRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
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

    /**
     * The removeSpecificContact() method removes the sending user and receiver user as contacts from contact db.
     *    - Adapted from: https://www.youtube.com/watch?v=O-P8mqtL4Sc&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=38
     */
    private void removeSpecificContact() {

        // remove sender/receiver relationship
        dbContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // if remove successful then remove receiver/sender user relationship from contact db
                        if(task.isSuccessful()) {
                            dbContactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            // if remove successful then update info to reflect new relationship status: not friends
                                            if(task.isSuccessful()) {
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
