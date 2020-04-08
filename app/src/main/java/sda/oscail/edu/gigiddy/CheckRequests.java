package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


// ref: https://www.youtube.com/watch?v=WGOY7Lsac1U&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=41
public class CheckRequests extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView requestList;

    private DatabaseReference dbRequestRef, dbUsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_requests);

        // Initialise fields
        requestList = findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(this));
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

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbRequestRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull final Contacts contacts) {

                requestViewHolder.itemView.findViewById(R.id.request_accept).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_deny).setVisibility(View.VISIBLE);

                final String userIDs = getRef(i).getKey();

                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();

                            if(type.equals("received")) {
                                dbUsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild("image")) {
                                            final String requestSenderName = dataSnapshot.child("name").getValue().toString();
                                            final String requestSenderStatus = dataSnapshot.child("status").getValue().toString();
                                            final String requestSenderImage = dataSnapshot.child("image").getValue().toString();

                                            requestViewHolder.userName.setText(requestSenderName);
                                            requestViewHolder.userStatus.setText(requestSenderStatus);

                                            Glide.with(requestViewHolder.profileImage.getContext())
                                                    .load(requestSenderImage)
                                                    .placeholder(R.drawable.profile_image)
                                                    .into(requestViewHolder.profileImage);
                                        } else {
                                            final String requestSenderName = dataSnapshot.child("name").getValue().toString();
                                            final String requestSenderStatus = dataSnapshot.child("status").getValue().toString();

                                            requestViewHolder.userName.setText(requestSenderName);
                                            requestViewHolder.userStatus.setText(requestSenderStatus);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_user_layout, parent, false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return viewHolder;
            }
        };

        requestList.setAdapter(adapter);
        adapter.startListening();

    }


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
