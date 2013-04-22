package ui.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import file.FileDocumentInfo;
import file.FileSearchDataManager;
import ui.page.ShowDocumentPage;
import http.HTTPRequest;
import http.HTTPResponse;

/**
 *
 * @author ZHS
 */
public class ShowDocumentServlet implements Servlet
{

	private FileSearchDataManager manager;

	public ShowDocumentServlet(FileSearchDataManager manager)
	{
		this.manager = manager;
	}

	@Override
	public void serve(HTTPRequest request, HTTPResponse response)
	{
		long id = 0;
		try
		{
			id = Long.parseLong(request.getParameter("id"));
		}
		catch (NumberFormatException ex)
		{
		}
		FileDocumentInfo info = manager.getDocumentInfo(id);
		if (info == null)
		{
			response.setRedirect("/list");
			return;
		}
		if ("download".equals(request.getParameter("download")))
		{
			response.serveFile(new File(info.getPathname()), info.getName(), "application/octet-stream");
			return;
		}
		try (FileInputStream fileInputStream = new FileInputStream(info.getPathname());)
		{
			ShowDocumentPage showDocumentPage = new ShowDocumentPage(
					info, fileInputStream,
					request.getParameter("pos"), request.getParameter("query"));
			ResponseWriter.write(showDocumentPage, response);
		}
		catch (IOException ex)
		{
			Logger.getLogger(ShowDocumentServlet.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
