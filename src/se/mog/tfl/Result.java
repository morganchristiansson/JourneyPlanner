package se.mog.tfl;

import java.util.List;

import org.json.JSONException;

import se.mog.tfl.JsonResult.NoTripsFoundException;
import se.mog.tfl.JsonResult.Trip;
import se.mog.tfl.JsonResult.TripLeg;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Result extends Activity {
	private static final String TAG = "JourneyResult";
	private ListView list;
	private LinearLayout layoutMain, layoutDetails;
	private String from, to;
	protected JsonResult json;
	private GoogleAnalyticsTracker analytics;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		Intent intent = getIntent();
		from = intent.getStringExtra("from");
		to   = intent.getStringExtra("to"  );

		layoutMain = (LinearLayout) findViewById(R.id.layout_main);
		layoutDetails = (LinearLayout) findViewById(R.id.layout_details);
		list = (ListView)layoutMain.findViewById(R.id.list);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showDetails(position);
			}
		});
    	((TextView)layoutMain.findViewById(R.id.title)).setText("From "+from+" To "+to);

    	inflater = LayoutInflater.from(this);
		showResults();
		analytics = GoogleAnalyticsTracker.getInstance();
        analytics.start("UA-21761998-1", this);
		analytics.trackPageView("/result?from="+from+"&to="+to);
	}
	private void showResults() {
		try {
			json = new JsonResult(getIntent().getStringExtra("response"));
			list.setAdapter(new ResultAdapter(json));
		} catch (JSONException e) {
			exitError(e);
		} catch (NoTripsFoundException e) {
			exitError("No results", "You probably misspelled something. Go back and check your typing");
		}
	}
	private LayoutInflater inflater;

	public void showDetails(int position) {
		final JsonResult.Trip trip = json.getTrip(position);
		try {
			ListView list = (ListView) layoutDetails.findViewById(R.id.list);
			final List<TripLeg> legs = trip.getLegs();
			final int length = legs.size();
			list.setAdapter(new BaseAdapter() {
				@Override public View getView(int position, View convertView, ViewGroup parent) {
					try {
						View row = inflater.inflate(R.layout.result_details_row, null);
						if(position <= length-1) {
							TripLeg leg = legs.get(position);
							((TextView)row.findViewById(R.id.start)).setText(leg.getStartTime());
							((ImageView)row.findViewById(R.id.type)).setImageResource(leg.getImageResource());
							((TextView)row.findViewById(R.id.from)).setText(leg.getFrom());
							((TextView)row.findViewById(R.id.to)).setText(Html.fromHtml(leg.getDestination()));
							// XXX
							//((TextView)row.findViewById(R.id.duration)).setText("Average journey time: "+);
						} else {
							TripLeg leg = legs.get(legs.size()-1);
							((TextView)row.findViewById(R.id.from)).setText(leg.getTo());
						}
						return row;
					} catch (JSONException e) {
						throw new RuntimeException(e);
					}
				}
				@Override public long getItemId(int position) {
					return position;
				}			
				@Override public Object getItem(int position) {
					return position;
				}
				@Override public int getCount() {
					return length+1;
				}
			});
			layoutMain.setVisibility(View.GONE);
			layoutDetails.setVisibility(View.VISIBLE);
		} catch(JSONException e) {
			exitError(e);
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.result, menu);
	    return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.open:
			Uri uri = Uri.parse(getIntent().getStringExtra("url_user"));
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class ResultAdapter extends BaseAdapter {
		ResultAdapter(JsonResult json) {
		}
		@Override public int getCount() {
			return json.getTrips().size();
		}

		@Override public Object getItem(int position) {
			return position;
		}

		@Override public long getItemId(int position) {
			return position;
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			try {
				//Log.d(TAG, "getView("+position+")");
				Trip trip = json.getTrip(position);
				String duration = trip.getDuration();

				String start = trip.getStartTime();
				String end = trip.getStopTime();

				if(convertView == null) {
					convertView = inflater.inflate(R.layout.result_row, null);
				}
				//((TextView)convertView.findViewById(R.id.content)).setText(trip.toString());
				((TextView)convertView.findViewById(R.id.start)).setText(start);
				((TextView)convertView.findViewById(R.id.duration)).setText(duration);
				LinearLayout typesLayout = ((LinearLayout)convertView.findViewById(R.id.types));
				for(TripLeg leg : trip.getLegs()) {
					ImageView image = new ImageView(Result.this);
					image.setImageResource(leg.getImageResource());
					typesLayout.addView(image);
				}
				((TextView)convertView.findViewById(R.id.end)).setText(end);
				//Log.d(TAG, "getView() "+convertView);
				return convertView;
			} catch (JSONException e) {
				exitError(e);
				return null;
			}
		}
	};

	private void exitError(Exception e) {
		Log.w(TAG, e);
		exitError("Exception occured", e.getMessage());
	}
	private void exitError(String title, String message) {
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					showResults();
				}
			})
			.setNegativeButton("Back", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override public void onCancel(DialogInterface dialog) {
					finish();
				}
			})
			.show();
	}
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && layoutMain.getVisibility() != View.VISIBLE) {
        	back();
        	return false;
        }
        return super.onKeyDown(keyCode, event);
    }

	private void back() {
    	layoutMain.setVisibility(View.VISIBLE);
    	layoutDetails.setVisibility(View.GONE);
	}
}
