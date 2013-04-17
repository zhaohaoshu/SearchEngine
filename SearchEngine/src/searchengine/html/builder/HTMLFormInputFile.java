package searchengine.html.builder;

import searchengine.html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLFormInputFile extends HTMLElement
{

	public HTMLFormInputFile()
	{
		super("input");
	}

	public HTMLFormInputFile(String name)
	{
		super("input");
		setAttribute("type", "file");
		setAttribute("name", name);
	}

	public HTMLFormInputFile(String name, boolean multiple)
	{
		super("input");
		setAttribute("type", "file");
		setAttribute("name", name);
		if (multiple)
			setAttribute("multiple", "multiple");
	}
}
