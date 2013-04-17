package searchengine.gae;

import java.util.Date;

/**
 *
 * @author ZHS
 */
public class Comment
{

	private String name;
	private Date time;
	private String comment;

	public Comment(String name, Date time, String comment)
	{
		this.name = name;
		this.time = time;
		this.comment = comment;
	}

	public String getName()
	{
		return name;
	}

	public Date getTime()
	{
		return time;
	}

	public String getComment()
	{
		return comment;
	}
}
