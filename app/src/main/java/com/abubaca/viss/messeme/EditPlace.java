package com.abubaca.viss.messeme;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditPlace extends AppCompatActivity {

    private EditText editPlace;
    private String placeName;
    private TextView addressView;
    private String address;
    private Double lat ,lgn;
    protected SQLiteDatabase db;
    protected View mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);

        Bundle bundle = new Bundle(getIntent().getExtras());
        address = bundle.getString("address");
        lat = bundle.getDouble("lat");
        lgn = bundle.getDouble("lgn");

        editPlace = (EditText) findViewById(R.id.edit_place);
        addressView = (TextView)findViewById(R.id.address);

        addressView.setText(address);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mView = view;
                writeToDatabase();
            }
        });
    }

    private void writeToDatabase(){
        placeName = editPlace.getText().toString();
        if(!TextUtils.isEmpty(placeName)) {
            String stringsForInsert = "'" + placeName + "','" + String.valueOf(lat) + "' , '" + String.valueOf(lgn) + "' , ''";
            db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
            db.execSQL("INSERT INTO PLACENOTES (PLACE,LAT,LGN,NOTE) VALUES ("+stringsForInsert+")");
//            db.execSQL("CREATE TABLE IF NOT EXISTS PLACES(NAME TEXT, LAT TEXT , LGN TEXT)");
//            db.execSQL("INSERT INTO PLACES (NAME,LAT,LGN) VALUES (" + stringsForInsert + ")");
            finish();
        }else{
            Snackbar.make(mView, "set a place name", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

}
