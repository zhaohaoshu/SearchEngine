package html.builder;

import html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLLink extends HTMLElement
{

	public HTMLLink(String href, String content)
	{
		super("a");
		setAttribute("href", href);
		addChild(content);
	}

	public HTMLLink(String href, String content, String target)
	{
		super("a");
		setAttribute("href", href);
		setAttribute("target", target);
		addChild(content);
	}
}
