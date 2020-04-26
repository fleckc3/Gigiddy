package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
 * The CheckRequests activity implements a FireBaseRecyclerAdapter to grab the current user's
 * friend requests saved in the Chat Request DB and show them in the activity using a custom layout.
 * Each item portrays the user who sent the request with their name, picture, and status along with an accept and cancel button.
 * The current user can then accept or cancel the friend request.
 *
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 09/04/2020
 *
 *
 * ref: https://www.youtube.com/watch?v=WGOY7Lsac1U&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=41
 */
public class CheckRequests extends AppCompatActivity {
    private static final String TAG = "CheckRequests";

    // toolbar and recyclerView declared
    private Toolbar toolbar;
    private RecyclerView requestList;

    // Firebase variables declared
    private DatabaseReference dbRequestRef, dbUsersRef, dbContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId, currentUserName, senderUserName;

    /**
     * The onCreate() method creates the activity view. Declared fields are initialised
     * @param savedInstanceState saves the view current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_requests);

        // Initialise fields
        requestList = findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(this));

        // Sets the toolbar
        toolbar = findViewById(R.id.request_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Requests");

        // intialise firebase references
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        dbRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        dbUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        // Checks Users DB for the currentUserId and gets their name
        dbUsersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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

    /**
     * This method allows the user to go back to the fragment they were on before they accessed the chat requests activity.
     * @param item gets the home button pressed
     * @return item selected
     *
     *  ref: https://stackoverflow.com/questions/31491093/how-to-go-back-to-previous-fragment-from-activity
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
     * The onStart() method gets the friend requests saved for the current user from the Chat Requests DB.
     * Those requests are then displayed in the activity via FirebaseRecyclerAdapter. Each item is displayed using a custom layout
     * that displays the user who sent the request and their info.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Firebase object queries request db for all requests between users using the contact model class
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbRequestRef.child(currentUserId), Contacts.class)
                .build();

        // Takes the options above and binds the information into the the viewholder. creates a viewholder for each option that meets the criteria
        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull final Contacts contacts) {

                // sets the accept/cancel buttons visible in the viewholder
                requestViewHolder.itemView.findViewById(R.id.request_accept).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_deny).setVisibility(View.VISIBLE);

                // gets user id at each position in options object passed into the adapter
                final String userIDs = getRef(i).getKey();
                Log.d(TAG, "////////////////////////////////////////////------------------------------ user keys: " + userIDs);

                // gets the request_type ref at each for each request reference in the db
                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();

                            // if request_type has value received
                            if(type.equals("received")) {
                                Log.d(TAG, "////////////////////////////////////////----------------------------- type = receieved");

                                // take the userId who sent the request and get their profile information from the users db
                                dbUsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        // check because having an image is optional
                                        if(dataSnapshot.hasChild("image")) {

                                            // user image from friebase stored in variable and passed to the view via Glide
                                            final String requestSenderImage = dataSnapshot.child("image").getValue().toString();
                                            Glide.with(requestViewHolder.profileImage.getContext().getApplicationContext())
                                                    .load(requestSenderImage)
                                                    .placeholder(R.drawable.profile_image)
                                                    .into(requestViewHolder.profileImage);
                                        }

                                        // gets the sender's name and
                                        // Sets the name of sender and his status as wanting to connect
                                        final String requestSenderName = dataSnapshot.child("name").getValue().toString();
                                        senderUserName = requestSenderName;
                                        requestViewHolder.userName.setText(requestSenderName);
                                        requestViewHolder.userStatus.setText("Wants to connect with you.");

                                        // creates alert dialog on click to accept or cancel the request
                                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[] {
                                                        "Accept Request",
                                                        "Cancel Request"
                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(CheckRequests.this);
                                                builder.setTitle("Join Request from " + requestSenderName);
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Accept request
                                                        if(which == 0) {

                                                            HashMap<String, Object> contactsMap = new HashMap<>();
                                                            contactsMap.put("Contacts", "Saved");
                                                            contactsMap.put("name", senderUserName);

                                                            // update contact db to for sender and receiever to now be contacts aka saved
                                                            // 1st update receiver
                                                            dbContactsRef.child(currentUserId).child(userIDs)
                                                                    .updateChildren(contactsMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    HashMap<String, Object> contactsMap = new HashMap<>();
                                                                    contactsMap.put("Contacts", "Saved");
                                                                    contactsMap.put("name", currentUserName);

                                                                    // if successful, now update the sender in contact db
                                                                    if(task.isSuccessful()) {
                                                                        dbContactsRef.child(userIDs).child(currentUserId)
                                                                                .updateChildren(contactsMap)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                // if successful...
                                                                                if(task.isSuccessful()) {

                                                                                    // update the request db and remove the request as they are now contacts
                                                                                    // 1st update receiver user
                                                                                    dbRequestRef.child(currentUserId).child(userIDs)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                    //if successful, update sender user and remove request in the request db
                                                                                                    if(task.isSuccessful()) {
                                                                                                        dbRequestRef.child(userIDs).child(currentUserId)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                                        // Updates user if successfull
                                                                                                                        if(task.isSuccessful()) {
                                                                                                                            Toast.makeText(CheckRequests.this, "Contact Saved!", Toast.LENGTH_SHORT).show();
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

                                                        // cancel request
                                                        if(which == 1) {

                                                            // update the request db and remove the request as they are now contacts
                                                            // 1st update receiver user
                                                            dbRequestRef.child(currentUserId).child(userIDs)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            //if successful, update sender user and remove request in the request db
                                                                            if(task.isSuccessful()) {
                                                                                dbRequestRef.child(userIDs).child(currentUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                // lets user know contact deleted
                                                                                                if(task.isSuccessful()) {
                                                                                                    Toast.makeText(CheckRequests.this, "Contact Deleted!", Toast.LENGTH_SHORT).show();
                                                                                                }

                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // handle canceled error here
                                    }
                                });
                            } else {
                                requestViewHolder.itemView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // handle canceled error here
                    }
                });
            }

            // Viewholder sets the layout view to be used by each option in the recycler adapter
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_user_layout, parent, false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return viewHolder;
            }
        };

        // updates the list and tells the adapter to listen for new entries
        requestList.setAdapter(adapter);
        adapter.startListening();
    }


    /**
     * The RequestViewholder class declares and intitialises the fields to be set by the adapter
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        // fields used to initialise objects in viewHolder
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptBtn, cancelBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            // initialise fields
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            acceptBtn = itemView.findViewById(R.id.request_accept);
            cancelBtn = itemView.findViewById(R.id.request_deny);
        }
    }
}
