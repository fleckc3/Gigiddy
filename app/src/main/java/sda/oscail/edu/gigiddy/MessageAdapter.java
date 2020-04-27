package sda.oscail.edu.gigiddy;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * The MessageAdapter adapts the 1:1 messages saved between users in the Messages DB using a recycler view adapter.
 *    - Adapted from: ref: https://www.youtube.com/watch?v=n8QWeqeUeA0&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=50
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 14/04/2020
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    // Firebase variables declared
    private FirebaseAuth mAuth;
    private DatabaseReference dbUsersRef;
    private List<Messages> userMessagesList;

    // MessageAdpater constructor
    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    /**
     * The MessageViewholder class creates the container and view elements for each message retrieved
     * from the Messages DB. It declares and intialises the view objects used by the custom layout
     */
    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        // variables declared
        public TextView senderMessageText, receiverMessageText, receiverName;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // view objects initialised
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiverName = itemView.findViewById(R.id.receiver_name);
        }
    }

    /**
     * The onCreateViewHolder() method inflates the custom message layout and returns the view
     * @param parent holds the layout
     * @param viewType is the position of the view
     * @return
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    /**
     * The onBindViewHolder() method binds that data from the Firebase DB to the vieholder for each
     * item saved in the Messaged DB. Here the messages are checked to see whether they belong to
     * the current user or from someone else. Each condition displays the message and the users info
     * is different ways
     *
     * @param holder is used to set the views information
     * @param position is the posotion of the viewholder in the group
     */
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        // Gets current user id and the position of the current message in the list
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        // gets the fromUserId whos message it is and the message type
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        // Users db is then referenced using the fromUserID to get their information
        dbUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        dbUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // If user has a profile image, then get and set his info inn the viewholder
                if(dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();

                    holder.receiverName.setText(name);
                    Glide.with(holder.receiverProfileImage.getContext())
                            .load(receiverImage)
                            .placeholder(R.drawable.profile_image)
                            .into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle db error here
            }
        });

        // checks message type
        if(fromMessageType.equals("text")) {

            // checks if sender is equal to current user
            if(fromUserID.equals(messageSenderID)) {
                // If so then the receiver message fields go invisible
                holder.receiverMessageText.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.receiverName.setVisibility(View.INVISIBLE);

                // And the sender message fields are made visible and set with message
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage());

                Log.d(TAG, "////////////////////////// ------------------------ message is from the ME");

            // Else if not equal to current user then message belongs to someone else
            // layout is then made visible and displayed using the recever fields
            } else {
                holder.senderMessageText.setVisibility(View.INVISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverName.setText(messages.getFrom());
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverName.setVisibility(View.VISIBLE);

                Log.d(TAG, "////////////////////////////--------------------------- message is from the Other person");
            }
        }
    }

    /**
     * getItemCount() method gets the messageList size
     * @return
     */
    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
