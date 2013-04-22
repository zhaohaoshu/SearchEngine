package file.manager;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import file.FileDocumentInfo;

/**
 *
 * @author ZHS
 */
public class DocumentManager implements Closeable
{

	private RandomAccessFile indexAccess;
	private RandomAccessFile documentAccess;

	public DocumentManager(File documentFile, File documentIndexFile, String mode) throws IOException
	{
		indexAccess = new RandomAccessFile(documentIndexFile, mode);
		if (indexAccess.length() == 0)
			indexAccess.writeLong(0);
		documentAccess = new RandomAccessFile(documentFile, mode);
	}

	/**
	 * Add a new document, and return the new added document's ID
	 *
	 * @param name
	 * @param length
	 * @param pathname
	 * @return the new added document's ID
	 */
	public long addNewDocument(String name, double length, String pathname) throws IOException
	{
		long offset = documentAccess.length();
		documentAccess.seek(offset);
		documentAccess.writeDouble(length);
		documentAccess.writeUTF(name);
		documentAccess.writeUTF(pathname);

		indexAccess.seek(0);
		long documentCount = indexAccess.readLong();
		documentCount++;
		indexAccess.seek(0);
		indexAccess.writeLong(documentCount);
		indexAccess.seek(documentCount * (Long.SIZE / 8));
		indexAccess.writeLong(offset);

		return documentCount;
	}

	public long getDocumentCount() throws IOException
	{
		indexAccess.seek(0);
		return indexAccess.readLong();
	}

	public FileDocumentInfo getDocumentInfo(long documentID) throws IOException
	{
		if (documentID < 1 || documentID > getDocumentCount())
			return null;
		indexAccess.seek(documentID * (Long.SIZE / 8));
		long offset = indexAccess.readLong();

		documentAccess.seek(offset);
		double length = documentAccess.readDouble();
		String name = documentAccess.readUTF();
		String pathname = documentAccess.readUTF();

		return new FileDocumentInfo(documentID, name, length, pathname);
	}

	public double getDocumentLength(long documentID) throws IOException
	{
		if (documentID < 1 || documentID > getDocumentCount())
			return 0;
		indexAccess.seek(documentID * (Long.SIZE / 8));
		long offset = indexAccess.readLong();

		documentAccess.seek(offset);
		double length = documentAccess.readDouble();
		return length;
	}

	public String getDocumentName(long documentID) throws IOException
	{
		if (documentID < 1 || documentID > getDocumentCount())
			return null;
		indexAccess.seek(documentID * (Long.SIZE / 8));
		long offset = indexAccess.readLong();

		documentAccess.seek(offset);
		double length = documentAccess.readDouble();
		String name = documentAccess.readUTF();
		return name;
	}

	@Override
	public void close() throws IOException
	{
		indexAccess.close();
		documentAccess.close();
	}
}
