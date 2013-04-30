package ui.servlet;

import java.io.PrintWriter;
import ui.page.MainPage;
import http.HTTPRequest;
import http.HTTPResponse;

/**
 *
 * @author ZHS
 */
public class MainServlet implements Servlet {

	@Override
	public void serve(HTTPRequest request, HTTPResponse response) {
		MainPage mainPage = new MainPage();
		ResponseWriter.write(mainPage, response);
	}
}
