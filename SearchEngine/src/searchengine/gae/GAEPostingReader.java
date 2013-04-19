package searchengine.gae;

import searchengine.gae.entity.EntityTermManager;
import searchengine.gae.entity.EntityPostingManager;
import searchengine.data.Posting;
import searchengine.data.PostingReader;

/**
 *
 * @author ZHS
 */
public class GAEPostingReader extends PostingReader
{

	private long currentCount;
	private long maxCount;
	private String term;

	public GAEPostingReader(String term)
	{
		this.term = term;
		currentCount = 1;
		maxCount = EntityTermManager.getCount(term);
	}

	@Override
	public void movePrevious()
	{
		if (currentCount > 1)
			currentCount--;
	}

	@Override
	public Posting read(boolean addPositions)
	{
		if (currentCount > maxCount)
			return null;
		Posting posting = EntityPostingManager.getPosting(term, currentCount, addPositions);
		if (posting != null)
			currentCount++;
		return posting;
	}

	@Override
	public long getCount()
	{
		return maxCount;
	}
}
