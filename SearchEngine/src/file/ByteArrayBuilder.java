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

	public int length()
	{
		return length;
	}

	public boolean equalsString(String str)
	{
		if (str.length() != length)
			return false;
		for (int i = 0; i < length; i++)
			if (str.charAt(i) != bytes[i])
				return false;
		return true;
	}

	public void clear()
	{
		length = 0;
	}

	public boolean isEmpty()
	{
		return length == 0;
	}

	@Override
	public String toString()
	{
		return new String(bytes, 0, length);
	}
}
