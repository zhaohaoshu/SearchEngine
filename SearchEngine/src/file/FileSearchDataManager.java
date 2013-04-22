package file;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.DocumentAnalyser;
import file.manager.DocumentManager;
import file.manager.TermManager;
import searchengine.data.SearchDataManager;

/**
 *
 * @author ZHS
 */
public class FileSearchDataManager extends SearchDataManager<FileDocumentInfo, FilePostingReader> implements Closeable
{

	private DocumentManager documentManager;
	private TermManager termManager;

	public FileSearchDataManager(
			File documentFile, File documentIndexFile,
			File termFile, File termIndexFile, String mode)
	{
		try
		{
			documentManager = new DocumentManager(documentFile, documentIndexFile, mode);
			termManager = new TermManager(termFile, termIndexFile, mode);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	//<editor-fold defaultstate="collapsed" desc="Add Document">

	public boolean addDocument(File file)
	{
		try
		{
			return addDocument(new FileInputStream(file), file.getName(), file.getPath());
		}
		catch (FileNotFoundException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	public boolean addDocument(InputStream inputStream, String name, String pathname)
	{
		Map<String, LinkedList<Integer>> map = new HashMap<>();
		DocumentAnalyser.tokenizeDocument(inputStream, map);
		double length = DocumentAnalyser.calcDocumentLength(map);
		try
		{
			long documentID = documentManager.addNewDocument(name, length, pathname);
			for (Map.Entry<String, LinkedList<Integer>> entry : map.entrySet())
				termManager.addPosting(entry.getKey(), documentID, entry.getValue());
			return true;
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
	//</editor-fold>

	@Override
	public long getDocumentCount()
	{
		try
		{
			return documentManager.getDocumentCount();
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	@Override
	public FileDocumentInfo getDocumentInfo(long documentID)
	{
		try
		{
			return documentManager.getDocumentInfo(documentID);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public double getDocumentLength(long documentID)
	{
		try
		{
			return documentManager.getDocumentLength(documentID);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	@Override
	public String getDocumentName(long documentID)
	{
		try
		{
			return documentManager.getDocumentName(documentID);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public FilePostingReader getPostingReader(String term)
	{
		return new FilePostingReader(term, termManager);
	}

	@Override
	public void close()
	{
		try
		{
			documentManager.close();
			termManager.close();
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
