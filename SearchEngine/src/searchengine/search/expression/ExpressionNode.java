package searchengine.search.expression;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author ZHS
 */
public abstract class ExpressionNode {

	public abstract boolean getValue();

	public abstract void print(PrintWriter writer);

	@Override
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		print(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
}
