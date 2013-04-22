package http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class HTTPServerResponse extends HTTPResponse
{

	private int status;
	private String statusMessage;
	private Map<String, String> headers;
	private ByteArrayOutputStream contentStream;
	private PrintWriter contentWriter;
	private OutputStream socketStream;

	public HTTPServerResponse(OutputStream socketStream)
	{
		status = 200;
		statusMessage = "OK";
		headers = new TreeMap<>();
		contentStream = new java.io.ByteArrayOutputStream();
		contentWriter = new PrintWriter(contentStream, true);
		this.socketStream = socketStream;
	}

	@Override
	public void setStatus(int status)
	{
		this.status = status;
	}

	@Override
	public void setStatusMessage(String statusMessage)
	{
		this.statusMessage = statusMessage;
	}

	@Override
	public void setHeader(String name, Object value)
	{
		headers.put(name, value.toString());
	}

	@Override
	public ByteArrayOutputStream getContentStream()
	{
		return contentStream;
	}

	@Override
	public PrintWriter getContentWriter()
	{
		return contentWriter;
	}

	public void write()
	{
		PrintWriter printWriter = new PrintWriter(socketStream, true);
		printWriter.println("HTTP/1.1 " + status + " " + statusMessage);
		for (Map.Entry<String, String> entry : headers.entrySet())
			printWriter.println(entry.getKey() + ": " + entry.getValue());
		printWriter.println();
		try
		{
			contentStream.writeTo(socketStream);
		}
		catch (IOException ex)
		{
			Logger.getLogger(HTTPServerResponse.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
