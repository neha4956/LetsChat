package com.example.letschat;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


   public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mlistMessages;
       private DatabaseReference mUserDatabase;

       String mCurrentUser;
    Context mContext;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mlistMessages) {
        this.mlistMessages = mlistMessages;


    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        MessageViewHolder holder=  new MessageViewHolder(view);
        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int i) {




         Messages c=mlistMessages.get(i);
        //Layout Changing
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUser=currentUser.getUid();
        String from_user=c.getFrom();
        String message_type = c.getType();


        if(from_user!=null) {


            if (from_user.equals(mCurrentUser)) {
                holder.messageText.setBackgroundResource(R.drawable.message_send_background_layout);
                holder.messageText.setTextColor(Color.BLACK);

                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

                mUserDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.mDisplay_Name.setText(name);

                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person).into(holder.ProfileImage);



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            } else {


                holder.messageText.setText(c.getMessage());
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.WHITE);




            }
            holder.messageText.setText(c.getMessage());
            String timeAgo = GetTimeAgo.getTimeAgo(c.getTime(),mContext);
            holder.messageTime.setText(timeAgo);

        }

    }
    @Override
    public int getItemCount() {
        return mlistMessages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView ProfileImage;
        public TextView messageTime;
        public TextView mDisplay_Name;


        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            ProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            messageTime = (TextView) itemView.findViewById(R.id.time_text_layout);
            mDisplay_Name=itemView.findViewById(R.id.name_text_layout);
        }
    }

}
