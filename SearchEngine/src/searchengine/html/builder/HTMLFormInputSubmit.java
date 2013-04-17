package searchengine.html.builder;

import searchengine.html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLFormInputSubmit extends HTMLElement
{

	public HTMLFormInputSubmit()
	{
		super("input");
	}

	public HTMLFormInputSubmit(String value)
	{
		super("input");
		setAttribute("type", "submit");
		setAttribute("value", value);
	}

	public HTMLFormInputSubmit(String value, String name)
	{
		super("input");
		setAttribute("type", "submit");
		setAttribute("value", value);
		setAttribute("name", name);
	}

	public HTMLFormInputSubmit(String value, String name, String formtarget)
	{
		super("input");
		setAttribute("type", "submit");
		setAttribute("value", value);
		setAttribute("name", name);
		setAttribute("formtarget", formtarget);
	}
}
