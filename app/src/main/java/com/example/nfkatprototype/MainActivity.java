package com.example.nfkatprototype;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button login = (Button) findViewById(R.id.log_btn);
        Button signup = (Button) findViewById(R.id.sign_btn);

        login.setOnClickListener(this);
        signup.setOnClickListener(this);
        }

    @Override
    public void onClick(View v) {
        EditText editUser = (EditText) findViewById(R.id.edit_user);
        EditText editPWD = (EditText) findViewById(R.id.edit_pwd);
        String username = editUser.getText().toString();
        String password = editPWD.getText().toString();

        if (!username.equals("") && !password.equals("")) {
            DBOpenHelper dbOpenHelper = new DBOpenHelper(this, DBOpenHelper.Constants.DATABASE_NAME, null, DBOpenHelper.Constants.DATABASE_VERSION);
            String selectClause = String.format("%s = '%s' AND %s = '%s'", DBOpenHelper.Constants.USERS_KEY_NAME, username, DBOpenHelper.Constants.USERS_KEY_PASSWORD, password);

            if (v.getId() == R.id.log_btn) {
                login(dbOpenHelper, selectClause, "The user does not exist or the password does not match it");
            } else if (v.getId() == R.id.sign_btn) {
                signup(dbOpenHelper, selectClause, username, password);
            }
        }
    }

    private void login (DBOpenHelper dbOpenHelper, String selectClause, String errorMSG) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor users = db.query(DBOpenHelper.Constants.USERS_TABLE,
                new String[]{DBOpenHelper.Constants.USERS_KEY_ID},
                selectClause, null, null, null, null);

        if (users.moveToFirst()) {
            startNewActivity(db, users);
        } else {
            users.close();
            db.close();
            Toast.makeText(this, errorMSG, Toast.LENGTH_SHORT).show();
        }
    }

    private void signup (DBOpenHelper dbOpenHelper, String selectClause, String username, String password) {
        String usersWhereClause = String.format("%s = '%s'", DBOpenHelper.Constants.USERS_KEY_NAME, username);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor users = db.query(DBOpenHelper.Constants.USERS_TABLE,
                new String[]{DBOpenHelper.Constants.USERS_KEY_ID},
                usersWhereClause, null, null, null, null);
        boolean exists = users.moveToFirst();
        users.close();

        if (exists) {
            Toast.makeText(this, "This username is already in use", Toast.LENGTH_SHORT).show();
        } else {
            long rowID = DBOpenHelper.insertUser(db, username, password);
            db.close();

            if (rowID == -1) {
                Toast.makeText(this, "Database error: user has not been saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User created", Toast.LENGTH_SHORT).show();
                login(dbOpenHelper, selectClause, "Database error: user was created but could not log in");
            }
        }
    }

    private void startNewActivity(SQLiteDatabase db, Cursor cursor) {
        Intent intent = new Intent(getApplicationContext(), KatListActivity.class);
        intent.putExtra("userID", cursor.getInt(0));
        intent.putExtra("sell", true);

        cursor.close();
        db.close();
        startActivity(intent);
    }
}