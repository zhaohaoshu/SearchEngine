package searchengine;

import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author ZHS
 */
public class DocumentAnalyser {

	/**
	 * Tokenize the input into map
	 *
	 * @param inputStream
	 * @param map
	 * @return The number of tokens
	 */
	public static int tokenizeDocument(InputStream inputStream, Map<String, Integer> map) {
		TypeTokenizer tokenizer = new TypeTokenizer(inputStream);
		int position = 0;
		for (;;) {
			String term = tokenizer.getNext(1);
			if (term == null)
				break;
			term = term.toLowerCase();
			Integer positionCount = map.get(term);
			if (positionCount == null)
				map.put(term, 1);
			else
				map.put(term, positionCount + 1);
			position++;
		}
		return position;
	}

	public static double calcDocumentLength(Map<String, Integer> map) {
		double length = 0;
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			double tf = 1 + Math.log(entry.getValue());
			length += tf * tf;
		}
		return Math.sqrt(length);
	}
}
