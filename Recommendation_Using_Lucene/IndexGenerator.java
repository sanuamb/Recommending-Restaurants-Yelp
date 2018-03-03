package LuceneApproach;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Recommendation {

	public static void main(String[] args) {
		try {
			indexUserReviews();
			indexRestReviews();
			indexBusiness();
			indexTestData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static final String TEST_YEAR = "2017";
	
	
	private static void indexUserReviews () {
		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File("/Users/absheth/course/search/final_project/xml_data/review_user.xml"));

			doc.getDocumentElement().normalize();

			//System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("row");
			String current_id = null;
			String next_id = null;
			String date = null;
			StringBuilder allReviewString = new StringBuilder();
			
			// Lucene stuff
			Directory trainDir = FSDirectory.open(Paths.get("/Users/absheth/course/search/final_project/indexes/train_user_review/"));
			Analyzer trainAnalyzer = new StandardAnalyzer();
			
			
			IndexWriterConfig trainIWC = new IndexWriterConfig(trainAnalyzer);
			trainIWC.setOpenMode(OpenMode.CREATE);
			IndexWriter trainWriter = new IndexWriter(trainDir, trainIWC);
			org.apache.lucene.document.Document trainLDoc = null;
			
			// ADD REFERENCE
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
			
			List queryList = new ArrayList();
			List dateConsidered = new ArrayList();
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					next_id = eElement.getElementsByTagName("user_id").item(0).getTextContent();
					date = eElement.getElementsByTagName("date").item(0).getTextContent();
					
					if (current_id == null) {
						current_id = next_id;
					}
					
					// Separating test data and training data || Consider users reviews before 2016. Ignore all the 2017 reviews.
					if (date.indexOf(TEST_YEAR) != -1) {
						continue;
					}
					
					
					if (current_id.equals(next_id)) {
						allReviewString.append(eElement.getElementsByTagName("text").item(0).getTextContent() + " ");
						dateConsidered.add(date); // Debug
					} else {
						StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
						Annotation document = new Annotation(allReviewString.toString().replace("\n", " ").replace("\r", " "));
						pipeline.annotate(document);

						List<CoreMap> sentences = document.get(SentencesAnnotation.class);

						// Extract all the propernouns as query terms of a particular user.
						for (CoreMap sentence : sentences) {
							for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
								if (queryList.size() == 1023) {
									break;
								}
								String word = token.get(TextAnnotation.class).toLowerCase();
								String pos = token.get(PartOfSpeechAnnotation.class);
								String ne = token.get(NamedEntityTagAnnotation.class);
								// pos.equals("NNP") || pos.equals("NNPS") || pos.equals("NNS") || pos.equals("NN")  // DECIDE ON THIS.
								if ((pos.equals("NNP") || pos.equals("NNPS") || pos.equals("NNS"))  && !ne.equals("PERSON") ) {
									if (word.indexOf(".") == -1 && word.indexOf(",") == -1 && word.indexOf("?") == -1 && word.indexOf("!") == -1 && word.indexOf("-") == -1 && word.indexOf("/") == -1) {
										
										// Ignoring words with less length less than 3 
										if (!queryList.contains(word) && word.length()>3 && !word.equalsIgnoreCase("Charlotte")) {
											queryList.add(word);
										}
									}
								}
							}
						}
						
						trainLDoc = new org.apache.lucene.document.Document();
						String query = String.join("", queryList.toString().replace("[", " ").replace("]", " ").trim().split(",")) + " ";
						trainLDoc.add(new StringField("user_id", current_id,
								Field.Store.YES));
						trainLDoc.add(new StringField("user_query", query.trim(),
								Field.Store.YES));
						
						BufferedWriter buffer = null;
						// BufferedWriter buffer2 = null;
						FileWriter f_writer = null;
						// FileWriter f_writer2 = null;
						File output_file = new File("/Users/absheth/course/search/final_project/indexes/user_reviews_out.txt");
						// File output_file2 = new File("/Users/absheth/course/search/final_project/indexes/skipwords.txt");
						
						if (!output_file.exists()) {
							output_file.createNewFile();
						}
						// if (!output_file2.exists()) {
						//	output_file2.createNewFile();
						// }
						
						f_writer = new FileWriter(output_file.getAbsoluteFile(), true);
						// f_writer2 = new FileWriter(output_file2.getAbsoluteFile(), true);
						buffer = new BufferedWriter(f_writer);
						// buffer2 = new BufferedWriter(f_writer2);
						StringBuilder query_builder = new StringBuilder();
						// StringBuilder query_builder2 = new StringBuilder();
						query_builder.append(current_id);
						query_builder.append("\n");
						query_builder.append("\n");
						query_builder.append(query);
						query_builder.append("\n");
						query_builder.append("\n");
						query_builder.append("Year --> " + dateConsidered.toString());
						
						
						query_builder.append("\n");
						query_builder.append("\n");
						query_builder.append("*************************************");
						query_builder.append("\n");
						query_builder.append("\n");
						// query_builder2.append(query);
						// System.out.println(query_builder2.toString());
						f_writer.write(query_builder.toString());
						// f_writer2.write(query_builder2.toString());
						
						
						try {
							
							if (buffer != null)
								buffer.close();
							if (f_writer != null)
								f_writer.close();
							
							// if (buffer2 != null)
							// 	buffer2.close();
							// if (f_writer2 != null)
							// 	f_writer2.close();
							
						} catch (IOException ex) {

							ex.printStackTrace();

						}
						
						 
						trainWriter.addDocument(trainLDoc);
						current_id = next_id;
						allReviewString = new StringBuilder();
						allReviewString.append(eElement.getElementsByTagName("text").item(0).getTextContent());
						// userRatingSum = Double.parseDouble(eElement.getElementsByTagName("stars").item(0).getTextContent());
						dateConsidered.add(date);
						queryList.clear();
						dateConsidered.clear();
					}
				}
			}
			
			trainWriter.forceMerge(1);
			trainWriter.commit();
			trainWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("User reviews indexed.");
		}
	}
	
	private static void indexRestReviews() {

		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File("/Users/absheth/course/search/final_project/xml_data/training_data.xml"));

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("row");
			
			String current_id = null;
			String next_id = null;
			IndexWriterConfig iwc = null;
			Directory dir = null;
			IndexWriter writer = null;
			Analyzer analyzer = null;
			org.apache.lucene.document.Document lDoc = null;
			StringBuilder allReviewStrBuilder = new StringBuilder();
			StringBuilder userQuery = new StringBuilder();
			double userRatingSum = 0.0;
			double averageUserRating = 0.0;
			int count = 0;
			dir = FSDirectory.open(Paths.get("/Users/absheth/course/search/final_project/indexes/rest_review/"));
			List queryList = new ArrayList();
			analyzer = new StandardAnalyzer();
			iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
			List<String> userIdList = new ArrayList<String>();
			
			
			
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					next_id = eElement.getElementsByTagName("business_id").item(0).getTextContent();
					
					if (current_id == null) {
						current_id = next_id;
					}
					
					if (current_id.equals(next_id)) {
						count++;
						allReviewStrBuilder.append(eElement.getElementsByTagName("text").item(0).getTextContent() + " ");
						userRatingSum += Double.parseDouble(eElement.getElementsByTagName("stars").item(0).getTextContent());
						String user_id = eElement.getElementsByTagName("user_id").item(0).getTextContent();
						if (!userIdList.contains(user_id)) {
							userIdList.add(user_id);
						}
						
					} else {
						
						String allReviewStr = allReviewStrBuilder.toString().replace("\n", " ").replace("\r", " ").toString().trim();
						
						lDoc = new org.apache.lucene.document.Document();
						
						lDoc.add(new StringField("business_id", current_id,
								Field.Store.YES));
						lDoc.add(new TextField("business_reviews", allReviewStr,
								Field.Store.YES));
						lDoc.add(new StringField("user_list", userIdList.toString(),
								Field.Store.YES));
						lDoc.add(new StringField("avg_rating", String.valueOf(userRatingSum/count),
								Field.Store.YES));
						
						BufferedWriter buffer = null;
						FileWriter f_writer = null;
						File output_file = new File("/Users/absheth/course/search/final_project/indexes/reviews_out.txt");
						
						if (!output_file.exists()) {
							output_file.createNewFile();
						}
						
						f_writer = new FileWriter(output_file.getAbsoluteFile(), true);
						buffer = new BufferedWriter(f_writer);
						StringBuilder query_builder = new StringBuilder();
						query_builder.append(current_id);
						
						query_builder.append("\n");
						query_builder.append("\n");
						query_builder.append("FULL STRING --> " + allReviewStr);
						query_builder.append("\n");
						query_builder.append("\n");
						query_builder.append("USER ID LIST --> " + userIdList.toString());
						query_builder.append("\n");
						query_builder.append("\n");
						query_builder.append("Average Rating --> " + String.valueOf(userRatingSum/count));
						query_builder.append("\n");
						query_builder.append("*********************************************************************************");
						query_builder.append("\n");
						query_builder.append("\n");
						
						
						f_writer.write(query_builder.toString());
						
						
						try {
							
							if (buffer != null)
								buffer.close();
							if (f_writer != null)
								f_writer.close();
							
						} catch (IOException ex) {

							ex.printStackTrace();

						}
						writer.addDocument(lDoc);
						current_id = next_id;
						allReviewStrBuilder = new StringBuilder();
						allReviewStrBuilder.append(eElement.getElementsByTagName("text").item(0).getTextContent() + " ");
						userRatingSum = Double.parseDouble(eElement.getElementsByTagName("stars").item(0).getTextContent());
						userIdList.clear();
						count = 1; 
						
					}
				}
			}
			writer.forceMerge(1);
			writer.commit();
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Restaurtant reviews indexed.");
		}
	
		
	}
	private static void indexBusiness() {

		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File("/Users/absheth/course/search/final_project/xml_data/business.xml"));

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("row");
			IndexWriterConfig iwc = null;
			Directory dir = null;
			IndexWriter writer = null;
			Analyzer analyzer = null;
			org.apache.lucene.document.Document lDoc = null;
			dir = FSDirectory.open(Paths.get("/Users/absheth/course/search/final_project/indexes/business_index/"));

			analyzer = new StandardAnalyzer();
			iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
			String business_id = null;
			String name = null;
			String isOpen = null;
			String rating = null;
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					business_id = eElement.getElementsByTagName("business_id").item(0).getTextContent();
					name = eElement.getElementsByTagName("name").item(0).getTextContent();
					rating = eElement.getElementsByTagName("stars").item(0).getTextContent();
					isOpen = eElement.getElementsByTagName("is_open").item(0).getTextContent();
					lDoc = new org.apache.lucene.document.Document();
					
					lDoc.add(new StringField("business_id", business_id,
							Field.Store.YES));
					lDoc.add(new StringField("name", name,
							Field.Store.YES));
					lDoc.add(new StringField("rating", rating,
							Field.Store.YES));
					lDoc.add(new StringField("is_open", isOpen,
							Field.Store.YES));
					
					BufferedWriter buffer = null;
					FileWriter f_writer = null;
					File output_file = new File("/Users/absheth/course/search/final_project/indexes/business_out.txt");
					
					if (!output_file.exists()) {
						output_file.createNewFile();
					}
					
					f_writer = new FileWriter(output_file.getAbsoluteFile(), true);
					buffer = new BufferedWriter(f_writer);
					StringBuilder query_builder = new StringBuilder();
					
					query_builder.append("\n");
					query_builder.append("\n");
					query_builder.append(business_id);
					query_builder.append("\n");
					query_builder.append("\n");
					query_builder.append(name);
					query_builder.append("\n");
					query_builder.append("\n");
					query_builder.append(rating);
					query_builder.append("\n");
					query_builder.append("\n");
					query_builder.append(isOpen);
					
					query_builder.append("\n");
					query_builder.append("*********************************************************************************");
					query_builder.append("\n");
					query_builder.append("\n");
					
					
					f_writer.write(query_builder.toString());
					
					
					try {
						
						if (buffer != null)
							buffer.close();
						if (f_writer != null)
							f_writer.close();
						
					} catch (IOException ex) {

						ex.printStackTrace();

					}
					writer.addDocument(lDoc);
				}
			}
			writer.forceMerge(1);
			writer.commit();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Businesses indexed.");
		}
	}
	private static void indexTestData () {
		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File("/Users/absheth/course/search/final_project/xml_data/review_user.xml"));

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("row");
			String current_id = null;
			String next_id = null;
			String date = null;
			StringBuilder allReviewString = new StringBuilder();
			List testBusinessList = new ArrayList();
			List dateConsidered = new ArrayList();
			// Lucene stuff
			Directory testDir = FSDirectory.open(Paths.get("/Users/absheth/course/search/final_project/indexes/test_index/"));
			Analyzer testAnalyzer = new StandardAnalyzer();
			IndexWriterConfig testIWC = new IndexWriterConfig(testAnalyzer);
			testIWC.setOpenMode(OpenMode.CREATE);
			IndexWriter testWriter = new IndexWriter(testDir, testIWC);
			org.apache.lucene.document.Document testLDoc = null;
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					next_id = eElement.getElementsByTagName("user_id").item(0).getTextContent();
					date = eElement.getElementsByTagName("date").item(0).getTextContent();
					
					if (current_id == null) {
						current_id = next_id;
					}
					
					// Separating test data and training data || Consider users reviews before 2016. Ignore all the 2017 reviews.
					if (date.indexOf(TEST_YEAR) != -1) {
						if (current_id.equals(next_id)) {
							testBusinessList.add(eElement.getElementsByTagName("business_id").item(0).getTextContent());
							dateConsidered.add(date);
						} else {
							dateConsidered.add(date);
							testLDoc = new org.apache.lucene.document.Document();
							testLDoc.add(new StringField("user_id", current_id,
									Field.Store.YES));
							testLDoc.add(new StringField("businessIdList", testBusinessList.toString(),
									Field.Store.YES));
							
							
							// Debug
							BufferedWriter buffer = null;
							FileWriter f_writer = null;
							File output_file = new File("/Users/absheth/course/search/final_project/indexes/test_data.txt");
							if (!output_file.exists()) {
								output_file.createNewFile();
							}
							f_writer = new FileWriter(output_file.getAbsoluteFile(), true);
							buffer = new BufferedWriter(f_writer);
							
							StringBuilder query_builder = new StringBuilder();
							
							query_builder.append("user_id --> " + current_id);
							query_builder.append("\n");
							query_builder.append("\n");
							query_builder.append("businessIdList --> " + testBusinessList.toString());
							query_builder.append("\n");
							query_builder.append("\n");
							query_builder.append("Dates --> " + dateConsidered.toString());
							query_builder.append("\n");
							query_builder.append("\n");
							query_builder.append("*************************************");
							query_builder.append("\n");
							query_builder.append("\n");
							
							f_writer.write(query_builder.toString());
							
							try {
								
								if (buffer != null)
									buffer.close();
								if (f_writer != null)
									f_writer.close();
							} catch (IOException ex) {

								ex.printStackTrace();

							}
							
							testWriter.addDocument(testLDoc);
							testBusinessList.clear();
							testBusinessList.add(eElement.getElementsByTagName("business_id").item(0).getTextContent());
							dateConsidered.clear();
							current_id = next_id;
						}
					}

				}
			}
			testWriter.forceMerge(1);
			testWriter.commit();
			testWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Test data indexed.");
		}
	}

}
