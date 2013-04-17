package searchengine.html;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ZHS
 */
public class HTMLElement
{

	private String tag;
	private Map<String, String> attributes;
	private List<HTMLElement> children;

	public HTMLElement(String tag)
	{
		this.tag = tag;
		attributes = new HashMap<>();
		children = new LinkedList<>();
	}

	public HTMLElement(String tag, String content)
	{
		this.tag = tag;
		attributes = new HashMap<>();
		children = new LinkedList<>();
		children.add(new StringElement(content));
	}

	public HTMLElement(String tag, Object[][] atts)
	{
		this.tag = tag;
		attributes = new HashMap<>();
		for (Object[] att : atts)
			attributes.put(att[0].toString(), att[1].toString());
		children = new LinkedList<>();
	}

	public String getTag()
	{
		return tag;
	}

	public void setAttribute(String name, Object value)
	{
		attributes.put(name, value.toString());
	}

	public StringElement addChild(String string)
	{
		StringElement child = new StringElement(string);
		return addChild(child);
	}

	public <T extends HTMLElement> T addChild(T child)
	{
		children.add(child);
		return child;
	}

	protected void printTabs(PrintWriter printWriter, int count)
	{
		for (int i = 0; i < count; i++)
			printWriter.print('\t');
	}

	public void print(PrintWriter printWriter, int level, boolean format)
	{
		if (format)
			printTabs(printWriter, level);
		printWriter.print('<' + tag);
		for (Map.Entry<String, String> entry : attributes.entrySet())
			printWriter.print(' ' + entry.getKey() + "=\"" + entry.getValue() + '\"');
		if (children.isEmpty())
		{
			printWriter.print("/>");
			if (format)
				printWriter.println();
		}
		else
		{
			printWriter.print('>');
			if (format)
				printWriter.println();
			for (HTMLElement child : children)
				child.print(printWriter, level + 1, format);
			if (format)
				printTabs(printWriter, level);
			printWriter.print("</" + tag + '>');
			if (format)
				printWriter.println();
		}
	}

	@Override
	public String toString()
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		print(printWriter, 0, false);
		return stringWriter.toString();
	}
}
