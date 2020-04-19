package sda.oscail.edu.gigiddy;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.Edits;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class Roster extends Fragment {


    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> idList = new ArrayList<>();
    private ArrayList<String> gigList = new ArrayList<>();
    List<EventDay> events = new ArrayList<>();
    private ArrayAdapter<String> adapter, gigAdapter;
    private Button setDates, chooseMember;
    private TextView chosenMemberTextView;
    private  int indexOfChosen;
    private CalendarView calendarView;
    private List<Calendar> selectedDates = new ArrayList<>();


    private DatabaseReference contactsRef, rootRef, rosterRef, currentUserRef;
    private FirebaseAuth mAuth;
    private String currentUID, memberChosen, memberChosenID, chosenGigAndTime;



    public Roster() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_roster, container, false);

        // intialise firebase refs
        mAuth = FirebaseAuth.getInstance();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        rootRef = FirebaseDatabase.getInstance().getReference();
        rosterRef = FirebaseDatabase.getInstance().getReference().child("Roster");
        currentUID = mAuth.getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID).child("user_type");

        // intialise fields
        chooseMember = root.findViewById(R.id.select_member);
        setDates = root.findViewById(R.id.set_dates);
        chosenMemberTextView = root.findViewById(R.id.member_chosen);
        calendarView = root.findViewById(R.id.calendarView);

        // set calendar view to current month
        calendarView.showCurrentMonthPage();

        // adapter for alertDialog
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        gigAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, gigList);

        getContacts();

        Log.d(TAG, "///////////////////////////////////----------------------events data: "+ events);
        calendarView.setEvents(events);

        // checks user type for member
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // if db value exists...
                if(dataSnapshot.exists()) {
                    // ...and if the type = member then they cant set dates for other user
                    // hides the set dates functionality
                    if(dataSnapshot.getValue().toString().equals("member")) {
                        Log.d(TAG, "/////////////////////////////////------------------------- user type READ");

                        chooseMember.setEnabled(false);
                        chooseMember.setVisibility(View.INVISIBLE);
                        setDates.setVisibility(View.INVISIBLE);
                        setDates.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // select member to set dates for
        chooseMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        // select dates for chosen member
        setDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGigAndTime();
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkForDates();
    }

    private void checkForDates() {
        Log.d(TAG, "////////////////////////////////////////-----------------------------------------   getDatesSaved method was reached ");

        // gets the gig dates/locations/times the current user has been assigned in db
        rosterRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    // loops through the gig locations and gets dates assigned to gig at that location
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String id = snapshot.getKey();
                        Log.d(TAG, "onDataChange: --------------------------------------------------------------------" + id);

                        // gets all dates under specific gig location
                        DatabaseReference gigRef = rosterRef.child(currentUID).child(id);
                        gigRef.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {
                                    Iterator itr = dataSnapshot.getChildren().iterator();
                                    Set<String> dateSet = new HashSet<>();

                                    // iterates over the dates and sends them to setDates() method to set an icon in the calendar view
                                    while(itr.hasNext()) {
                                        String date = ((DataSnapshot)itr.next()).getValue().toString();
                                        Log.d(TAG, "onDataChange: ----------------------------------------------------------" + date);
                                        dateSet.add(date);
                                    }

                                    try {
                                        // sends sates for icons to be reflected in calendar view
                                        setDates(dateSet, id);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Receives dates retrieved from the db
    // convertxs them to calendar format
    // sets the dates as new events with an icon on the calendar view
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setDates(Set<String> dates, String id) throws ParseException {

        // loops over multiple dates and sets event with icon in calendar view
        for(String date : dates) {

            //converts date to calendar format
            String toConvert = date;
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date foramttedDate = sdf.parse(toConvert);
            Calendar cal = Calendar.getInstance();
            cal.setTime(foramttedDate);

            // sets events with icon in the calendarView
            events.add(new EventDay(cal, R.drawable.murrays_normal));
            calendarView.setEvents(events);
        }
        Log.d(TAG, "setDates: --------------------------------------------- " + events);
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

                // Lets user know who they have selected
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

                        if(!memberChosenID.isEmpty()) {
                            setDates.setVisibility(View.VISIBLE);
                            setDates.setEnabled(true);
                        }
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builder.show();
    }

    // ref: https://github.com/Applandeo/Material-Calendar-View
    // shows picker to select dates for specific member
    // Dates saved when saveDates() method invoked
    private void showDatePickerDialog() {

        OnSelectDateListener listener = new OnSelectDateListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSelect(List<Calendar> calendars) {
                selectedDates = calendars;
                saveDates();
            }
        };

        DatePickerBuilder builder = new DatePickerBuilder(getContext(), listener)
                .pickerType(CalendarView.MANY_DAYS_PICKER)
                .selectionColor(R.color.colorPrimary)
                .setTodayLabelColor(R.color.colorPrimary)
                .headerColor(R.color.colorPrimary);

        DatePicker datePicker = builder.build();
        datePicker.show();
    }

    // gets gig location and time data from db
    // puts the data into alert dialog to be selected by user
    private void selectGigAndTime() {

        // grabs the gig locations and time from db
        // sets them in a list for the alert dialog picker selection
        DatabaseReference gigRef = rootRef.child("Roster").child("Gigs");
        gigRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    final Set<String> gigNameSet = new HashSet<>();

                    // iterates over gigs in list and adds them to set
                    while (iterator.hasNext()) {
                        String id = ((DataSnapshot)iterator.next()).getKey();
                        String gigName = dataSnapshot.child(id).getValue().toString();
                        Log.d(TAG, "/////////////////////////////---------------------- gigName: " + gigName);
                        gigNameSet.add(gigName);
                    }

                    // gigList array updated with gigs
                    gigList.clear();
                    gigList.addAll(gigNameSet);

                    // adapter sets them in the alert dialog
                    gigAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // builds alert dialog
        // ref: https://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Gig & Time");

        builder.setAdapter(gigAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // gets the selected gig from user
                final String gigChosen = gigAdapter.getItem(which);

                // confirms to user their selection in another dialog
                AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                builderInner.setMessage(gigChosen);
                builderInner.setTitle("You selected:");

                // confirmed 'ok' clicked: gig location and time saved
                // dates are then selected by user in the showDatePickerDialog() method
                builderInner.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenGigAndTime = gigChosen;
                        showDatePickerDialog();
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builder.show();
    }

    // method saves dates selected in datepicker
    // ref: https://github.com/Applandeo/Material-Calendar-View
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void saveDates() {

        // hashmap to hold the key and date value
        HashMap<String, Object> rosterMap = new HashMap<>();
        Calendar cal;
        for(int i = 0; i < selectedDates.size(); i++) {

            // gets firebase unique key for each date saved
            DatabaseReference ref = rootRef.child("Roster").child(memberChosenID).child(chosenGigAndTime).push();

            //converts calendar dates selected to dd-mm-yyyy format
            cal = selectedDates.get(i);
            Date date = cal.getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String dateToSave = dateFormat.format(date);
            Log.d(TAG, "/////////////////////////////////-------------------------- date saved: " + dateToSave);

            // sets key and date in the map to be saved in db below
            rosterMap.put(ref.getKey(), dateToSave);
        }

        // saves dates to DB under the user id and the gig location/time
        rootRef.child("Roster").child(memberChosenID).child(chosenGigAndTime)
                .updateChildren(rosterMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // alerts user dates saved successfully
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "Dates successfully saved for " + memberChosen, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // get the list of contacts user has
    // grabs their name to be used in list to be selected in alert dialog above
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
