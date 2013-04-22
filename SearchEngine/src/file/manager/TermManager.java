package file.manager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class TermManager implements Closeable
{

	private static final int CHILD_POINTER_SIZE = Long.SIZE / 8;
	private static final int TERM_POINTER_SIZE = Long.SIZE / 8;
	private static final int NEXT_POINTER_SIZE = Long.SIZE / 8;
	private RandomAccessFile indexAccess;
	private RandomAccessFile termAccess;

	public TermManager(File termFile, File termIndexFile, String mode) throws IOException
	{
		indexAccess = new RandomAccessFile(termIndexFile, mode);
		if (indexAccess.length() == 0)
			appendIndexNode();
		termAccess = new RandomAccessFile(termFile, mode);
	}
	//<editor-fold defaultstate="collapsed" desc="Add">

	public void addPosting(String term, long documentID, List<Integer> positions) throws IOException
	{
		long termPointer = getTermPointer(term, true);

		termAccess.seek(termPointer + NEXT_POINTER_SIZE);
		long count = termAccess.readLong();
		termAccess.seek(termPointer + NEXT_POINTER_SIZE);
		termAccess.writeLong(count + 1);

		long nextPointerOffset = termPointer;
		termAccess.seek(nextPointerOffset);
		for (;;)
		{
			long nextPointer = termAccess.readLong();
			if (nextPointer < 0)
			{
				long postingPointer = appendPosting(documentID, positions);
				termAccess.seek(nextPointerOffset);
				termAccess.writeLong(postingPointer);
				break;
			}
			termAccess.seek(nextPointer);
			nextPointerOffset = nextPointer;
		}
	}

	/**
	 * Append a new index node to index file.
	 *
	 * @return the start offset of the new index node.
	 * @throws IOException
	 */
	private long appendIndexNode() throws IOException
	{
		long length = indexAccess.length();
		indexAccess.seek(length);
		//offset pointer
		indexAccess.writeLong(-1);
		//child pointer
		for (int i = 0; i < 26; i++)
			indexAccess.writeLong(-1);
		return length;
	}

	/**
	 * Append a new term to term file
	 *
	 * @return the start offset of the new term
	 * @throws IOException
	 */
	private long appendTerm() throws IOException
	{
		long length = termAccess.length();
		termAccess.seek(length);
		//next
		termAccess.writeLong(-1);
		//count
		termAccess.writeLong(0);
		return length;
	}

	/**
	 * Append a new posting, including the document ID, size of positions, and
	 * positions, to the end of term file.
	 *
	 * @param documentID
	 * @param positions
	 * @return the start offset of the new posting
	 * @throws IOException
	 */
	private long appendPosting(long documentID, List<Integer> positions) throws IOException
	{
		long length = termAccess.length();
		termAccess.seek(length);
		//next
		termAccess.writeLong(-1);
		termAccess.writeLong(documentID);
		termAccess.writeInt(positions.size());
		for (Integer position : positions)
			termAccess.writeInt(position);
		return length;
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Get">

	public TermPointer getTermPointer(String term) throws IOException
	{
		long termPointer = getTermPointer(term, false);
		if (termPointer < 0)
			return new TermPointer(-1, 0);
		termAccess.seek(termPointer + NEXT_POINTER_SIZE);
		long count = termAccess.readLong();
		return new TermPointer(termPointer, count);
	}

	/**
	 * Get the pointer to the next posting
	 *
	 * @param termPointer
	 * @return
	 * @throws IOException
	 */
	public void moveNext(TermPointer termPointer) throws IOException
	{
		termAccess.seek(termPointer.termPointer);
		termPointer.termPointer = termAccess.readLong();
	}

	public Posting getPosting(TermPointer termPointer, boolean addPositions) throws IOException
	{
		termAccess.seek(termPointer.termPointer);
		termAccess.skipBytes(NEXT_POINTER_SIZE);
		long documentID = termAccess.readLong();
		int size = termAccess.readInt();
		if (addPositions)
		{
			int[] positions = new int[size];
			for (int i = 0; i < size; i++)
				positions[i] = termAccess.readInt();
			return new Posting(documentID, positions);
		}
		return new Posting(documentID, size);
	}
	//</editor-fold>

	/**
	 * Get the offset of the term in term file.
	 *
	 * @param term
	 * @param add
	 * @return the offset of the term in term file
	 * @throws IOException
	 */
	private long getTermPointer(String term, boolean add) throws IOException
	{
		indexAccess.seek(0);
		for (int i = 0; i < term.length(); i++)
		{
			indexAccess.skipBytes(TERM_POINTER_SIZE + (term.charAt(i) - 'a') * CHILD_POINTER_SIZE);
			long childPointer = indexAccess.readLong();
			if (childPointer < 0)
			{
				if (!add)
					return -1;
				long childPointerOffset = indexAccess.getFilePointer() - CHILD_POINTER_SIZE;
				childPointer = appendIndexNode();
				indexAccess.seek(childPointerOffset);
				indexAccess.writeLong(childPointer);
			}
			indexAccess.seek(childPointer);
		}
		long termPointer = indexAccess.readLong();
		if (termPointer < 0)
		{
			if (!add)
				return -1;
			long termPointerOffset = indexAccess.getFilePointer() - TERM_POINTER_SIZE;
			termPointer = appendTerm();
			indexAccess.seek(termPointerOffset);
			indexAccess.writeLong(termPointer);
		}
		return termPointer;
	}

	@Override
	public void close() throws IOException
	{
		indexAccess.close();
		termAccess.close();
	}

	public class TermPointer
	{

		private long termPointer;
		private long count;

		private TermPointer(long termPointer, long count)
		{
			this.termPointer = termPointer;
			this.count = count;
		}

		/**
		 * Get the number of postings the term has.
		 *
		 * @return
		 */
		public long getCount()
		{
			return count;
		}

		public boolean end()
		{
			return termPointer < 0;
		}
	}
}
