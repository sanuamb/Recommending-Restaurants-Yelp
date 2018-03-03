package FeatureProbabilisticRecommendation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
	public String id;
	private List<String> visited;
	private Map<String,Double> neighborhood_scores;
	private Map<String,Double> category_scores;
	private Map<String,Double> attribute_scores;
	
	public User(String user_id)	{
		id = user_id;
		visited = new ArrayList<String>();
		neighborhood_scores = new HashMap<String,Double>();
		category_scores = new HashMap<String,Double>();
		attribute_scores = new HashMap<String,Double>();
	}
	
	public void setVisited(List<String> visited_restaurants) {
		for(String s : visited_restaurants)
			visited.add(s);
	}
	
	public void setNeighborhoods(Map<String,Integer> neighborhoods,int total_neighborhood) {		
		for(String neighborhood : neighborhoods.keySet()) {
			neighborhood_scores.put(neighborhood, (neighborhoods.get(neighborhood).doubleValue()/total_neighborhood));
		}
	}

	public void setCategories(Map<String,Integer> categories,int total_categories) {		
		for(String category : categories.keySet()) {
			category_scores.put(category, (categories.get(category).doubleValue()/total_categories));
		}
	}
	
	public void setAttributes(Map<String,Integer> attributes,int total_attributes) {		
		for(String attribute : attributes.keySet()) {
			attribute_scores.put(attribute, (attributes.get(attribute).doubleValue()/total_attributes));
		}
	}
	
	public List<String> getVisited() {
		return visited;
	}
	public Map<String,Double> getNeighborhoodScores(){
		return neighborhood_scores;
	}
	
	public Map<String,Double> getCategoryScores(){
		return category_scores;
	}
	
	public Map<String,Double> getAttributeScores(){
		return attribute_scores;
	}
}
