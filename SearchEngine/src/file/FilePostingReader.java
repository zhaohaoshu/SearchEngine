package file;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import file.manager.PostingManager;
import searchengine.data.Posting;
import searchengine.data.PostingReader;

/**
 *
 * @author ZHS
 */
public class FilePostingReader extends PostingReader
{

	private PostingManager termManager;
	private PostingManager.PostingPointer postingPointer;

	public FilePostingReader(String term, PostingManager postingManager)
	{
		try
		{
			this.termManager = postingManager;
			postingPointer = postingManager.getTermPointer(term);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FilePostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void moveNext()
	{
		try
		{
			if (!postingPointer.end())
				postingPointer.moveNext();
		}
		catch (IOException ex)
		{
			Logger.getLogger(FilePostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Posting read()
	{
		if (postingPointer.end())
			return null;
		try
		{
			return postingPointer.getPosting();
		}
		catch (IOException ex)
		{
			Logger.getLogger(FilePostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public long getCount()
	{
		return postingPointer.getPostingCount();
	}
}
