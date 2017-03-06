package com.abubaca.viss.messeme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class AboutHelpViewer extends AppCompatActivity {

    TextView textViewerHeader , textViewerContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        textViewerHeader = (TextView)findViewById(R.id.textViewerHeader);
        textViewerContent = (TextView)findViewById(R.id.textViewerContent);
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
