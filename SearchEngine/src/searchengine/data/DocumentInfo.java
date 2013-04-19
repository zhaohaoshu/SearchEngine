package searchengine.data;

/**
 *
 * @author ZHS
 */
public class DocumentInfo
{

	private long documentID;
	private String name;
	private double length;

	public DocumentInfo(long documentID, String name, double length)
	{
		this.documentID = documentID;
		this.name = name;
		this.length = length;
	}

	public long getDocumentID()
	{
		return documentID;
	}

	public String getName()
	{
		return name;
	}

	public double getLength()
	{
		return length;
	}
}
