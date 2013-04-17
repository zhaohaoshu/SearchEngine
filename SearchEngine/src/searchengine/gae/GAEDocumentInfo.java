package searchengine.gae;

import com.google.appengine.api.blobstore.BlobKey;

/**
 *
 * @author ZHS
 */
public class GAEDocumentInfo
{

	private long documentID;
	private String name;
	private BlobKey key;
	private double length;

	public GAEDocumentInfo(long documentID, String name, BlobKey key, double length)
	{
		this.documentID = documentID;
		this.name = name;
		this.key = key;
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

	public BlobKey getKey()
	{
		return key;
	}

	public double getLength()
	{
		return length;
	}
}
