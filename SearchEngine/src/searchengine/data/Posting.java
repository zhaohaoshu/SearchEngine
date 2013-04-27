package searchengine.data;

import java.util.ArrayList;

/**
 *
 * @author ZHS
 */
public class Posting
{

	private long documentID;
	private int positionCount;

	/**
	 *
	 * @param documentID
	 * @param positionCount the number of positions
	 */
	public Posting(long documentID, int positionCount)
	{
		this.documentID = documentID;
		this.positionCount = positionCount;
	}

	public long getDocumentID()
	{
		return documentID;
	}

	/**
	 * How many of this term in this document. Same as
	 * <code>positions.length</code>
	 *
	 * @return
	 */
	public int getPositionCount()
	{
		return positionCount;
	}

	@Override
	public String toString()
	{
		return "{" + documentID + "," + positionCount + '}';
	}
}
