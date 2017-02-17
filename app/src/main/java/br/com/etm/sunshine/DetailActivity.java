package br.com.etm.sunshine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
//        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        Bundle arguments = new Bundle();
        arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
        arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
        // Being here means we are in animation mode
        supportPostponeEnterTransition();
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.detail, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }


}
