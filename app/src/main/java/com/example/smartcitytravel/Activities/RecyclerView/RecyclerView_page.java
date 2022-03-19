package com.example.smartcitytravel.Activities.RecyclerView;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.R;

public class RecyclerView_page extends AppCompatActivity {


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.recyclerview_page);

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            Dataofplaces[] Dataofplaces = new Dataofplaces[]{
                    new Dataofplaces("Avengers","2019 film", R.drawable.home_background),
                    new Dataofplaces("Venom","2018 film",R.drawable.home_background),
                    new Dataofplaces("Batman Begins","2005 film",R.drawable.home_background),
                    new Dataofplaces("Jumanji","2019 film",R.drawable.home_background),
                    new Dataofplaces("Good Deeds","2012 film",R.drawable.home_background),
                    new Dataofplaces("Hulk","2003 film",R.drawable.home_background),
                    new Dataofplaces("Avatar","2009 film",R.drawable.home_background),
            };

        Data_Adopter myMovieAdapter = new Data_Adopter(Dataofplaces,RecyclerView_page.this);
        recyclerView.setAdapter(myMovieAdapter);

        }
    }
