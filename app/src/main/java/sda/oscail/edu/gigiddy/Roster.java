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

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
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
    private ArrayList<String> idList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Button setDates, chooseMember;
    private TextView chosenMemberTextView;
    private  int indexOfChosen;
    private CalendarView calendarView;
    private List<Calendar> selectedDates = new ArrayList<>();


    private DatabaseReference contactsRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String currentUID, memberChosen, memberChosenID;



    public Roster() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_roster, container, false);

        // intialise firebase refs
        mAuth = FirebaseAuth.getInstance();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUID = mAuth.getCurrentUser().getUid();

        // intialise fields
        chooseMember = root.findViewById(R.id.select_member);
        setDates = root.findViewById(R.id.set_dates);
        chosenMemberTextView = root.findViewById(R.id.member_chosen);
        calendarView = root.findViewById(R.id.calendarView);



        calendarView.showCurrentMonthPage();

        // adapter for alertdialog
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);

        getContacts();

        chooseMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                setDates.setVisibility(View.VISIBLE);
                setDates.setEnabled(true);
            }
        });

        setDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });



        return root;
    }




    // Alert dialog shows list of names to set dates for, click one to choose
    // ref: https://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
    private void showDialog() {

        // builds alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Member");

        // adapter populates dialog with contacts to choose from and allows user to choose by clicking an item
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // grabs member name and id chosen
                final String memberName = adapter.getItem(which);
                indexOfChosen = adapter.getPosition(memberName);
                Log.d(TAG, "/////////////////////--------------------------- membername: " + memberName);

                // Lets suer know who they have selected
                AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                builderInner.setMessage(memberName);
                builderInner.setTitle("You selected:");

                // user confirms selection
                builderInner.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // name of member chosen appears in view
                        memberChosen = memberName;
                        chosenMemberTextView.setText(memberChosen);
                        chosenMemberTextView.setVisibility(View.VISIBLE);

                        // chosen id saved to update db with dates
                        memberChosenID = idList.get(indexOfChosen);
                        Log.d(TAG, "/////////////////////////////////////----------------------------------- memberCHosenID: " + memberChosenID);
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builder.show();
    }


    private void showDatePickerDialog() {

        OnSelectDateListener listener = new OnSelectDateListener() {
            @Override
            public void onSelect(List<Calendar> calendars) {

            }
        };

        DatePickerBuilder builder = new DatePickerBuilder(getContext(), listener)
                .pickerType(CalendarView.MANY_DAYS_PICKER)
                .selectionColor(R.color.colorPrimary)
                .headerColor(R.color.colorPrimary);

        DatePicker datePicker = builder.build();
        datePicker.show();


    }

    // get the list of contacts user has and grabs there name to be used in the dialog above
    private void getContacts() {

        // get contacts ref of current user
        contactsRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    // Sets are populated with db data: names and corresponding ids of the contacts to the currentUID
                    final Set<String> setNameList = new HashSet<>();
                    final Set<String> setIDList = new HashSet<>();

                    // Creates itarable object of the contacts to the currentUId
                    Iterator iterator = dataSnapshot.getChildren().iterator();

                    // iterates over the children
                    // Grabs name and id of the contact and puts them in their sets
                    while(iterator.hasNext()) {
                        String id = ((DataSnapshot)iterator.next()).getKey();
                        String name = dataSnapshot.child(id).child("name").getValue().toString();
                        Log.d(TAG, "/////////////////////////////---------------------- name: " + name);
                        setNameList.add(name);
                        setIDList.add(id);
                    }

                    Log.d(TAG, "/////////////////////////////------------------ setNameList items: " + setNameList);
                    Log.d(TAG, "/////////////////////////////------------------ setIDList items: " + setIDList);

                    list.clear();
                    idList.clear();

                    // Array Lists created for alertdialog and chosenID
                    idList.addAll(setIDList);
                    list.addAll(setNameList);
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
