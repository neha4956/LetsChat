package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    // Views
    private TextView mName,mStatus,mFriendsCount;
    private ImageView mImage;
    private Button mSendFriendRequestBtn;
    private Button mDeclineFriendRequestBtn;
    private ProgressDialog mProgressDialog;
    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser currentUser;
    private DatabaseReference mRootDatabase;
    //constants
    private int mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String user_id=getIntent().getStringExtra("user_id2");



        //Views
        mName = (TextView) findViewById(R.id.profile_display_name);
        mStatus = (TextView) findViewById(R.id.profile_status_text_view);
        mImage = (ImageView) findViewById(R.id.profile_image_view);
        mFriendsCount =(TextView) findViewById(R.id.profile_total_friends);
        mSendFriendRequestBtn = (Button) findViewById(R.id.profile_send_request_btn);
        mDeclineFriendRequestBtn =(Button) findViewById(R.id.profile_decline_request_btn);
        // constants
        // 0 : not Friend request sent
        // 1 : Friend request sent
        // 2 : Received Friend Request
        mCurrentState =0;//0 is not friend

        //Progress Dialog
        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please Wait, while we load the user Data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        //Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mCurrentUserId = currentUser.getUid().toString();

        mRootDatabase =FirebaseDatabase.getInstance().getReference();
        mFriendsDatabase =  FirebaseDatabase.getInstance().getReference().child("friends");
        mFriendRequestDatabase =  FirebaseDatabase.getInstance().getReference().child("friend_req");
        mUsersDatabase =  FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        countFriends(mCurrentUserId);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mName.setText(dataSnapshot.child("name").getValue().toString());
                mStatus.setText(dataSnapshot.child("status").getValue().toString());
                Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.ic_person).into(mImage);

                //---------------Check friend  request is sent or not
                mFriendRequestDatabase.child(mCurrentUserId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(user_id)){
                                    String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                                    if(req_type.equals("received")){
                                        mDeclineFriendRequestBtn.setEnabled(true);
                                        mDeclineFriendRequestBtn.setVisibility(View.VISIBLE);
                                        mCurrentState =2;
                                        mSendFriendRequestBtn.setText("Accept Friend Request");
                                    }else { if(req_type.equals("sent")){
                                        mCurrentState=1;
                                        mSendFriendRequestBtn.setText("Cancel Friend Request");
                                    } }
                                }else {
                                    mFriendsDatabase.child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(user_id)){
                                                mCurrentState =3;
                                                mSendFriendRequestBtn.setText("Unfriend This Person");
                                            } }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }});
                                }
                                mProgressDialog.dismiss();
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { mProgressDialog.dismiss(); }
                        }); }
            @Override
            public void onCancelled(DatabaseError databaseError) { mProgressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Loading User Data is Failed", Toast.LENGTH_SHORT).show();
            }
        });
        //------------click listner on Decline Friend Request ---------------------
        mDeclineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map map = new HashMap();
                map.put("friend_req/"+mCurrentUserId + "/" + user_id + "/request_type",null);
                map.put("friend_req/"+user_id + "/" + mCurrentUserId + "/request_type",null);
                mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        mSendFriendRequestBtn.setEnabled(true);
                        mCurrentState= 0;
                        mSendFriendRequestBtn.setText("Send Friend Request");
                        mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                        mDeclineFriendRequestBtn.setEnabled(false);
                    }
                });
            }
        });
        mSendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendFriendRequestBtn.setEnabled(false);
                //0: send request state
                if(mCurrentState==0){ sendFriendRequest(mCurrentUserId,user_id); }
                //1: cancel request state
                if(mCurrentState==1){ cancelFriendRequest(mCurrentUserId,user_id); }
                //2: Accept Friend Request
                if(mCurrentState==2){ acceptFriendRequest(mCurrentUserId,user_id); }
                //3: UnFriend This  person
                if(mCurrentState==3){ unFriendThisPerson(mCurrentUserId,user_id); }
            }
        });
    }
    private void unFriendThisPerson(final String mCurrentUserId,final String uid) {
        Map map = new HashMap();
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/date",null);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/date",null);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mSendFriendRequestBtn.setEnabled(true);
                mCurrentState=0;
                mSendFriendRequestBtn.setText("Send Friend request");
            }
        });
    }

    private void acceptFriendRequest(final String mCurrentUserId,final String uid) {
        final String date = DateFormat.getDateTimeInstance().format(new Date());
        Map map = new HashMap();
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/date",date);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/date",date);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/request_type",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/request_type",null);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mSendFriendRequestBtn.setEnabled(true);
                mCurrentState=3;
                mSendFriendRequestBtn.setText("Unfriend this Person");
                mDeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                mDeclineFriendRequestBtn.setEnabled(false);
            }
        });
    }

    private void cancelFriendRequest(final String mCurrentUserId,final String uid) {
        Map map = new HashMap();
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/request_type",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/request_type",null);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mSendFriendRequestBtn.setEnabled(true);
                mCurrentState= 0;//1 for freindRequestSent
                mSendFriendRequestBtn.setText("Send Friend Request");
            }
        });
    }

    private void sendFriendRequest(final String mCurrentUserId,final String uid) {
        String key= mRootDatabase.child("notification").child(uid).push().getKey().toString();
        HashMap<String,String> notificationData = new HashMap<String, String>();
        notificationData.put("from",mCurrentUserId);
        notificationData.put("type","request");
        Map map = new HashMap();
        map.put("friend_req/"+ mCurrentUserId + "/" + uid + "/request_type","sent");
        map.put("friend_req/"+ uid + "/" + mCurrentUserId + "/request_type","received");
        //map.put("notification/" + uid +"/" + key ,notificationData);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mSendFriendRequestBtn.setEnabled(true);
                mCurrentState= 1;//1 for freindRequestSent
                mSendFriendRequestBtn.setText("Cancel Friend Request");
            }

        });
    }
    private void countFriends(String mCurrentUserID){
        mFriendsDatabase.child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count=dataSnapshot.getChildrenCount();
                String countFriend = "Total Friend "+count;
                mFriendsCount.setText(countFriend);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        LapitChat.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LapitChat.activityPaused();
    }
}