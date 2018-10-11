package com.fgapps.tracku.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fgapps.tracku.R;
import com.fgapps.tracku.activity.MainActivity;
import com.fgapps.tracku.activity.MapsActivity;
import com.fgapps.tracku.database.RealtimeDatabase;
import com.fgapps.tracku.database.StorageDatabase;
import com.fgapps.tracku.model.Contact;
import com.fgapps.tracku.model.ListModel;
import com.fgapps.tracku.service.SyncDatabases;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by (Engenharia) Felipe on 26/03/2018.
 */

public class ListAdapter extends RecyclerView.Adapter<ListModel> {

    private ArrayList<Contact> contacts;
    private RealtimeDatabase rtdb;

    public ListAdapter(ArrayList<Contact> contacts) {
        this.contacts = contacts;
        this.rtdb = new RealtimeDatabase();
    }

    @NonNull
    @Override
    public ListModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_list, parent, false);
        return new ListModel(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListModel holder, int position) {
        holder.getTxtName().setText(contacts.get(position).getName());
        holder.getTxtLocation().setText(contacts.get(position).getLocation());
        holder.getTxtTime().setText(contacts.get(position).getTime());
        holder.getTxtPhone().setText(contacts.get(position).getPhone());
        StorageDatabase sd = new StorageDatabase();
        Bitmap photo = sd.loadPhoto(contacts.get(position).getPhone());
        if(photo!=null) holder.getImgPhoto().setImageBitmap(photo);
        else  holder.getImgPhoto().setImageResource(R.drawable.no_photo);

        if(MainActivity.getSelected().size()==0) {
            switch (contacts.get(position).getStatus()) {
                case 1:
                    holder.getImgPhoto().setBorderColor(Color.GREEN);
                    holder.getImgBanner().setImageResource(R.drawable.banner_g);
                    holder.getLytFundo().setBackgroundResource(R.drawable.list_back_g);
                    break;
                case 2:
                    holder.getImgPhoto().setBorderColor(Color.YELLOW);
                    holder.getImgBanner().setImageResource(R.drawable.banner_y);
                    holder.getLytFundo().setBackgroundResource(R.drawable.list_back_y);
                    break;
                default:
                    holder.getImgPhoto().setBorderColor(Color.RED);
                    holder.getImgBanner().setImageResource(R.drawable.banner_r);
                    holder.getLytFundo().setBackgroundResource(R.drawable.list_back_r);
                    break;
            }
        }


        if(SyncDatabases.isOnline()){
            rtdb.updateContact(contacts.get(position).getPhone());
            String[] l = contacts.get(position).getLocation().split(",");
            if(l.length>1) {
                String a = MapsActivity.getAdress4Loc(new LatLng(Double.parseDouble(l[0]), Double.parseDouble(l[1])));
                holder.getTxtLocation().setText(a);
            }
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
