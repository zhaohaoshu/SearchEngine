package searchengine.data;

/**
 *
 * @author ZHS
 */
public abstract class PostingReader
{

	public abstract void movePrevious();

	public abstract Posting read(boolean addPositions);

	public abstract long getCount();
}
