package com.example.smartcropapp.merchant;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
    Context ctx;
    List<Comment> list;

    public CommentAdapter(Context ctx, List<Comment> list){
        this.ctx = ctx;
        this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType){
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_comment,parent,false));
    }

    @Override
    public void onBindViewHolder(VH holder, int pos){
        Comment c = list.get(pos);
        holder.tvUser.setText(c.userName == null ? "" : c.userName);
        holder.tvText.setText(c.text == null ? "" : c.text);

        if(c.userPhoto != null) {
            try {
                holder.imgUser.setImageURI(Uri.parse(c.userPhoto));
            } catch(Exception e) {
                holder.imgUser.setImageResource(R.drawable.userprofile);
            }
        } else {
            holder.imgUser.setImageResource(R.drawable.userprofile);
        }
    }

    @Override
    public int getItemCount(){ return list.size(); }

    static class VH extends RecyclerView.ViewHolder{
        ImageView imgUser;
        TextView tvUser, tvText;

        VH(View v){
            super(v);
            imgUser = v.findViewById(R.id.imgUser);
            tvUser = v.findViewById(R.id.tvUser);
            tvText = v.findViewById(R.id.tvText);
        }
    }
}
