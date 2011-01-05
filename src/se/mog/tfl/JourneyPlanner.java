package se.mog.tfl;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

public class JourneyPlanner extends Activity {
	private static final String TAG = "JourneyPlanner";
	private LinearLayout layoutMain, layoutFrom, layoutTo, activeLayout;
	private Button buttonFrom, buttonTo;
	private TextView textFrom, textTo;
	private ListView listFrom, listTo;
	private HistoryDb db;
	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journey_planner);
        layoutMain = (LinearLayout)findViewById(R.id.layout_main);
        activeLayout = layoutMain;
        layoutFrom = (LinearLayout)findViewById(R.id.layout_from);
        layoutTo   = (LinearLayout)findViewById(R.id.layout_to  );
        buttonFrom = (Button)findViewById(R.id.btn_from);
        buttonTo   = (Button)findViewById(R.id.btn_to  );
        textFrom   = (TextView)findViewById(R.id.text_from);
        textTo     = (TextView)findViewById(R.id.text_to  );
        listFrom   = (ListView)findViewById(R.id.list_from);
        listTo     = (ListView)findViewById(R.id.list_to  );
        db         = new HistoryDb(this);
        Cursor fromCursor = db.getFrom();
        Cursor toCursor   = db.getTo();
        try {
	        fromCursor.moveToFirst();
	        buttonFrom.setText(fromCursor.getString(fromCursor.getColumnIndex("name")));
        } catch(CursorIndexOutOfBoundsException e) {}
        try {
	        toCursor.moveToFirst();
	        buttonTo.setText(toCursor.getString(toCursor.getColumnIndex("name")));
        } catch(CursorIndexOutOfBoundsException e) {}
        listFrom.setAdapter(new SimpleCursorAdapter(this, R.layout.journey_planner_history_row, fromCursor, new String[] {"name"}, new int[] {R.id.name}));
        listTo  .setAdapter(new SimpleCursorAdapter(this, R.layout.journey_planner_history_row, toCursor  , new String[] {"name"}, new int[] {R.id.name}));
        listFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CharSequence from = ((TextView)view.findViewById(R.id.name)).getText();
				Log.d(TAG, "clicked from="+from);
				textFrom.setText("");
				buttonFrom.setText(from);
				back();
			}
		});
        listTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CharSequence to = ((TextView)view.findViewById(R.id.name)).getText();
				textTo.setText("");
				buttonTo.setText(to);
				back();
			}
		});
    }

	public void onClickFrom(View from) {
        layoutMain.setVisibility(View.GONE   );
        layoutFrom.setVisibility(View.VISIBLE);
        activeLayout = layoutFrom;
    }

	public void onClickTo(View to) {
        layoutMain.setVisibility(View.GONE   );
        layoutTo  .setVisibility(View.VISIBLE);
        activeLayout = layoutTo;
    }

	public void onClickReverse(View reverse) {
    	CharSequence from = buttonFrom.getText();
    	CharSequence to   = buttonTo  .getText();
    	buttonFrom.setText(to  );
    	  textFrom.setText(to  );
    	buttonTo  .setText(from);
    	  textTo  .setText(from);
    }
	public void onClickSearch(View search) {
    	CharSequence from = buttonFrom.getText();
    	CharSequence to   = buttonTo  .getText();
    	db.touch(from, to);
    	Intent i = new Intent(this, JourneyResult.class);
    	if("".equals(from)) return; // XXX
    	if("".equals(to  )) return; // XXX
    	i.putExtra("from", from);
    	i.putExtra("to"  , to  );
    	startActivity(i);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && layoutMain.getVisibility() != View.VISIBLE) {
        	back();
        	if(activeLayout == layoutFrom) buttonFrom.setText(textFrom.getText());
        	if(activeLayout == layoutTo  ) buttonTo  .setText(textTo  .getText());
        	return false;
        }
        return super.onKeyDown(keyCode, event);
    }

	private void back() {
    	layoutMain.setVisibility(View.VISIBLE);
    	layoutFrom.setVisibility(View.GONE   );
    	layoutTo  .setVisibility(View.GONE   );
    	activeLayout = layoutMain;
	}
}
