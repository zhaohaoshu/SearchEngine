package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class HTTPServer
{

	private int port;
	private RequestDeliver deliver;
	private HTTPThread httpThread = null;
	private boolean stop = false;

	public HTTPServer(int port, RequestDeliver deliver)
	{
		this.port = port;
		this.deliver = deliver;
	}

	public void start()
	{
		if (httpThread == null)
		{
			httpThread = new HTTPThread();
			httpThread.start();
		}
	}

	public void stop()
	{
		stop = true;
		httpThread.closeServerSocket();
	}

	private class HTTPThread extends Thread
	{

		private ServerSocket serverSocket;

		private void closeServerSocket()
		{
			try
			{
				if (serverSocket != null)
					serverSocket.close();
			}
			catch (IOException ex)
			{
				Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		@Override
		public void run()
		{
			try
			{
				serverSocket = new ServerSocket(port);
				while (!stop)
				{
					Socket socket = serverSocket.accept();
					ResponseThread responseThread = new ResponseThread(socket);
					responseThread.start();
				}
				serverSocket.close();
			}
			catch (IOException ex)
			{
			}
		}
	}

	private class ResponseThread extends Thread
	{

		private Socket socket;

		public ResponseThread(Socket socket)
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			try (InputStream inputStream = socket.getInputStream(); OutputStream outputStream = socket.getOutputStream();)
			{
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String requestLine = bufferedReader.readLine();
				System.out.println(requestLine);
				if (requestLine != null)
				{
					StringTokenizer tokenizer = new StringTokenizer(requestLine);
					String method = tokenizer.nextToken();
					String requestURL = tokenizer.nextToken();
					TreeMap<String, String> headers = new TreeMap<>();
					for (;;)
					{
						String headerLine = bufferedReader.readLine();
						if (headerLine == null || headerLine.isEmpty())
							break;
						String[] headerSplit = headerLine.split(":");
						StringBuilder value = new StringBuilder(headerSplit[1]);
						for (int i = 2; i < headerSplit.length; i++)
							value.append(':').append(headerSplit[i]);
						headers.put(headerSplit[0], value.toString());
					}
					HTTPRequest request = new HTTPServerRequest(method, requestURL, headers, inputStream);
					HTTPServerResponse response = new HTTPServerResponse(outputStream);
					deliver.getResponse(request, response);
					response.write();
				}
			}
			catch (IOException ex)
			{
				Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
