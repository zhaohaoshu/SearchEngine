package html;

import java.io.PrintWriter;

/**
 *
 * @author ZHS
 */
public class StringElement extends HTMLElement
{

	public StringElement(String string)
	{
		super(string);
	}

	@Override
	public void print(PrintWriter printWriter, int level, boolean format)
	{
		if (format)
			printTabs(printWriter, level);
		printWriter.print(getTag());
		if (format)
			printWriter.println();
	}
}
