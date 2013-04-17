package searchengine.search.expression;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * @author ZHS
 */
public class ExpressionNodeOr extends ExpressionNodeBinary
{

	public ExpressionNodeOr(ExpressionNode left, ExpressionNode right)
	{
		super(left, right);
	}

	@Override
	public boolean getValue()
	{
		return getLeft().getValue() || getRight().getValue();
	}

	@Override
	public void print(PrintWriter writer)
	{
		writer.print('(');
		getLeft().print(writer);
		writer.print(" | ");
		getRight().print(writer);
		writer.print(')');
	}
}
