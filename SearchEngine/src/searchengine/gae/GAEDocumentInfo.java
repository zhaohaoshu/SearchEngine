package searchengine.gae;

import com.google.appengine.api.blobstore.BlobKey;
import searchengine.data.DocumentInfo;

/**
 *
 * @author ZHS
 */
public class GAEDocumentInfo extends DocumentInfo
{

	private BlobKey key;

	public GAEDocumentInfo(long documentID, String name, BlobKey key, double length)
	{
		super(documentID, name, length);
		this.key = key;
	}

	public BlobKey getKey()
	{
		return key;
	}
}
