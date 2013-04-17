package searchengine;

/**
 *
 * @author ZHS
 */
public class Posting
{

	private long documentID;
	private int size;
	private int[] positions;

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
	 * How many of this term in this document, same as positions.length
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
