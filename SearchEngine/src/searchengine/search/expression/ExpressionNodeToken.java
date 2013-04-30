package searchengine.search.expression;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author ZHS
 */
public class ExpressionNodeToken extends ExpressionNode {

	private ArrayList<Boolean> values;
	private int count;

	public ExpressionNodeToken(ArrayList<Boolean> values, int count) {
		this.values = values;
		this.count = count;
	}

	@Override
	public boolean getValue() {
		return values.get(count).booleanValue();
	}

	@Override
	public void print(PrintWriter writer) {
		writer.print('[');
		writer.print(count);
		writer.print(']');
	}
}
