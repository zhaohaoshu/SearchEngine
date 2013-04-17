package searchengine.servlet;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import searchengine.search.VectorSearch;
import searchengine.gae.GAEDictionary;
import searchengine.gae.GAEDocumentInfo;
import searchengine.html.Encoder;
import searchengine.html.builder.HTMLForm;
import searchengine.html.builder.HTMLFormInput;
import searchengine.html.builder.HTMLFormInputSubmit;
import searchengine.html.builder.HTMLLink;
import searchengine.html.builder.HTMLTable;
import searchengine.html.builder.HTMLTableCell;
import searchengine.search.BooleanSearch;
import searchengine.search.PositionalSearch;
import searchengine.search.expression.ExpressionNode;
import searchengine.servlet.common.MainFrame;

/**
 *
 * @author ZHS
 */
public class SearchServlet extends HttpServlet
{
	//<editor-fold defaultstate="collapsed" desc="Vector">

	private void vectorSearch(HTMLTable talbe, String query)
	{
		Calendar start = Calendar.getInstance();
		VectorSearch.VectorSearchResult[] searchResults = VectorSearch.vectorSearch(query, new GAEDictionary());
		Calendar end = Calendar.getInstance();

		talbe.addRow("<b>Type: </b>Vector search");
		talbe.addRow("<b>Time used (ms):</b> " + (end.getTime().getTime() - start.getTime().getTime()));

		DecimalFormat decimalFormat = new DecimalFormat("0.0000000000");
		for (VectorSearch.VectorSearchResult result : searchResults)
		{
			talbe.addRow("<hr/>");
			HTMLTableCell cell = new HTMLTableCell();
			cell.addChild("[" + decimalFormat.format(result.getScore()) + "] ");
			cell.addChild(new HTMLLink(
					"/show?id=" + result.getDocumentInfo().getDocumentID() +
					"&query=" + Encoder.encodeURL(query) +
					"#anchor",
					result.getDocumentInfo().getName(), "_blank"));
			talbe.addRow(cell);
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Positional">

	private class PositionalSearchResultWriter extends PositionalSearch.PositionalSearchResultWriter
	{

		private HTMLTable talbe;

		public PositionalSearchResultWriter(HTMLTable talbe)
		{
			this.talbe = talbe;
		}

		@Override
		public void write(GAEDocumentInfo documentInfo, List<int[]> results)
		{
			talbe.addRow("<hr/>");
			talbe.addRow(new HTMLLink(
					"/show?id=" + documentInfo.getDocumentID(), documentInfo.getName(), "_blank"));
			for (int[] result : results)
			{
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < result.length - 1; i++)
					buf.append(result[i]).append(',');
				buf.append(result[result.length - 1]);
				talbe.addRow(new HTMLLink(
						"/show?id=" + documentInfo.getDocumentID() +
						"&pos=" + buf + "#anchor",
						'[' + buf.toString() + ']', "_blank"));
			}
		}
	}

	private void positionalSearch(HTMLTable talbe, String query)
	{
		talbe.addRow("<b>Type: </b>Positional search");
		HTMLTableCell timeCell = talbe.addRow(new HTMLTableCell());

		Calendar start = Calendar.getInstance();
		PositionalSearch.positionalSearch(
				query, new GAEDictionary(), new PositionalSearchResultWriter(talbe));
		Calendar end = Calendar.getInstance();

		timeCell.addChild("<b>Time used (ms):</b> " + (end.getTime().getTime() - start.getTime().getTime()));
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Boolean">

	private class BooleanSearchResultWriter extends BooleanSearch.BooleanSearchResultWriter
	{

		private HTMLTable talbe;
		private HTMLTableCell expressionCell;
		private String query;

		public BooleanSearchResultWriter(HTMLTable talbe, HTMLTableCell expressionCell, String query)
		{
			this.talbe = talbe;
			this.expressionCell = expressionCell;
			this.query = query;
		}

		@Override
		public void writeExpression(ExpressionNode expression)
		{
			expressionCell.addChild("<b>Expression: </b>" + expression);
		}

		@Override
		public void write(GAEDocumentInfo documentInfo, ArrayList<Boolean> values)
		{
			talbe.addRow("<hr/>");
			HTMLTableCell cell = new HTMLTableCell();
			cell.addChild(new HTMLLink(
					"/show?id=" + documentInfo.getDocumentID() +
					"&query=" + Encoder.encodeURL(query) +
					"#anchor",
					documentInfo.getName(), "_blank"));
			cell.addChild("<br/>");
			for (int i = 0; i < values.size(); i++)
				cell.addChild(" [" + i + "]=" + values.get(i));
			talbe.addRow(cell);
		}
	}

	private void booleanSearch(HTMLTable talbe, String query)
	{
		talbe.addRow("<b>Type: </b>Boolean search");
		HTMLTableCell expressionCell = talbe.addRow(new HTMLTableCell());
		HTMLTableCell timeCell = talbe.addRow(new HTMLTableCell());

		Calendar start = Calendar.getInstance();
		BooleanSearch.booleanSearch(
				query, new GAEDictionary(), new BooleanSearchResultWriter(talbe, expressionCell, query));
		Calendar end = Calendar.getInstance();

		timeCell.addChild("<b>Time used (ms):</b> " + (end.getTime().getTime() - start.getTime().getTime()));
	}
	//</editor-fold>

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		MainFrame mainFrame = new MainFrame("Search");
		HTMLTable talbe = mainFrame.getContent().addChild(new HTMLTable());

		HTMLForm form = talbe.addRow(new HTMLForm("/search", "get"));
		form.addChild("<b>Search:</b><br/>");
		HTMLFormInput inputElement = form.addChild(new HTMLFormInput("text", "search"));
		inputElement.setAttribute("class", "search");
		form.addChild("<br/>");
		form.addChild(new HTMLFormInputSubmit("Vector search", "vector"));
		form.addChild(new HTMLFormInputSubmit("Positional search", "positional"));
		form.addChild(new HTMLFormInputSubmit("Boolean search", "boolean"));

		String query = req.getParameter("search");
		if (query != null)
		{
			talbe.addRow("<b>Query: </b>" + query);
			inputElement.setAttribute("value", query);
			if (req.getParameter("boolean") != null)
				booleanSearch(talbe, query);
			else if (req.getParameter("positional") != null)
				positionalSearch(talbe, query);
			else
				vectorSearch(talbe, query);
		}
		mainFrame.write(resp);
	}
}
