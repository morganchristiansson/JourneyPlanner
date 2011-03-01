package se.mog.tfl;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class Planner extends Activity {
	private static final String TAG = "JourneyPlanner";
//	<select name="type_origin" id="type_origin">
//    <option value="stop">Station or stop</option>
//    <option value="locator">Post code</option>
//    <option value="address">Address</option>
//    <option value="poi">Place of interest</option>
//  </select>
	private static final String[] TYPE_SPINNER_KEYS = new String[] {
		"stop",
		"address",
		"locator",
		"poi",
		//"poiID",
		//"coord"
	};
	private static final String[] TYPE_SPINNER_NAMES = new String[] {
		"Station or stop",
		"Address",
		"Post code",
		"Place of interest",
		//"poiID",
		//"coord"
	};
	private LinearLayout layoutMain, layoutFrom, layoutTo, activeLayout;
	private Button buttonFrom, buttonTo;
	private TextView textFrom, textTo;
	private ListView listFrom, listTo;
	private Spinner typeFrom, typeTo;
	private HistoryDb db;
	private GoogleAnalyticsTracker analytics;
	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner);
        layoutMain   = (LinearLayout)findViewById(R.id.layout_main);
        activeLayout = layoutMain;
        layoutFrom   = (LinearLayout)findViewById(R.id.layout_from);
        layoutTo     = (LinearLayout)findViewById(R.id.layout_to  );
        buttonFrom   = (Button)findViewById(R.id.btn_from);
        buttonTo     = (Button)findViewById(R.id.btn_to  );
        textFrom     = (TextView)findViewById(R.id.text_from);
        textTo       = (TextView)findViewById(R.id.text_to  );
        listFrom     = (ListView)findViewById(R.id.list_from);
        listTo       = (ListView)findViewById(R.id.list_to  );
        typeFrom     = (Spinner)findViewById(R.id.type_from);
        typeTo       = (Spinner)findViewById(R.id.type_to  );
        db           = new HistoryDb(this);
        Cursor fromCursor = db.getFrom();
        Cursor toCursor   = db.getTo();
        try {
	        fromCursor.moveToFirst();
	        setFrom(fromCursor.getString(fromCursor.getColumnIndex("name")),
	        		fromCursor.getString(fromCursor.getColumnIndex("type")));
        } catch(CursorIndexOutOfBoundsException e) {}
        try {
	        toCursor.moveToFirst();
	        setTo(toCursor.getString(toCursor.getColumnIndex("name")),
	        	  toCursor.getString(toCursor.getColumnIndex("type")));
        } catch(CursorIndexOutOfBoundsException e) {}
        listFrom.setAdapter(new SimpleCursorAdapter(this, R.layout.planner_history_row, fromCursor, new String[] {"name"}, new int[] {R.id.name}));
        listTo  .setAdapter(new SimpleCursorAdapter(this, R.layout.planner_history_row, toCursor  , new String[] {"name"}, new int[] {R.id.name}));
        listFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CharSequence from = ((TextView)view.findViewById(R.id.name)).getText();
				Log.d(TAG, "clicked from="+from);
				setFrom(from, getFromSpinnerValue());
				back();
			}
		});
        listTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CharSequence to = ((TextView)view.findViewById(R.id.name)).getText();
				setTo(to, getToSpinnerValue());
				back();
			}
		});
        textFrom.setOnEditorActionListener(editorActionListener);
        textTo.setOnEditorActionListener(editorActionListener);
        ArrayAdapter a = new ArrayAdapter(this, android.R.layout.simple_spinner_item, TYPE_SPINNER_NAMES);
        typeFrom.setAdapter(a);
        typeTo  .setAdapter(a);
        analytics = GoogleAnalyticsTracker.getInstance();
        analytics.start("UA-21761998-1", this);
        analytics.trackPageView("/");
    }
	
	private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
// XXX select [origin/destionation]_type:
//		Station or stop
//		Post code
//		Address
//		Place of interest 
		@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			backExtra();
			return false;
		}
	};
	private CharSequence from, to;
	private CharSequence fromType, toType;

	public void onClickFrom(View view) {
  	  	textFrom.setText(from);
        layoutMain.setVisibility(View.GONE   );
        layoutFrom.setVisibility(View.VISIBLE);
        activeLayout = layoutFrom;
        analytics.trackPageView("/from");
    }

	public void onClickTo(View view) {
		textTo.setText(to);
        layoutMain.setVisibility(View.GONE   );
        layoutTo  .setVisibility(View.VISIBLE);
        activeLayout = layoutTo;
        analytics.trackPageView("/to");
    }

	public void onClickReverse(View reverse) {
    	CharSequence fromOld = from;
    	CharSequence fromTypeOld = fromType;
    	CharSequence toOld   = to;
    	CharSequence toTypeOld   = toType;
    	setFrom(toOld  , toTypeOld);
    	setTo  (fromOld, fromTypeOld);
	    analytics.trackPageView("/reverse");
    }
	public void onClickSearch(View search) {
    	db.touch(from, fromType, to, toType);
    	Intent i = new Intent(this, Result.class);
    	if("".equals(from)) return; // XXX
    	if("".equals(to  )) return; // XXX
    	i.putExtra("name_origin",      from    .toString());
    	i.putExtra("type_origin",      fromType.toString());
    	i.putExtra("name_destination", to    .toString());
    	i.putExtra("type_destination", toType.toString());
    	startActivity(i);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && layoutMain.getVisibility() != View.VISIBLE) {
        	backExtra();
        	return false;
        }
        return super.onKeyDown(keyCode, event);
    }

	private void backExtra() {
    	if(activeLayout == layoutFrom) setFrom(textFrom.getText(), getFromSpinnerValue());
    	if(activeLayout == layoutTo  ) setTo  (textTo  .getText(), getToSpinnerValue());
    	back();
	}

	private CharSequence getFromSpinnerValue() {
		return TYPE_SPINNER_KEYS[typeFrom.getSelectedItemPosition()];
	}
	private CharSequence getToSpinnerValue() {
		return TYPE_SPINNER_KEYS[typeTo  .getSelectedItemPosition()];
	}

	private void setFrom(CharSequence text, CharSequence type) {
		buttonFrom.setText(text);
		from=text;
		fromType=type;
	}
	private void setTo(CharSequence text, CharSequence type) {
		buttonTo  .setText(text);
		to=text;
		toType=type;
	}

	private void back() {
    	layoutMain.setVisibility(View.VISIBLE);
    	layoutFrom.setVisibility(View.GONE   );
    	layoutTo  .setVisibility(View.GONE   );
    	activeLayout = layoutMain;
	}
}
