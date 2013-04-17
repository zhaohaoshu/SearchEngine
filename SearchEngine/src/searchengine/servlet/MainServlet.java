package searchengine.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.servlet.common.MainFrame;

/**
 *
 * @author ZHS
 */
public class MainServlet extends HttpServlet
{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		MainFrame mainFrame = new MainFrame("ZHS Search Engine");
		mainFrame.getContent().addChild(
				"<p>This search engine allow users to upload documents, and retrieve them.</p>");
		mainFrame.getContent().addChild(
				"<p>I implemented 3 kinds of search method: boolean search, positional search, and vector search.</p>");
		mainFrame.getContent().addChild(
				"<p>Boolean search supports boolean expression. You can search like \"<b>!abc&bcd&!(cde|def)|efg&fgh</b>\". Operator \"&\" can be omitted. That means previous expression is equivalent to \"<b>!abc bcd !(cde|def)|efg fgh</b>\".</p>");
		mainFrame.getContent().addChild(
				"<p>In positional search, you can use \"<b>/n</b>\" to specify the distance between tokens. A \"<b>/n</b>\" at the beginning of the query indicates the default distance. That means \"<b>/3 a b /5 c d</b>\" is equivalent to \"<b>a /3 b /5 c /3 d</b>\".</p>");
		mainFrame.write(resp);
	}
}
