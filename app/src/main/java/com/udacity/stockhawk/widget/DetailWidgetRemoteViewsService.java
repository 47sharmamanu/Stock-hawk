package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailActivity;

/**
 * Created & Developed by Manu Sharma on 12/3/2016.
 */

public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                //query data
                data = getContentResolver().query(Contract.Quote.uri,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                //fill in all the data.
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                remoteViews.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
                remoteViews.setTextViewText(R.id.price, getString(R.string.dollar_string) + data.getString(Contract.Quote.POSITION_PRICE));

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                String changeType;
                if (rawAbsoluteChange > 0) {
                    changeType = getString(R.string.plus_string);
                    remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    changeType = getString(R.string.minus_string);
                    remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                    rawAbsoluteChange *= -1;
                    percentageChange *= -1;
                }

                if (PrefUtils.getDisplayMode(DetailWidgetRemoteViewsService.this)
                        .equals(getString(R.string.pref_display_mode_absolute_key))) {
                    remoteViews.setTextViewText(R.id.change, changeType + getString(R.string.dollar_string) + rawAbsoluteChange);
                } else {
                    remoteViews.setTextViewText(R.id.change, changeType + percentageChange + getString(R.string.percentage_string));
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(DetailActivity.SYMBOL_KEY, data.getString(Contract.Quote.POSITION_SYMBOL));
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
