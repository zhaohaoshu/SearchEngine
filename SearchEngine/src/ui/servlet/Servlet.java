package ui.servlet;

import http.HTTPRequest;
import http.HTTPResponse;

/**
 *
 * @author ZHS
 */
public interface Servlet
{

	public abstract void serve(HTTPRequest request, HTTPResponse response);
}
