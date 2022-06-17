package com.example.nfkatprototype;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.StrictMode;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DBOpenHelper extends SQLiteOpenHelper {


    public static class Constants implements BaseColumns {
        public static final String DATABASE_NAME = "NFKat.db";
        public static final int DATABASE_VERSION = 1;

        //USERS
        public static final String USERS_TABLE = "Users";

        public static final String USERS_KEY_ID = "user_id";
        public static final String USERS_KEY_NAME = "username";
        public static final String USERS_KEY_PASSWORD = "password";

        //KITTENS
        public static final String KITTENS_TABLE = "kittens";

        public static final String KITTENS_KEY_ID = "kitten_id";
        public static final String KITTENS_KEY_NAME = "name";
        public static final String KITTENS_KEY_IMG = "image";
        public static final String KITTENS_KEY_BTC_PRICE = "btc_price";
        public static final String KITTENS_KEY_EUR_PRICE = "eur_price";
        public static final String KITTENS_KEY_ETH_PRICE = "eth_price";
        public static final String KITTENS_KEY_USER_ID = USERS_KEY_ID;
    }

    private static final String CREATE_USERS = String.format("create table %s(%s INTEGER primary key autoincrement, %s TEXT, %s TEXT)",
            Constants.USERS_TABLE, Constants.USERS_KEY_ID, Constants.USERS_KEY_NAME, Constants.USERS_KEY_PASSWORD);

    private static final String CREATE_KITTENS = String.format("create table %s(%s INTEGER primary key autoincrement, %s INTEGER, %s TEXT, %s REAL, %s REAL, %s REAL, %s INTEGER)",
            Constants.KITTENS_TABLE, Constants.KITTENS_KEY_ID, Constants.KITTENS_KEY_NAME, Constants.KITTENS_KEY_IMG,
            Constants.KITTENS_KEY_BTC_PRICE, Constants.KITTENS_KEY_EUR_PRICE, Constants.KITTENS_KEY_ETH_PRICE,
            Constants.KITTENS_KEY_USER_ID);

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
        initKittens(db);
    }

    public static void initKittens(SQLiteDatabase db) {
        db.execSQL(CREATE_KITTENS);
        insertKitten(db, R.drawable.carthusian, "Carthusian", 15.f);
        insertKitten(db, R.drawable.exotic_shorthair, "Exotic", 25.f);
        insertKitten(db, R.drawable.persian, "Persian", 20.f);
        insertKitten(db, R.drawable.siamese, "Siamese", 40.f);
        insertKitten(db, R.drawable.sphynx, "Sphynx", 100.f);
    }

    public static long insertUser(SQLiteDatabase db, String nick, String pwd) {
        ContentValues newTaskValues = new ContentValues();
        newTaskValues.put(Constants.USERS_KEY_NAME, nick);
        newTaskValues.put(Constants.USERS_KEY_PASSWORD, pwd);
        return db.insert(Constants.USERS_TABLE, null, newTaskValues);
    }

    public static long insertKitten(SQLiteDatabase db, int image, String name, float price) {
        return insertKitten(db, image, name, price, -1);
    }

    public static long insertKitten(SQLiteDatabase db, int image, String name, float price, int userID) {
        ContentValues newTaskValues = new ContentValues();
        newTaskValues.put(Constants.KITTENS_KEY_NAME, name);
        newTaskValues.put(Constants.KITTENS_KEY_IMG, image);
        float btcPrice = price;
        float ethPrice = price;
        if (price != 0) {
            try {
                JSONObject btcValues = new JSONObject(NFTsManager.getResponseText("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur%2Ceth")).getJSONObject("bitcoin");
                float eurValue = (float) btcValues.getDouble("eur");
                float ethValue = (float) btcValues.getDouble("eth");
                btcPrice = price / eurValue;
                ethPrice = ethValue * btcPrice;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return -1;
            }
        }
        newTaskValues.put(Constants.KITTENS_KEY_BTC_PRICE, btcPrice);
        newTaskValues.put(Constants.KITTENS_KEY_EUR_PRICE, price);
        newTaskValues.put(Constants.KITTENS_KEY_ETH_PRICE, ethPrice);
        newTaskValues.put(Constants.KITTENS_KEY_USER_ID, userID);
        return db.insert(Constants.KITTENS_TABLE, null, newTaskValues);
    }

    public static long tradeKitten(SQLiteDatabase db, Integer kittenID, Integer userID) {
        ContentValues editTaskValues = new ContentValues();
        editTaskValues.put(Constants.KITTENS_KEY_USER_ID, userID);
        return db.update(Constants.KITTENS_TABLE, editTaskValues, Constants.KITTENS_KEY_ID + " = " + kittenID, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
