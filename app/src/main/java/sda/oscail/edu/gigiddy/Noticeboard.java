package sda.oscail.edu.gigiddy;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class Noticeboard extends Fragment {


    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public Noticeboard() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_noticeboard, container, false);



        Button logout = root.findViewById(R.id.btn_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent toMain = new Intent(getContext(), MainActivity.class);
                startActivity(toMain);
            }
        });

        return root;
    }

}
