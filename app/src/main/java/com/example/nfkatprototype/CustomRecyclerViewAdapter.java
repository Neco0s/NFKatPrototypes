package com.example.nfkatprototype;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {
    private final Context currentContext;
    private final Boolean sell;
    private final SQLiteDatabase db;
    private final Integer userID;

    private final List<Integer> katIDs = new LinkedList<>();
    private final List<String> katNames = new LinkedList<>();
    private final List<Integer> katImages = new LinkedList<>();
    private final List<Float> katEURPrices = new LinkedList<>();
    private final List<Float> katBTCPrices = new LinkedList<>();
    private final List<Float> katETHPrices = new LinkedList<>();
    private float eurValue;
    private float ethValue;

    CustomRecyclerViewAdapter(Context context, SQLiteDatabase _db, Integer _userID, Boolean _sell) {
        currentContext = context;
        db = _db;
        userID = _userID;
        sell = _sell;
        String selectClause = String.format("%s = '%s'", DBOpenHelper.Constants.KITTENS_KEY_USER_ID, (sell) ? userID : -1);

        Cursor kittens = db.query(DBOpenHelper.Constants.KITTENS_TABLE,
                new String[]{
                        DBOpenHelper.Constants.KITTENS_KEY_ID,
                        DBOpenHelper.Constants.KITTENS_KEY_NAME,
                        DBOpenHelper.Constants.KITTENS_KEY_IMG,
                        DBOpenHelper.Constants.KITTENS_KEY_BTC_PRICE,
                        DBOpenHelper.Constants.KITTENS_KEY_EUR_PRICE,
                        DBOpenHelper.Constants.KITTENS_KEY_ETH_PRICE
                },
                selectClause, null, null, null, null);

        if (kittens.getCount() > 0) {
            int idCol = kittens.getColumnIndex(DBOpenHelper.Constants.KITTENS_KEY_ID);
            int nameCol = kittens.getColumnIndex(DBOpenHelper.Constants.KITTENS_KEY_NAME);
            int imgCol = kittens.getColumnIndex(DBOpenHelper.Constants.KITTENS_KEY_IMG);
            int btcCol = kittens.getColumnIndex(DBOpenHelper.Constants.KITTENS_KEY_BTC_PRICE);
            int eurCol = kittens.getColumnIndex(DBOpenHelper.Constants.KITTENS_KEY_EUR_PRICE);
            int ethCol = kittens.getColumnIndex(DBOpenHelper.Constants.KITTENS_KEY_ETH_PRICE);

            kittens.moveToFirst();
            while (!kittens.isAfterLast()) {
                katIDs.add(kittens.getInt(idCol));
                katNames.add(kittens.getString(nameCol));
                katImages.add(kittens.getInt(imgCol));
                katBTCPrices.add(kittens.getFloat(btcCol));
                katEURPrices.add(kittens.getFloat(eurCol));
                katETHPrices.add(kittens.getFloat(ethCol));
                kittens.moveToNext();
            }
        }
        kittens.close();

        try {
            JSONObject btcValues = new JSONObject(NFTsManager.getResponseText("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur%2Ceth")).getJSONObject("bitcoin");
            eurValue = (float) btcValues.getDouble("eur");
            ethValue = (float) btcValues.getDouble("eth");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            eurValue = 0.f;
            ethValue = 0.f;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_layout, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView img = (ImageView) holder.versionName.findViewById(R.id.kat_img);
        TextView name = (TextView) holder.versionName.findViewById(R.id.kat_name);
        TextView btc = (TextView) holder.versionName.findViewById(R.id.kat_btc);
        TextView eur = (TextView) holder.versionName.findViewById(R.id.kat_eur);
        TextView eurTrend = (TextView) holder.versionName.findViewById(R.id.kat_eur_trend);
        TextView eth = (TextView) holder.versionName.findViewById(R.id.kat_eth);
        TextView ethTrend = (TextView) holder.versionName.findViewById(R.id.kat_eth_trend);
        Button action = (Button) holder.versionName.findViewById(R.id.act_btn);

        img.setImageBitmap(NFTsManager.reduceBitmapSize(BitmapFactory.decodeResource(currentContext.getResources(), katImages.get(position))));
        name.setText(katNames.get(position));

        Float price = katBTCPrices.get(position);
        Float eurPrice = price * eurValue;
        Float ethPrice = price * ethValue;
        String btcPriceText = String.format("%s BTC", price);
        btc.setText(btcPriceText);

        String eurPriceText = String.format("%s EUR", eurPrice);
        eur.setText(eurPriceText);
        String eurTrendText = String.format("%s%%", eurPrice / katEURPrices.get(position) * 100);
        eurTrend.setText(eurTrendText);

        String ethPriceText = String.format("%s ETH", ethPrice);
        eth.setText(ethPriceText);
        String ethTrendText = String.format("%s%%", ethPrice / katETHPrices.get(position) * 100);
        ethTrend.setText(ethTrendText);

        action.setText((sell) ? "Sell" : "Buy");
    }

    @Override
    public int getItemCount() {
        return katNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RelativeLayout versionName;

        public ViewHolder(@NonNull RelativeLayout itemView) {
            super(itemView);
            versionName = itemView;
            Button actBtn = versionName.findViewById(R.id.act_btn);

            actBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            long rowId = DBOpenHelper.tradeKitten(db, katIDs.get(pos), (sell) ? -1 : userID);
            if (rowId == 0) {
                Toast.makeText(currentContext, "Error update", Toast.LENGTH_SHORT).show();
            } else {
                katIDs.remove(pos);
                katNames.remove(pos);
                katImages.remove(pos);
                katBTCPrices.remove(pos);
                katEURPrices.remove(pos);
                katETHPrices.remove(pos);

                notifyItemRemoved(pos);

                Toast.makeText(currentContext, String.format("Item %s!", (sell) ? "sold" : "bought"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
