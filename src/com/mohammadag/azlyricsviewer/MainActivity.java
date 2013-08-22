package com.mohammadag.azlyricsviewer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	private static final String TAG = "AZLyricsViewer";
	private static final boolean DEBUG = false;
	
	private TextView mLyricsView;
	private EditText mSongTitleView;
	private EditText mArtistNameView;
	private Button mFetchButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViews();
		
		mSongTitleView.requestFocus();
		mArtistNameView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					getLyrics(v);
					return true;
				}
				return false;
			}
		});
	}

	private void findViews() {
		mLyricsView = (TextView)findViewById(R.id.lyricsTextView);
		mSongTitleView = (EditText)findViewById(R.id.songNameTextEdit);
		mFetchButton = (Button)findViewById(R.id.fetchButton);
		mArtistNameView = (EditText)findViewById(R.id.artistNameTextEdit);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void getLyrics(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
		String artistName = mArtistNameView.getText().toString();
		artistName = artistName.replaceAll("\\s","");
		artistName = artistName.replaceAll(" ","");
		
		String songName = mSongTitleView.getText().toString();
		songName = songName.replaceAll("\\s","");
		songName = songName.replaceAll(" ","");
		
		String urlString = "http://www.azlyrics.com/lyrics/" + artistName.toLowerCase() + "/" + songName.toLowerCase() + ".html";
		
		mFetchButton.setEnabled(false);
		new FetchLyrics().execute(urlString);
	}
	
	private void setLyrics(String lyrics) {
		mLyricsView.setText(Html.fromHtml(lyrics));
		mFetchButton.setEnabled(true);
	}
	
	private class FetchLyrics extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... arg0) {
			try {
			    HttpClient client = new DefaultHttpClient();  ;
			    HttpGet get = new HttpGet(arg0[0]);
			    HttpResponse responseGet = client.execute(get);  
			    HttpEntity resEntityGet = responseGet.getEntity();  
			    if (resEntityGet != null) {  
			        String response = EntityUtils.toString(resEntityGet);
			    	Pattern p = Pattern.compile(
			                "<!-- start of lyrics -->(.*)<!-- end of lyrics -->",
			                Pattern.DOTALL
			            );
			    	
			    	Matcher matcher = p.matcher(response);

			    	if (matcher.find()) {
			    		String htmlLyrics = matcher.group(1);
			    		return htmlLyrics;
			    	} else {
			    		if (DEBUG) Log.i(TAG, "doesn't match");
			    	}
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				setLyrics(result);
			} else {
				setLyrics(getString(R.string.lyrics_not_found));
			}
			super.onPostExecute(result);
		}
	}
}
