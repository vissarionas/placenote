package com.abubaca.viss.messeme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    static final String TAG = "MAIN_ACTIVITY";

    private PlaceNoteUtils placeNoteUtils;
    private FloatingActionButton addPlaceFloatingActionButton;
    private Boolean notified = false;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Boolean batterySaver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_icon);
        placeNoteUtils = new PlaceNoteUtils(this);
        addPlaceFloatingActionButton = (FloatingActionButton)findViewById(R.id.add_place_floating_action_button);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupDrawer();
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Actions");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(R.string.main_title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        //Check if the activity started by the pending intent of a notification
        if(!notified && getIntent().getAction().equals("NOTIFICATION")){
            String place = getIntent().getStringExtra("PLACE");
            placeNoteUtils.viewNote(place);
            notified = true;
        }
        new ListPopulator(this).populate();
        addPlaceFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Starter(MainActivity.this).startMapActivity();
            }
        });
        batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
        new Starter(this).startStopFusedLocationService();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(0);
        item.setChecked(batterySaver);
        if(item.isChecked()){
            item.setIcon(R.drawable.battery_saver_on);
        }else{
            item.setIcon(R.drawable.battery_saver_off);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_battery_save:
                if(!item.isChecked()){
                    item.setChecked(true);
                    item.setIcon(R.drawable.battery_saver_on);
                    savePreferences(item.isChecked());
                    new CustomToast().makeSuccessToast(this , getResources().getString(R.string.battery_saver_on));
                }else{
                    item.setChecked(false);
                    item.setIcon(R.drawable.battery_saver_off);
                    savePreferences(item.isChecked());
                    new CustomToast().makeWarningToast(this, getResources().getString(R.string.battery_saver_off));
                }
                break;
        }
        batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
        new Starter(this).startStopFusedLocationService();
        return super.onOptionsItemSelected(item);
    }

    private void savePreferences(Boolean batterySaver){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("battery_saver" , batterySaver);
        editor.apply();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case (R.id.nav_remove_places):
                placeNoteUtils.clearDB();
                break;
            case (R.id.nav_clear_notes):
                placeNoteUtils.clearNotes();
                break;
            case (R.id.nav_about):
                new Starter(this).startTextViewer("ABOUT");
                break;
            case (R.id.nav_help):
                new Starter(this).startTextViewer("HELP");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


