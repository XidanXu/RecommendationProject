package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

// Purity data: JSONObj -> Obj -> handle Obj -> JSONObj -> Browser
public class Item {
	private String itemId; // If public, no need private setter
	private String name;
	private String address;
	private Set<String> keywords; // Can use Map to store to differentiate weight of keys
	private String imageUrl;
	private String url;
	
	public String getItemId() {
		return itemId;
	}
	
	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getUrl() {
		return url;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;
	}

}
