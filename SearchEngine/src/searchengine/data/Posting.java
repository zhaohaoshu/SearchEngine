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
	private int[] positions;

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

	public Posting(long documentID, int[] positions)
	{
		this.documentID = documentID;
		this.positionCount = positions.length;
		this.positions = positions;
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

	public int[] getPositions()
	{
		return positions;
	}

	@Override
	public String toString()
	{
		ArrayList<Integer> list;
		if (positions == null)
			list = null;
		else
		{
			list = new ArrayList<>(positionCount);
			for (int position : positions)
				list.add(position);
		}
		return "{" + documentID + "," + positionCount + "," + list + '}';
	}
}
