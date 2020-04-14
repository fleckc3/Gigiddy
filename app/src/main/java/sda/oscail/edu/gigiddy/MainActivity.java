package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            Toast.makeText(MainActivity.this, "Welcome " + mUser.getEmail(), Toast.LENGTH_SHORT).show();

        }



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
            toSettingsActivity.putExtra("from_activity", "main");
            startActivity(toSettingsActivity);
        }

        if(item.getItemId() == R.id.find_friends_option) {
            Intent findMemberActivity = new Intent(MainActivity.this, FindMembers.class);
            startActivity(findMemberActivity);
        }

        if(item.getItemId() == R.id.requests_option) {
            Intent checkRequestsIntent = new Intent(MainActivity.this, CheckRequests.class);
            startActivity(checkRequestsIntent);
        }

        return true;
    }

}
