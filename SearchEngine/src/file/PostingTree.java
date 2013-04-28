package file;

import file.manager.PostingManager;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.data.Posting;

/**
 *
 * @author ZHS
 */
public class PostingTree
{

	private long lastTime = Calendar.getInstance().getTimeInMillis();
	private long lastDocumentID = 0;
	private long currentDocumentID = 0;
	private PostingManager postingManager;
	private long maxPostingCount;
	private Node root;
	private long totalPostingCount;

	public PostingTree(PostingManager postingManager, long maxPostingCount)
	{
		this.postingManager = postingManager;
		this.maxPostingCount = maxPostingCount;
		root = new Node();
		totalPostingCount = 0;
	}

	public void addPosting(String term, long documentID, int positionCount)
	{
		totalPostingCount++;
		Node p = root;
		for (int i = 0; i < term.length(); i++)
		{
			int index = term.charAt(i) - 'a';
			if (p.children[index] == null)
				p.children[index] = new Node();
			p = p.children[index];
		}
		p.postings.addLast(new Posting(documentID, positionCount));
		currentDocumentID = documentID;
		if (totalPostingCount > maxPostingCount)
			flush();
	}

	public void clear()
	{
		root = new Node();
		totalPostingCount = 0;
	}

	public void flush()
	{
		try
		{
			long documentCount = currentDocumentID - lastDocumentID;
			if (documentCount <= 0)
				documentCount = 1;
			long flushStartTime = Calendar.getInstance().getTimeInMillis();
			FileLogger.log("\t\tflushing " +
					documentCount + "(" +
					(lastDocumentID + 1) + "~" + currentDocumentID + ")\t" +
					(flushStartTime - lastTime) + " (" +
					(flushStartTime - lastTime) / documentCount + ")");
			postingManager.addTermTree(this);
			long time = Calendar.getInstance().getTimeInMillis();
			FileLogger.log("\t\t\t" +
					(time - flushStartTime) + "/" + (time - lastTime) + " (" +
					((time - flushStartTime) / documentCount) + "/" +
					((time - lastTime) / documentCount) + ")");
			lastTime = time;
			lastDocumentID = currentDocumentID;
		}
		catch (IOException ex)
		{
			Logger.getLogger(PostingTree.class.getName()).log(Level.SEVERE, null, ex);
		}
		clear();
	}

	public Node getRoot()
	{
		return root;
	}

	public class Node
	{

		private LinkedList<Posting> postings;
		private Node[] children;

		public Node()
		{
			postings = new LinkedList<>();
			children = new Node[26];
		}

		public LinkedList<Posting> getPostings()
		{
			return postings;
		}

		public Node[] getChildren()
		{
			return children;
		}
	}
}
