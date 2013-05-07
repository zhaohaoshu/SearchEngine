package searchengine.data;

/**
 *
 * @author ZHS
 */
public class DocumentInfo {

	private long documentID;
	private String name;
	private double length;
	private String url;

	public DocumentInfo(long documentID, String name, double length, String url) {
		this.documentID = documentID;
		this.name = name;
		this.length = length;
		this.url = url;
	}

	public long getDocumentID() {
		return documentID;
	}

	public String getName() {
		if (name.isEmpty())
			return "(empty)";
		return name;
	}

	public double getLength() {
		return length;
	}

	public String getUrl() {
		if (url.isEmpty())
			return "(empty)";
		return url;
	}
}
