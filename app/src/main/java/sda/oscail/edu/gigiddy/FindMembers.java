package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The FindMembers activity implements a FirebaseRecyclerViewAdapter to get and display all the users who have created accounts with the app.
 * Their User information are then displayed using a custom layout. Clicking on an item will start that users Profile activity.
 *    - Adapted from: https://www.youtube.com/watch?v=UTTqzqX9oXQ&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=28
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 07/04/2020
 */
public class FindMembers extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView findFriendList;
    private DatabaseReference dbUserRef;

    /**
     * The onCreate() method sets the activity view and initialises the view objects
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_members);

        //Initialise fields
        findFriendList = findViewById(R.id.find_friend_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(this));

        // Firebase db ref to Users
        dbUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Toolbar initialised
        toolbar = findViewById(R.id.app_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Members");
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
     * The onStart() method implements the FirebaseRecyclerViewAdapter when the activity is started
     *
     * ref: https://www.youtube.com/watch?v=h19KeH2Z26I&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=30
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Firebase object queries Users db for all Users sing the Contacts model calss
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbUserRef, Contacts.class)
                .build();

        // Takes the options and passses them into the viewholder and binds the user information to the layout view objects
        FirebaseRecyclerAdapter<Contacts, findMembersViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, findMembersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findMembersViewHolder findMembersViewHolder, final int i, @NonNull Contacts contacts) {

                // gets username, profile image, and status of the users and passes them into the viewholder
                findMembersViewHolder.userName.setText(contacts.getName());
                findMembersViewHolder.userStatus.setText(contacts.getStatus());
                Glide.with(findMembersViewHolder.profileImage.getContext())
                        .load(contacts.getImage())
                        .placeholder(R.drawable.profile_image)
                        .into(findMembersViewHolder.profileImage);

                // OnClick calls the profile activity of the user who is clicked on
                findMembersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String goToUser = getRef(i).getKey();
                        Intent gotToProfile = new Intent(FindMembers.this, Profile.class);
                        gotToProfile.putExtra("user_id", goToUser);
                        startActivity(gotToProfile);
                    }
                });
            }

            // Inflates the custom layout for the viewholder
            @NonNull
            @Override
            public findMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_user_layout, parent, false);
                findMembersViewHolder viewHolder = new findMembersViewHolder(view);
                return viewHolder;
            }
        };
        findFriendList.setAdapter(adapter);
        adapter.startListening();
    }

    /**
     * The findMembersViewHolder class declares and intitialises the fields to be set by the adapter
     */
    public static class findMembersViewHolder extends RecyclerView.ViewHolder {

        // fields declared
        TextView userName, userStatus;
        CircleImageView profileImage;

        public findMembersViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialise fields
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}
