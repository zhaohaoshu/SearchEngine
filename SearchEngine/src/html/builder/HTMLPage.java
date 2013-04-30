package html.builder;

import java.io.PrintWriter;
import html.HTMLElement;

/**
 *
 * @author ZHS
 */
public class HTMLPage extends HTMLElement {

	private HTMLElement headElement;
	private HTMLElement titleElement;
	private HTMLElement bodyElement;

	public HTMLPage(String title) {
		super("html");
		headElement = addChild(new HTMLElement("head"));
		titleElement = headElement.addChild(new HTMLElement("title", title));
		bodyElement = addChild(new HTMLElement("body"));
	}

	public HTMLPage(String title, String css) {
		super("html");
		headElement = addChild(new HTMLElement("head"));
		titleElement = headElement.addChild(new HTMLElement("title", title));
		HTMLElement cssElement = headElement.addChild(new HTMLElement("link"));
		cssElement.setAttribute("rel", "stylesheet");
		cssElement.setAttribute("type", "text/css");
		cssElement.setAttribute("href", css);
		bodyElement = addChild(new HTMLElement("body"));
	}

	public HTMLElement getBodyElement() {
		return bodyElement;
	}

	public void write(PrintWriter writer) {
		writer.println("<!DOCTYPE html>");
		writer.println(this);
	}
}
