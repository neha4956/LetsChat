package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private ArrayList<Users> mUsers;
    private Context ctx;





    public UsersAdapter(ArrayList<Users> mUsers, Context ctx ) {
        this.mUsers = mUsers;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.users_single_layout, parent, false);

        return new UsersAdapter.ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView name= holder.itemView.findViewById(R.id.user_single_name);
        name.setText(mUsers.get(position).getName());
        TextView status=holder.itemView.findViewById(R.id.user_single_status);
        status.setText(mUsers.get(position).getStatus());
        CircleImageView image=holder.itemView.findViewById(R.id.user_single_image);
        String imageurl=mUsers.get(position).getThumb_image();
        if(imageurl.equals("default")) imageurl=mUsers.get(position).getImage();
        Glide.with(ctx).load(imageurl).placeholder(R.drawable.ic_person).into(image);

      final String user_id=mUsers.get(position).getUserId();

      holder.mView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent profileIntent=new Intent(ctx,ProfileActivity.class);
              profileIntent.putExtra("user_id2",user_id);
              ctx.startActivity(profileIntent);

          }
      });



    }







    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public static  class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }
    }
}
