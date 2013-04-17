package searchengine.servlet;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.gae.GAEDictionary;
import searchengine.html.builder.HTMLForm;
import searchengine.html.builder.HTMLFormInputFile;
import searchengine.html.builder.HTMLFormInputSubmit;
import searchengine.servlet.common.MainFrame;

/**
 *
 * @author ZHS
 */
public class AddDocumentServlet extends HttpServlet
{

	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		MainFrame mainFrame = new MainFrame("Add Document");
		mainFrame.getContent().addChild("<p>Add a document for search</p>");
		HTMLForm form = mainFrame.getContent().addChild(new HTMLForm(blobstoreService.createUploadUrl("/add"), "post", "multipart/form-data"));
		form.addChild(new HTMLFormInputFile("document", true));
		form.addChild("<br />");
		form.addChild(new HTMLFormInputSubmit("Add"));
		String addedString = req.getParameter("added");
		if (addedString != null)
		{
			String[] split = addedString.split(",");
			StringBuffer added = new StringBuffer();
			for (String string : split)
				added.append(string).append("<br/>");
			mainFrame.getContent().addChild("<p>Successfully added " + split.length + " document(s) : </p>" + added);
		}
		mainFrame.write(resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		List<BlobInfo> infos = blobstoreService.getBlobInfos(req).get("document");
		StringBuilder added = new StringBuilder();
		int count = 0;
		if (infos != null)
		{
			GAEDictionary dictionary = new GAEDictionary();
			for (BlobInfo info : infos)
				if (dictionary.adds(info))
				{
					added.append(info.getFilename()).append(",");
					count++;
				}
				else
					blobstoreService.delete(info.getBlobKey());
		}
		if (count > 0)
			resp.sendRedirect("/add?added=" + added);
		else
			resp.sendRedirect("/add");
	}
}
