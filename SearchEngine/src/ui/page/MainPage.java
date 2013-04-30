package ui.page;

/**
 *
 * @author ZHS
 */
public class MainPage extends MainFrame {

	public MainPage() {
		super("ZHS Search Engine");
		getContent().addChild(
				"<p>This search engine allow users to upload documents, and retrieve them.</p>");
		getContent().addChild(
				"<p>I implemented 3 kinds of search method: boolean search, positional search, and vector search.</p>");
		getContent().addChild(
				"<p>Boolean search supports boolean expression. You can search like \"<b>!abc&bcd&!(cde|def)|efg&fgh</b>\". Operator \"&\" can be omitted. That means previous expression is equivalent to \"<b>!abc bcd !(cde|def)|efg fgh</b>\".</p>");
		getContent().addChild(
				"<p>In positional search, you can use \"<b>/n</b>\" to specify the distance between tokens. A \"<b>/n</b>\" at the beginning of the query indicates the default distance. That means \"<b>/3 a b /5 c d</b>\" is equivalent to \"<b>a /3 b /5 c /3 d</b>\".</p>");
	}
}
