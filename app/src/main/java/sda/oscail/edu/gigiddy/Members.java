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
 * The Members fragment contains all the contacts a user has saved in the contacts DB.
 * Those 'friends' then are retrieved using a FirebaseRecyclerAdapter and the Contacts Model class.
 * Each contact is then displayed in a custom layout showing their name, status, and profile image.
 * A chat button is also present in each users layout. If the user clicks anywhere on a users layout
 * it will open up a 1:1 chat with that friend.
 *   - Adapted from: https://www.youtube.com/watch?v=FFHuYcB3YnU&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=39
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 05/04/2020
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

    /**
     * The onCreateView() method inflates the fragment view. Fields are then initialised which are used
     * in the FirebaseRecyclerAdapter.
     *     - Adapted from: https://www.youtube.com/playlist?list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj
     *
     * @param inflater inflates the fragment layout
     * @param container
     * @param savedInstanceState
     * @return
     */
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

    /**
     * The onStart() method implements the FirebaseRecyclerAdapter whenever the fragment is started.
     * Which makes sure the fragment is up to date with the most current information every time it
     * is accessed.
     */
    @Override
    public void onStart() {
        super.onStart();

        // queries the db for contacts associated with current auth user using the contacts model class
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbContactsRef, Contacts.class)
                .build();

        // Takes each option and formulates them into the custom RecyclerAdapter view
        FirebaseRecyclerAdapter<Contacts, MembersViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, MembersViewHolder>(options) {

            // retrieves the user info and binds them to the fields in each view in recycler view
            @Override
            protected void onBindViewHolder(@NonNull final MembersViewHolder membersViewHolder, int i, @NonNull Contacts contacts) {
                // chat btn visible
                membersViewHolder.itemView.findViewById(R.id.chat_btn).setVisibility(View.VISIBLE);

                // gets the string ref of the user ID at each position 'i' in the list
                // then references that ID in the Users DB to get the users information
                final String usersIDs = getRef(i).getKey();
                dbUsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userProfileImage = "";

                            // check user has image in DB, then gets the rest of the users information
                            if(dataSnapshot.hasChild("image")) {
                                userProfileImage = dataSnapshot.child("image").getValue().toString();
                                final String userProfileName = dataSnapshot.child("name").getValue().toString();
                                String userProfileStatus = dataSnapshot.child("status").getValue().toString();

                                // user information is now set in the correct views
                                membersViewHolder.userName.setText(userProfileName);
                                membersViewHolder.userStatus.setText(userProfileStatus);
                                Glide.with(membersViewHolder.profileImage.getContext())
                                        .load(userProfileImage)
                                        .placeholder(R.drawable.profile_image)
                                        .into(membersViewHolder.profileImage);

                                // User profile image copied over to this variable so that it can be passed to the private chat intent
                                final String finalUserProfileImage1 = userProfileImage;
                                membersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d(TAG, "MEMBERS ////////////////////////////// ----------------------------------------- " + finalUserProfileImage1);

                                        // Private chat intent is passed the users information to be used in that chat
                                        Intent goToChatIntent = new Intent(getContext(), PrivateChat.class);
                                        goToChatIntent.putExtra("user_id", usersIDs);
                                        goToChatIntent.putExtra("user_name", userProfileName);
                                        goToChatIntent.putExtra("user_image", finalUserProfileImage1);
                                        startActivity(goToChatIntent);
                                    }
                                });

                            // if no user image, get rest of the users info and set in the view layout
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
                        // handle db error here
                    }
                });
            }

            // creates the container for each item in the recyclerview using the custom layout
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

    /**
     * The MembersViewHolder class initialises the view objects inside the viewholder
     */
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

