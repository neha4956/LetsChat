package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private  String mChatUser;
    private Toolbar mChatToolbar;
     private DatabaseReference mRootRef;
      private TextView mTitleView;
       private  TextView mLastSeenView;
     private CircleImageView mImageView;
     private  Context ctx;
     private FirebaseAuth mAuth;
     private String mCurrentUserId;
     private ImageButton mChatAddButton;
     private ImageButton mChatSendButton;
     private EditText mChatMessageView;

     private RecyclerView mMessagesList;
     private SwipeRefreshLayout mRefreshLayout;

     private final  List <Messages> messagesList=new ArrayList<>();
     private LinearLayoutManager mLinearLayout;
       private  MessageAdapter mAdapter;
       private DatabaseReference mMessageDatabase;
       private static final int TOTAL_ITEM_TO_LOAD=10;
       private int mCurrentPage=1;

       private int itemPos=0;
       private  String mLastKey="";
       private String mPrevKey="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mChatToolbar=findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        mRootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();

        mChatUser=getIntent().getStringExtra("user_id2");

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        //--------custom Action Bar items------

        mTitleView=findViewById(R.id.custom_bar_titile);
        mLastSeenView=findViewById(R.id.custom_bar_seen);
        mImageView=findViewById(R.id.custom_bar_image);

        mChatAddButton=  findViewById(R.id.chat_add_btn);
        mChatSendButton=  findViewById(R.id.chat_send_btn);
        mChatMessageView =(EditText) findViewById(R.id.chat_message_view);
        mMessagesList=findViewById(R.id.messages_list);

        mRefreshLayout=findViewById(R.id.message_swipe_layout);

        mLinearLayout=new LinearLayoutManager(this);

        mAdapter=new MessageAdapter(messagesList);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
//        mMessagesList.smoothScrollToPosition(messagesList.size()-1);

        loadMessages();

        mRootRef.child("Users").child(mChatUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTitleView.setText(dataSnapshot.child("name").getValue().toString());
               if(dataSnapshot.hasChild("thumb_image")) {

                   Picasso.get().load(dataSnapshot.child("thumb_image").getValue().toString()).placeholder(R.drawable.ic_person).into(mImageView);
               }


               if(dataSnapshot.hasChild("online")) {


                   String online = dataSnapshot.child("online").getValue().toString();

                   if (online.equals("true")) {
                       mLastSeenView.setText("online");
                   } else {
                       GetTimeAgo getTimeAgo= new GetTimeAgo();
                       long lastSeen = Long.parseLong(online);
                       mLastSeenView.setText(getTimeAgo.getTimeAgo(lastSeen,getApplicationContext()).toString());

                   }
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatMap = new HashMap();
                    chatMap.put("chat/" + mCurrentUserId + "/" + mChatUser ,chatAddMap);
                    chatMap.put("chat/" + mChatUser + "/" + mCurrentUserId ,chatAddMap);

                    mRootRef.updateChildren(chatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){ }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();

            }
        });



    }

    private void loadMoreMessages() {

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                Messages message=dataSnapshot.getValue(Messages.class);

                String messageKey=dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }else{
                  mPrevKey=mLastKey;
                }

                if(itemPos==1){

                    mLastKey=messageKey;
                }


                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Messages message=dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    mLastKey=messageKey;
                    mPrevKey=messageKey;
                }

                messagesList.add(message);

                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);




            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshott) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage() {
        String message=mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){

            DatabaseReference user_message_push= mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id=user_message_push.getKey();


            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref =    "messages/" + mChatUser + "/" + mCurrentUserId;

            Map messageMap=new HashMap();
            messageMap.put("message" , message);
            messageMap.put("seen" , false);
            messageMap.put( "type" , "text");
            messageMap.put( "time" , ServerValue.TIMESTAMP);
            messageMap.put("from" ,mCurrentUserId);

            Map messageUserMap =  new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);
            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError !=  null){
                        Log.d("CHAT_LOG","Error  in sendig meassages");
                    }
                }
            });


        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        LapitChat.activityResumed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mRefreshLayout.setRefreshing(false);
        LapitChat.activityPaused();
    }

}