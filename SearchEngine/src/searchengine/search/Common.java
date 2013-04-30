package searchengine.search;

import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class Common {

	public static long getMinID(Posting[] postings) {
		long id = -1;
		for (Posting posting : postings)
			if (posting != null && (id < 0 || posting.getDocumentID() < id))
				id = posting.getDocumentID();
		return id;
	}
}
