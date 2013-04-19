package searchengine.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.gae.entity.EntityDocumentManager;
import searchengine.gae.GAEDocumentInfo;
import searchengine.html.builder.HTMLLink;
import searchengine.html.builder.HTMLTable;
import searchengine.html.builder.HTMLTableCell;
import searchengine.servlet.common.MainFrame;

/**
 *
 * @author ZHS
 */
public class ListDocumentServlet extends HttpServlet
{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		List<GAEDocumentInfo> infos = EntityDocumentManager.getAllDocumentInfos();

		MainFrame mainFrame = new MainFrame("Document List");
		HTMLTable table = mainFrame.getContent().addChild(new HTMLTable());
		table.addRow("<b>Documents: </b>");
		for (GAEDocumentInfo info : infos)
		{
			HTMLTableCell cell = new HTMLTableCell();
			cell.addChild("[");
			cell.addChild(new HTMLLink("/show?id=" + info.getDocumentID() + "&download=download", "download"));
			cell.addChild("] ");
			cell.addChild(new HTMLLink("show?id=" + info.getDocumentID(), info.getName()));
			table.addRow(cell);
		}
		mainFrame.write(resp);
	}
}
