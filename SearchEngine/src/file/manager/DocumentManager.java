package file.manager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import file.FileDocumentInfo;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author ZHS
 */
public class DocumentManager implements Closeable {

	private ByteBuffer documentBuffer;
	private ByteBuffer indexBuffer;
	private FileChannel documentChannel;
	private FileChannel indexChannel;
	private long currentDocumentID;

	public DocumentManager(File documentFile, File documentIndexFile, String mode) throws IOException {
		indexChannel = (new RandomAccessFile(documentIndexFile, mode)).getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(8);
		if (indexChannel.size() == 0) {
			byteBuffer.putLong(0);
			byteBuffer.rewind();
			while (byteBuffer.hasRemaining())
				indexChannel.write(byteBuffer);
			currentDocumentID = 0;
		}
		else {
			while (byteBuffer.hasRemaining())
				indexChannel.read(byteBuffer);
			byteBuffer.rewind();
			currentDocumentID = byteBuffer.getLong();
		}
		documentChannel = (new RandomAccessFile(documentFile, mode)).getChannel();
		documentBuffer = ByteBuffer.allocate(16384);
		indexBuffer = ByteBuffer.allocate(16384);
	}

	public void flush() throws IOException {
		flushDocumentBuffer();
		flushIndexBuffer();
	}

	private void flushDocumentBuffer() throws IOException {
		documentBuffer.flip();
		documentChannel.position(documentChannel.size());
		while (documentBuffer.hasRemaining())
			documentChannel.write(documentBuffer);
		documentBuffer.clear();
	}

	private void flushIndexBuffer() throws IOException {
		indexBuffer.flip();
		indexChannel.position(indexChannel.size());
		while (indexBuffer.hasRemaining())
			indexChannel.write(indexBuffer);
		indexBuffer.clear();
		ByteBuffer byteBuffer = ByteBuffer.allocate(8);
		byteBuffer.putLong(currentDocumentID);
		byteBuffer.rewind();
		indexChannel.position(0);
		while (byteBuffer.hasRemaining())
			indexChannel.write(byteBuffer);
	}

	/**
	 * Add a new document, and return the new added document's ID
	 *
	 * @return the new added document's ID
	 */
	public long addNewDocument(String name, double length, String pathname, long start, long end, String url) throws IOException {
		byte[] nameBytes = name.getBytes();
		byte[] pathnameBytes = pathname.getBytes();
		byte[] urlBytes = url.getBytes();
		//length(8)+start(8)+end(8)+3*2
		short len = (short) (30 + nameBytes.length + pathnameBytes.length + urlBytes.length);
		long offset = documentChannel.size() + documentBuffer.position();

		if (indexBuffer.remaining() < 10)
			flushIndexBuffer();
		currentDocumentID++;
		indexBuffer.putLong(offset);
		indexBuffer.putShort(len);

		if (documentBuffer.remaining() < len)
			flushDocumentBuffer();
		documentBuffer.putDouble(length);
		documentBuffer.putShort((short) nameBytes.length);
		documentBuffer.put(nameBytes);
		documentBuffer.putShort((short) pathnameBytes.length);
		documentBuffer.put(pathnameBytes);
		documentBuffer.putLong(start);
		documentBuffer.putLong(end);
		documentBuffer.putShort((short) urlBytes.length);
		documentBuffer.put(urlBytes);

		return currentDocumentID;
	}

	public long getDocumentCount() throws IOException {
		return currentDocumentID;
	}

	public FileDocumentInfo getDocumentInfo(long documentID) throws IOException {
		if (documentID < 1 || documentID > getDocumentCount())
			return null;

		//8+(id-1)*10
		indexChannel.position(documentID * 10 - 2);
		ByteBuffer byteBuffer = ByteBuffer.allocate(10);
		while (byteBuffer.hasRemaining())
			indexChannel.read(byteBuffer);
		byteBuffer.rewind();
		long offset = byteBuffer.getLong();
		short len = byteBuffer.getShort();

		byteBuffer = ByteBuffer.allocate(len);
		documentChannel.position(offset);
		while (byteBuffer.hasRemaining())
			documentChannel.read(byteBuffer);
		byteBuffer.rewind();

		double length = byteBuffer.getDouble();

		short nameLen = byteBuffer.getShort();
		byte[] nameBytes = new byte[nameLen];
		byteBuffer.get(nameBytes);

		short pathnameLen = byteBuffer.getShort();
		byte[] pathnameBytes = new byte[pathnameLen];
		byteBuffer.get(pathnameBytes);

		long start = byteBuffer.getLong();
		long end = byteBuffer.getLong();

		short urlLen = byteBuffer.getShort();
		byte[] urlBytes = new byte[urlLen];
		byteBuffer.get(urlBytes);

		return new FileDocumentInfo(documentID, new String(nameBytes), length,
				new String(pathnameBytes), start, end, new String(urlBytes));
	}

	public double getDocumentLength(long documentID) throws IOException {
		if (documentID < 1 || documentID > getDocumentCount())
			return 0;

		//8+(id-1)*12
		indexChannel.position(documentID * 10 - 2);
		ByteBuffer byteBuffer = ByteBuffer.allocate(8);
		while (byteBuffer.hasRemaining())
			indexChannel.read(byteBuffer);
		byteBuffer.rewind();
		long offset = byteBuffer.getLong();

		byteBuffer.rewind();
		documentChannel.position(offset);
		while (byteBuffer.hasRemaining())
			documentChannel.read(byteBuffer);
		byteBuffer.rewind();

		double length = byteBuffer.getDouble();
		return length;
	}

	@Override
	public void close() throws IOException {
		indexChannel.close();
		documentChannel.close();
	}
}
