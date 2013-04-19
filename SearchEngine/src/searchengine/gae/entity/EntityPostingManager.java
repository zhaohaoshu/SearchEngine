package searchengine.gae.entity;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.LinkedList;
import java.util.List;
import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class EntityPostingManager
{

	private static final String KIND_POSTING = "Posting";
	private static final String PROPERTY_POSTING_DOCUMENT_ID = "document_id";
	private static final String PROPERTY_POSTING_SIZE = "size";
	private static final String PROPERTY_POSTING_POSITIONS = "positions";
	private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	public static Key getPostingKey(String term, long postingID)
	{
		return KeyFactory.createKey(EntityTermManager.getTermKey(term), KIND_POSTING, postingID);
	}

	public static void addPosting(String term, long postingID, long documentID, LinkedList<Integer> positions)
	{
		Entity postingEntity = new Entity(getPostingKey(term, postingID));
		postingEntity.setUnindexedProperty(PROPERTY_POSTING_DOCUMENT_ID, documentID);
		postingEntity.setUnindexedProperty(PROPERTY_POSTING_SIZE, positions.size());
		postingEntity.setUnindexedProperty(PROPERTY_POSTING_POSITIONS, positions);
		datastoreService.put(postingEntity);
	}

	public static Posting getPosting(String term, long postingID, boolean addPositions)
	{
		Key postingKey = getPostingKey(term, postingID);
		try
		{
			Entity postingEntity = datastoreService.get(postingKey);
			long documentID = ((Number) postingEntity.getProperty(PROPERTY_POSTING_DOCUMENT_ID)).longValue();
			int size = ((Number) postingEntity.getProperty(PROPERTY_POSTING_SIZE)).intValue();
			if (addPositions)
			{
				int[] positions = new int[size];
				int count = 0;
				for (Object position : (List) postingEntity.getProperty(PROPERTY_POSTING_POSITIONS))
					positions[count++] = ((Number) position).intValue();
				return new Posting(documentID, size, positions);
			}
			return new Posting(documentID, size);
		}
		catch (EntityNotFoundException ex)
		{
		}
		return null;
	}
}
