package searchengine.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import searchengine.Posting;
import searchengine.TypeTokenizer;
import searchengine.gae.GAEDictionary;
import searchengine.gae.GAEDocumentInfo;
import searchengine.gae.GAEPostingReader;

/**
 *
 * @author ZHS
 */
public class PositionalSearch
{

	private static void merge(
			LinkedList<int[]> results,
			int[] pointers,
			int[][] positions,
			ArrayList<Integer> distances,
			int count)
	{
		while (pointers[count] < positions[count].length &&
				(count == 0 ||
				positions[count - 1][pointers[count - 1]] + distances.get(count - 1) >=
				positions[count][pointers[count]]))
		{
			if (count == 0 ||
					positions[count][pointers[count]] > positions[count - 1][pointers[count - 1]])
				if (count < positions.length - 1)
					merge(results, pointers, positions, distances, count + 1);
				else
				{
					int[] result = new int[positions.length];
					for (int i = 0; i < positions.length; i++)
						result[i] = positions[i][pointers[i]];
					results.add(result);
				}
			pointers[count]++;
		}
		if (count > 0)
			while (pointers[count] > 0 &&
					(pointers[count] >= positions[count].length - 1 ||
					positions[count][pointers[count]] > positions[count - 1][pointers[count - 1]]))
				pointers[count]--;
	}

	public static void positionalSearch(String queryString, GAEDictionary dictionary, PositionalSearchResultWriter writer)
	{
		TypeTokenizer tokenizer = new TypeTokenizer(new StringReader(queryString));
		tokenizer.addTypes(new String[]
		{
			"/", "0123456789"
		});
		ArrayList<GAEPostingReader> readers = new ArrayList<>();
		ArrayList<Integer> distances = new ArrayList<>();
		int defaultDistance = 1;
		boolean needNumber = false;
		for (;;)
		{
			String token = tokenizer.getNext();
			int type = tokenizer.getStringType();
			if (type <= 0)
				break;
			switch (type)
			{
				case 1://token
					if (readers.size() > distances.size())
						distances.add(defaultDistance);
					readers.add(new GAEPostingReader(token.toLowerCase()));
					//readers.add(null);
					break;
				case 3://0123456789
					if (needNumber)
					{
						int distance = Integer.parseInt(token);
						if (readers.isEmpty())
							defaultDistance = distance;
						else
							distances.add(distance);
					}
					break;
			}
			needNumber = (type == 2);
		}

		int queryCount = readers.size();
		Posting[] postings = new Posting[queryCount];
		for (int i = 0; i < queryCount; i++)
			postings[i] = readers.get(i).read(false);

		int[][] positions = new int[queryCount][];
		for (;;)
		{
			long id = Common.getMinID(postings);
			if (id < 0)
				break;
			boolean flag = true;
			for (int i = 0; i < queryCount; i++)
				if (postings[i] == null || postings[i].getDocumentID() != id)
					flag = false;
			if (flag)
			{
				for (int i = 0; i < queryCount; i++)
				{
					readers.get(i).movePrevious();
					positions[i] = readers.get(i).read(true).getPositions();
				}
				LinkedList<int[]> results = new LinkedList<>();
				merge(results, new int[queryCount], positions, distances, 0);
				if (!results.isEmpty())
					writer.write(dictionary.getDocumentInfo(id), results);
			}
			for (int i = 0; i < queryCount; i++)
				if (postings[i] != null && postings[i].getDocumentID() == id)
					postings[i] = readers.get(i).read(false);
		}
	}

	public abstract static class PositionalSearchResultWriter
	{

		public abstract void write(GAEDocumentInfo documentInfo, List<int[]> results);
	}
}
