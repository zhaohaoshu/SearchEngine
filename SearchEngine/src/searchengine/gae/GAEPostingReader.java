package searchengine.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.Posting;

/**
 *
 * @author ZHS
 */
public class GAEPostingReader
{

	private DatastoreService datastoreService;
	private long currentCount;
	private long maxCount;
	private Key entryKey;

	public GAEPostingReader(String entry)
	{
		datastoreService = DatastoreServiceFactory.getDatastoreService();
		entryKey = KeyFactory.createKey(GAEDictionary.DICTIONARY_KEY, GAEDictionary.KIND_ENTRY, entry);
		currentCount = 1;
		try
		{
			Entity entryEntity = datastoreService.get(entryKey);
			maxCount = ((Number) entryEntity.getProperty(GAEDictionary.PROPERTY_ENTRY_COUNT)).longValue();
		}
		catch (EntityNotFoundException ex)
		{
			maxCount = 0;
		}
	}

	public void movePrevious()
	{
		if (currentCount > 1)
			currentCount--;
	}

	public Posting read(boolean addPositions)
	{
		if (currentCount > maxCount)
			return null;
		Key postingKey = KeyFactory.createKey(entryKey, GAEDictionary.KIND_POSTING, currentCount);
		currentCount++;
		try
		{
			Entity postingEntity = datastoreService.get(postingKey);
			long documentID = ((Number) postingEntity.getProperty(GAEDictionary.PROPERTY_POSTING_DOCUMENT_ID)).longValue();
			int size = ((Number) postingEntity.getProperty(GAEDictionary.PROPERTY_POSTING_SIZE)).intValue();
			if (addPositions)
			{
				int[] positions = new int[size];
				int count = 0;
				for (Object position : (List) postingEntity.getProperty(GAEDictionary.PROPERTY_POSTING_POSITIONS))
					positions[count++] = ((Number) position).intValue();
				return new Posting(documentID, size, positions);
			}
			return new Posting(documentID, size);
		}
		catch (EntityNotFoundException ex)
		{
			Logger.getLogger(GAEPostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public long getCount()
	{
		return maxCount;
	}
}
