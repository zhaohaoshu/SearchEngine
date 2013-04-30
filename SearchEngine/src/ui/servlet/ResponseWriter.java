package ui.servlet;

import java.io.PrintWriter;
import html.builder.HTMLPage;
import http.HTTPResponse;

/**
 *
 * @author ZHS
 */
public class ResponseWriter {

	public static void write(HTMLPage page, HTTPResponse response) {
		try (PrintWriter writer = new PrintWriter(response.getContentStream())) {
			page.write(writer);
		}
	}
}
