package searchengine.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import searchengine.data.Posting;
import searchengine.TypeTokenizer;
import searchengine.data.PostingReader;
import searchengine.data.SearchDataManager;

/**
 *
 * @author ZHS
 */
public class VectorSearch {

	/**
	 * Tokenize the query
	 *
	 * @param queryString
	 * @return
	 */
	private static Map<String, Integer> tokenizeEntry(String queryString) {
		TypeTokenizer tokenizer = new TypeTokenizer(queryString);
		List<String> tokens = tokenizer.getStrings(1);
		HashMap<String, Integer> entries = new HashMap<>();
		for (String token : tokens) {
			Integer get = entries.get(token);
			if (get == null)
				entries.put(token, 1);
			else
				entries.put(token, get + 1);
		}
		return entries;
	}

	private static void countQuery(
			Map<String, Integer> entries, SearchDataManager manager,
			ArrayList<PostingReader> readers, int[] queryCount) {
		int i = 0;
		for (Map.Entry<String, Integer> entry : entries.entrySet()) {
			readers.add(manager.getPostingReader(entry.getKey().toLowerCase()));
			queryCount[i] = entry.getValue();
			i++;
		}
	}

	public static void vectorSearch(
			String queryString, SearchDataManager manager,
			VectorSearchResultWriter writer) {
		Map<String, Integer> entries = tokenizeEntry(queryString);
		int entryCount = entries.size();

		//prepare data
		ArrayList<PostingReader> readers = new ArrayList<>();
		int[] queryCount = new int[entryCount];
		countQuery(entries, manager, readers, queryCount);

		long documentCount = manager.getDocumentCount();
		double[] queryProduct = new double[entryCount];
		double querySquarSum = 0;
		for (int i = 0; i < entryCount; i++) {
			long count = readers.get(i).getCount();
			if (count > 0) {
				queryProduct[i] = (1 + Math.log(queryCount[i])) *
						Math.log((double) (documentCount + 1) / count);
				querySquarSum += queryProduct[i] * queryProduct[i];
			}
		}
		for (int i = 0; i < entryCount; i++)
			queryProduct[i] /= Math.sqrt(querySquarSum);
//		System.out.println("query products");
//		for (double d : queryProduct)
//			System.out.println(d);
//		System.out.println("------");

		Posting[] postings = new Posting[entryCount];
		for (int i = 0; i < entryCount; i++) {
			postings[i] = readers.get(i).read();
			readers.get(i).moveNext();
		}

		for (;;) {
			//get the first document id
			long id = Common.getMinID(postings);
			if (id < 0)
				break;
			//do the score
			double score = 0;
			for (int i = 0; i < entryCount; i++)
				if (postings[i] != null && postings[i].getDocumentID() == id) {
					double documentProduct = 1 + Math.log(postings[i].getPositionCount());
					score += queryProduct[i] * documentProduct;
					postings[i] = readers.get(i).read();
					readers.get(i).moveNext();
				}
			score /= manager.getDocumentLength(id);
			writer.write(score, id);
		}
	}

	public static abstract class VectorSearchResultWriter {

		public abstract void write(double score, long documentID);
	}
}
