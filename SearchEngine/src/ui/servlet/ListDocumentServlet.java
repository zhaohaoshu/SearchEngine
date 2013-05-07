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
		long page;
		try {
			page = Long.parseLong(request.getParameter("page"));
		}
		catch (NumberFormatException ex) {
			page = 1;
		}
		long count = manager.getDocumentCount();
		int countPerPage = 30;
		long pageCount = (count + countPerPage - 1) / countPerPage;
		if (page > pageCount)
			page = pageCount;
		LinkedList<DocumentInfo> infos = new LinkedList<>();
		long startID = countPerPage * (page - 1) + 1;
		long endID = Math.min(count, startID + countPerPage - 1);
		for (long i = startID; i <= endID; i++)
			infos.add(manager.getDocumentInfo(i));
		ListDocumentPage listDocumentPage = new ListDocumentPage(infos, pageCount, page);
		ResponseWriter.write(listDocumentPage, response);
	}
}
