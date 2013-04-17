package searchengine.html.builder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import searchengine.html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLPage extends HTMLElement
{

	private HTMLElement headElement;
	private HTMLElement titleElement;
	private HTMLElement bodyElement;

	public HTMLPage(String title)
	{
		super("html");
		headElement = addChild(new HTMLElement("head"));
		titleElement = headElement.addChild(new HTMLElement("title", title));
		bodyElement = addChild(new HTMLElement("body"));
	}

	public HTMLPage(String title, String css)
	{
		super("html");
		headElement = addChild(new HTMLElement("head"));
		titleElement = headElement.addChild(new HTMLElement("title", title));
		HTMLElement cssElement = headElement.addChild(new HTMLElement("link"));
		cssElement.setAttribute("rel", "stylesheet");
		cssElement.setAttribute("type", "text/css");
		cssElement.setAttribute("href", css);
		bodyElement = addChild(new HTMLElement("body"));
	}

	public HTMLElement getBodyElement()
	{
		return bodyElement;
	}

	public void write(HttpServletResponse resp)
	{
		resp.setContentType("text/html");
		try
		{
			resp.getWriter().println("<!DOCTYPE html>");
			resp.getWriter().println(this);
		}
		catch (IOException ex)
		{
			Logger.getLogger(HTMLPage.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
