package FeatureProbabilisticRecommendation;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RestaurantRecommendation {
	
	private UserIndex uIndex;
	private FeatureIndex fIndex;
	private GroundTruth groundTruth;
	
	public RestaurantRecommendation(String r_path,String n_path,String c_path,String a_path,String t_path) {

		uIndex = new UserIndex();
		uIndex.restaurantData(n_path, c_path, a_path);
		uIndex.indexData(r_path);
		
		fIndex = new FeatureIndex();
		
		groundTruth = new GroundTruth();
		groundTruth.generateGroundTruth(t_path);
	}
	
	public double recommendRestaurants(String user_id,int top_k,FileWriter writer,int user_count,int min_training, int min_testing) {
		
		Map<String,Double> recommendations = new HashMap<String,Double>();
		
		User user = uIndex.getUserData(user_id);
		
		if ( user == null ) {
			return -1;
	 	}
		else {
			
			if ( min_training == 0 || min_testing == 0) 
				min_training = 50; min_testing = 10;
			
			List<String> visited = user.getVisited();
			
			List<String> user_ground_truth = groundTruth.getGroundTruth(user_id);
			
			if(visited.size() < min_training || user_ground_truth.size() < min_testing) {
				return -1;
			}
			
			Map<String,Double> feature_scores = user.getNeighborhoodScores();		
			recommendations = getScoredRestaurants(feature_scores,recommendations,1);
			
			feature_scores = user.getCategoryScores();		
			recommendations = getScoredRestaurants(feature_scores,recommendations,1);
			
			feature_scores = user.getAttributeScores();		
			recommendations = getScoredRestaurants(feature_scores,recommendations,1);
			
			List<Entry<String, Double>> recommendation_list = sortSearchResults(recommendations);
			
			RestaurantFeatures restaurant_info = RestaurantFeatures.getInstanceObject();
			
			int count = 0;
			int matches = 0;

			int rank = 0;
			
			for(Entry<String,Double> restaurant : recommendation_list) {
				String business_id = restaurant.getKey();
				if(!visited.contains(business_id)) {					
					if(writer != null) {
						rank++;
						try {
							writer.append(user_count+" 0 "+business_id+" "+rank+" "+restaurant.getValue()+" RestaurantRecommendation\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}					
					if(user_ground_truth.contains(business_id))
						matches++;
					count++;	
					if(count >= top_k) {
						break;
					}
				}
			}
		
			return ((double)(user_ground_truth.size() - matches)/user_ground_truth.size());
			
		}
	}
	
	public void runGroundTruthUsers(int recom_count,int min_training,int min_testing){
		
		int users = 0;
		double total_error = 0.0,error = 0.0;
		for(String user_id : groundTruth.getCurrentUsers()) {
			error = recommendRestaurants(user_id,recom_count,null,0,min_training,min_testing);
			if ( error > 0 ) {
				total_error += error;
				if(error != -1 )
					users++;
			}
		}
		
		System.out.println("MAPE for "+users+" users is : "+((total_error*100)/users));
		
	}
	
	public void generateRecommendationWithFile(boolean generateGroundTruth,int recom_count,int min_training,int min_testing) {
		
		FileWriter writer = null;
		
		try {
			writer = new FileWriter("..\\recommendations.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int users = 0;
		double error = 0.0;
		
		for(String user_id : groundTruth.getCurrentUsers()) {
			error = recommendRestaurants(user_id,recom_count,writer,users,min_training,min_testing);
			if ( error != -1 )
				users++;
		}
		
		try {
			writer.flush();
			writer.close();
			System.out.println("Recommendation file generated for "+users+" users");
			if ( generateGroundTruth == true )
				groundTruth.createGroundTruthFile("..\\ground_truth.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Map<String,Double> getScoredRestaurants(Map<String,Double> feature_scores, Map<String,Double> recommendations, float alpha) {
		
		for(String feature : feature_scores.keySet()) {
			
			double score = feature_scores.get(feature);
			
			List<String> restaurants = fIndex.getFeatureBusinessList(feature);
			
			for(String restaurant : restaurants) {				
				double r_score = score;				
				if(recommendations.containsKey(restaurant)) {
					r_score = (recommendations.get(restaurant)*alpha) + r_score;
				}				
				recommendations.put(restaurant,r_score);	
			}
		}		
		return recommendations;
	}

	public static List<Entry<String, Double>> sortSearchResults(Map<String, Double> searchResult)
    {

        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(searchResult.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2)
            {	
            	return o2.getValue().compareTo(o1.getValue());
            }
        });

        return list;
    }
	
	public static void main(String args[]) {
		
		/*
		String n_path = "src\\FeatureProbabilisticRecommendation\\data\\Las_Vegas\\BusinessInformation.xml",
			   c_path = "src\\FeatureProbabilisticRecommendation\\data\\Las_Vegas\\Category.xml",
			   a_path = "src\\FeatureProbabilisticRecommendation\\data\\Las_Vegas\\Attribute.xml",
			   r_path = "src\\FeatureProbabilisticRecommendation\\data\\Las_Vegas\\TrainingReviews.xml",
			   t_path = "src\\FeatureProbabilisticRecommendation\\data\\Las_Vegas\\TestingReviews.xml";

		int recom_count = 100,
			min_training = 1,
			min_testing = 1;
		
		*/
		String n_path = "src\\FeatureProbabilisticRecommendation\\data\\Charlotte\\BusinessInformation.xml",
			   c_path = "src\\FeatureProbabilisticRecommendation\\data\\Charlotte\\Category.xml",
			   a_path = "src\\FeatureProbabilisticRecommendation\\data\\Charlotte\\Attribute.xml",
			   r_path = "src\\FeatureProbabilisticRecommendation\\data\\Charlotte\\TrainingReviews.xml",
			   t_path = "src\\FeatureProbabilisticRecommendation\\data\\Charlotte\\TestingReviews.xml";
		
		int recom_count = 5,
			min_training = 1,
			min_testing = 1;
		
		RestaurantRecommendation recommender = new RestaurantRecommendation(r_path,n_path,c_path,a_path,t_path);
		
		recommender.runGroundTruthUsers(recom_count,min_training,min_testing);
		//recommender.generateRecommendationWithFile(true,recom_count,min_training,min_testing);
		
	}
}
