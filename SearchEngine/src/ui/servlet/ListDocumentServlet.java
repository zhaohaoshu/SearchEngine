package ui.servlet;

import http.HTTPRequest;
import http.HTTPResponse;
import java.util.LinkedList;
import searchengine.data.DocumentInfo;
import file.FileSearchDataManager;
import ui.page.ListDocumentPage;

/**
 *
 * @author ZHS
 */
public class ListDocumentServlet implements Servlet {

	private FileSearchDataManager manager;

	public ListDocumentServlet(FileSearchDataManager manager) {
		this.manager = manager;
	}

	@Override
	public void serve(HTTPRequest request, HTTPResponse response) {
		long count = manager.getDocumentCount();
		LinkedList<DocumentInfo> infos = new LinkedList<>();
		for (int i = 1; i <= count; i++)
			infos.add(manager.getDocumentInfo(i));
		boolean appendInfo = false;
		if (request.getParameter("append") != null)
			appendInfo = true;
		ListDocumentPage listDocumentPage = new ListDocumentPage(infos, appendInfo);
		ResponseWriter.write(listDocumentPage, response);
	}
}
