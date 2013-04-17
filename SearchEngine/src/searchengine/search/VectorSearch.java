package searchengine.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import searchengine.Posting;
import searchengine.TypeTokenizer;
import searchengine.gae.GAEDictionary;
import searchengine.gae.GAEDocumentInfo;
import searchengine.gae.GAEPostingReader;

/**
 *
 * @author ZHS
 */
public class VectorSearch
{

	/**
	 * Tokenize the query
	 *
	 * @param queryString
	 * @return
	 */
	private static Map<String, Integer> tokenizeEntry(String queryString)
	{
		TypeTokenizer tokenizer = new TypeTokenizer(new StringReader(queryString));
		List<String> tokens = tokenizer.getStrings(1);
		HashMap<String, Integer> entries = new HashMap<>();
		for (String token : tokens)
		{
			Integer get = entries.get(token);
			if (get == null)
				entries.put(token, 1);
			else
				entries.put(token, get + 1);
		}
		return entries;
	}

	private static void countQuery(Map<String, Integer> entries, GAEPostingReader[] readers, int[] queryCount)
	{
		int i = 0;
		for (Map.Entry<String, Integer> entry : entries.entrySet())
		{
			readers[i] = new GAEPostingReader(entry.getKey().toLowerCase());
			queryCount[i] = entry.getValue();
			i++;
		}
	}

	public static VectorSearchResult[] vectorSearch(String queryString, GAEDictionary dictionary)
	{
		Map<String, Integer> entries = tokenizeEntry(queryString);
		int entryCount = entries.size();

		//prepare data
		GAEPostingReader[] postingReaders = new GAEPostingReader[entryCount];
		int[] queryCount = new int[entryCount];
		countQuery(entries, postingReaders, queryCount);

		long documentCount = dictionary.getDocumentCount();
		double[] queryProduct = new double[entryCount];
		double querySquarSum = 0;
		for (int i = 0; i < entryCount; i++)
			if (postingReaders[i].getCount() > 0)
			{
				queryProduct[i] = (1 + Math.log(queryCount[i])) *
						Math.log((double) (documentCount + 1) / postingReaders[i].getCount());
				querySquarSum += queryProduct[i] * queryProduct[i];
			}
		for (int i = 0; i < entryCount; i++)
			queryProduct[i] /= Math.sqrt(querySquarSum);
//		System.out.println("query products");
//		for (double d : queryProduct)
//			System.out.println(d);
//		System.out.println("------");

		Posting[] postings = new Posting[entryCount];
		for (int i = 0; i < entryCount; i++)
			postings[i] = postingReaders[i].read(false);

		ArrayList<VectorSearchResult> resultList = new ArrayList<>();
		for (;;)
		{
			//get the first document id
			long id = Common.getMinID(postings);
			if (id < 0)
				break;
			GAEDocumentInfo documentInfo = dictionary.getDocumentInfo(id);
			//do the score
			double score = 0;
			for (int i = 0; i < entryCount; i++)
				if (postings[i] != null && postings[i].getDocumentID() == id)
				{
					double documentProduct = 1 + Math.log(postings[i].getSize());
					score += queryProduct[i] * documentProduct;
					postings[i] = postingReaders[i].read(false);
				}
			score /= documentInfo.getLength();
			resultList.add(new VectorSearchResult(score, documentInfo));
		}
		VectorSearchResult[] results = resultList.toArray(new VectorSearchResult[resultList.size()]);
		Arrays.sort(results);
		return results;
	}

	//<editor-fold defaultstate="collapsed" desc="Result">
	public static class VectorSearchResult implements Comparable<VectorSearchResult>
	{

		private double score;
		private GAEDocumentInfo documentInfo;

		public VectorSearchResult(double score, GAEDocumentInfo documentInfo)
		{
			this.score = score;
			this.documentInfo = documentInfo;
		}

		public double getScore()
		{
			return score;
		}

		public GAEDocumentInfo getDocumentInfo()
		{
			return documentInfo;
		}

		@Override
		public int compareTo(VectorSearchResult o)
		{
			if (score < o.score)
				return 1;
			if (score > o.score)
				return -1;
			return 0;
		}
	}
	//</editor-fold>
}
