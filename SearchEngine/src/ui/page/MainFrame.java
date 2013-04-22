package ui.page;

import html.HTMLElement;
import html.builder.HTMLLink;
import html.builder.HTMLPage;
import html.builder.HTMLTable;
import html.builder.HTMLTableCell;

/**
 *
 * @author ZHS
 */
public class MainFrame extends HTMLPage
{

	private HTMLElement content;

	public MainFrame(String title)
	{
		super(title, "/resource/main.css");
		HTMLTable table = getBodyElement().addChild(new HTMLTable());
		table.setAttribute("class", "main");
		HTMLTableCell cell = table.addRow(new HTMLTableCell(true));
		cell.addChild("<h1>");
		cell.addChild(new HTMLLink("/", "ZHS Search Engine"));
		cell.addChild("</h1>");
		HTMLTable tabTable = new HTMLTable();
		tabTable.setAttribute("class", "title");
		tabTable.addHeadRow(new HTMLElement[]
		{
			new HTMLLink("/search", "Search"),
			new HTMLLink("/list", "List all documents"),
			//new HTMLLink("/add", "Add document"),
			//new HTMLLink("/about", "About"),
		});
		table.addRow(tabTable);
		table.addRow("<hr/>");
		content = table.addRow(new HTMLElement("div"));
		content.setAttribute("class", "main");
	}

	public HTMLElement getContent()
	{
		return content;
	}
}
