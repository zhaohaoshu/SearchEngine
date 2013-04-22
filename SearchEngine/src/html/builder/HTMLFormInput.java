package html.builder;

import html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLFormInput extends HTMLElement
{

	public HTMLFormInput()
	{
		super("input");
	}

	public HTMLFormInput(String type, String name)
	{
		super("input");
		setAttribute("type", type);
		setAttribute("name", name);
	}
}
