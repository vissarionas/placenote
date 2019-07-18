package com.abubaca.viss.placenote;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

  private static final String TAG = "MAIN_ACTIVITY";
  private static final int FINE_LOCATION_REQUEST = 0x1;


  private PlacenoteUtils placenoteUtils;
  private FloatingActionButton addPlaceFAB;
  private Boolean notified = false;
  private ActionBarDrawerToggle drawerToggle;
  private DrawerLayout drawerLayout;
  private Boolean batterySaver = false;
  private IntentFilter filter = new IntentFilter("SELECTED_ITEMS");
  private List<String> selectedPlaces = new ArrayList<>();
  private MenuItem clearItem , batteryItem;
  private TextView noPlacesTV;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_icon);
    placenoteUtils = new PlacenoteUtils(this);
    addPlaceFAB = (FloatingActionButton)findViewById(R.id.add_place_fab);
    drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
    noPlacesTV = (TextView)findViewById(R.id.no_places_tv);
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    setupDrawer();
  }

  @Override
  protected void onPause() {
    unregisterReceiver(broadcastReceiver);
    super.onPause();
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
        getSupportActionBar().setTitle(R.string.app_name);
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
    addPlaceFAB.show();
    //Check if the activity started by the pending intent of a notification
    if(!notified && getIntent().getAction().equals("NOTIFICATION")){
      String place = getIntent().getStringExtra("PLACE");
      placenoteUtils.viewNote(place);
      notified = true;
    }
    new PlaceListPopulator(this).populate();
    addPlaceFAB.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext() , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(MainActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST );
          return;
        }
        new Starter(MainActivity.this).startMapActivity();
        noPlacesTV.setVisibility(View.INVISIBLE);
        addPlaceFAB.hide();
      }
    });
    batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
    new Starter(this).startLocationService();
    registerReceiver(broadcastReceiver , filter);
    super.onResume();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
      if (requestCode == FINE_LOCATION_REQUEST){
        new Starter(MainActivity.this).startMapActivity();
      }
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else if(selectedPlaces.size()>0) {
      selectedPlaces.clear();
      handleMenuItemVisibility();
      addPlaceFAB.show();
      new PlaceListPopulator(this).populate();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    batteryItem = menu.findItem(R.id.action_battery_save);
    clearItem = menu.findItem(R.id.action_clear_notes);
    handleMenuItemVisibility();

    batteryItem.setChecked(batterySaver);
    if(batteryItem.isChecked()){
      batteryItem.setIcon(R.drawable.battery_saver_on);
    }else{
      batteryItem.setIcon(R.drawable.battery_saver_off);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
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
          new CustomToast().makeToast(this , Constants.SUCCESS_TOAST , getResources().getString(R.string.battery_saver_on));
        }else{
          item.setChecked(false);
          item.setIcon(R.drawable.battery_saver_off);
          savePreferences(item.isChecked());
          new CustomToast().makeToast(this, Constants.WARNING_TOAST , getResources().getString(R.string.battery_saver_off));
        }
        break;
      case R.id.action_clear_notes:
        if(selectedPlaces.size()>0){
          placenoteUtils.clearSelectedNotes(selectedPlaces);
        }
        break;
    }
    batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
    new Starter(this).startLocationService();
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
        placenoteUtils.clearDB();
        break;
      case (R.id.nav_clear_notes):
        placenoteUtils.clearAllNotes();
        break;
      case (R.id.nav_help):
        new Starter(this).startHelpActivity();
        break;
      case (R.id.nav_privacy_policy):
        new Starter(this).startPrivacyPolicyWeb();
        break;
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      selectedPlaces = intent.getStringArrayListExtra("SELECTED_PLACES");
      if(selectedPlaces.size()>0) {
        addPlaceFAB.hide();
      }else{
        addPlaceFAB.show();
      }
      handleMenuItemVisibility();
    }
  };

  private void handleMenuItemVisibility(){
    clearItem.setVisible(selectedPlaces.size()>0);
    batteryItem.setVisible(selectedPlaces.size() == 0);
  }
}


