package searchengine.html.builder;

import searchengine.html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLForm extends HTMLElement
{

	public HTMLForm()
	{
		super("form");
	}

	public HTMLForm(String action, String method)
	{
		super("form");
		setAttribute("action", action);
		setAttribute("method", method);
	}

	public HTMLForm(String action, String method, String enctype)
	{
		super("form");
		setAttribute("action", action);
		setAttribute("method", method);
		setAttribute("enctype", enctype);
	}
}
