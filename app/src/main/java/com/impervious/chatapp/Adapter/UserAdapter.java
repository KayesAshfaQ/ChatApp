package com.impervious.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.chatapp.MessageActivity;
import com.impervious.chatapp.Model.Chats;
import com.impervious.chatapp.Model.Users;
import com.impervious.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<Users> usersList;
    private boolean isChat;

    String theLastMessage;

    public UserAdapter(Context context, List<Users> usersList, boolean isChat) {
        this.context = context;
        this.usersList = usersList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);

    }



    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {

        Users users = usersList.get(position);

        //set name
        holder.username.setText(users.getName());

        //set img
        if (users.getImgUrl().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context).load(users.getImgUrl()).into(holder.profile_image);
        }

        //last_msg
        if (isChat){
            lastMessage(users.getUid(), holder.last_msg);
        }else {
            holder.last_msg.setVisibility(View.GONE);
        }

        //status
        if(isChat){
            if (users.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else {
                holder.img_off.setVisibility(View.VISIBLE);
                holder.img_on.setVisibility(View.GONE);
            }
        }else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId", users.getUid());
                context.startActivity(intent);
            }
        });


    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }


    //lastMessage
    private void lastMessage(String userId, MaterialTextView last_msg){

        theLastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    if (chats.getReceiver().equals(firebaseUser.getUid()) && chats.getSender().equals(userId) ||
                            chats.getReceiver().equals(userId) && chats.getSender().equals(firebaseUser.getUid())) {

                        theLastMessage = chats.getMessage();

                    }
                }

                switch (theLastMessage){

                    case "default":
                        last_msg.setText("No message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;

                }
                theLastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    //ViewHolder as Inner Class
    public class ViewHolder extends RecyclerView.ViewHolder{

        public MaterialTextView username;
        public CircleImageView profile_image;
        public CircleImageView img_on;
        public CircleImageView img_off;
        public MaterialTextView last_msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }
    }

}
