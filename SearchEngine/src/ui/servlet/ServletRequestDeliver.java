package ui.servlet;

import java.io.File;
import file.FileSearchDataManager;
import http.HTTPRequest;
import http.HTTPResponse;
import http.RequestDeliver;

/**
 *
 * @author ZHS
 */
public class ServletRequestDeliver implements RequestDeliver {

	private File dictionaryDirFile;
	private File documentDirFile;
	private File servetDirFile;

	public ServletRequestDeliver(File dictionaryDirFile, File documentDirFile, File servetDirFile) {
		this.dictionaryDirFile = dictionaryDirFile;
		this.documentDirFile = documentDirFile;
		this.servetDirFile = servetDirFile;
	}

	@Override
	public void getResponse(HTTPRequest request, HTTPResponse response) {
		Servlet servlet;
		String url = request.getURL();
		if (url.startsWith("/resource"))
			response.serveFile(new File(servetDirFile, url));
		else
			switch (url) {
				case "/search":
					try (FileSearchDataManager manager = new FileSearchDataManager(
							documentDirFile, dictionaryDirFile, "r")) {
						servlet = new SearchServlet(manager);
						servlet.serve(request, response);
					}
					break;
				case "/show":
					try (FileSearchDataManager manager = new FileSearchDataManager(
							documentDirFile, dictionaryDirFile, "r")) {
						servlet = new ShowDocumentServlet(manager);
						servlet.serve(request, response);
					}
					break;
				case "/list":
					try (FileSearchDataManager manager = new FileSearchDataManager(
							documentDirFile, dictionaryDirFile, "r")) {
						servlet = new ListDocumentServlet(manager);
						servlet.serve(request, response);
					}
					break;
				default:
					servlet = new MainServlet();
					servlet.serve(request, response);
					break;
			}
	}
}