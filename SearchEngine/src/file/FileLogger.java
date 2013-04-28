package file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class FileLogger
{

	private static DateFormat dateFormat = DateFormat.getTimeInstance();
	private static PrintWriter writer;

	static
	{
		try
		{
			writer = new PrintWriter(new FileWriter(new File("message.txt")), true);
		}
		catch (FileNotFoundException ex)
		{
			Logger.getLogger(FileLogger.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex)
		{
			Logger.getLogger(FileLogger.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void log(String message)
	{
		String out = "(" + dateFormat.format(Calendar.getInstance().getTime()) + ")" +
				message;
		System.out.println(out);
		writer.println(out);
	}
}
