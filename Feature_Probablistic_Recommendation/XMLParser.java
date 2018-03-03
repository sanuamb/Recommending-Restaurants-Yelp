package FeatureProbabilisticRecommendation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLParser 
{
	private Matcher row_matcher_start;	
	private Matcher row_matcher_end;
	
	private String[][] field_tags;
	private String row_text;
	private String parser_data;
	
	public XMLParser(String doc_text,String[][] fields) {
		field_tags = fields;
		parser_data = doc_text;
		row_matcher_start = Pattern.compile("<row>").matcher(doc_text);
		row_matcher_end = Pattern.compile("</row>").matcher(doc_text);
	}
	public boolean rowExist() {
		if (row_matcher_start.find()) {
			row_matcher_end.find();
			row_text = parser_data.substring(row_matcher_start.end(),row_matcher_end.start());					
			return true;
		}
		else {
			return false;
		}
	}
	public String[] rowDataGet() {	
		String fields_data[] = new String[field_tags.length];
		Matcher matcher_field_start, matcher_field_end;
		
		for(int i=0;i<field_tags.length;i++) {
			matcher_field_start = Pattern.compile(field_tags[i][0]).matcher(row_text);
			matcher_field_end = Pattern.compile(field_tags[i][1]).matcher(row_text);
			while(matcher_field_start.find()) {
				matcher_field_end.find();
				fields_data[i] = row_text.substring(matcher_field_start.end(),matcher_field_end.start());
			}
		}		
		return fields_data;
	}
}
