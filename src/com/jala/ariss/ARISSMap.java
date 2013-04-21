package com.jala.ariss;

import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class ARISSMap extends Activity {
	private WebView viewPage;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arissmap);
		viewPage = (WebView)findViewById(R.id.webView1);
		viewPage.getSettings().setJavaScriptEnabled(true);
		StrictMode.ThreadPolicy policy = new  StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		viewPage.loadUrl("file:///android_asset/index.html"); 
		
		
             
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.arissmap, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.camitemm:
	        	Intent intent =
                new Intent(ARISSMap.this, SpaceFinder.class);

              //Iniciamos la nueva actividad
	        	startActivity(intent);
	            return true;
	         
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}

}
