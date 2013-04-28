package file;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author ZHS
 */
public class OffsetReader implements Closeable
{

	private FileChannel fileChannel;
	private ByteBuffer byteBuffer;

	public OffsetReader(File file) throws FileNotFoundException
	{
		fileChannel = new FileInputStream(file).getChannel();
		byteBuffer = ByteBuffer.allocate(8192);
		byteBuffer.limit(0);
	}

	public int read() throws IOException
	{
		if (!byteBuffer.hasRemaining())
		{
			byteBuffer.clear();
			int count = fileChannel.read(byteBuffer);
			if (count < 0)
				return -1;
			byteBuffer.position(0);
			byteBuffer.limit(count);
		}
		return byteBuffer.get() & 0xff;
	}

	public long getOffset() throws IOException
	{
		return fileChannel.position();
	}

	@Override
	public void close() throws IOException
	{
		fileChannel.close();
	}
}
