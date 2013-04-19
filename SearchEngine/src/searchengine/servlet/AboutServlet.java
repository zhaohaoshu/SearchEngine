package searchengine.servlet;

import java.io.IOException;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.gae.entity.EntityComment;
import searchengine.gae.entity.EntityCommentManager;
import searchengine.html.Encoder;
import searchengine.html.HTMLElement;
import searchengine.html.builder.HTMLForm;
import searchengine.html.builder.HTMLFormInput;
import searchengine.html.builder.HTMLFormInputSubmit;
import searchengine.html.builder.HTMLLink;
import searchengine.servlet.common.MainFrame;

/**
 *
 * @author ZHS
 */
public class AboutServlet extends HttpServlet
{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		MainFrame mainFrame = new MainFrame("About");
		mainFrame.getContent().addChild("<p>Homework for PKU ");
		mainFrame.getContent().addChild(new HTMLLink("http://net.pku.edu.cn/~course/cs410/2013/", "CS410 Web-based Information Architecture", "_blank"));
		mainFrame.getContent().addChild(" course</p>");

		mainFrame.getContent().addChild("<hr/>");
		mainFrame.getContent().addChild("<p>Please post comments</p>");
		mainFrame.getContent().addChild("Posted comments:<br/>");
		for (EntityComment comment : EntityCommentManager.getAllComments())
		{
			mainFrame.getContent().addChild("<hr/>");
			mainFrame.getContent().addChild("<p><b>" +
					Encoder.escapeHTML(comment.getName()) +
					"</b> at " + comment.getTime() + ":<br/>" +
					Encoder.escapeHTML(comment.getComment()) + "</p>");
		}

		mainFrame.getContent().addChild("<span id=\"postcomment\"/>");
		mainFrame.getContent().addChild("<hr/>");
		mainFrame.getContent().addChild("Post a comment (don't leave name and comment empty):<br/>");
		HTMLForm postForm = mainFrame.getContent().addChild(new HTMLForm("/about", "post"));
		postForm.addChild("Your name: ");
		postForm.addChild(new HTMLFormInput("text", "name"));
		postForm.addChild("<br/>Your comment: <br/>");
		HTMLElement comment = postForm.addChild(new HTMLElement("textarea"));
		comment.setAttribute("class", "comment");
		comment.setAttribute("name", "comment");
		comment.setAttribute("rows", 10);
		comment.addChild("");
		postForm.addChild("<br/>");
		postForm.addChild(new HTMLFormInputSubmit("Comment"));

		mainFrame.write(resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String name = req.getParameter("name");
		String comment = req.getParameter("comment");
		if (name != null && !name.isEmpty() && comment != null && !comment.isEmpty())
			EntityCommentManager.addComment(name, Calendar.getInstance().getTime(), comment);
		resp.sendRedirect("/about#postcomment");
	}
}
