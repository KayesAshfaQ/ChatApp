package com.impervious.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.impervious.chatapp.Adapter.ChatAdapter;
import com.impervious.chatapp.Fragments.APIService;
import com.impervious.chatapp.Model.Chats;
import com.impervious.chatapp.Model.Users;
import com.impervious.chatapp.notification.Client;
import com.impervious.chatapp.notification.Data;
import com.impervious.chatapp.notification.MyResponse;
import com.impervious.chatapp.notification.Sender;
import com.impervious.chatapp.notification.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    ValueEventListener seenListener;
    private CircleImageView profile_image;
    private MaterialTextView user_name;
    private MaterialButton btn_send;
    private TextInputEditText txt_send;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Intent intent;
    private String userId;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Chats> chatList;
    private APIService apiService;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //set Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, MainActivity.class);

                startActivity(intent);
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //notification
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        //show receiver on toolbar
        profile_image = findViewById(R.id.profile_image);
        user_name = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        txt_send = findViewById(R.id.txt_send);

        intent = getIntent();
        userId = intent.getStringExtra("userId");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);
                user_name.setText(users.getName());

                if (users.getImgUrl().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(users.getImgUrl()).into(profile_image);
                }

                readMessage(firebaseUser.getUid(), userId, users.getImgUrl());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //send message
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;

                String message = txt_send.getText().toString();

                if (!message.equals("")) {
                    sendMessage(firebaseUser.getUid(), userId, message);
                    txt_send.setText("");
                }

            }
        });

        //showMessage
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MessageActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //seen status
        seenMessage(userId);


    }

    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> map = new HashMap<>();

        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("isSeen", false);

        databaseReference.child("Chats").push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    //save the receiver id on Chatlist
                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist");

                    chatRef.child(firebaseUser.getUid()).child(userId).child("id").setValue(userId);
                    chatRef.child(userId).child(firebaseUser.getUid()).child("id").setValue(firebaseUser.getUid());

                    Toast.makeText(MessageActivity.this, "done", Toast.LENGTH_SHORT).show();

                }
            }
        });


        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserName = snapshot.getValue(Users.class).getName();

                //send notification
                sendNotification(receiver, message, currentUserName);
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sendNotification(String receiver, String message, String username) {

        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokenRef.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher_round, username+": "+ message, "New Message", userId);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200){
                                if (response.body().success != 1){
                                    Toast.makeText(MessageActivity.this, "Notification send failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void readMessage(final String myID, final String userID, final String imgUrl) {
        chatList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Chats chats = snap.getValue(Chats.class);

                    if (chats.getSender().equals(myID) && chats.getReceiver().equals(userID)
                            || chats.getReceiver().equals(myID) && chats.getSender().equals(userID)) {

                        chatList.add(chats);



                    }

                }
                chatAdapter = new ChatAdapter(MessageActivity.this, chatList, imgUrl);
                recyclerView.setAdapter(chatAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //seen status
    private void seenMessage(String userId) {

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Chats chats = dataSnapshot.getValue(Chats.class);

                    if (chats.getReceiver().equals(firebaseUser.getUid()) && chats.getSender().equals(userId)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("isSeen", true);
                        dataSnapshot.getRef().updateChildren(map);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //set Status
    private void status(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);

        databaseReference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }

}