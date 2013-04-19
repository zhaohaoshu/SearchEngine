package searchengine;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ZHS
 */
public class DocumentTokenizer
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
		TypeTokenizer tokenizer = new TypeTokenizer(new InputStreamReader(inputStream));
		int position = 0;
		for (;;)
		{
			String term = tokenizer.getNext(1);
			if (term == null)
				break;
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
}
