package file;

import searchengine.data.DocumentInfo;

/**
 *
 * @author ZHS
 */
public class FileDocumentInfo extends DocumentInfo
{

	private String pathname;

	public FileDocumentInfo(long documentID, String name, double length, String pathname)
	{
		super(documentID, name, length);
		this.pathname = pathname;
	}

	public String getPathname()
	{
		return pathname;
	}

	@Override
	public String toString()
	{
		return "{" + getDocumentID() + ":" + getLength() + "," + getName() + "," + getPathname() + '}';
	}
}
