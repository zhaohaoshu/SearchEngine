package file;

import java.util.Arrays;

/**
 *
 * @author ZHS
 */
public class ByteArrayBuilder
{

	private byte[] bytes;
	private int length = 0;

	public ByteArrayBuilder()
	{
		bytes = new byte[16];
	}

	public void append(byte b)
	{
		if (length == bytes.length)
			bytes = Arrays.copyOf(bytes, length * 2);
		bytes[length++] = b;
	}

	public byte[] getBytes()
	{
		return Arrays.copyOf(bytes, length);
	}

	public int length()
	{
		return length;
	}

	public boolean startsWith(String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) != bytes[i])
				return false;
		return true;
	}

	public byte[] subBytes(int start, int end)
	{
		return Arrays.copyOfRange(bytes, start, end);
	}

	public String subString(int start, int end)
	{
		return new String(bytes, start, end - start);
	}

	public void clear()
	{
		length = 0;
	}
}
