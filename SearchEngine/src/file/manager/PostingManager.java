package file.manager;

import file.PostingTree;
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
public class PostingManager implements Closeable
{

	private RandomAccessFile indexAccess;
	private RandomAccessFile postingAccess;

	public PostingManager(File postingFile, File postingIndexFile, String mode) throws IOException
	{
		indexAccess = new RandomAccessFile(postingIndexFile, mode);
		if (indexAccess.length() == 0)
			appendIndexNode();
		postingAccess = new RandomAccessFile(postingFile, mode);
	}
	//<editor-fold defaultstate="collapsed" desc="Add">

	public void addTermTree(PostingTree termTree) throws IOException
	{
		PostingTree.Node root = termTree.getRoot();
		addTermTreeNode(root, new IndexNode(0));
	}

	private void addTermTreeNode(PostingTree.Node node, IndexNode indexNode) throws IOException
	{
		LinkedList<Posting> postings = node.getPostings();
		if (!postings.isEmpty())
		{
			if (indexNode.readFirstPostingPointer() < 0)
				indexNode.writeFirstPostingPointer(postingAccess.length());
			else
				indexNode.getTailPostingNode().writeNextPointer(postingAccess.length());
			indexNode.writePostingCount(indexNode.readPostingCount() + postings.size());
			indexNode.writeTailPostingPointer(postingAccess.length() + PostingNode.SIZE * (postings.size() - 1));
			appendPostings(postings);
		}
		PostingTree.Node[] children = node.getChildren();
		long childOffset = indexAccess.length();
		int toAdd = 0;
		long[] childPointers = new long[children.length];
		for (int i = 0; i < children.length; i++)
		{
			PostingTree.Node child = children[i];
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

	private void appendPostings(LinkedList<Posting> postings) throws IOException
	{
		long filePointer = postingAccess.length();
		postingAccess.seek(filePointer);
		int i = 0;
		for (Posting posting : postings)
		{
			//nextPointer
			if (i < postings.size() - 1)
			{
				//8 nextPointer, 8 documentID, 4 positionCount
				filePointer += PostingNode.SIZE;
				postingAccess.writeLong(filePointer);
			}
			else
				postingAccess.writeLong(-1);
			postingAccess.writeLong(posting.getDocumentID());
			//positionCount
			postingAccess.writeInt(posting.getPositionCount());
			i++;
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Get">

	public PostingPointer getTermPointer(String term) throws IOException
	{
		IndexNode indexNode = new IndexNode(0);
		for (int i = 0; i < term.length() && indexNode != null; i++)
			indexNode = indexNode.getChildNode(term.charAt(i) - 'a', false);
		if (indexNode == null)
			return new PostingPointer(null, 0);
		return new PostingPointer(indexNode.getFirstPostingNode(), indexNode.readPostingCount());
	}
	//</editor-fold>

	@Override
	public void close() throws IOException
	{
		indexAccess.close();
		postingAccess.close();
	}

	public class PostingPointer
	{

		private PostingNode postingNode;
		private long postingCount;

		private PostingPointer(PostingNode termNode, long postingCount)
		{
			this.postingNode = termNode;
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
			postingNode = postingNode.getNext();
		}

		public Posting getPosting() throws IOException
		{
			return postingNode.getPosting();
		}

		public boolean end()
		{
			return postingNode == null;
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

		//termPointer
		//postingCount
		//tailPointer
		//childPointer*26
		static final int SIZE = 29 * 8;

		public IndexNode(long base)
		{
			super(base);
		}

		long readFirstPostingPointer() throws IOException
		{
			indexAccess.seek(base);
			return indexAccess.readLong();
		}

		PostingNode getFirstPostingNode() throws IOException
		{
			long firstPostingPointer = readFirstPostingPointer();
			if (firstPostingPointer < 0)
				return null;
			return new PostingNode(firstPostingPointer);
		}

		void writeFirstPostingPointer(long value) throws IOException
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

		long readTailPostingPointer() throws IOException
		{
			indexAccess.seek(base + 16);
			return indexAccess.readLong();
		}

		PostingNode getTailPostingNode() throws IOException
		{
			long tailPointer = readTailPostingPointer();
			if (tailPointer < 0)
				return null;
			return new PostingNode(tailPointer);
		}

		void writeTailPostingPointer(long value) throws IOException
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

	private class PostingNode extends Node
	{

		//nextPointer
		//documentID
		//positionCount
		static final int SIZE = 20;

		public PostingNode(long base)
		{
			super(base);
		}

		Posting getPosting() throws IOException
		{
			postingAccess.seek(base + 8);
			long documentID = postingAccess.readLong();
			int positionCount = postingAccess.readInt();
			return new Posting(documentID, positionCount);
		}

		long readNextPointer() throws IOException
		{
			postingAccess.seek(base);
			return postingAccess.readLong();
		}

		void writeNextPointer(long value) throws IOException
		{
			postingAccess.seek(base);
			postingAccess.writeLong(value);
		}

		PostingNode getNext() throws IOException
		{
			long nextPointer = readNextPointer();
			if (nextPointer < 0)
				return null;
			return new PostingNode(nextPointer);
		}
	}
}
