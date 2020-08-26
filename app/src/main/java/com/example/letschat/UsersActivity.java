package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolabr;
    private RecyclerView mUsersList;
    RecyclerView.Adapter mAdapter;
    private ArrayList<Users> AllUsers;

    private DatabaseReference mUsersDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mToolabr=findViewById(R.id.users_appBar);
        setSupportActionBar(mToolabr);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AllUsers = new ArrayList<>();


        mUsersList=findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UsersAdapter(AllUsers,UsersActivity.this);
        mUsersList.setAdapter(mAdapter);

        FirebaseDatabase.getInstance().getReference("/Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    AllUsers.clear();

                    for(DataSnapshot dataSnapshot:snapshot.getChildren())
                    {

                        Users currUser = dataSnapshot.getValue(Users.class);
                        AllUsers.add(currUser);
                    }
                    mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });







    }




 // public class UsersViewHolder extends RecyclerView.ViewHolder {
  //     View mView;
  //    public UsersViewHolder(@NonNull View itemView) {
  //      super(itemView);
  //       mView=itemView; }
//      public  void setName(String name){
//          TextView userNameView=(TextView) mView.findViewById(R.id.user_single_name);
//          userNameView.setText(name);
//
//      }
 //}
}