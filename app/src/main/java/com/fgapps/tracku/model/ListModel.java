package com.fgapps.tracku.model;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fgapps.tracku.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by (Engenharia) Felipe on 26/03/2018.
 */

public class ListModel extends RecyclerView.ViewHolder {

    private TextView txtName;
    private TextView txtLocation;
    private TextView txtTime;
    private TextView txtPhone;
    private CircleImageView imgPhoto;
    private ImageView imgBanner;
    private ConstraintLayout lytFundo;

    public ListModel(View itemView) {
        super(itemView);

        txtName = itemView.findViewById(R.id.name_id);
        txtLocation = itemView.findViewById(R.id.location_id);
        txtTime = itemView.findViewById(R.id.time_id);
        txtPhone = itemView.findViewById(R.id.thephone_id);
        imgPhoto = itemView.findViewById(R.id.photo_id);
        imgBanner = itemView.findViewById(R.id.banner_id);
        lytFundo = itemView.findViewById(R.id.modelLayout_id);
    }

    public TextView getTxtName() {
        return txtName;
    }

    public TextView getTxtLocation() {
        return txtLocation;
    }

    public TextView getTxtTime() {
        return txtTime;
    }

    public TextView getTxtPhone() {
        return txtPhone;
    }

    public CircleImageView getImgPhoto() {
        return imgPhoto;
    }

    public ImageView getImgBanner() {
        return  imgBanner;
    }

    public ConstraintLayout getLytFundo() {
        return lytFundo;
    }
}
