package br.com.etm.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Date;


/**
 * Created by EDUARDO_MARGOTO on 12/14/2016.
 */

public class ForecastAdapter extends CursorAdapter {

    Context mContext;

    private boolean mUseTodayLayout = true;

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

//        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
//            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
//        } else {
//            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
//        }

        int viewType = getItemViewType(cursor.getPosition());
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
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

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);



        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String friendlyDate = Utility.getFriendlyDayString(context, dateInMillis);
        viewHolder.dateView.setText(friendlyDate);

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descView.setText(description);
        viewHolder.descView.setContentDescription(context.getString(R.string.a11y_forecast, description));
        viewHolder.iconView.setContentDescription(context.getString(R.string.a11y_forecast_icon, description));

        boolean isMetric = Utility.isMetric(context);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highTemp = Utility.formatTemperature(context, high, isMetric);
        viewHolder.highView.setText(highTemp);
        viewHolder.highView.setContentDescription(context.getString(R.string.a11y_high_temp, highTemp));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowTemp = Utility.formatTemperature(context, low, isMetric);
        viewHolder.lowView.setText(lowTemp);
        viewHolder.highView.setContentDescription(context.getString(R.string.a11y_low_temp, lowTemp));


    }


    private class ViewHolder {
        TextView dateView;
        TextView descView;
        TextView highView;
        TextView lowView;
        ImageView iconView;

        public ViewHolder(View view) {
            this.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.descView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
            this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        }
    }

}
