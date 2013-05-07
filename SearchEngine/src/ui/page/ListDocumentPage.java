package ui.page;

import html.HTMLElement;
import html.builder.HTMLForm;
import html.builder.HTMLFormInput;
import html.builder.HTMLFormInputSubmit;
import html.builder.HTMLLink;
import html.builder.HTMLTable;
import html.builder.HTMLTableCell;
import java.util.List;
import searchengine.data.DocumentInfo;

/**
 *
 * @author ZHS
 */
public class ListDocumentPage extends MainFrame {

	public ListDocumentPage(List<? extends DocumentInfo> infos,
			long pageCount, long page) {
		super("Dcouments");

		HTMLElement pageDiv = new HTMLElement("div");
		pageDiv.addChild("<b>Pages: </b>");
		int showLength = 10;
		if (showLength > pageCount)
			showLength = (int) pageCount;
		long start = page - showLength / 2;
		if (start < 1)
			start = 1;
		if (start + showLength - 1 > pageCount)
			start = pageCount - showLength + 1;
		pageDiv.addChild(new HTMLLink("/list?page=1", "First"));
		pageDiv.addChild(" ");
		if (page > 1)
			pageDiv.addChild(new HTMLLink("/list?page=" + (page - 1), "Prev"));
		else
			pageDiv.addChild("Prev");
		pageDiv.addChild(" ");
		for (int i = 0; i < showLength; i++) {
			if (start + i == page)
				pageDiv.addChild("" + page);
			else
				pageDiv.addChild(new HTMLLink("/list?page=" + (start + i), "" + (start + i)));
			pageDiv.addChild(" ");
		}
		if (page < pageCount)
			pageDiv.addChild(new HTMLLink("/list?page=" + (page + 1), "Next"));
		else
			pageDiv.addChild("Next");
		pageDiv.addChild(" ");
		pageDiv.addChild(new HTMLLink("/list?page=" + pageCount, "Last"));
		HTMLForm form = pageDiv.addChild(new HTMLForm());
		form.addChild(new HTMLFormInput("text", "page"));
		form.addChild(new HTMLFormInputSubmit("Go"));

		getContent().addChild(pageDiv);
		getContent().addChild("<hr>");
		HTMLTable table = getContent().addChild(new HTMLTable());
		table.addRow("<b>Documents: </b>");
		for (DocumentInfo info : infos) {
			HTMLTableCell cell = new HTMLTableCell();
			cell.addChild("[");
			cell.addChild(new HTMLLink("/show?id=" + info.getDocumentID() + "&download=download", "download"));
			cell.addChild("] [");
			cell.addChild(new HTMLLink(info.getUrl(), "url", "_blank"));
			cell.addChild("] ");
			cell.addChild(new HTMLLink("show?id=" + info.getDocumentID(), info.getName(), "_blank"));
			table.addRow(cell);
		}

		getContent().addChild("<hr>");
		getContent().addChild(pageDiv);
	}
}
