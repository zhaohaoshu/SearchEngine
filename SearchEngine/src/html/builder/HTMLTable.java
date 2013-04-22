package html.builder;

import html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLTable extends HTMLElement
{

	public HTMLTable()
	{
		super("table");
	}

	public void addRow(HTMLTableCell[] cells)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		for (HTMLElement cell : cells)
			row.addChild(cell);
	}

	public void addRow(HTMLElement[] elements)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		for (HTMLElement element : elements)
		{
			HTMLTableCell cell = new HTMLTableCell();
			cell.addChild(element);
			row.addChild(cell);
		}
	}

	public HTMLTableCell addRow(HTMLTableCell cell)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		row.addChild(cell);
		return cell;
	}

	public HTMLTableCell addRow(Object element)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		HTMLTableCell cell = new HTMLTableCell();
		cell.addChild(element.toString());
		row.addChild(cell);
		return cell;
	}

	public <T extends HTMLElement> T addRow(T element)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		HTMLTableCell cell = new HTMLTableCell();
		cell.addChild(element);
		row.addChild(cell);
		return element;
	}

	public void addHeadRow(HTMLElement[] elements)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		for (HTMLElement element : elements)
		{
			HTMLTableCell cell = new HTMLTableCell(true);
			cell.addChild(element);
			row.addChild(cell);
		}
	}

	public HTMLTableCell addHeadRow(Object element)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		HTMLTableCell cell = new HTMLTableCell(true);
		cell.addChild(element.toString());
		row.addChild(cell);
		return cell;
	}

	public <T extends HTMLElement> T addHeadRow(T element)
	{
		HTMLElement row = addChild(new HTMLElement("tr"));
		HTMLTableCell cell = new HTMLTableCell(true);
		cell.addChild(element);
		row.addChild(cell);
		return element;
	}
}
