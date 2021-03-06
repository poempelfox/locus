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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ProjectMapActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile"; // TODO
	
	private String  serverUrl;
	private String  user;
	private int     updateTick;
	private boolean useNetLocation;
	private boolean useGPSLocation;
	private int     gpsReqRate;
	
    //private Button       mapButton;
	//private ToggleButton serviceButton;
	private TextView     serverText;
	private TextView     userText;
	private EditText     updateTickText;
	private Button       saveButton;
	private Button		 qrscanButton;
	private CheckBox     useNetLocCkbox;
	private CheckBox     useGPSLocCkbox;
	private Spinner      gpsReqRateSpin;
	
	public static void debug(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void setupButtons() {
	    // It only makes sense to set an update rate if GPS is enabled at all. 
	    gpsReqRateSpin.setEnabled(useGPSLocCkbox.isChecked());
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.x).requestFocus(); // really don't focus anything...
        
        serverText     = (TextView)     findViewById(R.id.serverText);
        userText       = (TextView)     findViewById(R.id.userText);
        updateTickText = (EditText)     findViewById(R.id.updateTickText);
        saveButton     = (Button)       findViewById(R.id.saveButton);
        qrscanButton   = (Button)       findViewById(R.id.qrscanButton);
        useNetLocCkbox = (CheckBox)     findViewById(R.id.useNetworkLocation);
        useGPSLocCkbox = (CheckBox)     findViewById(R.id.useGPSLocation);
        gpsReqRateSpin = (Spinner)      findViewById(R.id.gpsreqrate);
        
        // restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverUrl  = settings.getString("serverUrl", "");
        user       = settings.getString("user", "");
        updateTick = settings.getInt("updateTick", 300);
        useNetLocation = settings.getBoolean("useNetLocation", true);
        useGPSLocation = settings.getBoolean("useGPSLocation", true);
        gpsReqRate     = settings.getInt("gpsReqRate", 0);
        
        serverText.setText(serverUrl);
        userText.setText(user);
        updateTickText.setText(String.format("%d", updateTick));
        useNetLocCkbox.setChecked(useNetLocation);
        useGPSLocCkbox.setChecked(useGPSLocation);
        gpsReqRateSpin.setSelection(gpsReqRate);
        
        saveButton.setOnClickListener(mCorkyListener); // TODO
        qrscanButton.setOnClickListener(qrsbListener);
        useGPSLocCkbox.setOnClickListener(useGPSLocCkboxClickListener);
        setupButtons();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	  if (scanResult != null) { // handle scan result
    	      String contents = scanResult.getContents();
    	      if (contents != null) {
    	    	  Log.d("locus", "Scanned: " + contents);
    	    	  if (contents.startsWith("http")) { // just the serverurl then
    	    		  serverText.setText(contents);
    	    	  } else {
    	    		  String spl[] = contents.split("!");
    	    		  boolean hadsucc = false;
    	    		  for (int i = 0; i < spl.length; i++) {
    	    			  String nv[] = spl[i].split("=", 2);
    	    			  if (nv.length != 2) { continue; }
    	    			  if (nv[0].equals("url")) { serverText.setText(nv[1]); hadsucc = true; }
    	    			  if (nv[0].equals("username")) { userText.setText(nv[1]); hadsucc = true; }
    	    		  }
    	    		  if (!hadsucc) {
    	    			  Toast.makeText(ProjectMapActivity.this,
    	    					  "Sorry, scanned settings could not be parsed.",
    	    					  Toast.LENGTH_LONG).show();
    	    		  }
    	    	  }
    	      }
    	  }
    	  // Nothing else to do right now?
	}
    
    private OnClickListener mCorkyListener = new OnClickListener() { // TODO
        public void onClick(View view) {
            serverUrl  = serverText.getText().toString().trim();
            user       = userText.getText().toString().trim();
            updateTick = Integer.parseInt(updateTickText.getText().toString());
            useNetLocation = useNetLocCkbox.isChecked();
            useGPSLocation = useGPSLocCkbox.isChecked();
            gpsReqRate     = gpsReqRateSpin.getSelectedItemPosition();

            if ((useNetLocation == false) && (useGPSLocation == false)) {
                // At least one of the two needs to be enabled.
                Toast.makeText(ProjectMapActivity.this,
                               R.string.errormsg_needalocationsource,
                               Toast.LENGTH_LONG).show();
                return;
            }
            if ((!serverUrl.startsWith("http://"))
             && (!serverUrl.startsWith("https://"))) {
                Toast.makeText(ProjectMapActivity.this,
                               R.string.errormsg_invalidurl_nohttp,
                               Toast.LENGTH_LONG).show();
                return;
            }
            // TODO: (sanity) check more values

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("serverUrl", serverUrl);
            editor.putString("user", user);
            editor.putInt("updateTick", updateTick);
            editor.putBoolean("useNetLocation", useNetLocation);
            editor.putBoolean("useGPSLocation", useGPSLocation);
            editor.putInt("gpsReqRate", gpsReqRate);
            editor.commit();
            
            //setupButtons();
            Toast.makeText(ProjectMapActivity.this,
                           R.string.toastmsg_settings_saved,
                           Toast.LENGTH_SHORT).show();
        }
    };

    private OnClickListener qrsbListener = new OnClickListener() {
        public void onClick(View view) {
        	IntentIntegrator integrator = new IntentIntegrator(ProjectMapActivity.this);
        	integrator.initiateScan();
        }
    };
    
    private OnClickListener useGPSLocCkboxClickListener  = new OnClickListener() {
        public void onClick(View view) {
            setupButtons();
        }
    };
}
