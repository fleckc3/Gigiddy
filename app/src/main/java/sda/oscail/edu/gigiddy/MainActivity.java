package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * The MainActivity class contains the bulk of the app. Here the 4 fragments are adapted in
 * the view with tabs.
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 10/03/2020
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT = 1;
    private ViewPager viewPager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mUser;
    private DatabaseReference dbRef;

    /**
     * The onCreate() method initilaises the main activity view which contains
     * four tabbed fragments and a toolbar. This activity also check the authenticated status of the user.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase auth and db references
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null) {
                    Intent login = new Intent(MainActivity.this, Login.class);
                    startActivity(login);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Welcome " + mUser.getEmail(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        //set the toolbar we have overridden
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //Sets up the fragment views
        viewPager = findViewById(R.id.pager);
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, getApplicationContext());
        viewPager.setAdapter(adapter);

        //initialises the tab layout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    //reference https://www.youtube.com/watch?v=E-Ri7tK0E5I&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=11
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // inflates the menu layout
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    /**
     * The onOptionsItemSelected() method is called when the user clicks on the three dot button in the toolbar.
     * Depending on the user's selection it will start an activity related to the selection.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        // If logout pressed then sign user out and send to Login activity
        if(item.getItemId() == R.id.logout_option) {
            FirebaseAuth.getInstance().signOut();
            Intent toMain = new Intent(this, Login.class);
            startActivity(toMain);
        }

        // if settings option pressed then send user to settings activity
        if(item.getItemId() == R.id.settings_option) {
            Intent toSettingsActivity = new Intent(this, Settings.class);
            toSettingsActivity.putExtra("from_activity", "main");
            startActivity(toSettingsActivity);
        }

        // if find friends option selected send user to find friends activity
        if(item.getItemId() == R.id.find_friends_option) {
            Intent findMemberActivity = new Intent(MainActivity.this, FindMembers.class);
            startActivity(findMemberActivity);
        }

        // if check requests option selected send user to check requests activity
        if(item.getItemId() == R.id.requests_option) {
            Intent checkRequestsIntent = new Intent(MainActivity.this, CheckRequests.class);
            startActivity(checkRequestsIntent);
        }
        return true;
    }
}
