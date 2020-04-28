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

/**
 * The SignUp class serves as the gateway into Gigiddy for first time users. Their username and password
 * are saved in the db for authentication. The user is then taken to the settings activity for onboarding.
 *    - Adapted from: https://www.youtube.com/playlist?list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 12/03/2020
 */
public class SignUp extends AppCompatActivity {
    private static final String TAG = "SignUp";

    // username, pwd, firebase variables declared
    EditText emailId, password;
    TextView login;
    Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    /**
     * The oncReate() method creates the singup activity and initialises the view objects along with any other
     * logic to be used with authentication or signup.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Initialise the firebase instance and db ref
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        //setup email and password input fields as variables
        emailId = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btn_register);
        login = findViewById(R.id.textView4);

        // clickable span of text at bottom
        // ref: https://stackoverflow.com/questions/10696986/how-to-set-the-part-of-the-text-view-is-clickable
        String bottomeText = "Already a member? Click to Login!";
        login.setMovementMethod(LinkMovementMethod.getInstance());
        login.setText(bottomeText, TextView.BufferType.SPANNABLE);
        Spannable span = (Spannable) login.getText();

        // sets clickable text to specified format and opens login activity
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
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 18, bottomeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Register onclick logic and send to user onboarding
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                String pwd = password.getText().toString();

                // check for empty email
                if (email.isEmpty()) {
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();

                // Check for empty password field
                } else if (pwd.isEmpty()) {
                    password.setError("PLease enter your password");
                    password.requestFocus();

                // Checks for both fields empty
                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(SignUp.this, "Fields Are Empty", Toast.LENGTH_SHORT).show();

                // If they arent empty Then Add user email and password to firebase
                } else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // If not added successfully....Alert user
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "Register Error, Please try again", Toast.LENGTH_SHORT).show();

                            // ... if task succesful then add info to user to db
                            } else {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                dbRef.child("Users").child(currentUserID).setValue("");

                                // send user to onboarding activity: settings
                                Intent toSettingsActivity = new Intent(SignUp.this, Settings.class);
                                toSettingsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                toSettingsActivity.putExtra("from_activity", "register");
                                startActivity(toSettingsActivity);
                                finish();
                            }
                        }
                    });

                // alert user if error
                } else {
                    Toast.makeText(SignUp.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
