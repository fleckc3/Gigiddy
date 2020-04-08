package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

// ref: https://www.youtube.com/watch?v=UTTqzqX9oXQ&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=28
public class FindMembers extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView findFriendList;
    private DatabaseReference dbUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_members);

        //Initialise fields
        findFriendList = findViewById(R.id.find_friend_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(this));


        dbUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //set back button to mainactivity
        toolbar = findViewById(R.id.app_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Members");


    }

    // ref: https://www.youtube.com/watch?v=h19KeH2Z26I&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=30
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(dbUserRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, findMembersViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, findMembersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findMembersViewHolder findMembersViewHolder, final int i, @NonNull Contacts contacts) {
                findMembersViewHolder.userName.setText(contacts.getName());
                findMembersViewHolder.userStatus.setText(contacts.getStatus());


                Glide.with(findMembersViewHolder.profileImage.getContext())
                        .load(contacts.getImage())
                        .placeholder(R.drawable.profile_image)
                        .into(findMembersViewHolder.profileImage);

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

    public static class findMembersViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public findMembersViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }

    }
}
