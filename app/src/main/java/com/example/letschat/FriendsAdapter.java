package com.example.letschat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;
import static com.bumptech.glide.Glide.*;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<String> mFriendList ;

    DatabaseReference mDataRef,mFriendRef;
    FirebaseUser currUser;
    Context ctx;
    public FriendsAdapter(List<String> friendList, Context c) {
        mFriendList = friendList;
        ctx = c;
    }



    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsAdapter.ViewHolder holder, final int position) {


        mDataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        currUser = FirebaseAuth.getInstance().getCurrentUser();
        final String user1 = currUser.getUid();
        final String user2 = mFriendList.get(position);
        Log.d("onBindViewHolder: ",  user2);


        mDataRef.child(user2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                {
                    Users sec_user = dataSnapshot.getValue(Users.class);
                    TextView tv =holder. mView.findViewById(R.id.user_single_name);
                    TextView tv2 =holder. mView.findViewById(R.id.user_single_status);
                    tv.setText(sec_user.getName());
                    tv2.setText(sec_user.getStatus());
                    if(dataSnapshot.hasChild("online")) {


                        String userOnline =  dataSnapshot.child("online").getValue().toString();

                        ImageView userOnlineView = holder.mView.findViewById(R.id.user_single_online_icon);
                        if (userOnline.equals("true")) {
                            userOnlineView.setVisibility(View.VISIBLE);
                        } else {
                            userOnlineView.setVisibility(View.INVISIBLE);
                        }
                    }
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CharSequence options[]=new CharSequence[]{"Open Profile", "Send Message"};
                            AlertDialog.Builder builder=new AlertDialog.Builder(ctx);
                            builder.setTitle("Select Options");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(i ==0){
                                        Intent intent = new Intent(ctx, ProfileActivity.class);
                                        intent.putExtra("user_id2", user2);
                                        ctx.startActivity(intent);
                                    }if(i==1){
                                        Intent intent = new Intent(ctx, ChatActivity.class);
                                        intent.putExtra("user_id2", user2);
                                        ctx.startActivity(intent);
                                    }

                                }
                            });
                            builder.show();
                        }
                    });

                    String image;
                    if(!sec_user.getThumb_image().equals("default")) image = sec_user.getThumb_image();
                    else image =sec_user.getImage();

                    CircleImageView circleImageView = holder.mView.findViewById(R.id.user_single_image);
                   Glide.with(ctx)
                            .load(image)

                            .placeholder(R.drawable.ic_person)
                            .into(circleImageView);




                    circleImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ctx, ProfileActivity.class);
                            intent.putExtra("user_id2",user2);
                            ctx.startActivity(intent);

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mFriendList.get(position)).equals(currUser.getUid())) {

                    Intent intent = new Intent(ctx, ProfileActivity.class);
                    intent.putExtra("user_id2", user2);

                    ctx.startActivity(intent);

                } else {

                    Intent intent = new Intent(ctx, SettingsActivity.class);
                    ctx.startActivity(intent);
                }

            }
        });





    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

    }


}
