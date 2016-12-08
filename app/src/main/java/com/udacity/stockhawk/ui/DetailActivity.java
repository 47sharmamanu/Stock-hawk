package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created & Developed by Manu Sharma on 12/2/2016.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.bar_chart)
    BarChart barChart;
    @BindView(R.id.symbol)
    TextView symbolTV;
    @BindView(R.id.price)
    TextView priceTV;
    @BindView(R.id.change)
    TextView changeTV;


    private static final int STOCK_LOADER = 0;

    public static final String SYMBOL_KEY = "symbol_key";

    String symbol;

    ArrayList<BarEntry> barEntries;
    ArrayList<String> labels;
    BarData barData;
    BarDataSet barDataSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        //get the input symbol.
        symbol = (String) getIntent().getExtras().get(SYMBOL_KEY);
        updateBarChart(null);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() != 0) {
            if (data.moveToFirst()) {
                int historyColumnIndex = data.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
//                Log.d("history", data.getString(historyColumnIndex));
                updateBarChart(data.getString(historyColumnIndex));
                symbolTV.setText(data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                priceTV.setText("$" + data.getString(data.getColumnIndex(Contract.Quote.COLUMN_PRICE)));

                Float absoluteChange = Float.valueOf(data.getString(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE)));
                Float percentageChange = Float.valueOf(data.getString(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)));

                String changeType;
                if (percentageChange < 0) {
                    changeType = getString(R.string.minus_string);
                    changeTV.setBackgroundResource(R.drawable.percent_change_pill_red);
                    absoluteChange *= -1;
                    percentageChange *= -1;
                } else {
                    changeType = getString(R.string.plus_string);
                    changeTV.setBackgroundResource(R.drawable.percent_change_pill_green);
                }

                if (PrefUtils.getDisplayMode(this)
                        .equals(getString(R.string.pref_display_mode_absolute_key))) {
                    changeTV.setText(changeType + getString(R.string.dollar_string) + absoluteChange);
                } else {
                    changeTV.setText(changeType + percentageChange + getString(R.string.percentage_string));
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void updateBarChart(String history) {
        try {
            if (history == null) {
                return;
            }
            String[] histories = history.split("\n");

            if (labels == null) {
                labels = new ArrayList<>();
            } else {
                labels.clear();
            }
            if (barEntries == null) {
                barEntries = new ArrayList<>();
            } else {
                barEntries.clear();
            }

            for (int i = 0; i < histories.length; i++) {
                String h = histories[i];
                if (h != null && !h.isEmpty()) {
                    String date;
                    Float stowkClosingValue;
                    String[] stowkArray = h.split(",");
                    date = stowkArray[0];
                    stowkClosingValue = Float.valueOf(stowkArray[1].trim());
                    labels.add(date);
                    barEntries.add(new BarEntry(stowkClosingValue, i));
                }
            }

            if (barChart.getBarData() == null) {
                barDataSet = new BarDataSet(barEntries, "Stock values over previous months");
                barData = new BarData(labels, barDataSet);

                barChart.setNoDataText("Data is not available");
                barChart.setDescription("");
                barChart.setDrawGridBackground(false);

                barChart.setData(barData);
            }
            //notify data chart
            barData.notifyDataChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Bar chart update", "Error occured: " + e.getMessage());
        }
    }
}
