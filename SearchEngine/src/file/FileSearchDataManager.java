package file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.DocumentAnalyser;
import file.manager.DocumentManager;
import file.manager.TermManager;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import searchengine.data.SearchDataManager;

/**
 *
 * @author ZHS
 */
public class FileSearchDataManager extends SearchDataManager<FileDocumentInfo, FilePostingReader> implements Closeable
{

	private DocumentManager documentManager;
	private TermManager termManager;
	private File documentDirFile;

	public FileSearchDataManager(File documentDirFile,
			File documentFile, File documentIndexFile,
			File termFile, File termIndexFile, File positionFile,
			String mode)
	{
		try
		{
			this.documentDirFile = documentDirFile;
			documentManager = new DocumentManager(documentFile, documentIndexFile, mode);
			termManager = new TermManager(termFile, termIndexFile, positionFile, mode);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	//<editor-fold defaultstate="collapsed" desc="Add Document">

	public void loadDocument(File file, long maxPostingCount, long maxPositionCount)
	{
		TermTree termTree = new TermTree(termManager, maxPostingCount, maxPositionCount);
		loadDocument(file, termTree);
		termTree.flush();
	}

	private void loadDocument(File file, TermTree termTree)
	{
		if (file.isDirectory())
		{
			System.out.println("Read directory " + file);
			for (File childFile : file.listFiles())
				loadDocument(childFile, termTree);
		}
		else
		{
			System.out.println("\tRead file " + file);
			readParadiseFile(file, termTree);
		}
	}

	public void readParadiseFile(File file, TermTree termTree)
	{
		try
		{
			FileInputStream inputStream = new FileInputStream(file);
			ByteArrayBuilder builder = new ByteArrayBuilder();
			int lastRead = -1;
			long startOffset = -1;
			String title = null;
			byte[] body = null;
			String url = null;
			long bodyStart = -1;
			long bodyEnd = -1;
			for (long offset = 0;; offset++)
			{
				int read = inputStream.read();
				if (read < 0)
					break;
				if (builder.length() == 0)
					startOffset = offset;
				builder.append((byte) read);
				if (read == '\n')
					if (lastRead == 30)
					{
						if (builder.startsWith("body="))
						{
							body = builder.subBytes(5, builder.length() - 2);
							bodyStart = startOffset + 5;
							bodyEnd = offset - 1;
						}
						else if (builder.startsWith("title="))
							title = builder.subString(6, builder.length() - 2);
						else if (builder.startsWith("url="))
							url = builder.subString(4, builder.length() - 2);
						builder.clear();
					}
					else if (lastRead == 31)
					{
						addDocument(termTree, new ByteArrayInputStream(body), title,
								documentDirFile.toURI().relativize(file.toURI()).getRawPath(),
								bodyStart, bodyEnd, url);
						builder.clear();
						startOffset = -1;
						body = null;
						title = null;
						url = null;
						bodyStart = -1;
						bodyEnd = -1;
					}
				lastRead = read;
			}
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean addDocument(TermTree termTree, InputStream inputStream, String name, String pathname, long start, long end, String url)
	{
		Map<String, LinkedList<Integer>> map = new HashMap<>();
		DocumentAnalyser.tokenizeDocument(inputStream, map);
		double length = DocumentAnalyser.calcDocumentLength(map);
		try
		{
			long documentID = documentManager.addNewDocument(name, length, pathname, start, end, url);
//			System.out.println("    Add\t" + documentID + "\t" + pathname);
			for (Map.Entry<String, LinkedList<Integer>> entry : map.entrySet())
				termTree.add(entry.getKey(), documentID, entry.getValue());
//			termManager.addPosting(entry.getKey(), documentID, entry.getValue());
			return true;
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
	//</editor-fold>

	public InputStream getDocumentInputStream(FileDocumentInfo info)
	{
		try (FileInputStream fileInputStream = new FileInputStream(new File(documentDirFile, info.getPathname()));)
		{
			fileInputStream.skip(info.getStart());
			byte[] bytes = new byte[(int) (info.getEnd() - info.getStart())];
			fileInputStream.read(bytes);
			return new ByteArrayInputStream(bytes);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	//<editor-fold defaultstate="collapsed" desc="For Search">

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
	//</editor-fold>

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
