package com.example.nfkatprototype;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class KatListActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase db;
    private Boolean sell;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kat_list);

        DBOpenHelper dbOpenHelper = new DBOpenHelper(this, DBOpenHelper.Constants.DATABASE_NAME, null, DBOpenHelper.Constants.DATABASE_VERSION);
        db = dbOpenHelper.getWritableDatabase();

        setUpRecycler();
        setUpButtons();
    }

    public void setUpButtons() {
        Button sellBtn = findViewById(R.id.sell_btn);
        Button buyBtn = findViewById(R.id.buy_btn);
        Button refreshBtn = findViewById(R.id.refresh_btn);

        sellBtn.setOnClickListener(this);
        buyBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
    }

    public void setUpRecycler() {
        userID = getIntent().getIntExtra("userID", -1);
        sell = getIntent().getBooleanExtra("sell", false);
        if (userID == -1)
            sell = false;

        CustomRecyclerViewAdapter recyclerViewAdapter = new CustomRecyclerViewAdapter(this, db, userID, sell);
        RecyclerView recyclerView = findViewById(R.id.list_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.refresh_btn || (v.getId() == R.id.sell_btn && sell) || (v.getId() == R.id.buy_btn && !sell)) {
            recreate();
        } else {
            Intent intent = new Intent(getApplicationContext(), this.getClass());
            intent.putExtra("userID", userID);
            if (v.getId() == R.id.sell_btn) {
                intent.putExtra("sell", true);
            } else if (v.getId() == R.id.buy_btn) {
                intent.putExtra("sell", false);
            }
            db.close();
            startActivity(intent);
            KatListActivity.this.finish();
        }
    }
}