package html.builder;

import html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLAnchor extends HTMLElement
{

	public HTMLAnchor(String id)
	{
		super("a");
		setAttribute("id", id);
		addChild("");
	}
}
