package searchengine.search.expression;

/**
 *
 * @author ZHS
 */
public abstract class ExpressionNodeBinary extends ExpressionNode
{

	private ExpressionNode left;
	private ExpressionNode right;

	public ExpressionNodeBinary(ExpressionNode right, ExpressionNode left)
	{
		this.left = left;
		this.right = right;
	}

	public ExpressionNode getLeft()
	{
		return left;
	}

	public ExpressionNode getRight()
	{
		return right;
	}
}
