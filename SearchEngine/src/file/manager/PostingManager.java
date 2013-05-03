package file.manager;

import file.FileLogger;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class PostingManager implements Closeable {

	long lastTime = Calendar.getInstance().getTimeInMillis();
	long lastDocumentID = 0;
	long currentDocumentID = 0;
	private FileChannel indexChannel;
	private FileChannel postingChannel;
	private PostingTree postingTree;
	private int maxPostingCount;

	public PostingManager(File postingFile, File postingIndexFile, String mode) throws IOException {
		indexChannel = new RandomAccessFile(postingIndexFile, mode).getChannel();
		if (indexChannel.size() == 0) {
			IndexNode indexNode = new IndexNode(0);
			indexNode.init();
			indexNode.save();
		}
		postingChannel = new RandomAccessFile(postingFile, mode).getChannel();
		postingTree = new PostingTree();
	}
	//<editor-fold defaultstate="collapsed" desc="Add">

	public void setMaxPostingCount(int maxPostingCount) {
		this.maxPostingCount = maxPostingCount;
	}

	public void addToBuffer(long documentID, Map<String, Integer> map) throws IOException {
		currentDocumentID = documentID;
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			postingTree.addPosting(entry.getKey(), documentID, entry.getValue());
			if (postingTree.currentPostingCount > maxPostingCount)
				flush();
		}
	}

	public void flush() throws IOException {
		long documentCount = currentDocumentID - lastDocumentID;
		if (documentCount <= 0)
			documentCount = 1;
		long flushStartTime = Calendar.getInstance().getTimeInMillis();
		FileLogger.log("\t\tflushing " +
				documentCount + "(" +
				(lastDocumentID + 1) + "~" + currentDocumentID + ")\t" +
				(flushStartTime - lastTime) + " (" +
				(flushStartTime - lastTime) / documentCount + ")");

		addPostingTreeNode(postingTree.root);
		ByteBuffer[] byteBuffers = new ByteBuffer[postingTree.appendIndexNodes.size()];
		int i = 0;
		for (IndexNode indexNode : postingTree.appendIndexNodes) {
			byteBuffers[i] = indexNode.buffer;
			byteBuffers[i].rewind();
			i++;
		}
		indexChannel.position(indexChannel.size());
		long offset = 0;
		while (offset < byteBuffers.length * IndexNode.SIZE) {
			int offsetIndex = (int) (offset / IndexNode.SIZE);
			offset += indexChannel.write(byteBuffers, offsetIndex, byteBuffers.length - offsetIndex);
		}
		postingTree.clear();

		long time = Calendar.getInstance().getTimeInMillis();
		FileLogger.log("\t\t\t" +
				(time - flushStartTime) + "/" + (time - lastTime) + " (" +
				((time - flushStartTime) / documentCount) + "/" +
				((time - lastTime) / documentCount) + ")");
		lastTime = time;
		lastDocumentID = currentDocumentID;
	}

	private void addPostingTreeNode(PostingTree.TreeNode treeNode) throws IOException {
		LinkedList<Posting> postings = treeNode.postings;
		IndexNode indexNode = treeNode.indexNode;
		if (!postings.isEmpty()) {
			treeNode.isModified = true;
			if (indexNode.readFirstPostingPointer() < 0)
				indexNode.writeFirstPostingPointer(postingChannel.size());
			else {
				long tailPostingPointer = indexNode.readTailPostingPointer();
				ByteBuffer byteBuffer = ByteBuffer.allocate(8);
				byteBuffer.putLong(postingChannel.size());
				byteBuffer.flip();
				postingChannel.position(tailPostingPointer);
				while (byteBuffer.hasRemaining())
					postingChannel.write(byteBuffer);
//				PostingNode tailPostingNode = indexNode.getTailPostingNode();
//				tailPostingNode.writeNextPointer(postingChannel.size());
//				tailPostingNode.save();
			}
			indexNode.writePostingCount(indexNode.readPostingCount() + postings.size());
			indexNode.writeTailPostingPointer(postingChannel.size() + PostingNode.SIZE * (postings.size() - 1));
			appendPostings(postings);
		}
		for (PostingTree.TreeNode child : treeNode.children)
			if (child != null)
				addPostingTreeNode(child);
		if (!treeNode.isAppend && treeNode.isModified)
			indexNode.save();
	}

	private void appendPostings(LinkedList<Posting> postings) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(postings.size() * PostingNode.SIZE);
		long filePointer = postingChannel.size();
		postingChannel.position(filePointer);
		int i = 0;
		for (Posting posting : postings) {
			//nextPointer
			if (i < postings.size() - 1) {
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

	public PostingPointer getTermPointer(String term) throws IOException {
		IndexNode indexNode = new IndexNode(0);
		indexNode.load();
		for (int i = 0; i < term.length() && indexNode != null; i++)
			indexNode = indexNode.getChildNode(term.charAt(i) - 'a');
		if (indexNode == null)
			return new PostingPointer(null, 0);
		return new PostingPointer(indexNode.getFirstPostingNode(), indexNode.readPostingCount());
	}

	public class PostingPointer {

		private PostingNode postingNode;
		private long postingCount;

		private PostingPointer(PostingNode termNode, long postingCount) {
			this.postingNode = termNode;
			this.postingCount = postingCount;
		}

		/**
		 * Get the number of postings the term has.
		 *
		 * @return
		 */
		public long getPostingCount() {
			return postingCount;
		}

		/**
		 * Move to the next posting
		 *
		 * @throws IOException
		 */
		public void moveNext() throws IOException {
			PostingNode node = postingNode;
			postingNode = null;
			postingNode = node.getNext();
		}

		public Posting getPosting() throws IOException {
			return postingNode.getPosting();
		}

		/**
		 * Whether all postings are read.
		 *
		 * @return
		 */
		public boolean isEnd() {
			return postingNode == null;
		}
	}
	//</editor-fold>

	@Override
	public void close() throws IOException {
		indexChannel.force(false);
		indexChannel.close();
		postingChannel.force(false);
		postingChannel.close();
	}
	//<editor-fold defaultstate="collapsed" desc="Node">

	private class Node {

		long base;
		ByteBuffer buffer;
		FileChannel channel;

		Node(FileChannel channel, long base, int size) throws IOException {
			this.channel = channel;
			this.base = base;
			buffer = ByteBuffer.allocate(size);
		}

		void load() throws IOException {
			buffer.rewind();
			channel.position(base);
			while (buffer.hasRemaining())
				channel.read(buffer);
		}

		void save() throws IOException {
			buffer.rewind();
			channel.position(base);
			while (buffer.hasRemaining())
				channel.write(buffer);
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Index Node">

	private class IndexNode extends Node {

		//termPointer
		//postingCount
		//tailPointer
		//childPointer*26
		/**
		 * Number of bytes
		 */
		static final int SIZE = 29 * 8;

		public IndexNode(long base) throws IOException {
			super(indexChannel, base, SIZE);
		}

		void init() throws IOException {
			writeFirstPostingPointer(-1);
			writePostingCount(0);
			writeTailPostingPointer(-1);
			for (int i = 0; i < 26; i++)
				writeChildPointer(i, -1);
		}

		long readFirstPostingPointer() {
			return buffer.getLong(0);
		}

		PostingNode getFirstPostingNode() throws IOException {
			long firstPostingPointer = readFirstPostingPointer();
			if (firstPostingPointer < 0)
				return null;
			return new PostingNode(firstPostingPointer);
		}

		void writeFirstPostingPointer(long value) throws IOException {
			buffer.putLong(0, value);
		}

		long readPostingCount() throws IOException {
			return buffer.getLong(8);
		}

		void writePostingCount(long value) throws IOException {
			buffer.putLong(8, value);
		}

		long readTailPostingPointer() {
			return buffer.getLong(16);
		}

		PostingNode getTailPostingNode() throws IOException {
			long tailPointer = readTailPostingPointer();
			return new PostingNode(tailPointer);
		}

		void writeTailPostingPointer(long value) {
			buffer.putLong(16, value);
		}

		long readChildPointer(int i) throws IOException {
			if (i < 0 || i >= 26)
				return -1;
			return buffer.getLong((i + 3) * 8);
		}

		void writeChildPointer(int i, long value) throws IOException {
			buffer.putLong((i + 3) * 8, value);
		}

		IndexNode getChildNode(int i) throws IOException {
			long childPointer = readChildPointer(i);
			if (childPointer < 0)
				return null;
			IndexNode indexNode = new IndexNode(childPointer);
			indexNode.load();
			return indexNode;
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Posting Node">

	private class PostingNode extends Node {

		//nextPointer
		//documentID
		//positionCount(int)
		static final int SIZE = 20;

		public PostingNode(long base) throws IOException {
			super(postingChannel, base, SIZE);
			load();
		}

		Posting getPosting() throws IOException {
			buffer.position(8);
			long documentID = buffer.getLong();
			int positionCount = buffer.getInt();
			return new Posting(documentID, positionCount);
		}

		long readNextPointer() throws IOException {
			return buffer.getLong(0);
		}

		void writeNextPointer(long value) throws IOException {
			buffer.putLong(0, value);
		}

		PostingNode getNext() throws IOException {
			long nextPointer = readNextPointer();
			if (nextPointer < 0)
				return null;
			return new PostingNode(nextPointer);
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Buffer">

	private class PostingTree {

		TreeNode root;
		long currentPostingCount;
		List<IndexNode> appendIndexNodes;
		long appendOffset;

		PostingTree() throws IOException {
			root = new TreeNode(0);
			currentPostingCount = 0;
			appendIndexNodes = new ArrayList<>();
			appendOffset = indexChannel.size();
		}

		void clear() throws IOException {
			root = new TreeNode(0);
			currentPostingCount = 0;
			appendIndexNodes.clear();
			appendOffset = indexChannel.size();
		}

		void addPosting(String term, long documentID, int positionCount) throws IOException {
			currentPostingCount++;
			TreeNode p = root;
			for (int strIndex = 0; strIndex < term.length(); strIndex++) {
				int index = term.charAt(strIndex) - 'a';
				if (p.children[index] == null) {
					if (!p.isAppend) {
						long childPointer = p.indexNode.readChildPointer(index);
						if (childPointer >= 0) {
							p = (p.children[index] = new TreeNode(childPointer));
							continue;
						}
						p.isModified = true;
					}
					TreeNode child = new TreeNode();
					p.children[index] = child;
					p.indexNode.writeChildPointer(index, child.indexNode.base);
				}
				p = p.children[index];
			}
			p.postings.addLast(new Posting(documentID, positionCount));
		}

		class TreeNode {

			LinkedList<Posting> postings = new LinkedList<>();
			TreeNode[] children = new TreeNode[26];
			boolean isAppend;
			boolean isModified;
			IndexNode indexNode;

			TreeNode() throws IOException {
				isAppend = true;
				indexNode = new IndexNode(appendOffset + appendIndexNodes.size() * IndexNode.SIZE);
				indexNode.init();
				appendIndexNodes.add(indexNode);
			}

			TreeNode(long base) throws IOException {
				isAppend = false;
				isModified = false;
				indexNode = new IndexNode(base);
				indexNode.load();
			}
		}
	}
	//</editor-fold>
}
