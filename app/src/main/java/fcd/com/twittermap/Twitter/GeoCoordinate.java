package fcd.com.twittermap.Twitter;

import com.google.gson.annotations.SerializedName;


public class GeoCoordinate {

	@SerializedName("type")
	private String type;
	
	@SerializedName("coordinates")
	private Coordinates coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "GeoCoordinate{" +
                "type='" + type + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
}
