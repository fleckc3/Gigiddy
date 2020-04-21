package sda.oscail.edu.gigiddy;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private Button newGroupChatBtn;

    private DatabaseReference dbRef;

    public Chat() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // db ref to Groups
        dbRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialise fields
        chatListView = root.findViewById(R.id.list_view);
        newGroupChatBtn = root.findViewById(R.id.new_group_chat);
        arrayAdapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_list_item_1, chatList);
        chatListView.setAdapter(arrayAdapter);

        // gets groups saved in db
        displayChatGroups();

        // started the new chat group logic
        newGroupChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGroup();
            }
        });

        // ref: https://www.youtube.com/watch?v=vSe8oZu3xRg&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=19
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // gets chat name selected
                String currentChatName = parent.getItemAtPosition(position).toString();

                Intent chatIntent = new Intent(getContext(), GroupChat.class);
                chatIntent.putExtra("chat_name", currentChatName);
                startActivity(chatIntent);
            }
        });

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

    //https://www.youtube.com/watch?v=sgMO1AbUJmA&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=16
    private void startNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText setGroupName = new EditText(getContext());
        setGroupName.setHint("My family chat...");
        builder.setView(setGroupName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = setGroupName.getText().toString();

                if(TextUtils.isEmpty(groupName)) {
                    Toast.makeText(getContext(), "Please enter a group name...", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        dbRef.child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), groupName + " group created successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
