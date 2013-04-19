package searchengine.gae.entity;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 *
 * @author ZHS
 */
public class EntityTermManager
{

	private static final String KIND_TERM = "Term";
	private static final String PROPERTY_TERM_COUNT = "count";
	private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	public static Key getTermKey(String term)
	{
		return KeyFactory.createKey(EntityDictionaryManager.getDictionaryKey(), KIND_TERM, term);
	}

	public static long createNewPostingID(String term)
	{
		Key termKey = getTermKey(term);
		Entity termEntity;
		long count;
		try
		{
			termEntity = datastoreService.get(termKey);
			count = ((Number) termEntity.getProperty(PROPERTY_TERM_COUNT)).longValue();
			count++;
		}
		catch (EntityNotFoundException ex)
		{
			termEntity = new Entity(termKey);
			count = 1;
		}
		termEntity.setUnindexedProperty(PROPERTY_TERM_COUNT, count);
		datastoreService.put(termEntity);
		return count;
	}

	public static long getCount(String term)
	{
		Key termKey = getTermKey(term);
		try
		{
			Entity termEntity = datastoreService.get(termKey);
			return ((Number) termEntity.getProperty(EntityTermManager.PROPERTY_TERM_COUNT)).longValue();
		}
		catch (EntityNotFoundException ex)
		{
			return 0;
		}
	}
}
