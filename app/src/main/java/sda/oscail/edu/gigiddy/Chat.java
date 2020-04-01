package sda.oscail.edu.gigiddy;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass. ref: https://www.youtube.com/watch?v=h1XoOj6-mmk&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=17
 */
public class Chat extends Fragment {
    private static final String TAG = "Chat";

    private ListView chatListView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> chatList = new ArrayList<>();

    private DatabaseReference dbRef;

    public Chat() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dbRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        chatListView = root.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, chatList);
        chatListView.setAdapter(arrayAdapter);

        displayChatGroups();

        return root;
    }

    // ref: https://www.youtube.com/watch?v=h1XoOj6-mmk&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=17
    private void displayChatGroups() {

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while(iterator.hasNext()) {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                chatList.clear();
                chatList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
