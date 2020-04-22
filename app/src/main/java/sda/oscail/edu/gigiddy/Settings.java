package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

// reference https://www.youtube.com/watch?v=pI53o4r5vSo&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=13
public class Settings extends AppCompatActivity {

    private static final String TAG = "Settings";
    private Button updateAccountSettings;
    private EditText username, userStatus;
    private CircleImageView userProfileImage;
    private Button btnHome;
    private Toolbar toolbar;
    private TextView setImageText;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private String imageUrl, setUserType;
    private static String fromActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Firebase and db references
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        //Initialize fields
        updateAccountSettings = findViewById(R.id.update_settings);
        username = findViewById(R.id.set_username);
        userStatus = findViewById(R.id.set_status);
        userProfileImage = findViewById(R.id.set_profile_image);

        // String value passed from signup activity to be checked in the following if statement
        fromActivity = getIntent().getExtras().get("from_activity").toString();
        Log.d(TAG, "///////////////// ------------- " +fromActivity);

        // If string variable equals regiser then previous activity was the signup activity
        if(fromActivity.equals("main") || fromActivity.equals("crop_image")) {

            toolbar = findViewById(R.id.setting_app_bar);
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");

        } else if(fromActivity.equals("register")) {
            btnHome = findViewById(R.id.btn_home);
            setImageText = findViewById(R.id.click_image);
            setImageText.setVisibility(View.VISIBLE);

            // go back to main activity btn
            btnHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent toMainActivity = new Intent(Settings.this, MainActivity.class);
                    startActivity(toMainActivity);
                }
            });
        }


        //update account settings
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        getUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getPicIntent = new Intent(Settings.this, SetProfileImage.class);
                startActivity(getPicIntent);
            }
        });
    }

    // goes back to fragment that calls this activity
    // ref: https://stackoverflow.com/questions/31491093/how-to-go-back-to-previous-fragment-from-activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(!fromActivity.equals("crop_image")) {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // retrieve user info from db and storage
    private void getUserInfo() {
        dbRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // checks db id the reference exists, three if conditions check in case snapshot doesn't have one of the vlaues
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status") && dataSnapshot.hasChild("image")) {

                            // Grabs users details
                            String getUsername = dataSnapshot.child("name").getValue().toString();
                            String getUserStatus = dataSnapshot.child("status").getValue().toString();
                            String getUserImage = dataSnapshot.child("image").getValue().toString();
                            imageUrl = getUserImage;

                            // sets the details in the views
                            Glide.with(getApplicationContext())
                                    .load(getUserImage)
                                    .into(userProfileImage);
                            username.setText(getUsername);
                            userStatus.setText(getUserStatus);

                        } else if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")) {

                            String getUsername = dataSnapshot.child("name").getValue().toString();
                            String getUserStatus = dataSnapshot.child("status").getValue().toString();
                            username.setText(getUsername);
                            userStatus.setText(getUserStatus);

                        }else if(dataSnapshot.exists() && dataSnapshot.hasChild("image")) {

                            String getUserImage = dataSnapshot.child("image").getValue().toString();
                            imageUrl = getUserImage;

                            Glide.with(Settings.this)
                                    .load(getUserImage)
                                    .into(userProfileImage);
                        } else {
                            Toast.makeText(Settings.this, "Update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    // update user info in db and storage
    private void updateSettings() {
        String setUsername = username.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(fromActivity.equals("main") || fromActivity.equals("crop_image")) {
            DatabaseReference currentUserType = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("user_type");
            currentUserType.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        setUserType = dataSnapshot.getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        Log.d(TAG, "////////////////////////////////--------------------- user type = " + setUserType);


        if(TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write a status...", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, "Please set your username...", Toast.LENGTH_SHORT).show();
        } else {

            HashMap<String, Object> profileMap = new HashMap<>();
            if(imageUrl == null) {
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/gigiddy-9e0c8.appspot.com/o/Profile%20Images%2Fprofile_image.png?alt=media&token=98a27baf-279f-4ba3-a34f-82308053aed3";
                profileMap.put("image", imageUrl);
            } else {
                profileMap.put("image", imageUrl);
            }

            if(fromActivity.equals("register")) {
                profileMap.put("user_type", "member");
            }

            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);


            dbRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(Settings.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = Objects.requireNonNull(task.getException()).toString();
                                Toast.makeText(Settings.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            if(fromActivity.equals("register")) {
                btnHome.setEnabled(true);
                btnHome.setVisibility(View.VISIBLE);
            }
        }


    }
}
