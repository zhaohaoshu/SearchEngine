package ui.servlet;

import ui.page.SearchPage;
import http.HTTPRequest;
import http.HTTPResponse;
import searchengine.data.SearchDataManager;

/**
 *
 * @author ZHS
 */
public class SearchServlet implements Servlet {

	private SearchDataManager manager;

	public SearchServlet(SearchDataManager manager) {
		this.manager = manager;
	}

	@Override
	public void serve(HTTPRequest request, HTTPResponse response) {
		SearchPage searchPage = new SearchPage();
		String searchQuery = request.getParameter("search");
		if (searchQuery != null) {
			String type = request.getParameter("type");
			if (type == null)
				type = "";
			searchPage.search(searchQuery, type, manager);
		}
		ResponseWriter.write(searchPage, response);
	}
}
