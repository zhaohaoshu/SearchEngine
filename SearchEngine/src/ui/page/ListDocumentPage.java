package ui.page;

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

	public ListDocumentPage(List<? extends DocumentInfo> infos, boolean appendInfo) {
		super("Dcouments");
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
			if (appendInfo)
				cell.addChild("\t" + info);
			table.addRow(cell);
		}
	}
}
