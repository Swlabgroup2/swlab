package com.example.swlab.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Leisure_Speech_Activity extends AppCompatActivity {
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(Leisure_Speech_Activity.this, Leisure_Activity.class);
        startActivity(intent);
        finish();
    }
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leisure_speech);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        DatabaseReference databaseRef=FirebaseDatabase.getInstance().getReference("server/speech");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot pisSnapshot : dataSnapshot.getChildren()) {
                    DB_Leisure_Speech lerisure=pisSnapshot.getValue(DB_Leisure_Speech.class);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Photo", "failed: " + databaseError.getMessage());
            }
        });
        FirebaseRecyclerAdapter<DB_Leisure_Speech,LeisureViewHolder> adapter=
                new FirebaseRecyclerAdapter<DB_Leisure_Speech, LeisureViewHolder>(DB_Leisure_Speech.class,R.layout.leisure_speech_list,LeisureViewHolder.class,databaseRef) {
                    @Override
                    protected void populateViewHolder(LeisureViewHolder viewHolder, DB_Leisure_Speech model, int position) {
                        viewHolder.setPhoto(model);
                    }
                };
        recyclerView.setAdapter(adapter);
    }
    public static class LeisureViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public LeisureViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.imageView);
            title = (TextView) itemView.findViewById(R.id.txt_title);
        }

        public void setPhoto(final DB_Leisure_Speech leisure) {
            title.setText(leisure.getTitle());
            Glide.with(image.getContext())
                    .load(leisure.getImageUrl())
                    .into(image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle=new Bundle();
                    bundle.putString("video_id",leisure.getId());
                    Intent intent = new Intent(itemView.getContext(),Leisure_Speech_Youtube_Activity.class);
                    intent.putExtras(bundle);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}

