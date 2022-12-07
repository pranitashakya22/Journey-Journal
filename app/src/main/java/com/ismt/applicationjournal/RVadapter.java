package com.ismt.applicationjournal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;

public class RVadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    ArrayList<Journal> list = new ArrayList<>();

    public RVadapter(Context ctx)
    {
        this.context = ctx;
    }

    public void setItems(ArrayList<Journal> journal)
    {
        list.addAll(journal);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_layout, parent, false);
        return new JournalVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JournalVH vh = (JournalVH) holder;
        Journal journal = list.get(position);

        vh.txt_title.setText(journal.getTitle());
        vh.txt_thought.setText(journal.getThought());


        vh.txt_option.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, vh.txt_option);
            popupMenu.inflate(R.menu.option_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        Intent intent = new Intent(context, JournalActivity.class);
                        intent.putExtra("EDIT", journal);
                        context.startActivity(intent);
                        break;

                    case R.id.menu_remove:
                        DAOJournal dao = new DAOJournal();
                        dao.remove(journal.getKey()).addOnSuccessListener(suc -> {
                            Intent journalClass = new Intent(context, MainActivity.class);
                            context.startActivity(journalClass);
                            Toast.makeText(context, "removed ", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(er -> {
                            Toast.makeText(context, "" + er.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        break;


                }
                return false;
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}