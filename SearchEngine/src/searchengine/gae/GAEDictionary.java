package searchengine.gae;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.TypeTokenizer;

/**
 *
 * @author ZHS
 */
public class GAEDictionary
{

	public static final String KIND_DICTIONARY = "Dictionary";
	public static final String PROPERTY_DICTIONARY_DOCUMENT_COUNT = "document_count";
	public static final String KIND_DOCUMENT = "Document";
	public static final String PROPERTY_DOCUMENT_KEY = "key";
	public static final String PROPERTY_DOCUMENT_NAME = "name";
	public static final String PROPERTY_DOCUMENT_LENGTH = "square_sum";
	public static final String KIND_ENTRY = "Entry";
	public static final String PROPERTY_ENTRY_COUNT = "count";
	//public static final String PROPERTY_ENTRY_SIZE = "size";
	public static final String KIND_POSTING = "Posting";
	public static final String PROPERTY_POSTING_DOCUMENT_ID = "document_id";
	public static final String PROPERTY_POSTING_SIZE = "size";
	public static final String PROPERTY_POSTING_POSITIONS = "positions";
	public static final Key DICTIONARY_KEY = KeyFactory.createKey(KIND_DICTIONARY, 1);
	private DatastoreService datastoreService;

	public GAEDictionary()
	{
		datastoreService = DatastoreServiceFactory.getDatastoreService();
	}
	//<editor-fold defaultstate="collapsed" desc="Get Document">

	public String getDocumentName(long documentID)
	{
		try
		{
			Entity entity = datastoreService.get(KeyFactory.createKey(DICTIONARY_KEY, KIND_DOCUMENT, documentID));
			String name = (String) entity.getProperty(PROPERTY_DOCUMENT_NAME);
			return name;
		}
		catch (EntityNotFoundException ex)
		{
			Logger.getLogger(GAEDictionary.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public double getDocumentLength(long documentID)
	{
		try
		{
			Entity entity = datastoreService.get(KeyFactory.createKey(DICTIONARY_KEY, KIND_DOCUMENT, documentID));
			return ((Number) entity.getProperty(PROPERTY_DOCUMENT_LENGTH)).doubleValue();
		}
		catch (EntityNotFoundException ex)
		{
		}
		return -1;
	}

	public GAEDocumentInfo getDocumentInfo(long documentID)
	{
		try
		{
			Entity entity = datastoreService.get(KeyFactory.createKey(DICTIONARY_KEY, KIND_DOCUMENT, documentID));
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

	public List<GAEDocumentInfo> getAllDocumentInfos()
	{
		LinkedList<GAEDocumentInfo> result = new LinkedList<>();
		Query query = new Query(KIND_DOCUMENT, DICTIONARY_KEY);
		PreparedQuery prepare = datastoreService.prepare(query);
		for (Entity documentEntity : prepare.asIterable())
			result.add(new GAEDocumentInfo(documentEntity.getKey().getId(),
					(String) documentEntity.getProperty(PROPERTY_DOCUMENT_NAME),
					(BlobKey) documentEntity.getProperty(PROPERTY_DOCUMENT_KEY),
					((Number) documentEntity.getProperty(PROPERTY_DOCUMENT_LENGTH)).doubleValue()));
		return result;
	}

	public List<BlobKey> getAllDocumentKeys()
	{
		LinkedList<BlobKey> result = new LinkedList<>();
		Query query = new Query(KIND_DOCUMENT, DICTIONARY_KEY);
		PreparedQuery prepare = datastoreService.prepare(query);
		for (Entity documentEntity : prepare.asIterable())
			result.add((BlobKey) documentEntity.getProperty(PROPERTY_DOCUMENT_KEY));
		return result;
	}

	/**
	 * Get how many documents are in the dictionary
	 *
	 * @return The number of documents
	 */
	public long getDocumentCount()
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
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Add Document">

	private long setDocumentInfo(String name, BlobKey BlobKey, double length)
	{
		long documentID = createNewDocumentID();
		Entity entity = new Entity(KIND_DOCUMENT, documentID, DICTIONARY_KEY);
		entity.setUnindexedProperty(PROPERTY_DOCUMENT_NAME, name);
		entity.setUnindexedProperty(PROPERTY_DOCUMENT_KEY, BlobKey);
		entity.setUnindexedProperty(PROPERTY_DOCUMENT_LENGTH, length);
		datastoreService.put(entity);
		return documentID;
	}

	private long createNewDocumentID()
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
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Add">

	public boolean adds(BlobInfo info)
	{
		Map<String, LinkedList<Integer>> map = new HashMap<>();
		try (BlobstoreInputStream inputStream = new BlobstoreInputStream(info.getBlobKey()))
		{
			readDocument(inputStream, map);
		}
		catch (IOException ex)
		{
			Logger.getLogger(GAEDictionary.class.getName()).log(Level.SEVERE, null, ex);
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
			long documentID = setDocumentInfo(info.getFilename(), info.getBlobKey(), Math.sqrt(length));
			for (Map.Entry<String, LinkedList<Integer>> entry : map.entrySet())
				add(entry.getKey(), documentID, entry.getValue());
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

	private void add(String term, long documentID, LinkedList<Integer> positions)
	{
		Key entryKey = KeyFactory.createKey(DICTIONARY_KEY, KIND_ENTRY, term);
		Entity entryEntity;
		long count;
		//long size;
		try
		{
			entryEntity = datastoreService.get(entryKey);
			count = ((Number) entryEntity.getProperty(PROPERTY_ENTRY_COUNT)).longValue();
			count++;
			//size = ((Number) entryEntity.getProperty(PROPERTY_ENTRY_SIZE)).longValue();
			//size += positions.size();
		}
		catch (EntityNotFoundException ex)
		{
			entryEntity = new Entity(entryKey);
			count = 1;
			//size = positions.size();
		}
		entryEntity.setUnindexedProperty(PROPERTY_ENTRY_COUNT, count);
		//entryEntity.setUnindexedProperty(PROPERTY_ENTRY_SIZE, size);
		Entity postingEntity = new Entity(KIND_POSTING, count, entryKey);
		postingEntity.setUnindexedProperty(PROPERTY_POSTING_DOCUMENT_ID, documentID);
		postingEntity.setUnindexedProperty(PROPERTY_POSTING_SIZE, positions.size());
		postingEntity.setUnindexedProperty(PROPERTY_POSTING_POSITIONS, positions);
		datastoreService.put(entryEntity);
		datastoreService.put(postingEntity);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Map">

	/**
	 * Tokenize the query
	 *
	 * @param inputStream
	 * @param map
	 * @return The number of tokens
	 */
	private int readDocument(InputStream inputStream, Map<String, LinkedList<Integer>> map)
	{
		TypeTokenizer tokenizer = new TypeTokenizer(new InputStreamReader(inputStream));
		int position = 0;
		for (;;)
		{
			String term = tokenizer.getNext(1);
			if (term == null)
				break;
			addEntry(map, term.toLowerCase(), position);
			position++;
		}
		return position;
	}

	private void addEntry(Map<String, LinkedList<Integer>> map, String term, int position)
	{
		LinkedList<Integer> positions = map.get(term);
		if (positions == null)
		{
			positions = new LinkedList<>();
			map.put(term, positions);
		}
		positions.addLast(position);
	}
	//</editor-fold>
}
