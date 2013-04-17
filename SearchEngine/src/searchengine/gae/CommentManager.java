package searchengine.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ZHS
 */
public class CommentManager
{

	public static final String KIND_COMMENTS = "Comments";
	public static final String PROPERTY_COMMENTS_COUNT = "count";
	public static final String KIND_COMMENT = "Comment";
	public static final String PROPERTY_COMMENT_NAME = "name";
	public static final String PROPERTY_COMMENT_TIME = "date";
	public static final String PROPERTY_COMMENT_COMMENT = "comment";
	public static final Key COMMENTS_KEY = KeyFactory.createKey(KIND_COMMENTS, 1);

	public static void addComment(String name, Date time, String comment)
	{
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		Transaction transaction = datastoreService.beginTransaction();
		try
		{
			Entity entityComments;
			long count;
			try
			{
				entityComments = datastoreService.get(COMMENTS_KEY);
				count = ((Number) entityComments.getProperty(PROPERTY_COMMENTS_COUNT)).longValue();
				count++;
			}
			catch (EntityNotFoundException ex)
			{
				entityComments = new Entity(COMMENTS_KEY);
				count = 1;
			}
			entityComments.setUnindexedProperty(PROPERTY_COMMENTS_COUNT, count);
			datastoreService.put(entityComments);
			Entity entityComment = new Entity(KIND_COMMENT, count, COMMENTS_KEY);
			entityComment.setUnindexedProperty(PROPERTY_COMMENT_NAME, name);
			entityComment.setUnindexedProperty(PROPERTY_COMMENT_TIME, time);
			entityComment.setUnindexedProperty(PROPERTY_COMMENT_COMMENT, comment);
			datastoreService.put(entityComment);
			transaction.commit();
		}
		finally
		{
			if (transaction.isActive())
				transaction.rollback();
		}
	}

	public static List<Comment> getAllComments()
	{
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		List<Comment> comments = new LinkedList<>();
		Query query = new Query(KIND_COMMENT, COMMENTS_KEY);
		PreparedQuery prepare = datastoreService.prepare(query);
		for (Entity entity : prepare.asIterable())
			comments.add(new Comment(
					(String) entity.getProperty(PROPERTY_COMMENT_NAME),
					(Date) entity.getProperty(PROPERTY_COMMENT_TIME),
					(String) entity.getProperty(PROPERTY_COMMENT_COMMENT)));
		return comments;
	}
}
