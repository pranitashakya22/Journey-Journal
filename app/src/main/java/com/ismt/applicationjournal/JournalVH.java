package com.ismt.applicationjournal;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class JournalVH extends RecyclerView.ViewHolder {

    public TextView txt_title, txt_thought, txt_option;
    public ImageView img_view;

    public JournalVH(@NonNull View itemView) {
        super(itemView);

        txt_title = itemView.findViewById(R.id.txt_title);
        txt_thought = itemView.findViewById(R.id.txt_thought);
        txt_option = itemView.findViewById(R.id.txt_option);

        img_view = itemView.findViewById(R.id.img_view);

    }
}
