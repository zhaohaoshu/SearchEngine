package searchengine.search.expression;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * @author ZHS
 */
public class ExpressionNodeAnd extends ExpressionNodeBinary {

	public ExpressionNodeAnd(ExpressionNode right, ExpressionNode left) {
		super(right, left);
	}

	@Override
	public boolean getValue() {
		return getLeft().getValue() && getRight().getValue();
	}

	@Override
	public void print(PrintWriter writer) {
		writer.print('(');
		getLeft().print(writer);
		writer.print(" & ");
		getRight().print(writer);
		writer.print(')');
	}
}
