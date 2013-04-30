package searchengine;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class TypeTokenizer implements Closeable {

	private StringBuilder buf = new StringBuilder();
	private int lastType = -1;
	private int lastRead = -1;
	private List<TreeSet<Character>> types = new LinkedList<>();
	private InputStream inputStream;

	public TypeTokenizer(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public TypeTokenizer(String string) {
		inputStream = new ByteArrayInputStream(string.getBytes());
	}

	public void addType(String string) {
		TreeSet<Character> set = new TreeSet<>();
		for (int i = 0; i < string.length(); i++)
			set.add(string.charAt(i));
		types.add(set);
	}

	public void addTypes(char[] chars) {
		for (char c : chars) {
			TreeSet<Character> set = new TreeSet<>();
			set.add(c);
			types.add(set);
		}
	}

	public void addTypes(String[] strings) {
		for (String string : strings)
			addType(string);
	}

	private int getType(int ch) {
		if (ch < 0)
			return -1;
		if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z'))
			return 1;
		int type = 2;
		for (TreeSet<Character> set : types) {
			if (set.contains((char) ch))
				return type;
			type++;
		}
		return type;
	}

	private void readNext() {
		lastType = -1;
		buf.delete(0, buf.length());
		for (;;) {
			int read = -1;
			if (lastRead >= 0) {
				read = lastRead;
				lastRead = -1;
			}
			else
				try {
					read = inputStream.read();
				}
				catch (IOException ex) {
					Logger.getLogger(TypeTokenizer.class.getName()).log(Level.SEVERE, null, ex);
				}
			if (read < 0)
				break;
			int type = getType(read);
			if (lastType < 0)
				lastType = type;
			if (type != lastType) {
				lastRead = read;
				break;
			}
			buf.append((char) read);
		}
	}

	public List<String> getStrings(int requiredType) {
		List<String> result = new LinkedList<>();
		for (;;) {
			String next = getNext(requiredType);
			if (next == null)
				break;
			result.add(next);
		}
		return result;
	}

	public String getNext() {
		readNext();
		return getString();
	}

	public String getNext(int requiredType) {
		do
			readNext();
		while (!(lastType < 0 || lastType == requiredType));
		return getString();
	}

	public int getStringType() {
		return lastType;
	}

	public String getString() {
		if (lastType <= 0)
			return null;
		return buf.toString();
	}

	@Override
	public void close() {
		try {
			inputStream.close();
		}
		catch (IOException ex) {
			Logger.getLogger(TypeTokenizer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
