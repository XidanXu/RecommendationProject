package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";

	public List<Item> search(double lat, double lon, String keyword) {
		// Prepare HTTP request parameter
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8"); // Xidan Xu -> Xidan+Xu
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		
		// Send HTTP request
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
			
			// Get HTTP response body
			if (response.getStatusLine().getStatusCode() != 200) {
				return new ArrayList<>();
			}
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return new ArrayList<>();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent())); // return type input stream, not give the same time, but in a stream
			StringBuilder responseBody = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
			JSONArray array = new JSONArray(responseBody.toString());
			return getItemList(array);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i ++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				descriptionList.add(description);
			}	
		}
		
		String[] descriptionArray = descriptionList.toArray(new String[descriptionList.size()]);
		List<List<String>> keywords = MonkeyLearnClient.extractKeywords(descriptionArray);
		
		for (int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));

			Set<String> keywordSet = new HashSet<>(keywords.get(i));
			builder.setKeywords(keywordSet);
			
			Item item = builder.build();
			itemList.add(item);
		}
		return itemList;
	}
	
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}
}
