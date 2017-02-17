package br.com.etm.sunshine;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import br.com.etm.sunshine.data.WeatherContract;

import static br.com.etm.sunshine.ForecastFragment.COL_WEATHER_DATE;


/**
 * Created by EDUARDO_MARGOTO on 12/14/2016.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    Context mContext;
    Cursor mCursor;
    private boolean mUseTodayLayout = true;
    final private ForecastAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;


    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler dh, View empty_view, int choiceMode) {
        mClickHandler = dh;
        mContext = context;
        mEmptyView = empty_view;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);

        int viewType = getItemViewType(mCursor.getPosition());
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int fallbackIconId;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                fallbackIconId = Utility.getArtResourceForWeatherCondition(
                        weatherId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(
                        weatherId);
                break;
            }
        }

        // this enables better animations. even if we lose state due to a device rotation,
        // the animator can use this to re-find the original view
        ViewCompat.setTransitionName(viewHolder.iconView, "iconView" + position);

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);


        long dateInMillis = mCursor.getLong(COL_WEATHER_DATE);
        String friendlyDate = Utility.getFriendlyDayString(mContext, dateInMillis);
        viewHolder.dateView.setText(friendlyDate);

        String description = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descView.setText(description);
        viewHolder.descView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));
        viewHolder.iconView.setContentDescription(mContext.getString(R.string.a11y_forecast_icon, description));

        boolean isMetric = Utility.isMetric(mContext);

        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highTemp = Utility.formatTemperature(mContext, high, isMetric);
        viewHolder.highView.setText(highTemp);
        viewHolder.highView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highTemp));

        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowTemp = Utility.formatTemperature(mContext, low, isMetric);
        viewHolder.lowView.setText(lowTemp);
        viewHolder.highView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowTemp));

        mICM.onBindViewHolder(viewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolder) {
            ViewHolder vfh = (ViewHolder) viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ViewHolder vh);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateView;
        TextView descView;
        TextView highView;
        TextView lowView;
        ImageView iconView;


        public ViewHolder(View view) {
            super(view);
            this.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.descView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
            this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //CursorAdapter returns a cursor at the correct position for getItem(), or null
            // if it cannot seek to that position.
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
            mICM.onClick(this);
        }
    }

}
