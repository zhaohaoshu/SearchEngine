package searchengine.data;

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
		this.positions = positions;
	}

	public Posting(long documentID, int size, int[] positions)
	{
		this.documentID = documentID;
		this.size = size;
		this.positions = positions;
	}

	public long getDocumentID()
	{
		return documentID;
	}

	/**
	 * How many of this term in this document. Same as <code>positions.length</code>
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
}
