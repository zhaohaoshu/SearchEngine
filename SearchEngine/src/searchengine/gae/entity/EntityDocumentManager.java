package searchengine.gae.entity;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.gae.GAEDocumentAdder;
import searchengine.gae.GAEDocumentInfo;

/**
 *
 * @author ZHS
 */
public class EntityDocumentManager
{

	private static final String KIND_DOCUMENT = "Document";
	private static final String PROPERTY_DOCUMENT_KEY = "key";
	private static final String PROPERTY_DOCUMENT_NAME = "name";
	private static final String PROPERTY_DOCUMENT_LENGTH = "square_sum";
	private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

	public static Key getDocumentKey(long documentID)
	{
		return KeyFactory.createKey(EntityDictionaryManager.getDictionaryKey(), KIND_DOCUMENT, documentID);
	}

	public static String getDocumentName(long documentID)
	{
		try
		{
			Entity entity = datastoreService.get(getDocumentKey(documentID));
			String name = (String) entity.getProperty(PROPERTY_DOCUMENT_NAME);
			return name;
		}
		catch (EntityNotFoundException ex)
		{
			Logger.getLogger(GAEDocumentAdder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static double getDocumentLength(long documentID)
	{
		try
		{
			Entity entity = datastoreService.get(getDocumentKey(documentID));
			return ((Number) entity.getProperty(PROPERTY_DOCUMENT_LENGTH)).doubleValue();
		}
		catch (EntityNotFoundException ex)
		{
		}
		return -1;
	}

	public static GAEDocumentInfo getDocumentInfo(long documentID)
	{
		try
		{
			Entity entity = datastoreService.get(getDocumentKey(documentID));
			String name = (String) entity.getProperty(PROPERTY_DOCUMENT_NAME);
			BlobKey key = (BlobKey) entity.getProperty(PROPERTY_DOCUMENT_KEY);
			double length = ((Number) entity.getProperty(PROPERTY_DOCUMENT_LENGTH)).doubleValue();
			return new GAEDocumentInfo(documentID, name, key, length);
		}
		catch (EntityNotFoundException ex)
		{
		}
		return null;
	}

	public static List<GAEDocumentInfo> getAllDocumentInfos()
	{
		LinkedList<GAEDocumentInfo> result = new LinkedList<>();
		Query query = new Query(KIND_DOCUMENT, EntityDictionaryManager.getDictionaryKey());
		PreparedQuery prepare = datastoreService.prepare(query);
		for (Entity documentEntity : prepare.asIterable())
			result.add(new GAEDocumentInfo(documentEntity.getKey().getId(),
					(String) documentEntity.getProperty(PROPERTY_DOCUMENT_NAME),
					(BlobKey) documentEntity.getProperty(PROPERTY_DOCUMENT_KEY),
					((Number) documentEntity.getProperty(PROPERTY_DOCUMENT_LENGTH)).doubleValue()));
		return result;
	}

	public static List<BlobKey> getAllDocumentKeys()
	{
		LinkedList<BlobKey> result = new LinkedList<>();
		Query query = new Query(KIND_DOCUMENT, EntityDictionaryManager.getDictionaryKey());
		PreparedQuery prepare = datastoreService.prepare(query);
		for (Entity documentEntity : prepare.asIterable())
			result.add((BlobKey) documentEntity.getProperty(PROPERTY_DOCUMENT_KEY));
		return result;
	}

	public static void addDocument(long documentID, String name, BlobKey BlobKey, double length)
	{
		Entity entity = new Entity(getDocumentKey(documentID));
		entity.setUnindexedProperty(PROPERTY_DOCUMENT_NAME, name);
		entity.setUnindexedProperty(PROPERTY_DOCUMENT_KEY, BlobKey);
		entity.setUnindexedProperty(PROPERTY_DOCUMENT_LENGTH, length);
		datastoreService.put(entity);
	}
}
