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
 * The GroupMessageAdapter gets the messages for each group chat and displays them via custom layouts depending
 * on who the sender and receiver are.
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 21/04/2020
 *
 *   - ref: https://www.youtube.com/playlist?list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj
 */
public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private List<GroupMessage> groupMessageList;

    public GroupMessageAdapter(List<GroupMessage> groupMessageList) {
        this.groupMessageList = groupMessageList;
    }

    /**
     * The MessageViewHolder declares and initialises the view objects used in each
     * viewolder for each message in the group chat
     */
    public class MessageViewHolder extends RecyclerView.ViewHolder {

        // fields declared
        public TextView receiverMessageText, receiverUserName;
        public CircleImageView receiverProfileImage;
        public TextView senderMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // fields initialised
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverUserName = itemView.findViewById(R.id.receiver_user_name);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    /**
     * The onCreateVieholder() method inlfates the custom layout for the messages
     * @param parent is the viewgroup for the messages
     * @param viewType is the position of the messages
     * @return
     */
    @NonNull
    @Override
    public GroupMessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    /**
     * The onBindViewHolder() method binds the data from the group chat DB into the viewholder objects
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull GroupMessageAdapter.MessageViewHolder holder, int position) {

        // Gets the current user(sender) ID to be checked against the id of the current message being binded in the groupMessageList
        String currentSenderID = mAuth.getCurrentUser().getUid();
        GroupMessage messages = groupMessageList.get(position);
        String fromUserID = messages.getID();
        Log.d(TAG, "////////////////////////////--------------------- from user id " + fromUserID);

        // checks user info for fromUserID in the Users db
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // checks is they have a profile image and sets the image in the view
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
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

        // hides the receiver message fields
        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiverUserName.setVisibility(View.INVISIBLE);

        // checks if the fromUserID is equal to current user
        // If so this means messages need to be set in sender message field
        if(fromUserID.equals(currentSenderID)) {
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
            holder.senderMessageText.setText(messages.getMessage());
            holder.senderMessageText.setVisibility(View.VISIBLE);

        // If not then messages set in the reciever fields
        } else {
            holder.senderMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
            holder.receiverMessageText.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setText(messages.getMessage());
            holder.receiverUserName.setVisibility(View.VISIBLE);
            holder.receiverUserName.setText(messages.getName());
        }
    }

    // gets the amount of messages
    @Override
    public int getItemCount() {
        return groupMessageList.size();
    }
}
