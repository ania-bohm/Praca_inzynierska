package com.annabohm.pracainzynierska;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class GuestListAdapter extends ArrayAdapter<User> {
    private ArrayList<User> userList;
    private ArrayList<String> userStatusList;
    private HashMap<User, String> userStatusMap;
    private Context context;

    public GuestListAdapter(@NonNull Context context, @NonNull List<User> userList, List<String> userStatusList) {
        super(context, R.layout.guest_list_item, userList);
        this.userList = (ArrayList<User>) userList;
        this.userStatusList = (ArrayList<String>) userStatusList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.guest_list_item, parent, false);
        }

        TextView guestListItemUserFirstNameTextView = convertView.findViewById(R.id.guestListItemUserFirstNameTextView);
        TextView guestListItemUserLastNameTextView = convertView.findViewById(R.id.guestListItemUserLastNameTextView);
        ImageView guestListItemUserPhotoImageView = convertView.findViewById(R.id.guestListItemUserPhotoImageView);
        ImageView guestListItemUserStatusImageView = convertView.findViewById(R.id.guestListItemUserStatusImageView);

        guestListItemUserFirstNameTextView.setText(user.getUserFirstName());
        guestListItemUserLastNameTextView.setText(user.getUserLastName());
        switch (userStatusList.get(position)) {
            case "Invited":
                guestListItemUserStatusImageView.setBackgroundResource(R.drawable.ic_unknown);
                break;
            case "Confirmed":
                guestListItemUserStatusImageView.setBackgroundResource(R.drawable.ic_confirm);
                break;
            case "Declined":
                guestListItemUserStatusImageView.setBackgroundResource(R.drawable.ic_decline);
                break;
        }

        String photoUri = user.getUserPhoto();
        if (photoUri != null && photoUri != "") {
            Picasso.get()
                    .load(user.getUserPhoto().trim())
                    .transform(new CropCircleTransformation())
                    .into(guestListItemUserPhotoImageView);
        } else {
            guestListItemUserPhotoImageView.setBackgroundResource(R.drawable.ic_no_photo);
        }
        return convertView;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList.clear();
        for (User user : userList) {
            this.userList.add(user);
        }
        notifyDataSetChanged();
    }

    public void setUserStatusList(ArrayList<String> userStatusList) {
        this.userStatusList.clear();
        for (String status : userStatusList) {
            this.userStatusList.add(status);
        }
        notifyDataSetChanged();
    }
}
