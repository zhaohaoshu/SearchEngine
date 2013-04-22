package http;

import java.io.InputStream;

/**
 *
 * @author ZHS
 */
public interface HTTPRequest
{

	/**
	 * Read the content
	 *
	 * @return
	 */
	public InputStream getContentInputStream();

	/**
	 * GET, PUT, etc.
	 *
	 * @return
	 */
	public String getMethod();

	/**
	 * /, /index.html, /index, etc.
	 *
	 * @return
	 */
	public String getURL();

	/**
	 * /index.html?key=value
	 *
	 * @param key key
	 * @return value
	 */
	public String getParameter(String key);

	/**
	 * Content-Type: text/html
	 *
	 * @param key Content-Type
	 * @return text/html
	 */
	public String getHeader(String key);
}
