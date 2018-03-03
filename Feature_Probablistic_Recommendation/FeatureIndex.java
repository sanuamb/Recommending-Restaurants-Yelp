package FeatureProbabilisticRecommendation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureIndex {
	
	private Map<String,List<String>> feature_index;
	public FeatureIndex() {
		
		feature_index = new HashMap<String,List<String>>();
		
		RestaurantFeatures restaurant_features = RestaurantFeatures.getInstanceObject();
		
		Map<String,String> neighborhoods = restaurant_features.getNeighborhoods();
		
		List<String> business_list = null;
		
		for(String business : neighborhoods.keySet()) {
			String neighborhood = neighborhoods.get(business);
			if(feature_index.containsKey(neighborhood)) {
				business_list = feature_index.get(neighborhood);
			}
			else {
				business_list = new ArrayList<String>();
			}
			business_list.add(business);
			feature_index.put(neighborhood, business_list);
		}

		Map<String,List<String>> category = restaurant_features.getCategories();
		
		for(String business : category.keySet()) {			
			List<String> category_list = category.get(business);
			for(String categ : category_list) {				
				if(feature_index.containsKey(categ)) {
					business_list = feature_index.get(categ);
				}
				else {
					business_list = new ArrayList<String>();
				}
				business_list.add(business);
				feature_index.put(categ, business_list);
			}
		}
		
		Map<String,List<String>> attribute = restaurant_features.getAttributes();
		
		for(String business : attribute.keySet()) {			
			List<String> attribute_list = attribute.get(business);			
			for(String attrb : attribute_list) {				
				if(feature_index.containsKey(attrb)) {
					business_list = feature_index.get(attrb);
				}
				else {
					business_list = new ArrayList<String>();
				}
				business_list.add(business);
				feature_index.put(attrb, business_list);	
			}
		}		
	}
	
	public List<String> getFeatureBusinessList(String feature) {
		return feature_index.get(feature);
	}
}
