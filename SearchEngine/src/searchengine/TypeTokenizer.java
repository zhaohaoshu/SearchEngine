package searchengine;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class TypeTokenizer implements Closeable
{

	private BufferedReader reader;
	private StringBuilder buf = new StringBuilder();
	private int lastType = -1;
	private List<TreeSet<Character>> types = new LinkedList<>();

	public TypeTokenizer(Reader reader)
	{
		this.reader = new BufferedReader(reader);
	}

	public void addType(String string)
	{
		TreeSet<Character> set = new TreeSet<>();
		for (int i = 0; i < string.length(); i++)
			set.add(string.charAt(i));
		types.add(set);
	}

	public void addTypes(char[] chars)
	{
		for (char c : chars)
		{
			TreeSet<Character> set = new TreeSet<>();
			set.add(c);
			types.add(set);
		}
	}

	public void addTypes(String[] strings)
	{
		for (String string : strings)
			addType(string);
	}

	private int getType(int ch)
	{
		if (ch < 0)
			return -1;
		if (Character.isLetter(ch))
			return 1;
		int type = 2;
		for (TreeSet<Character> set : types)
		{
			if (set.contains((char) ch))
				return type;
			type++;
		}
		return type;
	}

	private void readNext()
	{
		lastType = -1;
		buf.delete(0, buf.length());
		for (;;)
		{
			int read = -1;
			try
			{
				reader.mark(1);
				read = reader.read();
			}
			catch (IOException ex)
			{
				Logger.getLogger(TypeTokenizer.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (read < 0)
				break;
			int type = getType(read);
			if (lastType < 0)
				lastType = type;
			if (type != lastType)
			{
				try
				{
					reader.reset();
				}
				catch (IOException ex)
				{
					Logger.getLogger(TypeTokenizer.class.getName()).log(Level.SEVERE, null, ex);
				}
				break;
			}
			buf.append((char) read);
		}
	}

	public List<String> getStrings(int requiredType)
	{
		List<String> result = new LinkedList<>();
		for (;;)
		{
			String next = getNext(requiredType);
			if (next == null)
				break;
			result.add(next);
		}
		return result;
	}

	public String getNext()
	{
		readNext();
		return getString();
	}

	public String getNext(int requiredType)
	{
		do
			readNext();
		while (!(lastType < 0 || lastType == requiredType));
		return getString();
	}

	public int getStringType()
	{
		return lastType;
	}

	public String getString()
	{
		if (lastType <= 0)
			return null;
		return buf.toString();
	}

	@Override
	public void close()
	{
		try
		{
			reader.close();
		}
		catch (IOException ex)
		{
			Logger.getLogger(TypeTokenizer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
