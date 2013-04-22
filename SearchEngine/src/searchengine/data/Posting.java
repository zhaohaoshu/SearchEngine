package searchengine.data;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author ZHS
 */
public class Posting
{

	private long documentID;
	private int size;
	private int[] positions;

	/**
	 *
	 * @param documentID
	 * @param size the number of positions
	 */
	public Posting(long documentID, int size)
	{
		this.documentID = documentID;
		this.size = size;
	}

	public Posting(long documentID, int[] positions)
	{
		this.documentID = documentID;
		this.size = positions.length;
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
	public int getSize()
	{
		return size;
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
			list = new ArrayList<>(size);
			for (int position : positions)
				list.add(position);
		}
		return "{" + documentID + "," + size + "," + list + '}';
	}
}
