package searchengine.search.expression;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * @author ZHS
 */
public class ExpressionNodeNot extends ExpressionNode {

	private ExpressionNode child;

	public ExpressionNodeNot(ExpressionNode child) {
		this.child = child;
	}

	@Override
	public boolean getValue() {
		return !child.getValue();
	}

	@Override
	public void print(PrintWriter writer) {
		writer.print('!');
		child.print(writer);
	}
}
