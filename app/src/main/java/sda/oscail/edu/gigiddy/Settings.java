package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

// reference https://www.youtube.com/watch?v=pI53o4r5vSo&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=13
public class Settings extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText username, userStatus;
    private CircleImageView userProfileImage;
    private Button btnHome;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        //Initialize fields
        updateAccountSettings = findViewById(R.id.update_settings);
        username = findViewById(R.id.set_username);
        userStatus = findViewById(R.id.set_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        btnHome = findViewById(R.id.btn_home);

        //update account settings
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMainActivity = new Intent(Settings.this, MainActivity.class);
                startActivity(toMainActivity);
            }
        });

        getUserInfo();
    }

    private void getUserInfo() {
        dbRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")) {
                            String getUsername = dataSnapshot.child("name").getValue().toString();
                            String getUserStatus = dataSnapshot.child("status").getValue().toString();
                            //String getUserImage = dataSnapshot.child("image").getValue().toString();

                            username.setText(getUsername);
                            userStatus.setText(getUserStatus);

                        } else {
                            Toast.makeText(Settings.this, "Update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateSettings() {
        String setUsername = username.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, "Please set your username...", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write a status...", Toast.LENGTH_SHORT).show();
        } else {

            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);
            dbRef.child("Users").child(currentUserID).setValue(profileMap)
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

        }
    }
}
