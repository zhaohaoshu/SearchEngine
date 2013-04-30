package searchengine.data;

/**
 *
 * @author ZHS
 */
public abstract class SearchDataManager<D extends DocumentInfo, R extends PostingReader> {

	/**
	 * Gets the number of documents
	 *
	 * @return the number of documents
	 */
	public abstract long getDocumentCount();

	public abstract D getDocumentInfo(long documentID);

	public abstract R getPostingReader(String term);

	/**
	 * Get the length of the document who's id is
	 * <code>documentID</code>
	 *
	 * @param documentID the id of the required document
	 * @return length of the document
	 */
	public double getDocumentLength(long documentID) {
		return getDocumentInfo(documentID).getLength();
	}

	public String getDocumentName(long documentID) {
		return getDocumentInfo(documentID).getName();
	}
}
