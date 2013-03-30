/*
 * locus Android
 * Sven James <kalterregen AT gmx.net>
 */

package org.inrain.pmap;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ProjectMapActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile"; // TODO
	
	private String serverUrl;
	private String user;
	private int    updateTick;
	
    //private Button       mapButton;
	//private ToggleButton serviceButton;
	private TextView     serverText;
	private TextView     userText;
	private EditText     updateTickText;
	private Button       saveButton;
	private Button		 qrscanButton;
	
	public static void debug(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}
	
	/*private void setupButtons() {
	    boolean enabled = !serverUrl.equals("") && !user.equals("");
	    serviceButton.setEnabled(enabled);
	    mapButton.setEnabled(enabled);
	}*/
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.x).requestFocus(); // really don't focus anything...
        
        //mapButton      = (Button)       findViewById(R.id.mapButton);
        //serviceButton  = (ToggleButton) findViewById(R.id.serviceButton);
        serverText     = (TextView)     findViewById(R.id.serverText);
        userText       = (TextView)     findViewById(R.id.userText);
        updateTickText = (EditText)     findViewById(R.id.updateTickText);
        saveButton     = (Button)       findViewById(R.id.saveButton);
        qrscanButton   = (Button)       findViewById(R.id.qrscanButton);
        
        // restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverUrl  = settings.getString("serverUrl", "");
        user       = settings.getString("user", "");
        updateTick = settings.getInt("updateTick", 300);
        
        serverText.setText(serverUrl);
        userText.setText(user);
        updateTickText.setText(String.format("%d", updateTick));
        
        saveButton.setOnClickListener(mCorkyListener); // TODO
        qrscanButton.setOnClickListener(qrsbListener);
        /*mapButton.setOnClickListener(mapButtonClickListener);
        serviceButton.setChecked(ProjectMapService.running);
        serviceButton.setOnClickListener(serviceButtonClickListener);
        setupButtons();*/
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	  if (scanResult != null) { // handle scan result
    	      String contents = scanResult.getContents();
    	      if (contents != null) {
    	    	  Log.d("locus", "Scanned: " + contents);
    	    	  String spl[] = contents.split("!");
    	    	  for (int i = 0; i < spl.length; i++) {
    	    		  String nv[] = spl[i].split("=", 2);
    	    		  if (nv.length != 2) { continue; }
    	    		  if (nv[0].equals("url")) { serverText.setText(nv[1]); }
    	    		  if (nv[0].equals("username")) { userText.setText(nv[1]); }
    	    	  }
    	    	  //Toast.makeText(ProjectMapActivity.this, "Scanresult!" + contents, Toast.LENGTH_LONG).show();
    	      }
    	  }
    	  // Nothing else to do right now?
	}
    
    /*private OnClickListener serviceButtonClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(
                ProjectMapActivity.this,
                ProjectMapService.class
            );
            if (serviceButton.isChecked()) {
                startService(intent);
                //debug(ProjectMapActivity.this, "Starting...");
            } else {
                stopService(intent);
                //debug(ProjectMapActivity.this, "Stopping...");
            }
        }
    };
    
    private OnClickListener mapButtonClickListener = new OnClickListener() {
        public void onClick(View view) {
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl)));
            startActivity(new Intent(ProjectMapActivity.this, LocusActivity.class));
        }
    };*/
    
    private OnClickListener mCorkyListener = new OnClickListener() { // TODO
        public void onClick(View view) {
            serverUrl  = serverText.getText().toString().trim();
            user       = userText.getText().toString().trim();
            updateTick = Integer.parseInt(updateTickText.getText().toString());
            // TODO: check values
        	
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("serverUrl", serverUrl);
            editor.putString("user", user);
            editor.putInt("updateTick", updateTick);
            editor.commit();
            
            //setupButtons();
        }
    };

    private OnClickListener qrsbListener = new OnClickListener() {
        public void onClick(View view) {
        	IntentIntegrator integrator = new IntentIntegrator(ProjectMapActivity.this);
        	integrator.initiateScan();
        }
    };
}
