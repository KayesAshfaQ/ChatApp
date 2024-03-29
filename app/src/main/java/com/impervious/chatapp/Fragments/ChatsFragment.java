package com.impervious.chatapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.impervious.chatapp.Adapter.UserAdapter;
import com.impervious.chatapp.Model.Chatlist;
import com.impervious.chatapp.Model.Users;
import com.impervious.chatapp.R;
import com.impervious.chatapp.notification.Token;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<Users> mUsers;

    private DatabaseReference reference;
    private FirebaseUser fUser;

    private List<Chatlist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        //show sender/receiver user in chatsFragment
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);

                }
                chatList();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //get and send token to FDB
        updateToken();

        return view;
    }

    //send token to Firebase Database
    private void updateToken() {

        //getToken
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }else {

                    // Get new FCM registration token
                    String token = task.getResult();

                    //send token to firebase database
                    if (fUser != null) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
                        Token token1 = new Token(token);
                        reference.child(fUser.getUid()).setValue(token1);

                    }

                }
            }
        });



    }


    //show users in chatList
    private void chatList() {

        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    Users users = dataSnapshot.getValue(Users.class);
                    for (Chatlist chatlist: usersList){
                        if (users.getUid().equals(chatlist.getId())){
                            mUsers.add(users);
                        }
                    }

                }
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}