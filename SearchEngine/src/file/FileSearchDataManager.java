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
public class FileSearchDataManager extends SearchDataManager<FileDocumentInfo, FilePostingReader> implements Closeable {

	private DocumentManager documentManager;
	private PostingManager postingManager;
	private File documentDirFile;
	private int maxTermLength = 50;
	private int maxNameLength = 100;
	private int maxURLLength = 100;

	public FileSearchDataManager(File documentDirFile, File dictionaryDirFile, String mode) {
		try {
			this.documentDirFile = documentDirFile;
			documentManager = new DocumentManager(new File(dictionaryDirFile, "document"),
					new File(dictionaryDirFile, "document_index"), mode);
			postingManager = new PostingManager(new File(dictionaryDirFile, "posting"),
					new File(dictionaryDirFile, "posting_index"), mode);
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	//<editor-fold defaultstate="collapsed" desc="Add Document">

	public void loadDocuments(File file, int maxPostingCount) throws IOException {
		postingManager.setMaxPostingCount(maxPostingCount);
		loadDocument(file);
		postingManager.flush();
		documentManager.flush();
	}

	private void loadDocument(File file) {
		if (file.isDirectory()) {
			FileLogger.log("Read directory " + file);
			for (File childFile : file.listFiles())
				loadDocument(childFile);
		}
		else {
			FileLogger.log("\tRead file " + file);
			readParadiseFile(file);
		}
	}

	public void readParadiseFile(File file) {
		try (OffsetReader reader = new OffsetReader(file);) {
			ByteArrayBuilder builder = new ByteArrayBuilder();
			Map<String, Integer> map = null;
			String title = null;
			String pathname = documentDirFile.toURI().relativize(file.toURI()).getRawPath();
			long bodyStart = -1;
			long bodyEnd = -1;
			String url = null;
			for (;;) {
				int read;
				while ((read = reader.read()) >= 0 &&
						read != '=' && read != 31)
					builder.append((byte) read);
				if (read < 0)//end of a file
					break;
				if (read == 31)//end of a document
				{
					addDocument(map, title, pathname, bodyStart, bodyEnd, url);
					reader.read();//read the '\n'
				}
				else //if (reader.getCurrentRead() == '=')
				{
					if (builder.equalsString("body")) {
						builder.clear();
						map = new TreeMap<>();
						bodyStart = reader.getOffset();
						while ((read = reader.read()) != 30)
							if ('a' <= read && read <= 'z') {
								if (builder.length() <= maxTermLength)
									builder.append((byte) read);
							}
							else if ('A' <= read && read <= 'Z') {
								if (builder.length() <= maxTermLength)
									builder.append((byte) (read - 'A' + 'a'));
							}
							else if (!builder.isEmpty())//not a letter
							{
								if (builder.length() <= maxTermLength) {
									String term = builder.toString();
									Integer positionCount = map.get(term);
									if (positionCount == null)
										map.put(term, 1);
									else
										map.put(term, positionCount + 1);
								}
								builder.clear();
							}
						bodyEnd = reader.getOffset();
					}
					else if (builder.equalsString("title")) {
						builder.clear();
						while ((read = reader.read()) != 30)
							if (builder.length() <= maxNameLength)
								builder.append((byte) read);
						if (builder.length() <= maxNameLength)
							title = builder.toString();
						else
							title = "Too long";
					}
					else if (builder.equalsString("url")) {
						builder.clear();
						while ((read = reader.read()) != 30)
							if (builder.length() <= maxURLLength)
								builder.append((byte) read);
						if (builder.length() <= maxURLLength)
							url = builder.toString();
						else
							url = "(Too long)";
					}
					else
						while (reader.read() != 30);
					reader.read();//read the '\n'
					builder.clear();
				}
			}
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean addDocument(Map<String, Integer> map, String name, String pathname, long start, long end, String url) {
		double length = DocumentAnalyser.calcDocumentLength(map);
		try {
			long documentID = documentManager.addNewDocument(name, length, pathname, start, end, url);
//			System.out.println("    Add\t" + documentID + "\t" + pathname);
			postingManager.addToBuffer(documentID, map);
			return true;
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
	//</editor-fold>

	public InputStream getDocumentInputStream(FileDocumentInfo info) {
		try (FileInputStream fileInputStream = new FileInputStream(new File(documentDirFile, info.getPathname()));) {
			fileInputStream.skip(info.getStart());
			byte[] bytes = new byte[(int) (info.getEnd() - info.getStart())];
			fileInputStream.read(bytes);
			return new ByteArrayInputStream(bytes);
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	//<editor-fold defaultstate="collapsed" desc="For Search">

	@Override
	public long getDocumentCount() {
		try {
			return documentManager.getDocumentCount();
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	@Override
	public FileDocumentInfo getDocumentInfo(long documentID) {
		try {
			return documentManager.getDocumentInfo(documentID);
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public double getDocumentLength(long documentID) {
		try {
			return documentManager.getDocumentLength(documentID);
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	@Override
	public FilePostingReader getPostingReader(String term) {
		try {
			return new FilePostingReader(postingManager.getTermPointer(term));
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	//</editor-fold>

	@Override
	public void close() {
		try {
			documentManager.close();
			postingManager.close();
		}
		catch (IOException ex) {
			Logger.getLogger(FileSearchDataManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
