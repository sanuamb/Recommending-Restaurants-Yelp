package FeatureProbabilisticRecommendation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestaurantFeatures {
	
	static RestaurantFeatures instance_object;
	static String c_path, a_path, b_path;
	private StringBuilder business_data;
	private StringBuilder category_data;
	private StringBuilder attribute_data;
	
	private Map<String,String> business_name;
	private Map<String,Double> business_rating;
	
	private Map<String,String> neighborhood;
	private Map<String,List<String>> categories;
	private Map<String,List<String>> attributes;
	
	private XMLParser parser;
	
	//This method improves efficiency by buffering calculated instance object if the paths are same
	public static RestaurantFeatures getInstanceObject(String category_path, String attribute_path, String business_path) {
		if(instance_object == null || !category_path.equals(c_path) || !attribute_path.equals(a_path) || !business_path.equals(b_path)) {
			instance_object = new RestaurantFeatures(category_path,attribute_path,business_path);
			b_path = business_path;
			c_path = category_path;
			a_path = attribute_path;
		}
		return instance_object;		
	}
	
	public static RestaurantFeatures getInstanceObject() {
		return instance_object;		
	}
	
	public RestaurantFeatures(String category_path, String attribute_path, String business_path) {
		
		business_data = FileReader.getFileText(business_path);
		category_data = FileReader.getFileText(category_path);
		attribute_data = FileReader.getFileText(attribute_path);
		
		business_name = new HashMap<String,String>();
		business_rating = new HashMap<String,Double>();
		neighborhood = new HashMap<String,String>();
		categories = new HashMap<String,List<String>>();		
		attributes = new HashMap<String,List<String>>();	
		
		parseBusinessData();
		parseCategories();
		parseAttributes();	
		
		parser = null;
	}
	
	private void parseBusinessData() {
		
		String[][] fields = new String[4][2];
		fields[0][0] = "<field_business_id>";
		fields[0][1] = "</field_business_id>";
		fields[1][0] = "<field_name>";
		fields[1][1] = "</field_name>";
		fields[2][0] = "<field_neighborhood>";
		fields[2][1] = "</field_neighborhood>";
		fields[3][0] = "<field_stars>";
		fields[3][1] = "</field_stars>";
		
		parser = new XMLParser(business_data.substring(0),fields);
		
		String[] field_data;
		
		while(parser.rowExist()) {	
			field_data = parser.rowDataGet();	
			business_name.put(field_data[0],field_data[1]);
			neighborhood.put(field_data[0],field_data[2]);
			business_rating.put(field_data[0],Double.parseDouble(field_data[3]));
		}
	}
	
	private void parseCategories() {
		
		String[][] fields = new String[2][2];
		fields[0][0] = "<field_business_id>";
		fields[0][1] = "</field_business_id>";
		fields[1][0] = "<field_category>";
		fields[1][1] = "</field_category>";
		
		parser = new XMLParser(category_data.substring(0),fields);
		
		String doc_id = "";
		String[] field_data;
		List<String> category = new ArrayList<String>();
		
		while(parser.rowExist()) {
			
			field_data = parser.rowDataGet();	
			if(!doc_id.equals(field_data[0])) {
				
				if(!doc_id.equals(""))
				{
					categories.put(doc_id, category);
				}
				doc_id = field_data[0];
				category = new ArrayList<String>();
			}			
			category.add(field_data[1]);
		}
		if(!doc_id.equals("")) {
			categories.put(doc_id, category);
		}
	}
	
	private void parseAttributes() {
		
		String[][] fields = new String[3][2];
		fields[0][0] = "<field_business_id>";
		fields[0][1] = "</field_business_id>";
		fields[1][0] = "<field_name>";
		fields[1][1] = "</field_name>";
		fields[2][0] = "<field_value>";
		fields[2][1] = "</field_value>";

		parser = new XMLParser(attribute_data.substring(0),fields);
		
		String doc_id = "";
		String[] field_data;
		List<String> attribute = new ArrayList<String>();
		
		while(parser.rowExist()) {
			
			field_data = parser.rowDataGet();	
			if(!doc_id.equals(field_data[0])) {
				
				if(!doc_id.equals("")) {
					attributes.put(doc_id, attribute);
				}
				
				doc_id = field_data[0];
				attribute = new ArrayList<String>();
			}	
			
			String value = getAttributeValue(field_data);
			if(!value.equals(""))
				attribute.add(getAttributeValue(field_data));
			
		}
		if(!doc_id.equals("")) {
			attributes.put(doc_id, attribute);
		}
	}

	public Map<String,String> getBusinessNames() {
		return business_name;
	}
	public Map<String,String> getNeighborhoods() {
		return neighborhood;
	}
	public Map<String,Double> getBusinessRatings() {
		return business_rating;
	}
	public Map<String,List<String>> getCategories()	{
		return categories;
	}
	public Map<String,List<String>> getAttributes()	{
		return attributes;
	}

	public String getBusinessName(String doc_id) {
		return business_name.get(doc_id);
	}
	public String getNeighborhood(String doc_id) {
		return neighborhood.get(doc_id);
	}
	public Double getBusinessRating(String doc_id) {
		return business_rating.get(doc_id);
	}
	public List<String> getCategory(String doc_id) {
		return categories.get(doc_id);
	}
	public List<String> getAttribute(String doc_id)	{	
		return attributes.get(doc_id);
	}
	public Set<String> getBusinessId() {
		return business_name.keySet();
	}
	
	public String getAttributeValue(String[] field_data) {
		
		String[] values;
		String attribute_value = "";
		switch (field_data[1])
		{
			case "Ambience":
			case "BusinessParking": 
			case "GoodForMeal":
			case "BestNights":
			case "DietaryRestrictions":
				field_data[2] = field_data[2].substring(1, field_data[2].length()-1);
				values = field_data[2].split("\\, ");
				for(int i=0;i<values.length;i++) {	
					String[] value = values[i].split("\\: ");
					value[0] = value[0].replaceAll("^\"|\"$", "");
					if(value[1].equals("true"))
						attribute_value = value[0].substring(1,value[0].length()-1);
				}
				break;
				
			case "RestaurantsAttire":
			case "AgesAllowed":
				attribute_value = field_data[2];
				break;
			
			case "WiFi":
				if(field_data[2].equals("free"))
					attribute_value = field_data[1];
				break;
			
			case "Alcohol":
			case "ByobCorkage": 
			case "Smoking": 
			case "RestaurantsPriceRange2": 
				attribute_value = field_data[1]+"_"+field_data[2];
				break;
			
			default:
				if(field_data[2].equals("1"))
					attribute_value = field_data[1];
				break;
		}
		return attribute_value;
	}
}
