package se.mog.tfl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonResult {
	public class NoTripsFoundException extends Exception {
		private static final long serialVersionUID = -70106037811843652L;

		public NoTripsFoundException(JSONException e) {
			super(e);
		}
	}

	private List<Trip> trips;
	private JSONObject json;

	public JsonResult(String string) throws JSONException, NoTripsFoundException {
		json = new JSONObject(string);
		ArrayList<JSONObject> trips1;
		try {
			trips1 = toArray(json.get("trips"));
		} catch(JSONException e) {
			throw new NoTripsFoundException(e);
		}
		ArrayList<Trip> trips2 = new ArrayList<Trip>(trips1.size());
		for (int i = 0; i < trips1.size(); i++) {
			trips2.set(i, new Trip((JSONObject) trips1.get(i)));
		}
		this.trips = trips2;
	}

	public List<Trip> getTrips() {
		return trips;
	}
	public Trip getTrip(int position) {
		return trips.get(position);
	}

	public static class Trip {
		private List<TripLeg> legs;
		private String duration;

		public Trip(JSONObject trip) throws JSONException {
			List<JSONObject> legs1 = toArray(trip.get("legs"));
			List<TripLeg> legs2 = new ArrayList<TripLeg>(legs1.size());
			for (int i = 0; i < legs1.size(); i++) {
				legs2.set(i, new TripLeg((JSONObject) legs1.get(i)));
			}
			this.legs = legs2;
			this.duration = trip.getString("duration");
		}

		public List<TripLeg> getLegs() throws JSONException {
			return legs;
		}

		public String getDuration() {
			return duration.replaceFirst("^0?(\\d+):", "$1h")+"m";
		}

		public TripLeg getFirstLeg() {
			return legs.get(0);
		}

		public TripLeg getLastLeg() {
			return legs.get(legs.size()-1);
		}

		public String getStartTime() throws JSONException {
			return getFirstLeg().getStartTime();
		}

		public String getStopTime() throws JSONException {
			return getLastLeg().getStopTime();
		}
	}

	public static class TripLeg {
		private JSONObject mode;
		private List<JSONObject> points;
		private JSONObject firstPoint, lastPoint;
		private int type;
		public TripLeg(JSONObject leg) throws JSONException {
			this.mode = leg.getJSONObject("mode");
			this.points = toArray(leg.get("points"));
			if(points.size() != 2) throw new RuntimeException(leg.toString());
			this.firstPoint = points.get(0);
			this.lastPoint = points.get(1);
			this.type = mode.getInt("type");
		}

		public List<JSONObject> getPoints() {
			return points;
		}

		public String getStartTime() throws JSONException {
			return firstPoint.getJSONObject("dateTime").getString("time");
		}
		public String getStopTime() throws JSONException {
			return lastPoint.getJSONObject("dateTime").getString("time");
		}

		public CharSequence getFrom() throws JSONException {
			return firstPoint.getString("name");
		}

		public String getTo() throws JSONException {
			switch(type) {
			case 3:
				return lastPoint.getString("name");
			default:
				if(mode.optInt("desc", -1) == 1) {
					return getDestination();
				}
				return mode.getString("desc");
			}
		}

		public String getDestination() throws JSONException {
			return mode.getString("desc");
//				switch(type) {
//				case 3: //bus with stop letter information
//				case 99:
//				case 100:
//					return mode.getString("desc");
//				default:
//					return "Towards "+mode.getString("destination");
//				}
		}

		public int getImageResource() throws JSONException {
			switch(type) {
			case 1:
				return R.drawable.icon_tube;
			case 2:
				return R.drawable.icon_dlr;
			case 3:
				return R.drawable.icon_buses;
			case 6: // southern trains
				return R.drawable.icon_rail;
			case 12: //first capital connect
				return R.drawable.icon_rail;
			case 99:
			case 100:
				return R.drawable.icon_walk;
			case 107:
				return R.drawable.icon_cycle;
			default:
				return android.R.drawable.ic_dialog_alert;	
			}
		}

	}

	/**
	 * Fix TfLs wierd format for single item arrays.
	 * 
	 * Example for single item:
	 * {trips:{trip: <trip1> }}
	 * 
	 * Example for 2+ items:
	 * {trips:[<trip1>, <trip2>, ...]}
	 * 
	 * @param unknownObject The trips object which is either a JSONArray or JSONObject containing a single property.
	 * @return List of JSONObjects contained in the JSONArray or the value of the single property if it's JSONObject
	 */
	public static ArrayList<JSONObject> toArray(Object unknownObject) throws JSONException {
		if(unknownObject instanceof JSONArray) {
			JSONArray array = (JSONArray)unknownObject;
			ArrayList<JSONObject> array2 = new ArrayList<JSONObject>(array.length());
			for (int i = 0; i < array.length(); i++) {
				array2.add(array.getJSONObject(i));
			}
			return array2;
		} else {
			JSONObject object = (JSONObject)unknownObject;
			String singular = (String)object.names().get(0);
			ArrayList<JSONObject> array = new ArrayList<JSONObject>(1);
			array.add(object.getJSONObject(singular));
			return array;
		}
	}
}
