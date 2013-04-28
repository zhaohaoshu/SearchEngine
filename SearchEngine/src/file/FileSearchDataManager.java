package file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.DocumentAnalyser;
import file.manager.DocumentManager;
import file.manager.PostingManager;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.TreeMap;
import searchengine.data.SearchDataManager;

/**
 *
 * @author ZHS
 */
public class FileSearchDataManager extends SearchDataManager<FileDocumentInfo, FilePostingReader> implements Closeable
{

	private DocumentManager documentManager;
	private PostingManager postingManager;
	private File documentDirFile;

	public FileSearchDataManager(File documentDirFile,
			File documentFile, File documentIndexFile,
			File postingFile, File postingIndexFile,
			String mode)
	{
		try
		{
			this.documentDirFile = documentDirFile;
			documentManager = new DocumentManager(documentFile, documentIndexFile, mode);
			postingManager = new PostingManager(postingFile, postingIndexFile, mode);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	//<editor-fold defaultstate="collapsed" desc="Add Document">

	public void loadDocument(File file, long maxPostingCount)
	{
		PostingTree postingTree = new PostingTree(postingManager, maxPostingCount);
		loadDocument(file, postingTree);
		postingTree.flush();
	}

	private void loadDocument(File file, PostingTree postingTree)
	{
		if (file.isDirectory())
		{
			FileLogger.log("Read directory " + file);
			for (File childFile : file.listFiles())
				loadDocument(childFile, postingTree);
		}
		else
		{
			FileLogger.log("\tRead file " + file);
			readParadiseFile(file, postingTree);
		}
	}

	public void readParadiseFile(File file, PostingTree postingTree)
	{
		try (OffsetReader reader = new OffsetReader(file);)
		{
			ByteArrayBuilder builder = new ByteArrayBuilder();
			Map<String, Integer> map = null;
			String title = null;
			String pathname = documentDirFile.toURI().relativize(file.toURI()).getRawPath();
			long bodyStart = -1;
			long bodyEnd = -1;
			String url = null;
			for (;;)
			{
				int read;
				while ((read = reader.read()) >= 0 &&
						read != '=' && read != 31)
					builder.append((byte) read);
				if (read < 0)//end of a file
					break;
				if (read == 31)//end of a document
				{
					addDocument(postingTree, map, title, pathname, bodyStart, bodyEnd, url);
					reader.read();//read the '\n'
				}
				else //if (reader.getCurrentRead() == '=')
				{
					if (builder.equalsString("body"))
					{
						builder.clear();
						map = new TreeMap<>();
						bodyStart = reader.getOffset();
						while ((read = reader.read()) != 30)
							if ('a' <= read && read <= 'z')
								builder.append((byte) read);
							else if ('A' <= read && read <= 'Z')
								builder.append((byte) (read - 'A' + 'a'));
							else if (!builder.isEmpty())//not a letter
							{
								String term = builder.toString();
								Integer positionCount = map.get(term);
								if (positionCount == null)
									map.put(term, 1);
								else
									map.put(term, positionCount + 1);
								builder.clear();
							}
						bodyEnd = reader.getOffset();
					}
					else if (builder.equalsString("title"))
					{
						builder.clear();
						while ((read = reader.read()) != 30)
							builder.append((byte) read);
						title = builder.toString();
					}
					else if (builder.equalsString("url"))
					{
						builder.clear();
						while ((read = reader.read()) != 30)
							builder.append((byte) read);
						url = builder.toString();
					}
					else
						while (reader.read() != 30);
					reader.read();//read the '\n'
					builder.clear();
				}
			}
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean addDocument(PostingTree postingTree, Map<String, Integer> map, String name, String pathname, long start, long end, String url)
	{
		double length = DocumentAnalyser.calcDocumentLength(map);
		try
		{
			long documentID = documentManager.addNewDocument(name, length, pathname, start, end, url);
//			System.out.println("    Add\t" + documentID + "\t" + pathname);
			for (Map.Entry<String, Integer> entry : map.entrySet())
				postingTree.addPosting(entry.getKey(), documentID, entry.getValue());
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
		return new FilePostingReader(term, postingManager);
	}
	//</editor-fold>

	@Override
	public void close()
	{
		try
		{
			documentManager.close();
			postingManager.close();
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
