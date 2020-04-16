package sda.oscail.edu.gigiddy;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class Roster extends Fragment {


    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> memberList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private DatabaseReference contactsRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String currentUID;



    public Roster() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_roster, container, false);

        mAuth = FirebaseAuth.getInstance();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUID = mAuth.getCurrentUser().getUid();


        getContacts();


        showDialog();




        return root;
    }



    private void showDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Member");


        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String memberID = adapter.getItem(which);

                Log.d(TAG, "/////////////////////--------------------------- memberID: " + memberID);

                AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                builderInner.setMessage(memberID);
                builderInner.setTitle("You selected:");
                builderInner.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builder.show();

    }

    private void getContacts() {

        contactsRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    final Set<String> set = new HashSet<>();
                    Iterator iterator = dataSnapshot.getChildren().iterator();

                    while(iterator.hasNext()) {

                        set.add(((DataSnapshot)iterator.next()).getKey());
                    }



                    Log.d(TAG, "/////////////////////////////------------------ set items: " + set);

                    list.clear();
                    list.addAll(set);
                    Log.d(TAG, "////////////////////// -------------------- array list items: " + list);
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




}
