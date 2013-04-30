package file;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import file.manager.PostingManager;
import searchengine.data.Posting;
import searchengine.data.PostingReader;

/**
 *
 * @author ZHS
 */
public class FilePostingReader extends PostingReader {

	private PostingManager.PostingPointer postingPointer;

	public FilePostingReader(PostingManager.PostingPointer postingPointer) {
		this.postingPointer = postingPointer;
	}

	@Override
	public void moveNext() {
		try {
			if (!postingPointer.isEnd())
				postingPointer.moveNext();
		}
		catch (IOException ex) {
			Logger.getLogger(FilePostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Posting read() {
		if (postingPointer.isEnd())
			return null;
		try {
			return postingPointer.getPosting();
		}
		catch (IOException ex) {
			Logger.getLogger(FilePostingReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public long getCount() {
		return postingPointer.getPostingCount();
	}
}
