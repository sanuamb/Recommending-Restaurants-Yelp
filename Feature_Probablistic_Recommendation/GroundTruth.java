package FeatureProbabilisticRecommendation;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroundTruth {
	
	private Map<String,List<String>> ground_truth;
	
	public void generateGroundTruth(String file_path) { 

		StringBuilder ground_truth_text = FileReader.getFileText(file_path);
		
		ground_truth = new HashMap<String,List<String>>();
		
		String[][] fields = new String[3][2];
		fields[0][0] = "<field_user_id>"; fields[0][1] = "</field_user_id>";
		fields[1][0] = "<field_business_id>";fields[1][1] = "</field_business_id>";
		fields[2][0] = "<field_date>";fields[2][1] = "</field_date>";

		XMLParser parser = new XMLParser(ground_truth_text.substring(0),fields);
		
		String user_id = "";
		String[] field_data;
		List<String> restaurants = new ArrayList<String>();
		
		while(parser.rowExist()) {	
			field_data = parser.rowDataGet();	
			if(!user_id.equals(field_data[0]))
			{	
				if(!user_id.equals(""))
				{
					ground_truth.put(user_id, restaurants);
				}
				user_id = field_data[0];
				restaurants = new ArrayList<String>();
			}			
			restaurants.add(field_data[1]);
		}
		if(!user_id.equals(""))
		{
			ground_truth.put(user_id, restaurants);
		}
	}
	
	public List<String> getGroundTruth(String user_id) {
		return ground_truth.get(user_id);
	}
	
	public void createGroundTruthFile(String path) throws IOException {
		
		FileWriter writer = new FileWriter(path);
		
		int user_count = 0;
		
		List<String> restaurants;
		
		RestaurantFeatures rf = RestaurantFeatures.getInstanceObject();
		Set<String> business_id = rf.getBusinessId();
		for (String user_id : ground_truth.keySet()) {
			user_count++;
			restaurants = ground_truth.get(user_id);
			for(String restaurant : business_id) {
				if(restaurants.contains(restaurant))
					writer.append(user_count+" 0 "+restaurant+" 1\n");
				else
					writer.append(user_count+" 0 "+restaurant+" 0\n");					
			}
			/*for(String restaurant : restaurants) {
				writer.append(user_id+" 0 "+restaurant+" 1\n");
			}*/
		}
		
		writer.flush();
		writer.close();
		System.out.println("Ground Truth File generated for "+user_count+" users");
	}
	public Set<String> getCurrentUsers() {
		return ground_truth.keySet();
	}
}
