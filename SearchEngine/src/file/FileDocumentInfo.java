package file;

import searchengine.data.DocumentInfo;

/**
 *
 * @author ZHS
 */
public class FileDocumentInfo extends DocumentInfo {

	private String pathname;
	private long start;
	private long end;

	public FileDocumentInfo(long documentID, String name, double length, String pathname, long start, long end, String url) {
		super(documentID, name, length, url);
		this.pathname = pathname;
		this.start = start;
		this.end = end;
	}

	public String getPathname() {
		return pathname;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "{" + getLength() + "," +
				getPathname() + "[" + getStart() + "," + getEnd() + "]}";
//		return "{" + getDocumentID() + ":" +
//				getLength() + "," +
//				getPathname() + "[" + getStart() + "," + getLength() + "]," +
//				getUrl() + "," +
//				getName() + '}';
	}
}
