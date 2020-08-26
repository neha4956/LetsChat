package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private TextInputLayout mStatusedit;
    private Button mSavebtn;

    private ProgressDialog mProgress;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

       mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
       String current_uid=mCurrentUser.getUid();
        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar=findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value=getIntent().getStringExtra("status_value");


        mStatusedit= findViewById(R.id.status_Input);
        mSavebtn=findViewById(R.id.status_save_btn);

        mStatusedit.getEditText().setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please Wait");
                mProgress.show();


                String status = mStatusedit.getEditText().getText().toString();


                    mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgress.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "There is some erroe", Toast.LENGTH_LONG).show();
                            }
                        }

                    });

                }
        });

    }
}