package http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public abstract class HTTPResponse
{

	public abstract void setStatus(int status);

	public abstract void setStatusMessage(String statusMessage);

	public abstract void setHeader(String name, Object value);

	public abstract ByteArrayOutputStream getContentStream();

	public abstract PrintWriter getContentWriter();

	public void setContentType(String type)
	{
		setHeader("Content-Type", type);
	}

	/**
	 * http://en.wikipedia.org/wiki/HTTP_302
	 *
	 * @param url
	 */
	public void setRedirect(String url)
	{
		setStatus(302);
		setStatusMessage("Found");
		setHeader("Location", url);
	}

	public void serveFile(File file)
	{
		try (FileInputStream inputStream = new FileInputStream(file);)
		{
			byte[] buf = new byte[1024];
			for (;;)
			{
				int len = inputStream.read(buf);
				if (len < 0)
					break;
				getContentStream().write(buf, 0, len);
			}
		}
		catch (IOException ex)
		{
			Logger.getLogger(HTTPResponse.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void serveFile(File file, String fileName)
	{
		setHeader("Content-Disposition", "attachment;filename=\"" + Coder.encodeURL(fileName) + '\"');
		serveFile(file);
	}

	public void serveFile(File file, String fileName, String type)
	{
		setHeader("Content-Disposition", "attachment;filename=\"" + Coder.encodeURL(fileName) + '\"');
		setContentType(type);
		serveFile(file);
	}
}
