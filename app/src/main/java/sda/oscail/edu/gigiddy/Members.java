package sda.oscail.edu.gigiddy;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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


/**
 * A simple {@link Fragment} subclass.
 * ref: https://www.youtube.com/watch?v=FFHuYcB3YnU&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=39
 */
public class Members extends Fragment {

    private static final String TAG = "Members";
    private RecyclerView memberList;

    private DatabaseReference dbContactsRef, dbUsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public Members() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_members, container, false);

        // Initialise fields
        memberList = root.findViewById(R.id.member_list);
        memberList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialise firebase references
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        dbContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        dbUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();

        // queries the db for contacts associated with current auth user
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbContactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, MembersViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, MembersViewHolder>(options) {

            // retrieves the user info and binds them to the fields in each view in recycler view
            @Override
            protected void onBindViewHolder(@NonNull final MembersViewHolder membersViewHolder, int i, @NonNull Contacts contacts) {

                membersViewHolder.itemView.findViewById(R.id.chat_btn).setVisibility(View.VISIBLE);

                // gets the string ref of the user ID at each position 'i' in the list
                final String usersIDs = getRef(i).getKey();

                dbUsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userProfileImage = "";
                            if(dataSnapshot.hasChild("image")) {
                                userProfileImage = dataSnapshot.child("image").getValue().toString();
                                final String userProfileName = dataSnapshot.child("name").getValue().toString();
                                String userProfileStatus = dataSnapshot.child("status").getValue().toString();

                                membersViewHolder.userName.setText(userProfileName);
                                membersViewHolder.userStatus.setText(userProfileStatus);

                                Glide.with(membersViewHolder.profileImage.getContext())
                                        .load(userProfileImage)
                                        .placeholder(R.drawable.profile_image)
                                        .into(membersViewHolder.profileImage);

                                final String finalUserProfileImage1 = userProfileImage;
                                membersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d(TAG, "MEMBERS ////////////////////////////// ----------------------------------------- " + finalUserProfileImage1);
                                        Intent goToChatIntent = new Intent(getContext(), PrivateChat.class);
                                        goToChatIntent.putExtra("user_id", usersIDs);
                                        goToChatIntent.putExtra("user_name", userProfileName);
                                        goToChatIntent.putExtra("user_image", finalUserProfileImage1);
                                        startActivity(goToChatIntent);

                                    }
                                });

                            } else {

                                String userProfileStatus = dataSnapshot.child("status").getValue().toString();
                                final String userProfileName = dataSnapshot.child("name").getValue().toString();

                                membersViewHolder.userName.setText(userProfileName);
                                membersViewHolder.userStatus.setText(userProfileStatus);

                                final String finalUserProfileImage = userProfileImage;
                                membersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent goToChatIntent = new Intent(getContext(), PrivateChat.class);
                                        goToChatIntent.putExtra("user_id", usersIDs);
                                        goToChatIntent.putExtra("user_name", userProfileName);
                                        goToChatIntent.putExtra("user_image", finalUserProfileImage);
                                        startActivity(goToChatIntent);
                                    }
                                });


                            }

                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            // creates the container for each item in the recyclerview
            @NonNull
            @Override
            public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_user_layout, parent, false);
                MembersViewHolder viewHolder = new MembersViewHolder(view);
                return viewHolder;

            }
        };

        memberList.setAdapter(adapter);
        adapter.startListening();
    }

    // intialises the views inside the viewholder
    public static class MembersViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button chatBtn;

        public MembersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            chatBtn = itemView.findViewById(R.id.chat_btn);

        }
    }


    @Override
    public void onPause() {
        super.onPause();

    }
}

