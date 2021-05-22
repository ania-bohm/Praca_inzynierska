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
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class UserSearchListAdapter extends ArrayAdapter<User> implements View.OnClickListener {

    private ArrayList<User> userList;
    private Context context;

    public UserSearchListAdapter(@NonNull Context context, @NonNull List<User> userList) {
        super(context, R.layout.user_list_item, userList);
        this.userList = (ArrayList) userList;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        }

        TextView userListItemUserFirstNameTextView = convertView.findViewById(R.id.userListItemUserFirstNameTextView);
        TextView userListItemUserLastNameTextView = convertView.findViewById(R.id.userListItemUserLastNameTextView);
        ImageView userListItemUserPhotoImageView = convertView.findViewById(R.id.userListItemUserPhotoImageView);

        userListItemUserFirstNameTextView.setText(user.getUserFirstName());
        userListItemUserLastNameTextView.setText(user.getUserLastName());

        String photoUri = user.getUserPhoto();
        if (photoUri != null && photoUri != "") {
            Picasso.get()
                    .load(user.getUserPhoto().trim())
                    .transform(new CropCircleTransformation())
                    .into(userListItemUserPhotoImageView);
        } else {
            userListItemUserPhotoImageView.setBackgroundResource(R.drawable.ic_no_photo);
        }

        return convertView;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList.clear();
        for (User user : userList) {
            this.userList.add(user);
        }
    }

    @Override
    public void remove(@Nullable User object) {
        super.remove(object);
    }
}
