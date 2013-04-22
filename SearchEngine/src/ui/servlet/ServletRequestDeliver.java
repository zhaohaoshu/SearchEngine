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
public class ServletRequestDeliver implements RequestDeliver
{

	private File servetPathFile;
	private FileSearchDataManager manager;

	public ServletRequestDeliver(File servetPathFile, FileSearchDataManager manager)
	{
		this.servetPathFile = servetPathFile;
		this.manager = manager;
	}

	@Override
	public void getResponse(HTTPRequest request, HTTPResponse response)
	{
		Servlet servlet;
		String url = request.getURL();
		if (url.startsWith("/resource"))
			response.serveFile(new File(servetPathFile, url));
		else
		{
			switch (url)
			{
				case "/search":
					servlet = new SearchServlet(manager);
					break;
				case "/show":
					servlet = new ShowDocumentServlet(manager);
					break;
				case "/list":
					servlet = new ListDocumentServlet(manager);
					break;
				default:
					servlet = new MainServlet();
					break;
			}
			servlet.serve(request, response);
		}
	}
}