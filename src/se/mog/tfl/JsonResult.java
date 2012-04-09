package se.mog.tfl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonResult {
	public class NoTripsFoundException extends Exception {
		public NoTripsFoundException(JSONException e) {
			super(e);
		}
	}

	private List<Trip> trips;
	private JSONObject json;

	public JsonResult(String string) throws JSONException, NoTripsFoundException {
		json = new JSONObject(string);
		List trips;
		try {
			trips = toArray(json.get("trips"));
		} catch(JSONException e) {
			throw new NoTripsFoundException(e);
		}
		for (int i = 0; i < trips.size(); i++) {
			trips.set(i, new Trip((JSONObject) trips.get(i)));
		}
		this.trips = trips;
	}

	public List<Trip> getTrips() {
		return trips;
	}
	public Trip getTrip(int position) {
		return trips.get(position);
	}

	public static class Trip {
		public class Leg {
			private JSONObject leg;
			private JSONObject mode;
			private List<JSONObject> points;
			private JSONObject firstPoint, lastPoint;
			private int type;
			public Leg(JSONObject leg) throws JSONException {
				this.leg = leg;
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
				switch(type) {
				case 3: //bus with stop letter information
				case 100:
					return mode.getString("desc");
				default:
					return "Towards "+mode.getString("destination");
				}
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

		private JSONObject trip;
		private List<Leg> legs;
		private String duration;

		public Trip(JSONObject trip) throws JSONException {
			this.trip = trip;
			List legs = toArray(trip.get("legs"));
			for (int i = 0; i < legs.size(); i++) {
				legs.set(i, new Leg((JSONObject) legs.get(i)));
			}
			this.legs = legs;
			this.duration = trip.getString("duration");
		}

		public List<Leg> getLegs() throws JSONException {
			return legs;
		}

		public String getDuration() {
			return duration.replaceFirst("^0?(\\d+):", "$1h")+"m";
		}

		public Leg getFirstLeg() {
			return legs.get(0);
		}

		public Leg getLastLeg() {
			return legs.get(legs.size()-1);
		}

		public String getStartTime() throws JSONException {
			return getFirstLeg().getStartTime();
		}

		public String getStopTime() throws JSONException {
			return getLastLeg().getStopTime();
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
			ArrayList array = new ArrayList(1);
			array.add(object.getJSONObject(singular));
			return array;
		}
	}
}
