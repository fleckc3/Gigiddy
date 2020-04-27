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
 * The Chat fragment view houses a button at the top that is used to create new group chats.
 * Group chats are saved in the Groups DB. This fragment consists of a recyclerVIew that uses the GroupMessageAdapter
 * to display the different group chats saved in the DB. Clicking on the individual group chat name starts the GroupChat activity for that chat.
 *   - Adapted from: https://www.youtube.com/watch?v=h1XoOj6-mmk&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=17
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 01/04/2020
 */
public class Chat extends Fragment {
    private static final String TAG = "Chat";

    // Declares the variables needed to shwo the group chats saved in Groups DB
    private ListView chatListView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> chatList = new ArrayList<>();
    private Button newGroupChatBtn;

    // Firebase DB reference for Groups
    private DatabaseReference dbRef;

    // Required empty constructor
    public Chat() {
        // Required empty public constructor
    }

    /**
     * This method inflates the fragment view and initialises the view objects.
     * @param inflater inflates the fragment xml layout
     * @param container
     * @param savedInstanceState
     * @return
     */
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

        // OnCLick button calls the new chat group logic to create a new chat group via an alert dialog
        newGroupChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGroup();
            }
        });

        // OnCLick starts the GroupChat Activity associated with the item clicked
        // ref: https://www.youtube.com/watch?v=vSe8oZu3xRg&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=19
        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // gets chat name selected
                String currentChatName = parent.getItemAtPosition(position).toString();

                // Starts the GroupChat activity with the currentChatName
                Intent chatIntent = new Intent(getContext(), GroupChat.class);
                chatIntent.putExtra("chat_name", currentChatName);
                startActivity(chatIntent);
            }
        });

        return root;
    }

    /**
     * The displayChatGroups() method checks the Groups DB and grabs all the Group chats saved there.
     * The groups are then added to the chatList which is then adapted to the fragment recyclerView via the arrayAdapter.
     *
     * ref: https://www.youtube.com/watch?v=h1XoOj6-mmk&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=17
     */
    private void displayChatGroups() {

        // checks the db reference for new groups
        // DB snapshot is iterated over and the group chats are added to the chatList to be displayed via the arrayAdapter
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
                // handle canceled error here
            }
        });
    }

    /**
     * The startNewGroup() method creates an alert dialog that allows the user the to type a name for a new chat.
     * Upon clicking ok the group name entered is then passed to the createNewGroup() method.
     *
     *  ref: https://www.youtube.com/watch?v=sgMO1AbUJmA&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=16
     */
    private void startNewGroup() {

        // Alert dialog created with Title telling user to enter a group name.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");
        final EditText setGroupName = new EditText(getContext());
        setGroupName.setHint("My family chat...");
        builder.setView(setGroupName);

        // OnClick listener for the ok button in alert dialog. Sets the groupName and passes to createNewGroup() method
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = setGroupName.getText().toString();

                // if no name hs been entered alert user...
                if(TextUtils.isEmpty(groupName)) {
                    Toast.makeText(getContext(), "Please enter a group name...", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(groupName);
                }
            }
        });

        // cancels the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * The createNewGroup() method takes the groupName passed to it from the dialog and creates the group in the Groups DB
     * @param groupName is passed from the startNewGroup() method and contains the name of the group
     */
    private void createNewGroup(final String groupName) {

        // creates group with set name in the Groups DB
        dbRef.child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // if Group chat created, user alerted of its success
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), groupName + " group created successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
