package com.example.kks.carpool.model;

import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kks.carpool.R;

import java.util.ArrayList;

import static com.example.kks.carpool.LoginSignup.Result.USER_NAME;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChattingMessageItem> messageData = new ArrayList<>();
    private String TARGET_ID;
    public static int people_count = 0;
    public static int noti_count = 1;

//    public ChattingAdapter(ArrayList<ChattingMessageItem> messageData) {
//        this.messageData = messageData;
//    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);

        return new ChattingRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ChattingRoomViewHolder) {
            final ChattingRoomViewHolder holder = (ChattingRoomViewHolder) viewHolder;

            if (messageData.get(position).name.equals(USER_NAME)) { // 내가 보낸거
                // 내 uid와 일치하는가 (말풍선 구분)
                holder.textView_Message.setText(messageData.get(position).message);
                holder.textView_Message.setGravity(Gravity.CENTER);

                // 오른쪽정렬
                holder.constraintLayout.setGravity(Gravity.RIGHT);
                holder.textView_time_right.setVisibility(View.GONE);
                holder.textView_time_left.setVisibility(View.VISIBLE);
                holder.textView_time_left.setText(messageData.get(position).time);

                if (position - 1 == -1) {
                    holder.textView_Message.setBackgroundResource(R.drawable.theme_chatroom_bubble_me_01_image);
                    holder.textView_Message.setTextColor(Color.WHITE);
                } else if (!messageData.get(position - 1).name.equals(USER_NAME)) {
                    holder.textView_Message.setTextColor(Color.WHITE);
                    holder.textView_Message.setBackgroundResource(R.drawable.theme_chatroom_bubble_me_01_image);
                } else if (messageData.get(position - 1).name.equals(USER_NAME)) {
                    holder.textView_Message.setTextColor(Color.WHITE);
                    holder.textView_Message.setBackgroundResource(R.drawable.theme_chatroom_bubble_me_02_image);

                    // 전에 보낸 메시지 시간과 일치할 경우
                    if (messageData.get(position).time.equals(messageData.get(position - 1).time)) {
//                        holder.textView_time_right.setVisibility(View.GONE);
                        messageData.get(position - 1).setTime("");
                    }
                }
                holder.textView_Message.setTextSize(15);

                // 내 메세지이기 때문에 프로필 필요없음 ( 이미지 / 이름 )
                holder.linearLayout_you.setVisibility(View.INVISIBLE);
                holder.textView_name.setVisibility(View.GONE);

//                setReadCount(position, holder.textView_readCounter_left);
            } else {
                // 불일치 (상대방)

                // 왼쪽 정렬
                holder.constraintLayout.setGravity(Gravity.LEFT);
                holder.textView_time_left.setVisibility(View.GONE);
                holder.textView_time_right.setVisibility(View.VISIBLE);
                holder.textView_time_right.setText(messageData.get(position).time);

//                Glide.with(holder.itemView.getContext()).load(YouUseritem.profileImageUrl).apply(new RequestOptions().circleCrop()).into(messageViewHolder.imageView_profile);
                // 사용자의 프로필 표시
                if (messageData.get(position).getProfile() != null) {
                    if (messageData.get(position).getProfile().contains("kakao")) { // 카카오 로그인
                        Uri img = Uri.parse(messageData.get(position).getProfile());
                        Glide.with(holder.itemView.getContext()).load(img).apply(new RequestOptions().circleCrop()).into(holder.imageView_profile);
                    } else if (messageData.get(position).getProfile().contains("http")) { // 페북 로그인
                        Uri img = Uri.parse("http://graph.facebook.com/" + messageData.get(position).getProfile() + "/picture?type=normal");
                        Glide.with(holder.itemView.getContext()).load(img).apply(new RequestOptions().circleCrop()).into(holder.imageView_profile);
                    } else { // 앱 로그인
                        Uri img = Uri.parse("http://54.180.95.149/uploadsMap/" + messageData.get(position).getProfile());
                        Glide.with(holder.itemView.getContext()).load(img).apply(new RequestOptions().circleCrop()).into(holder.imageView_profile);
                    }
                }

                holder.textView_name.setText(TARGET_ID);
                holder.linearLayout_you.setVisibility(View.VISIBLE);
                holder.textView_Message.setText(messageData.get(position).message);
                holder.textView_Message.setGravity(Gravity.CENTER);
                Log.e("ggg", "" + messageData.get(position).message);
                if (position - 1 == -1) {
                    messageData.get(position).setName(TARGET_ID);
                    holder.textView_Message.setBackgroundResource(R.drawable.theme_chatroom_bubble_you_01_image);
                    holder.textView_time_right.setVisibility(View.VISIBLE);

                } else if (!messageData.get(position - 1).name.equals(TARGET_ID) && !messageData.get(position - 1).name.equals("")) {
                    messageData.get(position).setName(TARGET_ID);
                    holder.textView_Message.setBackgroundResource(R.drawable.theme_chatroom_bubble_you_01_image);
                    holder.textView_time_right.setVisibility(View.VISIBLE);

                } else if (messageData.get(position - 1).name.equals(TARGET_ID) || messageData.get(position - 1).name.equals("")) {
                    //이미지 인비지블처리
                    holder.textView_name.setVisibility(View.GONE);
                    holder.imageView_profile.setVisibility(View.INVISIBLE);
                    messageData.get(position).setName("");
                    holder.textView_Message.setBackgroundResource(R.drawable.theme_chatroom_bubble_you_02_image);

                    // 전에 보낸 메시지 시간과 일치할 경우
                    if (messageData.get(position).time.equals(messageData.get(position - 1).time)) {
//                        holder.textView_time_right.setVisibility(View.GONE);
                        messageData.get(position - 1).setTime("");
                    }
                }
                holder.textView_Message.setTextSize(15);

//                // 읽은 사람 표시
//                setReadCount(position, holder.textView_readCounter_right);
            }
        }
    }

    void setReadCount(final int position, final TextView textView) {
        // 읽은 유저가 없을 경우
        if (people_count == 0) {
//            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
//                    people_count = users.size();
//                    int count = people_count - comments.get(position).readUsers.size();
//                    if (count > 0) {
//                        textView.setVisibility(View.VISIBLE);
//                        textView.setText(String.valueOf(count));
//                        noti_count = 1;
//                    } else {
//                        textView.setVisibility(View.GONE);
//                        noti_count = 0;
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
        } else { // 읽은 유저가 있을 경우
//            int count = people_count - comments.get(position).readUsers.size();
//            if (count > 0) {
//                textView.setVisibility(View.VISIBLE);
//                textView.setText(String.valueOf(count));
//                noti_count = 0;
//                Log.e("이 부분은 언제??", " 111111111111111!!!!");
//            } else {
//                textView.setVisibility(View.GONE);
//                noti_count = 0;
//                Log.e("이 부분은 언제??", " 22222222222222222222!!!!");
//            }
        }
    }

    @Override
    public int getItemCount() {
        return messageData.size();
    }

    public static class ChattingRoomViewHolder extends RecyclerView.ViewHolder {

        public TextView textView_Message;
        public TextView textView_name;
        public ImageView imageView_profile;
        public LinearLayout linearLayout_you;
        public LinearLayout constraintLayout;
        //        public TextView textView_Time;
        public TextView textView_time_left;
        public TextView textView_time_right;

        public ChattingRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_Message = itemView.findViewById(R.id.messageItem_textView_message);
            textView_name = itemView.findViewById(R.id.messageItem_textView_name);
            imageView_profile = itemView.findViewById(R.id.messageItem_ImageView_profile);
            linearLayout_you = itemView.findViewById(R.id.messageItem_linearlayout);
            constraintLayout = itemView.findViewById(R.id.messageItem_constraint);
//            textView_Time = itemView.findViewById(R.id.messageitem_textView_timestamp);
            textView_time_left = itemView.findViewById(R.id.messageItem_textView_readCount_left);
            textView_time_right = itemView.findViewById(R.id.messageItem_textView_readCount_right);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<ChattingMessageItem> itemLists, String TARGET_ID) {
        this.messageData = itemLists;
        this.TARGET_ID = TARGET_ID;
    }

    public void setLoadData(ArrayList<ChattingMessageItem> items) {
        this.messageData = items;
    }
}
