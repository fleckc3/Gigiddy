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
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;

// ref: https://www.youtube.com/watch?v=n8QWeqeUeA0&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=50
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference dbUsersRef;
    private List<Messages> userMessagesList;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText, receiverName;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiverName = itemView.findViewById(R.id.receiver_name);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        dbUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        dbUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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

            }
        });



        // checks message type
        if(fromMessageType.equals("text")) {

            // checks who sender is and updates the view to reflect correct layout and design
            if(fromUserID.equals(messageSenderID)) {
                holder.receiverMessageText.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.receiverName.setVisibility(View.INVISIBLE);

                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage());

                Log.d(TAG, "////////////////////////// ------------------------ message is from the ME");

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


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
