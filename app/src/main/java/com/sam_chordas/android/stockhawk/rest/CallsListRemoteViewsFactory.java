package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by vaibhav on 22/5/16.
 */
public class CallsListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private boolean dataIsValid;
    private int rowIdColumn;
    private DataSetObserver mDataSetObserver;
    private Cursor mCursor;
    Context context;


    public CallsListRemoteViewsFactory(Context applicationContext, Intent intent) {

        Cursor cursor = applicationContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        context = applicationContext;
        mCursor = cursor;
        dataIsValid = cursor != null;
        rowIdColumn = dataIsValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (dataIsValid) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        Cursor cursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        swapCursor(cursor);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (dataIsValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        mCursor.moveToPosition(position);
        row.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex("symbol")));
        row.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex("bid_price")));
        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (dataIsValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(rowIdColumn);
        }
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            synchronized (context) {
                super.onChanged();
                dataIsValid = true;

                context.notify();
            }
        }

        @Override
        public void onInvalidated() {
            synchronized (context) {
                super.onInvalidated();
                dataIsValid = false;
                context.notify();
            }
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            rowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            dataIsValid = true;
            synchronized (context) {
                context.notify();
            }
        } else {
            rowIdColumn = -1;
            dataIsValid = false;
            synchronized (context) {
                context.notify();
            }
        }
        return oldCursor;
    }

}
