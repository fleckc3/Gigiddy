package sda.oscail.edu.gigiddy;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * The Noticeboard fragment is the welcome screen for the Gigiddy app. Here the user is greeted with
 * their name and profile image along with a quick introduction on how to use the app.
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 10/03/2020
 */
public class Noticeboard extends Fragment {

    // View and firebase variables declared
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseUser mUser;
    private String currentUID, currentUserName;
    private CircleImageView userProfileImage;
    private TextView currentUserNameField, bodyText;

    public Noticeboard() {
        // Required empty public constructor
    }

    /**
     * The onCreateView() method inflates the fragment view.
     * @param inflater inflates the fragment layout
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_noticeboard, container, false);

        // Initialise fields
        currentUserNameField = root.findViewById(R.id.current_user_name);
        userProfileImage = root.findViewById(R.id.current_user_profile_image);

        // Initialise firebase auth and db references
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getUid().toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        getUserInfo();

        return root;
    }

    /**
     * The getUserInfo() method gets the current user information fromm the Users DB
     */
    private void getUserInfo() {

        // checks db at the current user ID reference
        userRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // ref: https://stackoverflow.com/questions/53693696/you-cannot-start-a-load-on-a-not-yet-attached-view-or-a-fragment-where-getactivi/53693826
                if(isAdded()) {
                    // if the current user is in db and has a name and image then grabs those values
                    if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String profileImage = dataSnapshot.child("image").getValue().toString();

                        // set the users name and image in their views
                        currentUserNameField.setText(name);
                        Glide.with(getActivity())
                                .load(profileImage)
                                .placeholder(R.drawable.profile_image)
                                .into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle db error here
            }
        });
    }
}
