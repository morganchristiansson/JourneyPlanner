package se.mog.tfl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

class JsonDownloader {
	private Main main;
	private Intent intent;
	private HttpGet request;
	private String from, fromType;
	private String to  , toType;
	public JsonDownloader(Main main, Intent intent) {
		this.main = main;
		this.intent = intent;

		from = intent.getStringExtra("from");
		fromType = intent.getStringExtra("from_type");
		to = intent.getStringExtra("to");
		toType = intent.getStringExtra("to_type");

		intent.putExtra("url_user", getUrl("user"));
	}
	private String getUrl() {
		return getUrl("lite");
	}
	private String getUrl(String mode) {
		try {
			return "http://journeyplanner.tfl.gov.uk/"+mode+"/XSLT_TRIP_REQUEST2?" +
						  "type_origin="+fromType+"&" +
						  "name_origin="+URLEncoder.encode(from, "UTF-8")+"&" +
						  "place_origin=London&" +
						  "type_destination="+toType+"&" +
						  "name_destination="+URLEncoder.encode(to, "UTF-8")+"&" +
						  "place_destination=London&" +
						  //"calcNumberOfTrips=1&" +
						  "language=en";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	private String getResponse(String url) throws ClientProtocolException, IOException {
    	//locator = postcode
    	//stop    = station or stop
		Log.d(Main.TAG, url);

		try {
			DefaultHttpClient client = new DefaultHttpClient();
			request = new HttpGet(url);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			HttpResponse response = client.execute(request);
			StatusLine s = response.getStatusLine();
			if(s.getStatusCode() != 200) throw new IOException(s.getStatusCode()+" "+s.getReasonPhrase());
			response.getEntity().writeTo(baos);
			String responseText = baos.toString();
			Log.d(Main.TAG, "response="+responseText);
			return responseText;
		} finally {
			request = null;
		}
	}
	void execute() {
		final ProgressDialog dialog = ProgressDialog.show(main, "Please wait", "Communicating with TfL");
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override public void onCancel(DialogInterface dialog) {
				if(request != null) request.abort();
			}
		});
		new AsyncTask<Void, Void, String>() {
			private Exception exception = null;
			private String title, message;
			@Override protected String doInBackground(Void... params) {
				try {
					return getResponse(getUrl());
				} catch(IOException e) {
					Log.w(Main.TAG, e);
					title="Network error";
					message="Error while communicating with Tfl";
					return null;
				} catch(Exception e) {
					Log.w(Main.TAG, e);
					exception = e;
					return null;
				}
//			    	String response = "{  parameters:[   {    name:\"requestID\",    value:0},   {    name:\"sessionID\",    value:0}],  trips:{   trip:{    duration:\"00:27\",    interchanges:1,    desc:0,    legs:[     {      points:[       {        name:\"Putney Station\",        usage:\"departure\",        desc:\"Departure from <b>Putney Station</b> at <b>03:20</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF31.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:20\"},        stamp:{         date:20110103,         time:0320},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF31.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_putney.pdf\"}],        ref:{         id:1001231,         area:1,         platform:\"A\",         coords:\"523944,824905\"}},       {        name:\"Putney Bridge Stn /Gonville St\",        usage:\"arrival\",        desc:\"Arrival at <b>Putney Bridge Stn /Gonville St</b> at <b>03:23</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF32.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:23\"},        stamp:{         date:20110103,         time:0323},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF32.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_putneybridge.pdf\"}],        ref:{         id:1007281,         area:0,         platform:\"FH\",         coords:\"524304,824143\"}}],      mode:{       name:\"Bus N74\",       type:3,       code:5,       destination:\"Baker Street Station\",       desc:\"Take Route Bus N74 towards Baker Street Station<br/> or Route Bus 14 towards Warren Street\",       diva:{        branch:48,        line:\"N74\",        supplement:\"f\",        project:\"y05\",        network:\"tfl\"}}},     {      points:[       {        name:\"Putney Bridge Stn /Gonville St\",        usage:\"departure\",        desc:\"Departure from <b>Putney Bridge Stn /Gonville St</b> at <b>03:30</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF32.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:30\"},        stamp:{         date:20110103,         time:0330},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF32.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_putneybridge.pdf\"}],        ref:{         id:1007281,         area:0,         platform:\"FE\",         coords:\"524308,824168\"}},       {        name:\"Richmond Station\",        usage:\"arrival\",        desc:\"Arrival at <b>Richmond Station</b> at 03:47 <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D213BF33.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:47\"},        stamp:{         date:20110103,         time:0347},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D213BF33.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_richmond.pdf\"}],        ref:{         id:1000192,         area:1,         platform:\"D\",         coords:\"518073,824807\"}}],      mode:{       name:\"Bus N22\",       type:3,       code:5,       destination:\"South Road / Fulwell\",       desc:\"Take Route Bus N22 towards South Road / Fulwell\",       diva:{        branch:48,        line:\"N22\",        supplement:\"e\",        project:\"y05\",        network:\"tfl\"}}}]}}}";
//			  		String response = "{  parameters:[   {    name:\"requestID\",    value:0},   {    name:\"sessionID\",    value:0}],  trips:[   {    duration:\"00:03\",    interchanges:0,    desc:0,    legs:[     {      points:[       {        name:\"Tottenham Court Road Station\",        usage:\"departure\",        desc:\"Departure from <b>Tottenham Court Road Station</b> at <b>03:34</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B13.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:34\"},        stamp:{         date:20110103,         time:0334},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B13.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/TK_TottenhamCR.pdf\"}],        ref:{         id:1000235,         area:16,         platform:\"X\",         coords:\"529971,818593\"}},       {        name:\"Great Titchfield Street\",        usage:\"arrival\",        desc:\"Arrival at <b>Great Titchfield Street</b> at <b>03:35</b>. <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B14.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:35\"},        stamp:{         date:20110103,         time:0335},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B14.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_oxfordcircus_db.pdf\"}],        ref:{         id:1010689,         area:1,         platform:\"OP\",         coords:\"529231,818732\"}}],      mode:{       name:\"Bus N207\",       type:3,       code:5,       destination:\"Hayes By-Pass (UB3)\",       desc:\"Take Route Bus N207 towards Hayes By-Pass (UB3)<br/> or Route Bus N207 towards Belmont Road (UB8)\",       diva:{        branch:24,        line:207,        supplement:\"N\",        project:\"y05\",        network:\"tfl\"}}},     {      points:[       {        name:\"Great Titchfield Street\",        usage:\"departure\",        desc:\"Departure from <b>Great Titchfield Street</b> <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B14.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:35\"},        stamp:{         date:20110103,         time:0335},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B14.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/SP_oxfordcircus_db.pdf\"}],        ref:{         id:1010689,         area:1,         platform:\"OP\",         coords:\"529231,818732\"}},       {        name:\"Oxford Circus\",        usage:\"arrival\",        desc:\"Arrival at <b>Oxford Circus</b> at 03:37 <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B15.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:37\"},        stamp:{         date:20110103,         time:0337},        links:[         {          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B15.pdf\"},         {          name:\"SM\",          type:\"SM\",          href:\"tfl/TK_OxfordCircus.pdf\"}],        ref:{         id:1000173,         area:16,         coords:\"529085,818762\"}}],      mode:{       name:\"Fussweg\",       type:99,       desc:\"Walk to Oxford Circus\"},      turnInst:{       inst:{        dir:\"STRAIGHT\",        dist:149,        name:\"Oxford Street\",        coords:\"529230,818726\"}}}]},   {    duration:\"00:10\",    interchanges:0,    desc:-1,    legs:{     leg:{      points:[       {        name:\"Tottenham Court Road\",        usage:\"departure\",        desc:\"Departure from <b>Tottenham Court Road</b> <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B11.pdf&itdLPxx_map=origin\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:29\"},        stamp:{         date:20110103,         time:0329},        links:{         link:{          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B11.pdf\"}},        ref:{         id:1000235,         coords:\"529806,818621\"}},       {        name:\"Oxford Circus\",        usage:\"arrival\",        desc:\"Arrival at <b>Oxford Circus</b> <a target=\'_blank\' href=\\\"XSLT_REQUEST?language=en&itdLPxx_src=FILELOAD?Filename=JP08__4D2142B11.pdf&itdLPxx_map=destination\\\">Print Map</a>\",        dateTime:{         date:\"3.01.2011\",         time:\"03:39\"},        stamp:{         date:20110103,         time:0339},        links:{         link:{          name:\"RM\",          type:\"RM\",          href:\"FILELOAD?Filename=JP08__4D2142B11.pdf\"}},        ref:{         id:1000173,         coords:\"529085,818762\"}}],      mode:{       name:\"Fussweg\",       type:100,       desc:\"Walk to Oxford Circus\"},      turnInst:{       inst:{        dir:\"STRAIGHT\",        dist:738,        name:\"Oxford Street\",        coords:\"529807,818625\"}}}}}]}";
			}
			protected void onPostExecute(String response) {
				try {
					if(message != null) {
						exitError(title, message);
						return;
					}
					if(exception != null) {
						exitError(exception);
						return;
					}
					intent.putExtra("response", response.toString());
					main.startActivity(intent);
			    	main.db.touch(from, fromType, to, toType);
			    	main.setFromAdapter();
			    	main.setToAdapter();
				} finally {
					dialog.dismiss();
				}
			}
		}.execute((Void[])null);
	}
	protected void exitError(Exception exception) {
	}
	protected void exitError(String title, String message) {
	}
}
