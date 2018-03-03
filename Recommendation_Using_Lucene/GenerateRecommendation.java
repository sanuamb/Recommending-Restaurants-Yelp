package LuceneApproach;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

public class IndexTester {

	private static final double RECOMMEND_RATING = 3.5;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String userIndexPath = "/Users/absheth/course/search/final_project/indexes/train_user_review/";
		String reviewsIndexPath = "/Users/absheth/course/search/final_project/indexes/rest_review/";
		String businessIndexPath = "/Users/absheth/course/search/final_project/indexes/business_index/";
		String testIndexPath = "/Users/absheth/course/search/final_project/indexes/test_index/";
		String userId = null;  // "-5e4VTnu_pR4Gpv3VSncaw" 
		 
		Map<String, Integer> hitMissMap = new HashMap<String, Integer>();
		try {
			IndexReader userIndexReader = DirectoryReader.open(FSDirectory.open(Paths.get(userIndexPath)));
			String queryString = null;
			double mape = 0.0;
			double sum = 0.0;
			// System.out.println("userIndexReader.maxDoc() --> " + userIndexReader.maxDoc());
			for (int i = 0; i < userIndexReader.maxDoc() ; i++) {
				int noOfHits = 0;
				int actual = 0;
				userId = userIndexReader.document(i).get("user_id");
				queryString = userIndexReader.document(i).get("user_query");
				// System.out.println(userId + " --> " + queryString.length());
				if (queryString.length() == 0) {
					// System.out.println("HERE - IN");
					// System.out.println(userId + " --> " + queryString);
					queryString = "Pizza burger fries drinks vodka whiskey";
					// System.out.println("HERE - OUT");
				}
				IndexReader reviewIndexReader = DirectoryReader.open(FSDirectory.open(Paths.get(reviewsIndexPath)));
				IndexSearcher reviewDocSearcher = new IndexSearcher(reviewIndexReader);
				Analyzer analyzer = new StandardAnalyzer();
				QueryParser parser = new QueryParser("business_reviews", analyzer);
				// System.out.println(userId + " --> " + queryString);
				Query query = parser.parse(QueryParserUtil.escape(queryString));
				Set<Term> queryTerms = new LinkedHashSet<Term>();
				reviewDocSearcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
				// reviewDocSearcher.setSimilarity(new BM25Similarity());
				reviewDocSearcher.setSimilarity(new ClassicSimilarity());
				
				
				TopDocs topDocs = reviewDocSearcher.search(query, 500);
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				
				List recommendRestList = new ArrayList();
				int count = 0;
				for (int k = 0; k < scoreDocs.length; k++) {
					String businessId = reviewDocSearcher.doc(scoreDocs[k].doc).get("business_id");
					double docScore = scoreDocs[k].score;
					if (recommendRestList.size() == 100) {
						break;
					}
					
					if (reviewDocSearcher.doc(scoreDocs[k].doc).get("user_list").indexOf(userId) == -1) {
						// recommendRestList.add(businessId);
						if (Double.parseDouble(reviewDocSearcher.doc(scoreDocs[k].doc).get("avg_rating")) >  RECOMMEND_RATING) {
							
							recommendRestList.add(businessId);
							
							/* StringBuilder top25Builder = new StringBuilder();
							
							
							BufferedWriter top25Buffer = null;
							FileWriter top25Writer = null;
							
							File top25File = new File("/Users/absheth/course/search/final_project/results/top.txt");
							
							if (!top25File.exists()) {
								top25File.createNewFile();
							}

							top25Writer = new FileWriter(top25File.getAbsoluteFile(), true);
							top25Buffer = new BufferedWriter(top25Writer);
							
							top25Builder.append(userId);
							top25Builder.append("\t");
							top25Builder.append(0);
							top25Builder.append("\t");
							top25Builder.append(businessId);
							top25Builder.append("\t");
							top25Builder.append(k+1);
							top25Builder.append("\t");
							top25Builder.append(docScore);
							top25Builder.append("\t");
							top25Builder.append("IndexTester");
							top25Builder.append("\n");
							top25Writer.write(top25Builder.toString());
							
							try {

								if (top25Buffer != null)
									top25Buffer.close();
								if (top25Writer != null)
									top25Writer.close();
								
							} catch (IOException ex) {

								ex.printStackTrace();

							}
							count++;*/
							
							
							/* IndexReader businessReader = DirectoryReader.open(FSDirectory.open(Paths.get(businessIndexPath)));
							
							for (int j = 0; j < businessReader.maxDoc(); j++) {
								// System.out.println(businessId);
								// System.out.println(businessReader.document(j).get("business_id"));
								
								if (businessReader.document(j).get("business_id").equals(businessId)) {
									if (businessReader.document(j).get("is_open").equals("1")) {
										recommendRestList.add(businessReader.document(j).get("name"));
										break;
									}
								}
							}
							businessReader.close();
							*/
							
							
						 }
					}
				}
				
				
				
				/*System.out.println("***Top 25 Recommendation***");
				for (Object name : recommendRestList) {
					System.out.println(name);
				}*/ 
				
				
				/*for (int k = 0; k < scoreDocs.length; k++) {
					String businessId = reviewDocSearcher.doc(scoreDocs[k].doc).get("business_id");
					double docScore = scoreDocs[k].score;
					if (recommendRestList.size() == 25) {
						break;
					}
					StringBuilder resultBuilder = new StringBuilder();
					
					BufferedWriter buffer = null;
					FileWriter writer = null;
					
					File resultFile = new File("/Users/absheth/course/search/final_project/results/all_results.txt");
					
					if (!resultFile.exists()) {
						resultFile.createNewFile();
					}

					writer = new FileWriter(resultFile.getAbsoluteFile(), true);
					buffer = new BufferedWriter(writer);
					
					resultBuilder.append(userId);
					resultBuilder.append("\t");
					resultBuilder.append(0);
					resultBuilder.append("\t");
					resultBuilder.append(businessId);
					resultBuilder.append("\t");
					resultBuilder.append(k+1);
					resultBuilder.append("\t");
					resultBuilder.append(docScore);
					resultBuilder.append("\t");
					resultBuilder.append("IndexTester");
					resultBuilder.append("\n");
					writer.write(resultBuilder.toString());
					
					try {

						if (buffer != null)
							buffer.close();
						if (writer != null)
							writer.close();
						
					} catch (IOException ex) {

						ex.printStackTrace();

					}
				}*/
				
				IndexReader testDataReader = DirectoryReader.open(FSDirectory.open(Paths.get(testIndexPath)));
				for (int j = 0; j < testDataReader.maxDoc(); j++) {
					
					// System.out.println(businessReader.document(j).get("business_id"));
					if (testDataReader.document(j).get("user_id").equals(userId)) {
						// System.out.println(userId);
						// System.out.println(testDataReader.document(j).get("businessIdList"));
						String [] businesIds = testDataReader.document(j).get("businessIdList").replace("[", " ").replace("]", " ").trim().split(",");
						actual = businesIds.length;
						String businessIdList = String.join("", businesIds);
						// System.out.println(businessIdList);
						for (int l = 0; l < recommendRestList.size() ; l++) {
							if (businessIdList.indexOf((String)recommendRestList.get(l)) != -1) {
								noOfHits++;
							}
						}
						hitMissMap.put(userId, noOfHits);
						//System.out.println();
						System.out.println("User ID --> " + userId + "  ||  Hits --> " + noOfHits + "  ||  Actual --> " + actual );
						//System.out.println("Actual --> " + actual);
						sum += (actual - noOfHits) / actual;
						// System.out.println("SUM --> " + sum);
						break;
					}
				}
				
				
				
			}
			IndexReader testDataReader = DirectoryReader.open(FSDirectory.open(Paths.get(testIndexPath)));
			System.out.println();
			// System.out.println("SUM --> " + sum);
			// System.out.println("testDataReader.maxDoc()--> " + testDataReader.maxDoc());
			mape = sum / testDataReader.maxDoc() * 100;
			System.out.println("MAPE --> " + String.format( "%.2f", mape ) + "%");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println();
			System.out.println("Completed.");
		}
	}
	
	// BACK UP
	/* public static void main(String[] args) {
		// TODO Auto-generated method stub
		String userIndexPath = "/Users/absheth/course/search/final_project/indexes/train_user_review/";
		String reviewsIndexPath = "/Users/absheth/course/search/final_project/indexes/rest_review/";
		String businessIndexPath = "/Users/absheth/course/search/final_project/indexes/business_index/";
		String testIndexPath = "/Users/absheth/course/search/final_project/indexes/test_user_review/";
		String userId = "-5e4VTnu_pR4Gpv3VSncaw"; 
		try {
			IndexReader userIndexReader = DirectoryReader.open(FSDirectory.open(Paths.get(userIndexPath)));
			// System.out.println(userIndexReader.maxDoc());
			// System.exit(0);
			// System.out.println(userIndexReader.docFreq(new Term("user_id", userId)));
			String queryString = null;
			boolean found = false;
			for (int i = 0; i < userIndexReader.maxDoc() ; i++) {
				if (userIndexReader.document(i).get("user_id").equals(userId)) {
					// System.out.println(String.join(" ", userIndexReader.document(i).get("user_query").replace("[", " ").replace("]", " ").trim().split(",")));
					queryString = userIndexReader.document(i).get("user_query");
					found = true;
					break;
				}
			}
			userIndexReader.close();
			if (!found) {
				//queryString = DEFAULTSTRING;  // Extract default keywords
				queryString = "Pizza burger fries drinks vodka whiskey";
			}
			
			IndexReader reviewIndexReader = DirectoryReader.open(FSDirectory.open(Paths.get(reviewsIndexPath)));
			IndexSearcher reviewDocSearcher = new IndexSearcher(reviewIndexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("business_reviews", analyzer);
			Query query = parser.parse(QueryParserUtil.escape(queryString));
			Set<Term> queryTerms = new LinkedHashSet<Term>();
			reviewDocSearcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
			reviewDocSearcher.setSimilarity(new BM25Similarity());
			TopDocs topDocs = reviewDocSearcher.search(query, 500);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			
			List recommendRestList = new ArrayList();
			
			for (int k = 0; k < scoreDocs.length; k++) {
				String businessId = reviewDocSearcher.doc(scoreDocs[k].doc).get("business_id");
				if (recommendRestList.size() == 25) {
					break;
				}
				// String.join("", queryList.toString().replace("[", " ").replace("]", " ").trim().split(",") + " ");
				// System.out.println(reviewDocSearcher.doc(scoreDocs[k].doc).get("business_id") + " | Score --> " + scoreDocs[k].score);
				// System.out.println(String.join("", reviewDocSearcher.doc(scoreDocs[k].doc).get("user_list").replace("[", " ").replace("]", " ").trim().split(",")));
				if (String.join("", reviewDocSearcher.doc(scoreDocs[k].doc).get("user_list").replace("[", " ").replace("]", " ").trim().split(",")).indexOf(userId) == -1) {
					if (Double.parseDouble(reviewDocSearcher.doc(scoreDocs[k].doc).get("avg_rating")) >  RECOMMEND_RATING) {
						IndexReader businessReader = DirectoryReader.open(FSDirectory.open(Paths.get(businessIndexPath)));
						
						for (int j = 0; j < businessReader.maxDoc(); j++) {
							// System.out.println(businessId);
							// System.out.println(businessReader.document(j).get("business_id"));
							
							if (businessReader.document(j).get("business_id").equals(businessId)) {
								if (businessReader.document(j).get("is_open").equals("1")) {
									recommendRestList.add(businessReader.document(j).get("name"));
									break;
								}
							}
						}
						businessReader.close();
					}
				}
			}
			System.out.println("***Top 25 Recommendation***");
			for (Object name : recommendRestList) {
				System.out.println(name);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
