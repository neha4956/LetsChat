 package com.example.letschat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


 public class FriendsFragment extends Fragment {
     private static final String TAG = "FriendsFragment";
     private RecyclerView mFriendList;
     private FirebaseUser current_user;
     private DatabaseReference databaseReference , mfriendRef;
     private FirebaseRecyclerAdapter adapter;
     private View mMainView;
     private Context ctx;
     private List<String> FriendList ;
     private RecyclerView.Adapter mAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_friends, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.keepSynced(true); ctx = container.getContext();
        mfriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");



        //Retreiving Friends in Friendlist array of string----------------------------------------------------------\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        if(current_user != null)
            mfriendRef.child(current_user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {};
                        Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator );
                        Log.i("asad", String.valueOf(map));
                        // FriendList.clear();
                        assert map != null;
                        FriendList = new ArrayList(map.keySet());
                        //Log.i(TAG, "onDataChange2: "+ FriendList.toString());

                        //Recycler view initialisation done here....
                        mFriendList =  mMainView.findViewById(R.id.friend_list);
                        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
                        mFriendList.setLayoutManager(new LinearLayoutManager(ctx));
                        mAdapter = new FriendsAdapter(FriendList,ctx);
                        mFriendList.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        return  mMainView;
    }


    public void  onStart() {
        super.onStart();

    }
     public static class FriendsViewHolder extends RecyclerView.ViewHolder {

         View mView;

         public FriendsViewHolder(View itemView) {
             super(itemView);

             mView = itemView;

         }
}}