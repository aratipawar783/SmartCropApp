package com.example.smartcropapp.merchant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; import android.widget.ImageView;
import android.widget.TextView; import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcropapp.R;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.VH> {
    Context ctx; List<Post> list; SocialDBHelper db; long me;
    public PostAdapter(Context c, List<Post> l, SocialDBHelper db, long me){ this.ctx=c; this.list=l; this.db=db; this.me=me; }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){ View v= LayoutInflater.from(ctx).inflate(R.layout.item_post,parent,false); return new VH(v); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Post p = list.get(pos);
        h.tvUserName.setText(p.userName==null?"":p.userName);
        h.tvLocation.setText(p.userLocation==null?"":p.userLocation);
        h.tvCaption.setText(p.caption==null?"":p.caption);
        h.tvPrice.setText(p.expectedPrice==null?"":("₹ "+p.expectedPrice));
        if(p.userPhoto!=null) try{ h.imgUser.setImageURI(Uri.parse(p.userPhoto)); }catch(Exception e){ h.imgUser.setImageResource(R.drawable.userprofile); } else h.imgUser.setImageResource(R.drawable.userprofile);
        if(p.imageUri!=null) try{ h.imgPost.setImageURI(Uri.parse(p.imageUri)); }catch(Exception e){ h.imgPost.setImageResource(R.drawable.userprofile); } else h.imgPost.setImageResource(R.drawable.userprofile);

        h.btnLike.setText(db.isLikedBy(p.id, me)?"Unlike":"Like");
        h.tvLikeCount.setText(db.getLikeCount(p.id) + " likes");
        h.btnLike.setOnClickListener(v->{
            if(db.isLikedBy(p.id, me)){ db.unlikePost(p.id, me); h.btnLike.setText("Like"); }
            else{ db.likePost(p.id, me); h.btnLike.setText("Unlike"); }
            h.tvLikeCount.setText(db.getLikeCount(p.id) + " likes");
        });
        h.tvLikeCount.setOnClickListener(v->{
            Intent i = new Intent(ctx, LikesActivity.class); i.putExtra("postId", p.id); ctx.startActivity(i);
        });

        h.btnComment.setOnClickListener(v-> { Intent i = new Intent(ctx, CommentsActivity.class); i.putExtra("postId", p.id); ctx.startActivity(i); });

        h.btnSend.setOnClickListener(v->{
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            String text = p.userName + " posted: " + p.caption + "\nPrice: " + p.expectedPrice;
            share.putExtra(Intent.EXTRA_TEXT, text);
            ctx.startActivity(Intent.createChooser(share, "Share post via"));
        });

        h.btnMessage.setOnClickListener(v->{
            // open MessagesActivity
            Intent i = new Intent(ctx, MessageActivity.class);
            i.putExtra("meId", me);
            i.putExtra("otherId", p.userId);
            ctx.startActivity(i);
        });

        h.btnCall.setOnClickListener(v->{
            if(p.userPhone==null || p.userPhone.isEmpty()){ Toast.makeText(ctx,"No phone available", Toast.LENGTH_SHORT).show(); return; }
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p.userPhone));
            ctx.startActivity(dial);
        });

        boolean following = db.isFollowing(p.userId, me);
        h.btnFollow.setText(following?"Connected":"Connect");
        h.btnFollow.setOnClickListener(v->{
            if(db.isFollowing(p.userId, me)){ db.unfollow(p.userId, me); h.btnFollow.setText("Connect"); }
            else{ db.follow(p.userId, me); h.btnFollow.setText("Connected"); }
        });
    }
    @Override public int getItemCount(){ return list.size(); }
    static class VH extends RecyclerView.ViewHolder {
        ImageView imgUser, imgPost; TextView tvUserName, tvLocation, tvCaption, tvPrice, tvLikeCount; Button btnLike, btnComment, btnSend, btnMessage, btnCall, btnFollow;
        public VH(View v){
            super(v);
            imgUser=v.findViewById(R.id.imgUser); imgPost=v.findViewById(R.id.imgPost);
            tvUserName=v.findViewById(R.id.tvUserName); tvLocation=v.findViewById(R.id.tvLocation);
            tvCaption=v.findViewById(R.id.tvCaption); tvPrice=v.findViewById(R.id.tvPrice); tvLikeCount=v.findViewById(R.id.tvLikeCount);
            btnLike=v.findViewById(R.id.btnLike); btnComment=v.findViewById(R.id.btnComment); btnSend=v.findViewById(R.id.btnSend);
            btnMessage=v.findViewById(R.id.btnMessage); btnCall=v.findViewById(R.id.btnCall); btnFollow=v.findViewById(R.id.btnConnect);
        }
    }
}
