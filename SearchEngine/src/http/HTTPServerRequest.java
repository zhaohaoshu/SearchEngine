package http;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author ZHS
 */
public class HTTPServerRequest implements HTTPRequest {

	private String method;
	private Map<String, String> headers;
	private String url;
	private Map<String, String> queries;
	private InputStream inputStream;

	public HTTPServerRequest(String method, String requestURL, Map<String, String> headers, InputStream inputStream) {
		this.method = method;
		this.headers = headers;
		this.inputStream = inputStream;
		queries = new TreeMap<>();
		String[] requestURLSplit = requestURL.split("\\?");
		url = requestURLSplit[0];
		if (requestURLSplit.length > 1)
			for (String query : requestURLSplit[1].split("&")) {
				String[] querySplit = query.split("=");
				if (querySplit.length > 1)
					queries.put(Coder.decodeURL(querySplit[0]), Coder.decodeURL(querySplit[1]));
				else if (querySplit.length > 0)
					queries.put(Coder.decodeURL(querySplit[0]), "");
			}
	}

	@Override
	public InputStream getContentInputStream() {
		return inputStream;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public String getParameter(String key) {
		return queries.get(key);
	}

	@Override
	public String getHeader(String key) {
		return headers.get(key);
	}
}
