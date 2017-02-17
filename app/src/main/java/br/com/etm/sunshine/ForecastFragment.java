package br.com.etm.sunshine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import br.com.etm.sunshine.data.WeatherContract;
import br.com.etm.sunshine.sync.SunshineSyncAdapter;

/**
 * Created by EDUARDO_MARGOTO on 11/27/2016.
 */

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private ForecastAdapter adapter;
    //    private ArrayAdapter adapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private boolean mHoldForTransition;

    private boolean mUseTodayLayout = true, mAutoSelectView;
    private int mChoiceMode;


    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder vh);
    }


    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onLocationChange() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        View emptyView = rootView.findViewById(R.id.empty_view);

        adapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, ForecastAdapter.ViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity())
                        .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                locationSetting, date), vh
                        );
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView, mChoiceMode);

        adapter.setUseTodayLayout(mUseTodayLayout);
        // Get a reference to the ListView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);
        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if (null != parallaxView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }

        final AppBarLayout appbarView = (AppBarLayout) rootView.findViewById(R.id.appbar);
        if (null != appbarView) {
            ViewCompat.setElevation(appbarView, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (0 == mRecyclerView.computeVerticalScrollOffset()) {
                            appbarView.setElevation(0);
                        } else {
                            appbarView.setElevation(appbarView.getTargetElevation());
                        }
                    }
                });
            }
        }
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

//
//        );

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                // The Recycler View probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
            adapter.onRestoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
        if (adapter != null) {
            adapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForecastFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        mHoldForTransition = a.getBoolean(R.styleable.ForecastFragment_sharedElementTransitions, false);
        a.recycle();
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (null != adapter) {
            Cursor c = adapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        adapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.forecastfragment, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        switch (id) {
//            case R.id.action_refresh:
//                updateWeather();
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    public void showMap(Uri geoLocation) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(geoLocation);
//        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivity(intent);
//        }
//    }

    public void updateWeather() {
//        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//        String location = Utility.getPreferredLocation(getActivity());
//        weatherTask.execute(location);

//        Intent intent = new Intent(getActivity(), SunshineService.class);
//        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
//                Utility.getPreferredLocation(getActivity()));
//        getActivity().startService(intent);
// -----------------------------------------------------
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
//
//        //Wrap in a pending intent which only fires once.
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);
//
//        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//
//        //Set the AlarmManager to wake up the system.
//
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
// -----------------------------------------------------
        SunshineSyncAdapter.syncImmediately(getContext());
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (mHoldForTransition)
            getActivity().supportPostponeEnterTransition();

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
        if (cursor.getCount() == 0) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = adapter.getSelectedItemPosition();
                        if (RecyclerView.NO_POSITION == itemPosition) itemPosition = 0;
                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
                        if (null != vh && mAutoSelectView) {
                            adapter.selectView(vh);
                        }
                        if (mHoldForTransition) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.empty_view);
            if (null != tv) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.no_weather_information_available;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.empty_server_invalid;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.no_network_available;
                        }
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }
}

