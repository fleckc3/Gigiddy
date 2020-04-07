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

import de.hdodenhof.circleimageview.CircleImageView;

// ref: https://www.youtube.com/watch?v=fatkPOq4AlA&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=33
public class Profile extends AppCompatActivity {

    private String receiverUserId, currentState, senderUserId;
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

        receiverUserId = getIntent().getExtras().get("user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        currentState = "new";


        getUserInfo();
    }

    private void getUserInfo() {

        dbUserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
    private void manageChatRequests() {

        // check if chat request has been sent
        dbChatReqRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)) {
                    String requestType = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    // toggles buttons based off the request types
                    if(requestType.equals("sent")) {
                        currentState = "request_sent";
                        sendMessageBtn.setText("Cancel chat Request");
                    } else if(requestType.equals("received")) {
                        currentState = "request_received";
                        sendMessageBtn.setText("Accept Chat Request");

                        cancelRequestBtn.setVisibility(View.VISIBLE);
                        cancelRequestBtn.setEnabled(true);

                        cancelRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatReq();
                            }
                        });
                    }
                } else {
                    dbContactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
    // sends chat request when send message buton is clicked in profile
    private void sendChatReq() {

        //saves the request status in db for user who sends request
        dbChatReqRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            // saves the recieved status of request with user receiving the chat request
                            dbChatReqRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                sendMessageBtn.setEnabled(true);
                                                currentState = "request_sent";
                                                sendMessageBtn.setText("Cancel chat request");
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
        dbChatReqRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            dbChatReqRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                sendMessageBtn.setEnabled(true);
                                                currentState = "new";
                                                sendMessageBtn.setText("Send Message");

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

        // update sender node in contacts db
        dbContactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            // update receiver node in contacts db
                            dbContactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {

                                                // remove sender node in chat requests db
                                                dbChatReqRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                // remove receiver node in chat requests db
                                                                if(task.isSuccessful()) {
                                                                    dbChatReqRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()) {
                                                                                        sendMessageBtn.setEnabled(true);
                                                                                        currentState = "friends";
                                                                                        sendMessageBtn.setText("Remove Contact");

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

    private void removeSpecificContact() {
        // remove the sending user and reciever user as contacts from contact db
        dbContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            // remove receiving user and sending user as contacts fro contact db
                            dbContactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                sendMessageBtn.setEnabled(true);
                                                currentState = "new";
                                                sendMessageBtn.setText("Send Message");

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
