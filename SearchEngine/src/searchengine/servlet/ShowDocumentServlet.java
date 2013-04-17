package searchengine.servlet;

import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.TypeTokenizer;
import searchengine.gae.GAEDocumentInfo;
import searchengine.gae.GAEDictionary;
import searchengine.html.Encoder;
import searchengine.html.builder.HTMLLink;
import searchengine.servlet.common.MainFrame;

/**
 *
 * @author ZHS
 */
public class ShowDocumentServlet extends HttpServlet
{

	private TreeSet<Integer> getPositions(HttpServletRequest req)
	{
		TreeSet<Integer> positions = new TreeSet<>();
		String parameter = req.getParameter("pos");
		if (parameter != null)
		{
			TypeTokenizer tokenizer = new TypeTokenizer(new StringReader(parameter));
			tokenizer.addType("0123456789");
			for (;;)
			{
				String token = tokenizer.getNext(2);
				if (token == null)
					break;
				positions.add(Integer.parseInt(token));
			}
		}
		return positions;
	}

	private TreeSet<String> getKeyWords(HttpServletRequest req)
	{
		TreeSet<String> keyWords = new TreeSet<>();
		String parameter = req.getParameter("query");
		if (parameter != null)
		{
			TypeTokenizer tokenizer = new TypeTokenizer(new BufferedReader(new StringReader(parameter)));
			for (String string : tokenizer.getStrings(1))
				keyWords.add(string.toLowerCase());
		}
		return keyWords;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		long id = 0;
		try
		{
			id = Long.parseLong(req.getParameter("id"));
		}
		catch (NumberFormatException ex)
		{
		}
		if (id <= 0)
		{
			resp.sendRedirect("/list");
			return;
		}
		GAEDictionary dictionary = new GAEDictionary();
		GAEDocumentInfo info = dictionary.getDocumentInfo(id);
		if (info == null)
		{
			resp.sendRedirect("/list");
			return;
		}
		if ("download".equals(req.getParameter("download")))
		{
			resp.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(info.getName(), "UTF-8") + '\"');
			BlobstoreServiceFactory.getBlobstoreService().serve(info.getKey(), resp);
			return;
		}
		MainFrame mainFrame = new MainFrame("Document - " + info.getName());
		mainFrame.getContent().addChild("<p><b>Document - " + info.getName() + "</b><br/>[");
		mainFrame.getContent().addChild(new HTMLLink("/show?id=" + info.getDocumentID() + "&download=download", "download"));
		mainFrame.getContent().addChild("]</p>");
		TreeSet<Integer> positions = getPositions(req);
		TreeSet<String> keyWords = getKeyWords(req);
		boolean flag = false;
		try (TypeTokenizer tokenizer = new TypeTokenizer(new BufferedReader(new InputStreamReader(new BlobstoreInputStream(info.getKey())))))
		{
			int position = 0;
			StringBuilder builder = new StringBuilder();
			for (;;)
			{
				String string = tokenizer.getNext();
				if (string == null)
					break;
				if (tokenizer.getStringType() == 1)
				{
					if (positions.contains(position) || keyWords.contains(string.toLowerCase()))
					{
						if (!flag)
						{
							builder.append("<span id=\"anchor\"/>");
							flag = true;
						}
						builder.append("<b style=\"background-color:yellow\">").append(string).append("</b>");
					}
					else
						builder.append(string);
					position++;
				}
				else
					builder.append(Encoder.escapeHTML(string));
			}
			mainFrame.getContent().addChild(builder.toString());
		}
		mainFrame.write(resp);
	}
}
