package searchengine.servlet;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.gae.entity.EntityDictionaryManager;
import searchengine.gae.entity.EntityDocumentManager;

/**
 *
 * @author ZHS
 */
public class ClearDataServlet extends HttpServlet
{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		String target = req.getParameter("target");
		if (target == null)
		{
			resp.getWriter().println("...");
			return;
		}
		switch (target)
		{
			case "datastore":
			{
				long start = System.currentTimeMillis();
				for (;;)
				{
					if (req.getParameter("do") == null)
					{
						QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/clear?target=datastore&do=do"));
						resp.getWriter().println("task added");
						return;
					}
					Query q = new Query().setKeysOnly();
					ArrayList<Key> keyList = new ArrayList<>();
					for (Entity entity : datastoreService.prepare(q).asIterable(FetchOptions.Builder.withLimit(128)))
						keyList.add(entity.getKey());
					if (keyList.size() > 0)
						try
						{
							datastoreService.delete(keyList);
						}
						catch (Exception ex)
						{
						}
					else
						break;
					if (System.currentTimeMillis() - start > 500000)
					{
						QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/clear?target=datastore&do=do"));
						break;
					}
				}
			}
			break;
			case "dictionary":
			{
				BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
				List<BlobKey> blobKeys = EntityDocumentManager.getAllDocumentKeys();
				blobstoreService.delete(blobKeys.toArray(new BlobKey[blobKeys.size()]));
				Query q = new Query(EntityDictionaryManager.getDictionaryKey());
				PreparedQuery prepare = datastoreService.prepare(q);
				LinkedList<Key> keys = new LinkedList<>();
				for (Entity entity : prepare.asIterable())
					keys.add(entity.getKey());
				datastoreService.delete(keys);
				resp.getWriter().println("dictionary cleared");
			}
			break;
		}
	}
}
