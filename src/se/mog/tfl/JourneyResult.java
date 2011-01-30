package se.mog.tfl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import se.mog.tfl.TflJson.Trip;
import se.mog.tfl.TflJson.Trip.Leg;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class JourneyResult extends Activity {
	private static final String TAG = "JourneyResult";
	private ListView list;
	private LinearLayout layoutMain, layoutDetails;
	private String from, to;
	protected TflJson json;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		Intent intent = getIntent();
		from = intent.getStringExtra("from");
		to   = intent.getStringExtra("to"  );
		
    	inflater = LayoutInflater.from(JourneyResult.this);
		showResults();
	}
	private void showResults() {
		final ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Communicating with TfL");
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override public void onCancel(DialogInterface dialog) {
				if(request != null) request.abort();
			}
		});
		new AsyncTask<Void, Void, String>() {
			private Exception exception = null;
			@Override protected String doInBackground(Void... params) {
				try {
					String response = getIntent().getStringExtra("response");
					if(response == null) {
						response = getResponse(getUrl());
					}
					return response;
				} catch(Exception e) {
					exception = e;
					return null;
				}
//		    	String response = "{  parameters:[   {    name:\"requestID\",    value:0},   {    name:\"sessionID\",    value:0}],  trips:{   trip:{    duration:\"00:27\",    interchanges:1,    desc:0,    legs:[     {      points:[       {        name:\"Putney Station\",        usage:\"departure\",        desc:\"Departure from <b>Putney Station</b> at <b>03:20</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF31.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:20\"},        stamp:{         date:20110103,         time:0320},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF31.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_putney.pdf\"}],        ref:{         id:1001231,         area:1,         platform:\"A\",         coords:\"523944,824905\"}},       {        name:\"Putney Bridge Stn /Gonville St\",        usage:\"arrival\",        desc:\"Arrival at <b>Putney Bridge Stn /Gonville St</b> at <b>03:23</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF32.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:23\"},        stamp:{         date:20110103,         time:0323},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF32.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_putneybridge.pdf\"}],        ref:{         id:1007281,         area:0,         platform:\"FH\",         coords:\"524304,824143\"}}],      mode:{       name:\"Bus N74\",       type:3,       code:5,       destination:\"Baker Street Station\",       desc:\"Take Route Bus N74 towards Baker Street Station<br/> or Route Bus 14 towards Warren Street\",       diva:{        branch:48,        line:\"N74\",        supplement:\"f\",        project:\"y05\",        network:\"tfl\"}}},     {      points:[       {        name:\"Putney Bridge Stn /Gonville St\",        usage:\"departure\",        desc:\"Departure from <b>Putney Bridge Stn /Gonville St</b> at <b>03:30</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF32.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:30\"},        stamp:{         date:20110103,         time:0330},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF32.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_putneybridge.pdf\"}],        ref:{         id:1007281,         area:0,         platform:\"FE\",         coords:\"524308,824168\"}},       {        name:\"Richmond Station\",        usage:\"arrival\",        desc:\"Arrival at <b>Richmond Station</b> at 03:47 <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF33.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:47\"},        stamp:{         date:20110103,         time:0347},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF33.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_richmond.pdf\"}],        ref:{         id:1000192,         area:1,         platform:\"D\",         coords:\"518073,824807\"}}],      mode:{       name:\"Bus N22\",       type:3,       code:5,       destination:\"South Road / Fulwell\",       desc:\"Take Route Bus N22 towards South Road / Fulwell\",       diva:{        branch:48,        line:\"N22\",        supplement:\"e\",        project:\"y05\",        network:\"tfl\"}}}]}}}";
//		  		String response = "{  parameters:[   {    name:\"requestID\",    value:0},   {    name:\"sessionID\",    value:0}],  trips:[   {    duration:\"00:03\",    interchanges:0,    desc:0,    legs:[     {      points:[       {        name:\"Tottenham Court Road Station\",        usage:\"departure\",        desc:\"Departure from <b>Tottenham Court Road Station</b> at <b>03:34</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B13.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:34\"},        stamp:{         date:20110103,         time:0334},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B13.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/TK_TottenhamCR.pdf\"}],        ref:{         id:1000235,         area:16,         platform:\"X\",         coords:\"529971,818593\"}},       {        name:\"Great Titchfield Street\",        usage:\"arrival\",        desc:\"Arrival at <b>Great Titchfield Street</b> at <b>03:35</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B14.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:35\"},        stamp:{         date:20110103,         time:0335},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B14.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_oxfordcircus_db.pdf\"}],        ref:{         id:1010689,         area:1,         platform:\"OP\",         coords:\"529231,818732\"}}],      mode:{       name:\"Bus N207\",       type:3,       code:5,       destination:\"Hayes By-Pass (UB3)\",       desc:\"Take Route Bus N207 towards Hayes By-Pass (UB3)<br/> or Route Bus N207 towards Belmont Road (UB8)\",       diva:{        branch:24,        line:207,        supplement:\"N\",        project:\"y05\",        network:\"tfl\"}}},     {      points:[       {        name:\"Great Titchfield Street\",        usage:\"departure\",        desc:\"Departure from <b>Great Titchfield Street</b> <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B14.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:35\"},        stamp:{         date:20110103,         time:0335},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B14.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_oxfordcircus_db.pdf\"}],        ref:{         id:1010689,         area:1,         platform:\"OP\",         coords:\"529231,818732\"}},       {        name:\"Oxford Circus\",        usage:\"arrival\",        desc:\"Arrival at <b>Oxford Circus</b> at 03:37 <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B15.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:37\"},        stamp:{         date:20110103,         time:0337},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B15.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/TK_OxfordCircus.pdf\"}],        ref:{         id:1000173,         area:16,         coords:\"529085,818762\"}}],      mode:{       name:\"Fussweg\",       type:99,       desc:\"Walk to Oxford Circus\"},      turnInst:{       inst:{        dir:\"STRAIGHT\",        dist:149,        name:\"Oxford Street\",        coords:\"529230,818726\"}}}]},   {    duration:\"00:10\",    interchanges:0,    desc:-1,    legs:{     leg:{      points:[       {        name:\"Tottenham Court Road\",        usage:\"departure\",        desc:\"Departure from <b>Tottenham Court Road</b> <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B11.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:29\"},        stamp:{         date:20110103,         time:0329},        links:{         link:{          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B11.pdf\"}},        ref:{         id:1000235,         coords:\"529806,818621\"}},       {        name:\"Oxford Circus\",        usage:\"arrival\",        desc:\"Arrival at <b>Oxford Circus</b> <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B11.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:39\"},        stamp:{         date:20110103,         time:0339},        links:{         link:{          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B11.pdf\"}},        ref:{         id:1000173,         coords:\"529085,818762\"}}],      mode:{       name:\"Fussweg\",       type:100,       desc:\"Walk to Oxford Circus\"},      turnInst:{       inst:{        dir:\"STRAIGHT\",        dist:738,        name:\"Oxford Street\",        coords:\"529807,818625\"}}}}}]}";
			}
			protected void onPostExecute(String response) {
				if(exception != null) {
					exitError(exception);
					return;
				}
				try {
					json = new TflJson(response.toString());
				} catch (JSONException e) {
					exitError(e);
					return;
				}
				layoutMain = (LinearLayout) findViewById(R.id.layout_main);
				layoutDetails = (LinearLayout) findViewById(R.id.layout_details);
				list = (ListView)layoutMain.findViewById(R.id.list);
				list.setAdapter(resultAdapter);
				list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						showDetails(position);
					}
				});
		    	((TextView)layoutMain.findViewById(R.id.title)).setText("From "+from+" To "+to);
		    	dialog.dismiss();
			}
		}.execute((Void[])null);
	}
	private LayoutInflater inflater;
	private HttpGet request;

	public void showDetails(int position) {
		final TflJson.Trip trip = json.getTrip(position);
		try {
			ListView list = (ListView) layoutDetails.findViewById(R.id.list);
			final List<TflJson.Trip.Leg> legs = trip.getLegs();
			final int length = legs.size();
			list.setAdapter(new BaseAdapter() {
				@Override public View getView(int position, View convertView, ViewGroup parent) {
					try {
						View row = inflater.inflate(R.layout.result_details_row, null);
						if(position <= length-1) {
							Leg leg = legs.get(position);
							((TextView)row.findViewById(R.id.start)).setText(leg.getStartTime());
							((ImageView)row.findViewById(R.id.type)).setImageResource(leg.getImageResource());
							((TextView)row.findViewById(R.id.from)).setText(leg.getFrom());
							((TextView)row.findViewById(R.id.to)).setText(Html.fromHtml(leg.getDestination()));
							// XXX
							//((TextView)row.findViewById(R.id.duration)).setText("Average journey time: "+);
						} else {
							Leg leg = legs.get(legs.size()-1);
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
		case R.id.refresh:
			showResults();
			return true;
		case R.id.open:
			Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getUrl("user")));
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private String getUrl() {
		return getUrl("lite");
	}
	private String getUrl(String mode) {
		Intent intent = getIntent();
		String from = intent.getStringExtra("from");
		String to   = intent.getStringExtra("to"  );
		String url  = "http://journeyplanner.tfl.gov.uk/"+mode+"/XSLT_TRIP_REQUEST2?" +
					  "type_origin=stop&" +
					  "name_origin="+URLEncoder.encode(from)+"&" +
					  "type_destination=stop&" +
					  "name_destination="+URLEncoder.encode(to)+"&" +
					  //"calcNumberOfTrips=1&" +
					  "language=en";
		return url;
	}
	private String getResponse(String url) throws ClientProtocolException, IOException {
    	//locator = postcode
    	//stop    = station or stop
		Log.d(TAG, url);

		try {
			DefaultHttpClient client = new DefaultHttpClient();
			request = new HttpGet(url);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			client.execute(request).getEntity().writeTo(baos);
			String response = baos.toString();
			Log.d(TAG, "response="+response);
			return response;
		} finally {
			request = null;
		}
	}

	private BaseAdapter resultAdapter = new BaseAdapter() {
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
				Log.d(TAG, "getView("+position+")");
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
				for(Leg leg : trip.getLegs()) {
					ImageView image = new ImageView(JourneyResult.this);
					image.setImageResource(leg.getImageResource());
					typesLayout.addView(image);
				}
				((TextView)convertView.findViewById(R.id.end)).setText(end);
				Log.d(TAG, "getView() "+convertView);
				return convertView;
			} catch (JSONException e) {
				exitError(e);
				return null;
			}
		}
	};

	private void exitError(Exception e) {
		new AlertDialog.Builder(this)
			.setTitle("Error occured")
			.setMessage(e.getMessage())
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
