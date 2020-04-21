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

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private List<GroupMessage> groupMessageList;

    public GroupMessageAdapter(List<GroupMessage> groupMessageList) {
        this.groupMessageList = groupMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView receiverMessageText, receiverUserName;
        public CircleImageView receiverProfileImage;
        public TextView senderMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverUserName = itemView.findViewById(R.id.receiver_user_name);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public GroupMessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull GroupMessageAdapter.MessageViewHolder holder, int position) {

        String currentSenderID = mAuth.getCurrentUser().getUid();
        GroupMessage messages = groupMessageList.get(position);

        String fromUserID = messages.getID();

        Log.d(TAG, "////////////////////////////--------------------- from user id " + fromUserID);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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

            }
        });

        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.receiverUserName.setVisibility(View.INVISIBLE);

        if(fromUserID.equals(currentSenderID)) {

            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
            holder.senderMessageText.setText(messages.getMessage());
            holder.senderMessageText.setVisibility(View.VISIBLE);

        //    holder.senderMessageDateTime.setText(messages.getDate() + " " + messages.getTime());

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

    @Override
    public int getItemCount() {
        return groupMessageList.size();
    }
}
