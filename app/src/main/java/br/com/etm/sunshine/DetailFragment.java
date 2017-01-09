package br.com.etm.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import br.com.etm.sunshine.data.WeatherContract;

/**
 * Created by EDUARDO_MARGOTO on 12/15/2016.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = DetailFragment.class.getSimpleName();

    private final static int DETAIL_LOADER = 0;
    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    String mForecastStr;
    ShareActionProvider mShareActionProvider;
    Uri mUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.main_fragment_detail, container, false);
        ViewHolder viewHolder = new ViewHolder(rootView);

        rootView.setTag(viewHolder);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

//        Intent intent = getActivity().getIntent();
//        if (intent == null || intent.getData() == null) {
//            return null;
//        }
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }


    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                    newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        String dateString = Utility.getDayName(getContext(), data.getLong(COL_WEATHER_DATE));
        String dateMonthString = Utility.getFormattedMonthDay(getContext(), data.getLong(COL_WEATHER_DATE));

        // Read wind speed and direction from cursor and update view
        float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDirStr = data.getFloat(COL_WEATHER_DEGREES);

        float humidity = data.getFloat(COL_WEATHER_PRESSURE);
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);

        String weatherDescription = data.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);


        viewHolder.dateView.setText(dateString);
        viewHolder.dateMonthView.setText(dateMonthString);

        viewHolder.descView.setText(weatherDescription);
        viewHolder.descView.setContentDescription(getString(R.string.a11y_forecast, weatherDescription));

//        viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));
        // Read weather condition ID from cursor
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

        // Use weather art image
        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                .error(Utility.getArtResourceForWeatherCondition(weatherId))
                .crossFade()
                .into(viewHolder.iconView);

        viewHolder.iconView.setContentDescription(getString(R.string.a11y_forecast_icon, weatherDescription));

        viewHolder.highView.setText(high);
        viewHolder.highView.setContentDescription(getString(R.string.a11y_high_temp, high));
        viewHolder.lowView.setText(low);
        viewHolder.lowView.setContentDescription(getString(R.string.a11y_low_temp, low));

        viewHolder.humidityView.setText(getContext().getString(R.string.format_humidity, humidity));
        viewHolder.humidityView.setContentDescription(viewHolder.humidityView.getText().toString());

        viewHolder.windView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
        viewHolder.windView.setContentDescription(viewHolder.windView.getText().toString());

        viewHolder.pressureView.setText(getContext().getString(R.string.format_pressure, pressure));
        viewHolder.pressureView.setContentDescription(viewHolder.pressureView.getText().toString());


        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }


    class ViewHolder {
        TextView dateView;
        TextView dateMonthView;
        TextView humidityView;
        TextView windView;
        TextView pressureView;
        TextView descView;
        TextView highView;
        TextView lowView;
        ImageView iconView;

        public ViewHolder(View view) {
            this.dateView = (TextView) view.findViewById(R.id.forecast_date);
            this.dateMonthView = (TextView) view.findViewById(R.id.forecast_date_month);
            this.humidityView = (TextView) view.findViewById(R.id.forecast_humidity);
            this.windView = (TextView) view.findViewById(R.id.forecast_wind);
            this.pressureView = (TextView) view.findViewById(R.id.forecast_pressure);
            this.descView = (TextView) view.findViewById(R.id.forecast_desc);
            this.highView = (TextView) view.findViewById(R.id.forecast_high);
            this.lowView = (TextView) view.findViewById(R.id.forecast_low);
            this.iconView = (ImageView) view.findViewById(R.id.forecast_icon);
        }

    }
}
