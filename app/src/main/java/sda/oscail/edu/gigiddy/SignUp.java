package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    private static final String TAG = "SignUp";

    EditText emailId, password;
    TextView login;
    Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Initialise the firebase instance
        //setup email and password input fields as variables
        mAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btn_register);
        login = findViewById(R.id.textView4);

        dbRef = FirebaseDatabase.getInstance().getReference();


        // clickable span of text at bottom
        // ref: https://stackoverflow.com/questions/10696986/how-to-set-the-part-of-the-text-view-is-clickable
        String bottomeText = "Already a member? Click to Login!";

        login.setMovementMethod(LinkMovementMethod.getInstance());
        login.setText(bottomeText, TextView.BufferType.SPANNABLE);
        Spannable span = (Spannable) login.getText();
        ClickableSpan clickSpan = new ClickableSpan() {
            // ref: https://stackoverflow.com/questions/16007147/how-to-get-rid-of-the-underline-in-a-spannable-string-with-a-clickable-object
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);    // this remove the underline
            }
            @Override
            public void onClick(@NonNull View widget) {
                Intent goToLogin = new Intent(SignUp.this, Login.class);
                startActivity(goToLogin);
            }
        };
        span.setSpan(clickSpan, 18, bottomeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#EC008C")), 18, bottomeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                String pwd = password.getText().toString();

                if (email.isEmpty()) {
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();
                } else if (pwd.isEmpty()) {
                    password.setError("PLease enter your password");
                    password.requestFocus();
                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(SignUp.this, "Fields Are Empty", Toast.LENGTH_SHORT).show();
                } else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "Register Error, Please try again", Toast.LENGTH_SHORT).show();
                            } else {

                                //add user to db
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                dbRef.child("Users").child(currentUserID).setValue("");

                                Intent toSettingsActivity = new Intent(SignUp.this, Settings.class);
                                toSettingsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                toSettingsActivity.putExtra("from_activity", "register");
                                startActivity(toSettingsActivity);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignUp.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
