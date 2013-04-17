package searchengine.html.builder;

import searchengine.html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLTableCell extends HTMLElement
{

	public HTMLTableCell()
	{
		super("td");
	}

	public HTMLTableCell(boolean head)
	{
		super(head ? "th" : "td");
	}

	public HTMLTableCell(int colspan, int rowspan)
	{
		super("td");
		setAttribute("colspan", colspan);
		setAttribute("rowspan", rowspan);
	}
}
