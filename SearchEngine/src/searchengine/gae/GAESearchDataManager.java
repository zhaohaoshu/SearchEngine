package searchengine.gae;

import searchengine.gae.entity.EntityDocumentManager;
import searchengine.gae.entity.EntityDictionaryManager;
import searchengine.data.SearchDataManager;

/**
 *
 * @author ZHS
 */
public class GAESearchDataManager extends SearchDataManager<GAEDocumentInfo, GAEPostingReader>
{

	@Override
	public long getDocumentCount()
	{
		return EntityDictionaryManager.getDocumentCount();
	}

	@Override
	public double getDocumentLength(long documentID)
	{
		return EntityDocumentManager.getDocumentLength(documentID);
	}

	@Override
	public String getDocumentName(long documentID)
	{
		return EntityDocumentManager.getDocumentName(documentID);
	}

	@Override
	public GAEDocumentInfo getDocumentInfo(long documentID)
	{
		return EntityDocumentManager.getDocumentInfo(documentID);
	}

	@Override
	public GAEPostingReader getPostingReader(String term)
	{
		return new GAEPostingReader(term);
	}
}
