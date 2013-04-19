package searchengine.gae.entity;

import java.util.Date;

/**
 *
 * @author ZHS
 */
public class EntityComment
{

	private String name;
	private Date time;
	private String comment;

	public EntityComment(String name, Date time, String comment)
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
