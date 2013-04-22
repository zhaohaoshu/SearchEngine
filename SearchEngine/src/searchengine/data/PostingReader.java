package searchengine.data;

/**
 *
 * @author ZHS
 */
public abstract class PostingReader
{

	public abstract void moveNext();

	public abstract Posting read(boolean addPositions);

	public abstract long getCount();
}
