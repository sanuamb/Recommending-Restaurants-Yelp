package FeatureProbabilisticRecommendation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserIndex {
	
	private Map<String,User> user_index;
    private RestaurantFeatures restaurant_features;
    
    public UserIndex() {
    	user_index = new HashMap<String,User>();
    }
    
    public void restaurantData(String neighborhood_path, String category_path, String attribute_path) {
		restaurant_features = RestaurantFeatures.getInstanceObject(category_path, attribute_path, neighborhood_path);
    }
    
    public void indexData(String review_path) {

    	StringBuilder review_data = FileReader.getFileText(review_path);

		String[][] fields = new String[2][2];
		fields[0][0] = "<field_user_id>";
		fields[0][1] = "</field_user_id>";
		fields[1][0] = "<field_business_id>";
		fields[1][1] = "</field_business_id>";
		
    	XMLParser parser = new XMLParser(review_data.substring(0),fields);
    	
    	String user_id = "";
    	String[] field_data;
    	Map<String,Integer> neighborhood = new HashMap<String,Integer>(),
    						category = new HashMap<String,Integer>(),
    						attribute = new HashMap<String,Integer>();
    	
    	List<String> visited = new ArrayList<String>();
    	
    	int total_category = 0, 
    	    total_attribute = 0,
    	    total_neighborhood = 0;
    	
		while(parser.rowExist()) {
			field_data = parser.rowDataGet();	
			if(!user_id.equals(field_data[0])) {
				
				if(!user_id.equals("")) {
					addUserIndex(user_id,visited,neighborhood,category,attribute,total_neighborhood,total_category,total_attribute);
				}
				
				//Initialize for user change
				user_id = field_data[0];
		    	neighborhood.clear();
		    	category.clear();
		    	attribute.clear();
		    	visited.clear();
		    	
		    	total_neighborhood = total_category = total_attribute = 0;
			}			
			
			visited.add(field_data[1]);
			
			String rest_neighborhood = restaurant_features.getNeighborhood(field_data[1]);			
			int count = 1;
			if(neighborhood.containsKey(rest_neighborhood)) {
				count = neighborhood.get(rest_neighborhood) + count;
			}
			neighborhood.put(rest_neighborhood, count);
			total_neighborhood++;
			
			List<String> rest_category = restaurant_features.getCategory(field_data[1]);
			if(rest_category != null) {
				for(String categ : rest_category) {		
					count = 1;
					if(category.containsKey(categ)) {
						count = category.get(categ) + count;
					}
					category.put(categ, count);
					total_category++;
				}
			}
			
			List<String> rest_attribute = restaurant_features.getAttribute(field_data[1]);
			if(rest_attribute != null) {
				for(String attrb : rest_attribute) {		
					count = 1;
					if(attribute.containsKey(attrb)) {
						count = attribute.get(attrb) + count;
					}
					attribute.put(attrb, count);
					total_attribute++;
				}
			}
		}
		if(!user_id.equals("")) {
			addUserIndex(user_id,visited,neighborhood,category,attribute,total_neighborhood,total_category,total_attribute);
		}
    	
    }
    
    public void addUserIndex(String user_id,
    						 List<String> visited,
    						 Map<String,Integer> neighborhood,
    						 Map<String,Integer> category,
    						 Map<String,Integer> attribute,
    						 int total_neighborhood,
    						 int total_category,
    						 int total_attribute) {
    	
    	User user_data = new User(user_id);
    	user_data.setVisited(visited);
    	user_data.setNeighborhoods(neighborhood, total_neighborhood);
    	user_data.setCategories(category, total_category);
    	user_data.setAttributes(attribute, total_attribute);
    	user_index.put(user_id, user_data);
    	
    }
    
    public User getUserData(String id) {
    	return user_index.get(id);
    }
    
}
