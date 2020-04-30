package sda.oscail.edu.gigiddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.BundleCompat;
import androidx.core.util.Pair;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static androidx.annotation.InspectableProperty.ValueType.COLOR;

/**
 * The Login class serves as the gateway into Gigiddy. It also is the welcome screen.
 * Here the user can log back in. If they are anew user and try to login they will get an error toast message.
 * A link at the bottom lets the user know uf they are new to 'click here' to sign up. This takes them to the
 * Register activity.
 *    - Adapted from: https://www.youtube.com/playlist?list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 12/03/2020
 */
public class Login extends AppCompatActivity {
    private static final String TAG = "Login";

    EditText emailId, password;
    TextView register;
    Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialise the firebase instance
        //setup email and password input fields as variables
        mAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        register = findViewById(R.id.textView4);

        // clickable span of text at bottom
        // ref: https://stackoverflow.com/questions/10696986/how-to-set-the-part-of-the-text-view-is-clickable
        String bottomeText = "Not a member? Click to Signup!";
        register.setMovementMethod(LinkMovementMethod.getInstance());
        register.setText(bottomeText, TextView.BufferType.SPANNABLE);
        Spannable span = (Spannable) register.getText();

        // sets clickable text to specified format and opens login activity
        ClickableSpan clickSpan = new ClickableSpan() {
            // ref: https://stackoverflow.com/questions/16007147/how-to-get-rid-of-the-underline-in-a-spannable-string-with-a-clickable-object
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);    // this remove the underline
            }
            @Override
            public void onClick(@NonNull View widget) {
                Intent goToRegister = new Intent(Login.this, SignUp.class);
                startActivity(goToRegister);
            }
        };
        span.setSpan(clickSpan, 14, bottomeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 14, bottomeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        // Checks if user is logged in already or not
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser mUser = mAuth.getCurrentUser();

                if (mUser != null) {
                    Toast.makeText(Login.this, "You are logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(Login.this, "Please Login", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //Login button onClickListener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                String pwd = password.getText().toString();

                // if input fields empty checks
                if (email.isEmpty()) {
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();

                } else if (pwd.isEmpty()) {
                    password.setError("Please enter your password");
                    password.requestFocus();

                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(Login.this, "Fields Empty", Toast.LENGTH_SHORT).show();

                // if the fields are not empty log the user on and send to mainactivity
                } else if (!(email.isEmpty() && pwd.isEmpty())) {

                    // check db for email and password match
                    mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // if sinin unsuccessful alert the user
                            if (!task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login Error, Please login again", Toast.LENGTH_SHORT).show();

                            // if signin worked then go to mainactivity
                            } else {
                                Intent toMainActivity = new Intent(Login.this, MainActivity.class);
                                startActivity(toMainActivity);
                            }
                        }
                    });

                // alert user error occurred
                } else {
                    Toast.makeText(Login.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // checks auth state as soon as app opens
    @Override
    public void onStart(){
        super.onStart();

        mAuth.addAuthStateListener((mAuthStateListener));
    }
}
