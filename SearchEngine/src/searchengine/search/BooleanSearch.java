package searchengine.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import searchengine.data.SearchDataManager;
import searchengine.data.DocumentInfo;
import searchengine.data.Posting;
import searchengine.data.PostingReader;
import searchengine.TypeTokenizer;
import searchengine.search.expression.ExpressionNode;
import searchengine.search.expression.ExpressionNodeAnd;
import searchengine.search.expression.ExpressionNodeNot;
import searchengine.search.expression.ExpressionNodeOr;
import searchengine.search.expression.ExpressionNodeToken;

/**
 *
 * @author ZHS
 */
public class BooleanSearch
{

	private static final int OP_L = 2;
	private static final int OP_R = 3;
	private static final int OP_OR = 4;
	private static final int OP_AND = 5;
	private static final int OP_NOT = 6;

	private static void addOP(LinkedList<ExpressionNode> nodeStack, LinkedList<Integer> opStack, int type)
	{

		while (!opStack.isEmpty() && opStack.peekFirst().intValue() >= type)
			switch (opStack.pollFirst().intValue())
			{
				case OP_NOT:
					nodeStack.addFirst(new ExpressionNodeNot(nodeStack.pollFirst()));
					break;
				case OP_AND:
					nodeStack.addFirst(new ExpressionNodeAnd(nodeStack.pollFirst(), nodeStack.pollFirst()));
					break;
				case OP_OR:
					nodeStack.addFirst(new ExpressionNodeOr(nodeStack.pollFirst(), nodeStack.pollFirst()));
					break;
			}
		opStack.addFirst(type);
	}

	private static <D extends DocumentInfo, R extends PostingReader> ExpressionNode getExpression(
			TypeTokenizer tokenizer,
			SearchDataManager<D, R> manager,
			ArrayList<Boolean> values,
			ArrayList<R> readers)
	{
		LinkedList<ExpressionNode> nodeStack = new LinkedList<>();
		LinkedList<Integer> opStack = new LinkedList<>();
		boolean needOP = false;
		for (;;)
		{
			String token = tokenizer.getNext();
			int type = tokenizer.getStringType();
			if (type <= 0 || type == OP_R)
				break;
			if (type >= 7)
				continue;
			if (needOP && !(type == OP_OR || type == OP_AND))
				addOP(nodeStack, opStack, OP_AND);
			needOP = !(type == OP_OR || type == OP_AND || type == OP_NOT);
			switch (type)
			{
				case 1:
					readers.add(manager.getPostingReader(token.toLowerCase()));
					nodeStack.addFirst(new ExpressionNodeToken(values, values.size()));
					values.add(false);
					break;
				case OP_L://(
					nodeStack.addFirst(getExpression(tokenizer, manager, values, readers));
					break;
				case OP_OR://|
				case OP_AND://&
				case OP_NOT://!
					addOP(nodeStack, opStack, type);
					break;
			}
		}
		addOP(nodeStack, opStack, -1);
		return nodeStack.pollFirst();
	}

	public static <D extends DocumentInfo, R extends PostingReader> void booleanSearch(
			String queryString, SearchDataManager<D, R> manager,
			BooleanSearchResultWriter writer)
	{
		TypeTokenizer tokenizer = new TypeTokenizer(new StringReader(queryString));
		tokenizer.addTypes(new char[]
		{
			'(', ')', '|', '&', '!'
		});

		ArrayList<Boolean> values = new ArrayList<>();
		ArrayList<R> readers = new ArrayList<>();
		ExpressionNode expression = getExpression(tokenizer, manager, values, readers);
		writer.writeExpression(expression);

		int queryCount = readers.size();
		Posting[] postings = new Posting[queryCount];
		for (int i = 0; i < queryCount; i++)
			postings[i] = readers.get(i).read(false);

		for (;;)
		{
			long id = Common.getMinID(postings);
			if (id < 0)
				break;
			for (int i = 0; i < queryCount; i++)
				if (postings[i] != null && postings[i].getDocumentID() == id)
				{
					values.set(i, true);
					postings[i] = readers.get(i).read(false);
				}
				else
					values.set(i, false);
			if (expression.getValue())
				writer.write(id, values);
		}
	}

	public static abstract class BooleanSearchResultWriter
	{

		public abstract void writeExpression(ExpressionNode expression);

		public abstract void write(long documentID, ArrayList<Boolean> values);
	}
}
