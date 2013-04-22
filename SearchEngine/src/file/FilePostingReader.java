package file;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import file.manager.TermManager;
import searchengine.data.Posting;
import searchengine.data.PostingReader;

/**
 *
 * @author ZHS
 */
public class FilePostingReader extends PostingReader
{

	private TermManager termManager;
	private TermManager.TermPointer termPointer;

	public FilePostingReader(String term, TermManager termManager)
	{
		try
		{
			this.termManager = termManager;
			termPointer = termManager.getTermPointer(term);
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
			if (!termPointer.end())
				termManager.moveNext(termPointer);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FilePostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Posting read(boolean addPositions)
	{
		if (termPointer.end())
			return null;
		try
		{
			return termManager.getPosting(termPointer, addPositions);
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
		return termPointer.getCount();
	}
}
