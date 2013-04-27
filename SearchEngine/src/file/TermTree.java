package file;

import file.manager.TermManager;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class TermTree
{

	private long lastTime = Calendar.getInstance().getTimeInMillis();
	private long lastDocumentID = 0;
	private long currentDocumentID = 0;
	private TermManager termManager;
	private long maxPostingCount;
	private long maxPositionCount;
	private Node root;
	private long postingCount;
	private long positionCount;

	public TermTree(TermManager termManager, long maxPostingCount, long maxPositionCount)
	{
		this.termManager = termManager;
		this.maxPostingCount = maxPostingCount;
		this.maxPositionCount = maxPositionCount;
		root = new Node();
		postingCount = 0;
		positionCount = 0;
	}

	public void add(String term, long documentID, LinkedList<Integer> positions)
	{
		postingCount++;
		positionCount += positions.size();
		Node p = root;
		for (int i = 0; i < term.length(); i++)
		{
			int index = term.charAt(i) - 'a';
			if (p.children[index] == null)
				p.children[index] = new Node();
			p = p.children[index];
		}
		p.postings.addLast(new Posting(documentID, positions));
		currentDocumentID = documentID;
		if ((maxPostingCount >= 0 && postingCount > maxPostingCount) ||
				(maxPositionCount >= 0 && positionCount > maxPositionCount))
			flush();
	}

	public void clear()
	{
		root = new Node();
		postingCount = 0;
		positionCount = 0;
	}

	public void flush()
	{
		try
		{
			long documentCount = currentDocumentID - lastDocumentID;
			if (documentCount <= 0)
				documentCount = 1;
			long flushStartTime = Calendar.getInstance().getTimeInMillis();
			System.out.println("\t\tflushing\t" +
					documentCount + "\t(" +
					(lastDocumentID + 1) + "~" + currentDocumentID + ")\t" +
					(flushStartTime - lastTime) + "\t" +
					(flushStartTime - lastTime) / documentCount);
			termManager.addTermTree(this);
			long time = Calendar.getInstance().getTimeInMillis();
			System.out.println("\t\t" +
					(time - flushStartTime) + "/" + (time - lastTime) + "\t" +
					((time - flushStartTime) / documentCount) + "/" +
					((time - lastTime) / documentCount));
			lastTime = time;
			lastDocumentID = currentDocumentID;
		}
		catch (IOException ex)
		{
			Logger.getLogger(TermTree.class.getName()).log(Level.SEVERE, null, ex);
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

	public class Posting
	{

		private long documentID;
		private LinkedList<Integer> positions;

		public Posting(long documentID, LinkedList<Integer> positions)
		{
			this.documentID = documentID;
			this.positions = positions;
		}

		public long getDocumentID()
		{
			return documentID;
		}

		public LinkedList<Integer> getPositions()
		{
			return positions;
		}

		@Override
		public String toString()
		{
			return "{" + documentID + "," + positions.size() + '}';
		}
	}
}
