package com.abubaca.viss.notepin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutHelpViewer extends AppCompatActivity {

    TextView textViewerHeader , textViewerContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textViewerHeader = (TextView)findViewById(R.id.textViewerHeader);
        textViewerContent = (TextView)findViewById(R.id.textViewerContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_help_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_email:
                new Starter(this).startEmailClient();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        if(!action.equals("")){
            switch (action){
                case "ABOUT":
                    textViewerHeader.setText(R.string.about_text_header);
                    textViewerContent.setText(R.string.about_text_content);
                    break;
                case "HELP":
                    textViewerHeader.setText(R.string.help_text_header);
                    textViewerContent.setText(R.string.help_text_content);
                    break;
            }
        }
    }
}
