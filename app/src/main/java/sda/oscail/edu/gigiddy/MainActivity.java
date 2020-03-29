package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;
    // View name of the header title. Used for activity scene transitions
    public static final String VIEW_NAME_HEADER_TITLE = "detail:header:title";
    ViewPager viewPager;


    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String mUsername;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();




        if (mUser == null) {
            // Not signed in, launch sign in activity
            Intent login = new Intent(this, Login.class);
            startActivity(login);
        } else {
            verifyUserExist();
        }

        //set the toolbar we have overridden
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Sets up the fragment views
        viewPager = findViewById(R.id.pager);
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, getApplicationContext());
        viewPager.setAdapter(adapter);

        //initialises the tab layout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void verifyUserExist() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        dbRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            private static final String TAG = "MainActivity";
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "|||||||||||||||||||||||||||||||||||||||||||  METHOD CALLED : ");
                if((dataSnapshot.child("name").exists())) {
                    Toast.makeText(MainActivity.this, "Welcome " + mUser.getEmail(), Toast.LENGTH_LONG).show();
                } else {
                    Intent toSettingsActivity = new Intent(MainActivity.this, Settings.class);
                    startActivity(toSettingsActivity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //reference https://www.youtube.com/watch?v=E-Ri7tK0E5I&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=11
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    // reference https://www.youtube.com/watch?v=E-Ri7tK0E5I&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=11
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.logout_option) {
            FirebaseAuth.getInstance().signOut();
            Intent toMain = new Intent(this, MainActivity.class);
            startActivity(toMain);
        }

        if(item.getItemId() == R.id.settings_option) {
            Intent toSettingsActivity = new Intent(this, Settings.class);
            startActivity(toSettingsActivity);
        }

        if(item.getItemId() == R.id.find_friends_option) {

        }

        return true;
    }
}
