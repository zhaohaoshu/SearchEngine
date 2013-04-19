package searchengine.gae;

import searchengine.gae.entity.EntityDocumentManager;
import searchengine.gae.entity.EntityTermManager;
import searchengine.gae.entity.EntityDictionaryManager;
import searchengine.gae.entity.EntityPostingManager;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.DocumentTokenizer;

/**
 *
 * @author ZHS
 */
public class GAEDocumentAdder
{

	private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	public boolean addDocument(BlobInfo info)
	{
		Map<String, LinkedList<Integer>> map = new HashMap<>();
		try (BlobstoreInputStream inputStream = new BlobstoreInputStream(info.getBlobKey()))
		{
			DocumentTokenizer.tokenizeDocument(inputStream, map);
		}
		catch (IOException ex)
		{
			Logger.getLogger(GAEDocumentAdder.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		Transaction transaction = datastoreService.beginTransaction();
		try
		{
			double length = 0;
			for (Map.Entry<String, LinkedList<Integer>> entry : map.entrySet())
			{
				double tf = 1 + Math.log(entry.getValue().size());
				length += tf * tf;
			}
			long documentID = EntityDictionaryManager.createNewDocumentID();
			EntityDocumentManager.addDocument(documentID, info.getFilename(), info.getBlobKey(), Math.sqrt(length));
			for (Map.Entry<String, LinkedList<Integer>> entry : map.entrySet())
			{
				String term = entry.getKey().toLowerCase();
				long postingID = EntityTermManager.createNewPostingID(term);
				EntityPostingManager.addPosting(term, postingID, documentID, entry.getValue());
			}
			transaction.commit();
		}
		finally
		{
			if (transaction.isActive())
			{
				transaction.rollback();
				return false;
			}
		}
		return true;
	}
}
