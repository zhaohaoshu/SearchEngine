package file.manager;

import file.PostingTree;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class PostingManager implements Closeable
{

	private FileChannel indexChannel;
	private FileChannel postingChannel;

	public PostingManager(File postingFile, File postingIndexFile, String mode) throws IOException
	{
		indexChannel = new RandomAccessFile(postingIndexFile, mode).getChannel();
		if (indexChannel.size() == 0)
			new IndexNode().save();
		postingChannel = new RandomAccessFile(postingFile, mode).getChannel();
	}
	//<editor-fold defaultstate="collapsed" desc="Add">

	public void addTermTree(PostingTree termTree) throws IOException
	{
		PostingTree.Node root = termTree.getRoot();
		List<IndexNode> appendIndexNodes = new LinkedList<>();
		addTermTreeNode(root, new IndexNode(0), appendIndexNodes);
		ByteBuffer byteBuffer = ByteBuffer.allocate(appendIndexNodes.size() * IndexNode.SIZE);
		for (IndexNode appendIndexNode : appendIndexNodes)
			byteBuffer.put(appendIndexNode.buffer);
		indexChannel.position(indexChannel.size());
		byteBuffer.rewind();
		while (byteBuffer.hasRemaining())
			indexChannel.write(byteBuffer);
	}

	private void addTermTreeNode(PostingTree.Node treeNode, IndexNode indexNode,
			List<IndexNode> appendIndexNodes) throws IOException
	{
		boolean modified = false;
		LinkedList<Posting> postings = treeNode.getPostings();
		if (!postings.isEmpty())
		{
			modified = true;
			if (indexNode.readFirstPostingPointer() < 0)
				indexNode.writeFirstPostingPointer(postingChannel.size());
			else
				indexNode.getTailPostingNode().writeNextPointer(postingChannel.size());
			indexNode.writePostingCount(indexNode.readPostingCount() + postings.size());
			indexNode.writeTailPostingPointer(postingChannel.size() + PostingNode.SIZE * (postings.size() - 1));
			appendPostings(postings);
		}
		PostingTree.Node[] children = treeNode.getChildren();
		for (int i = 0; i < children.length; i++)
			if (children[i] != null)
			{
				modified = true;
				long childPointer = indexNode.readChildPointer(i);
				IndexNode childNode;
				if (childPointer < 0)
				{
					childNode = new IndexNode(appendIndexNodes);
					appendIndexNodes.add(childNode);
					indexNode.writeChildPointer(i, childNode.base);
				}
				else
					childNode = new IndexNode(childPointer);
				addTermTreeNode(children[i], childNode, appendIndexNodes);
			}
		if (modified && !indexNode.isAppend)
			indexNode.save();
	}

	private void appendPostings(LinkedList<Posting> postings) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(postings.size() * PostingNode.SIZE);
		long filePointer = postingChannel.size();
		postingChannel.position(filePointer);
		int i = 0;
		for (Posting posting : postings)
		{
			//nextPointer
			if (i < postings.size() - 1)
			{
				//8 nextPointer, 8 documentID, 4 positionCount
				filePointer += PostingNode.SIZE;
				buffer.putLong(filePointer);
			}
			else
				buffer.putLong(-1);
			buffer.putLong(posting.getDocumentID());
			//positionCount
			buffer.putInt(posting.getPositionCount());
			i++;
		}
		buffer.rewind();
		while (buffer.hasRemaining())
			postingChannel.write(buffer);
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
		indexChannel.force(false);
		indexChannel.close();
		postingChannel.force(false);
		postingChannel.close();
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

		/**
		 * Whether all postings are read.
		 *
		 * @return
		 */
		public boolean isEnd()
		{
			return postingNode == null;
		}
	}

	private class Node
	{

		long base;
		ByteBuffer buffer;
		FileChannel channel;

		Node(FileChannel channel, long base, int size) throws IOException
		{
			this.channel = channel;
			this.base = base;
			buffer = ByteBuffer.allocate(size);
		}

		void load() throws IOException
		{
			buffer.rewind();
			channel.position(base);
			while (buffer.hasRemaining())
				channel.read(buffer);
		}

		void save() throws IOException
		{
			buffer.rewind();
			channel.position(base);
			while (buffer.hasRemaining())
				channel.write(buffer);
		}
	}

	private class IndexNode extends Node
	{

		//termPointer
		//postingCount
		//tailPointer
		//childPointer*26
		/**
		 * Number of bytes
		 */
		static final int SIZE = 29 * 8;
		boolean isAppend;

		public IndexNode(long base) throws IOException
		{
			super(indexChannel, base, SIZE);
			load();
		}

		/**
		 * Append a new index node
		 *
		 * @throws IOException
		 */
		public IndexNode(List<IndexNode> appendIndexNodes) throws IOException
		{
			super(indexChannel, indexChannel.size() + appendIndexNodes.size() * SIZE, SIZE);
			init();
			isAppend = true;
		}

		/**
		 * Create a new index node at offset 0
		 *
		 * @throws IOException
		 */
		public IndexNode() throws IOException
		{
			super(indexChannel, 0, SIZE);
			init();
		}

		private void init() throws IOException
		{
			writeFirstPostingPointer(-1);
			writePostingCount(0);
			writeTailPostingPointer(-1);
			for (int i = 0; i < 26; i++)
				writeChildPointer(i, -1);
		}

		long readFirstPostingPointer()
		{
			return buffer.getLong(0);
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
			buffer.putLong(0, value);
		}

		long readPostingCount() throws IOException
		{
			return buffer.getLong(8);
		}

		void writePostingCount(long value) throws IOException
		{
			buffer.putLong(8, value);
		}

		long readTailPostingPointer()
		{
			return buffer.getLong(16);
		}

		PostingNode getTailPostingNode() throws IOException
		{
			long tailPointer = readTailPostingPointer();
			if (tailPointer < 0)
				return null;
			return new PostingNode(tailPointer);
		}

		void writeTailPostingPointer(long value)
		{
			buffer.putLong(16, value);
		}

		long readChildPointer(int i) throws IOException
		{
			return buffer.getLong((i + 3) * 8);
		}

		void writeChildPointer(int i, long value) throws IOException
		{
			buffer.putLong((i + 3) * 8, value);
		}

		IndexNode getChildNode(int i, boolean add) throws IOException
		{
			long childPointer = readChildPointer(i);
			if (childPointer < 0)
			{
				if (!add)
					return null;
				IndexNode indexNode = new IndexNode();
				writeChildPointer(i, indexNode.base);
				return indexNode;
			}
			return new IndexNode(childPointer);
		}
	}

	private class PostingNode extends Node
	{

		//nextPointer
		//documentID
		//positionCount(int)
		static final int SIZE = 20;

		public PostingNode(long base) throws IOException
		{
			super(postingChannel, base, SIZE);
			load();
		}

		Posting getPosting() throws IOException
		{
			buffer.position(8);
			long documentID = buffer.getLong();
			int positionCount = buffer.getInt();
			return new Posting(documentID, positionCount);
		}

		long readNextPointer() throws IOException
		{
			return buffer.getLong(0);
		}

		void writeNextPointer(long value) throws IOException
		{
			buffer.putLong(0, value);
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
