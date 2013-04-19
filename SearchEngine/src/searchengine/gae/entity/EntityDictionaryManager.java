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
public class EntityDictionaryManager
{

	private static final String KIND_DICTIONARY = "Dictionary";
	private static final String PROPERTY_DICTIONARY_DOCUMENT_COUNT = "document_count";
	private static final Key DICTIONARY_KEY = KeyFactory.createKey(KIND_DICTIONARY, 1);
	private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	public static Key getDictionaryKey()
	{
		return DICTIONARY_KEY;
	}

	/**
	 * Get how many documents are in the dictionary
	 *
	 * @return The number of documents
	 */
	public static long getDocumentCount()
	{
		try
		{
			Entity entity = datastoreService.get(DICTIONARY_KEY);
			return ((Number) entity.getProperty(PROPERTY_DICTIONARY_DOCUMENT_COUNT)).longValue();
		}
		catch (EntityNotFoundException ex)
		{
		}
		return 0;
	}

	public static long createNewDocumentID()
	{
		long count;
		Entity entity;
		try
		{
			entity = datastoreService.get(DICTIONARY_KEY);
			count = ((Number) entity.getProperty(PROPERTY_DICTIONARY_DOCUMENT_COUNT)).longValue();
			count++;
		}
		catch (EntityNotFoundException ex)
		{
			entity = new Entity(DICTIONARY_KEY);
			count = 1;
		}
		entity.setUnindexedProperty(PROPERTY_DICTIONARY_DOCUMENT_COUNT, count);
		datastoreService.put(entity);
		return count;
	}
}
