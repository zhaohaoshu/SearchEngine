package searchengine;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ZHS
 */
public class DocumentAnalyser
{

	/**
	 * Tokenize the input into map
	 *
	 * @param inputStream
	 * @param map
	 * @return The number of tokens
	 */
	public static int tokenizeDocument(InputStream inputStream, Map<String, LinkedList<Integer>> map)
	{
		TypeTokenizer tokenizer = new TypeTokenizer(inputStream);
		int position = 0;
		for (;;)
		{
			String term = tokenizer.getNext(1);
			if (term == null)
				break;
			term = term.toLowerCase();
			LinkedList<Integer> positions = map.get(term);
			if (positions == null)
			{
				positions = new LinkedList<>();
				map.put(term, positions);
			}
			positions.addLast(position);
			position++;
		}
		return position;
	}

	public static double calcDocumentLength(Map<String, LinkedList<Integer>> map)
	{
		double length = 0;
		for (Map.Entry<String, LinkedList<Integer>> entry : map.entrySet())
		{
			double tf = 1 + Math.log(entry.getValue().size());
			length += tf * tf;
		}
		return Math.sqrt(length);
	}
}
