package file.manager;

import file.TermTree;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class TermManager implements Closeable
{

	private RandomAccessFile indexAccess;
	private RandomAccessFile termAccess;
	private RandomAccessFile positionAccess;

	public TermManager(File termFile, File termIndexFile, File positionFile, String mode) throws IOException
	{
		indexAccess = new RandomAccessFile(termIndexFile, mode);
		if (indexAccess.length() == 0)
			appendIndexNode();
		termAccess = new RandomAccessFile(termFile, mode);
		positionAccess = new RandomAccessFile(positionFile, mode);
	}
	//<editor-fold defaultstate="collapsed" desc="Add">

	public void addTermTree(TermTree termTree) throws IOException
	{
		TermTree.Node root = termTree.getRoot();
		addTermTreeNode(root, new IndexNode(0));
//		Calendar time = Calendar.getInstance();
//		System.out.println("    \t" +
//				time.get(Calendar.HOUR) + ":" +
//				time.get(Calendar.MINUTE) + ":" +
//				time.get(Calendar.SECOND) + "." +
//				(time.getTimeInMillis() % 1000));
	}

	private void addTermTreeNode(TermTree.Node node, IndexNode indexNode) throws IOException
	{
		LinkedList<TermTree.Posting> postings = node.getPostings();
		if (!postings.isEmpty())
		{
			if (indexNode.readTermPointer() < 0)
				indexNode.writeTermPointer(termAccess.length());
			else
				indexNode.getTailTermNode().writeNextPointer(termAccess.length());
			indexNode.writePostingCount(indexNode.readPostingCount() + postings.size());
			indexNode.writeTailPointer(termAccess.length() + 28 * (postings.size() - 1));
			appendPostings(postings);
		}
		TermTree.Node[] children = node.getChildren();
		long childOffset = indexAccess.length();
		int toAdd = 0;
		long[] childPointers = new long[children.length];
		for (int i = 0; i < children.length; i++)
		{
			TermTree.Node child = children[i];
			if (child != null)
			{
				childPointers[i] = indexNode.readChildPointer(i);
				if (childPointers[i] < 0)
				{
					indexNode.writeChildPointer(i, childOffset);
					childPointers[i] = childOffset;
					childOffset += IndexNode.SIZE;
					toAdd++;
				}
			}
			else
				childPointers[i] = -1;
		}
		for (int i = 0; i < toAdd; i++)
			appendIndexNode();
		for (int i = 0; i < children.length; i++)
			if (childPointers[i] >= 0)
				addTermTreeNode(children[i], new IndexNode(childPointers[i]));
	}

	/**
	 * Append a new index node to index file.
	 *
	 * @throws IOException
	 */
	private void appendIndexNode() throws IOException
	{
		indexAccess.seek(indexAccess.length());
		//termPointer
		indexAccess.writeLong(-1);
		//postingCount
		indexAccess.writeLong(0);
		//tailPointer
		indexAccess.writeLong(-1);
		//child pointer
		for (int i = 0; i < 26; i++)
			indexAccess.writeLong(-1);
	}

	private void appendPostings(LinkedList<TermTree.Posting> postings) throws IOException
	{
		long filePointer = termAccess.length();
		termAccess.seek(filePointer);
		int i = 0;
		for (TermTree.Posting posting : postings)
		{
			//nextPointer
			if (i < postings.size() - 1)
			{
				//8 nextPointer, 8 documentID, 4 positionCount, 8 positionPointer
				filePointer += 28;
				termAccess.writeLong(filePointer);
			}
			else
				termAccess.writeLong(-1);
			termAccess.writeLong(posting.getDocumentID());
			LinkedList<Integer> positions = posting.getPositions();
			//positionCount
			termAccess.writeInt(positions.size());
			//positionPointer
			termAccess.writeLong(positionAccess.length());
			appendPositions(positions);
			i++;
		}
	}

	private void appendPositions(LinkedList<Integer> positions) throws IOException
	{
		positionAccess.seek(positionAccess.length());
		for (Integer position : positions)
			positionAccess.writeInt(position);
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Get">

	public TermPointer getTermPointer(String term) throws IOException
	{
		IndexNode indexNode = new IndexNode(0);
		for (int i = 0; i < term.length() && indexNode != null; i++)
			indexNode = indexNode.getChildNode(term.charAt(i) - 'a', false);
		if (indexNode == null)
			return new TermPointer(null, 0);
		return new TermPointer(indexNode.getTermNode(), indexNode.readPostingCount());
	}
	//</editor-fold>

	@Override
	public void close() throws IOException
	{
		indexAccess.close();
		termAccess.close();
		positionAccess.close();
	}

	public class TermPointer
	{

		private TermNode termNode;
		private long postingCount;

		private TermPointer(TermNode termNode, long postingCount)
		{
			this.termNode = termNode;
			this.postingCount = postingCount;
		}

		/**
		 * Get the number of postings the term has.
		 *
		 * @return
		 */
		public long getPostingCount()
		{
			return postingCount;
		}

		/**
		 * Move to the next posting
		 *
		 * @throws IOException
		 */
		public void moveNext() throws IOException
		{
			termNode = termNode.getNext();
		}

		public Posting getPosting(boolean addPositions) throws IOException
		{
			return termNode.getPosting(addPositions);
		}

		public boolean end()
		{
			return termNode == null;
		}
	}

	private class Node
	{

		long base;

		public Node(long base)
		{
			this.base = base;
		}
	}

	private class IndexNode extends Node
	{

		static final int SIZE = 29 * 8;
		//termPointer
		//postingCount
		//tailPointer
		//childPointer*26

		public IndexNode(long base)
		{
			super(base);
		}

		long readTermPointer() throws IOException
		{
			indexAccess.seek(base);
			return indexAccess.readLong();
		}

		TermNode getTermNode() throws IOException
		{
			long termPointer = readTermPointer();
			if (termPointer < 0)
				return null;
			return new TermNode(termPointer);
		}

		void writeTermPointer(long value) throws IOException
		{
			indexAccess.seek(base);
			indexAccess.writeLong(value);
		}

		long readPostingCount() throws IOException
		{
			indexAccess.seek(base + 8);
			return indexAccess.readLong();
		}

		void writePostingCount(long value) throws IOException
		{
			indexAccess.seek(base + 8);
			indexAccess.writeLong(value);
		}

		long readTailPointer() throws IOException
		{
			indexAccess.seek(base + 16);
			return indexAccess.readLong();
		}

		TermNode getTailTermNode() throws IOException
		{
			long tailPointer = readTailPointer();
			if (tailPointer < 0)
				return null;
			return new TermNode(tailPointer);
		}

		void writeTailPointer(long value) throws IOException
		{
			indexAccess.seek(base + 16);
			indexAccess.writeLong(value);
		}

		long readChildPointer(int i) throws IOException
		{
			indexAccess.seek(base + (i + 3) * 8);
			return indexAccess.readLong();
		}

		void writeChildPointer(int i, long value) throws IOException
		{

			indexAccess.seek(base + (i + 3) * 8);
			indexAccess.writeLong(value);
		}

		IndexNode getChildNode(int i, boolean add) throws IOException
		{
			long childPointer = readChildPointer(i);
			if (childPointer < 0)
			{
				if (!add)
					return null;
				childPointer = indexAccess.length();
				writeChildPointer(i, childPointer);
				appendIndexNode();
			}
			return new IndexNode(childPointer);
		}
	}

	private class TermNode extends Node
	{

		//nextPointer
		//documentID
		//positionCount
		//positionPointer
		public TermNode(long base)
		{
			super(base);
		}

		Posting getPosting(boolean addPositions) throws IOException
		{
			termAccess.seek(base + 8);
			long documentID = termAccess.readLong();
			int positionCount = termAccess.readInt();
			if (addPositions)
			{
				positionAccess.seek(termAccess.readLong());
				int[] positions = new int[positionCount];
				for (int i = 0; i < positionCount; i++)
					positions[i] = positionAccess.readInt();
				return new Posting(documentID, positions);
			}
			return new Posting(documentID, positionCount);
		}

		long readNextPointer() throws IOException
		{
			termAccess.seek(base);
			return termAccess.readLong();
		}

		void writeNextPointer(long value) throws IOException
		{
			termAccess.seek(base);
			termAccess.writeLong(value);
		}

		TermNode getNext() throws IOException
		{
			long nextPointer = readNextPointer();
			if (nextPointer < 0)
				return null;
			return new TermNode(nextPointer);
		}
	}
}
