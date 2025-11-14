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

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.VH> {
    Context ctx; List<User> list;
    public FollowerAdapter(Context ctx, List<User> list){ this.ctx=ctx; this.list=list; }
    @Override public VH onCreateViewHolder(ViewGroup p,int v){ return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_follower,p,false)); }
    @Override public void onBindViewHolder(VH h,int pos){
        User u = list.get(pos); h.tvName.setText(u.getName()); h.tvPhone.setText(u.getPhone());
        if(u.getPhoto()!=null) try{ h.imgUser.setImageURI(Uri.parse(u.getPhoto())); } catch(Exception e){ h.imgUser.setImageResource(R.drawable.userprofile); }
    }
    @Override public int getItemCount(){ return list.size(); }
    static class VH extends RecyclerView.ViewHolder{
        ImageView imgUser; TextView tvName, tvPhone;
        VH(View v){ super(v); imgUser=v.findViewById(R.id.imgUser); tvName=v.findViewById(R.id.tvName); tvPhone=v.findViewById(R.id.tvPhone); }
    }
}
