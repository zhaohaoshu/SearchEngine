package ui.page;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import http.Coder;
import html.builder.HTMLForm;
import html.builder.HTMLFormInput;
import html.builder.HTMLFormInputSubmit;
import html.builder.HTMLLink;
import html.builder.HTMLTable;
import html.builder.HTMLTableCell;
import searchengine.search.BooleanSearch;
import searchengine.search.PositionalSearch;
import searchengine.search.VectorSearch;
import searchengine.data.DocumentInfo;
import searchengine.data.SearchDataManager;
import searchengine.search.expression.ExpressionNode;

/**
 *
 * @author ZHS
 */
public class SearchPage extends MainFrame {

	private HTMLFormInput inputElement;
	private HTMLTable talbe;

	public SearchPage() {
		super("Search");

		talbe = getContent().addChild(new HTMLTable());

		HTMLForm form = talbe.addRow(new HTMLForm("/search", "get"));
		form.addChild("<b>Search:</b><br/>");
		inputElement = form.addChild(new HTMLFormInput("text", "search"));
		inputElement.setAttribute("class", "search");

		form.addChild(new HTMLFormInputSubmit("vector search", "type"));
		form.addChild(new HTMLFormInputSubmit("positional search", "type"));
		form.addChild(new HTMLFormInputSubmit("boolean search", "type"));
	}

	public void search(String query, String type, SearchDataManager manager) {
		talbe.addRow("<hr/>");
		talbe.addRow("<b>Query: </b>" + query);
		inputElement.setAttribute("value", query);
		if (type.startsWith("boolean"))
			booleanSearch(talbe, query, manager);
		else if (type.startsWith("positional"))
			positionalSearch(talbe, query, manager);
		else
			vectorSearch(talbe, query, manager);
	}
	//<editor-fold defaultstate="collapsed" desc="Vector">

	private class VectorSearchResultWriter extends VectorSearch.VectorSearchResultWriter {

		private TreeMap<Double, LinkedList<Long>> map;
		private int maxCount;
		private int count = 0;

		public VectorSearchResultWriter(int maxCount) {
			this.maxCount = maxCount;
			map = new TreeMap<>();
		}

		@Override
		public void write(double score, long documentID) {
			LinkedList<Long> list = map.get(score);
			if (list == null) {
				list = new LinkedList<>();
				map.put(score, list);
			}
			list.add(documentID);
			if (count < maxCount)
				count++;
			else {
				Map.Entry<Double, LinkedList<Long>> firstEntry = map.firstEntry();
				LinkedList<Long> firstList = firstEntry.getValue();
				firstList.remove();
				if (firstList.isEmpty())
					map.remove(firstEntry.getKey());
			}
		}

		public TreeMap<Double, LinkedList<Long>> getMap() {
			return map;
		}
	}

	private void vectorSearch(HTMLTable talbe, String query, SearchDataManager manager) {
		Calendar start = Calendar.getInstance();
		int maxCount = 0;
		if (query.startsWith("/"))
			for (int i = 1; i < query.length(); i++)
				if (Character.isDigit(query.charAt(i))) {
					for (int j = i; j < query.length() && Character.isDigit(query.charAt(j)); j++)
						maxCount = maxCount * 10 + (query.charAt(j) - '0');
					break;
				}
		if (maxCount <= 0)
			maxCount = 30;
		VectorSearchResultWriter writer = new VectorSearchResultWriter(maxCount);
		VectorSearch.vectorSearch(query, manager, writer);
		Calendar end = Calendar.getInstance();

		talbe.addRow("<b>Type: </b>Vector search");
		talbe.addRow("<b>Time used (ms):</b> " + (end.getTime().getTime() - start.getTime().getTime()));

		DecimalFormat decimalFormat = new DecimalFormat("0.0000000000");
		for (Map.Entry<Double, LinkedList<Long>> entry : writer.getMap().descendingMap().entrySet()) {
			Double score = entry.getKey();
			for (Long id : entry.getValue()) {
				talbe.addRow("<hr/>");
				HTMLTableCell cell = new HTMLTableCell();
				cell.addChild("[" + decimalFormat.format(score) + "] ");
				cell.addChild(new HTMLLink(
						"/show?id=" + id +
						"&query=" + Coder.encodeURL(query),
						manager.getDocumentName(id), "_blank"));
				talbe.addRow(cell);
			}
		}
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Positional">

	private class PositionalSearchResultWriter extends PositionalSearch.PositionalSearchResultWriter {

		private HTMLTable talbe;
		private String query;
		private SearchDataManager manager;

		public PositionalSearchResultWriter(HTMLTable talbe, String query, SearchDataManager manager) {
			this.talbe = talbe;
			this.query = query;
			this.manager = manager;
		}

		@Override
		public void write(long documentID, List<int[]> results) {
			DocumentInfo documentInfo = manager.getDocumentInfo(documentID);
			talbe.addRow("<hr/>");
			talbe.addRow(new HTMLLink(
					"/show?id=" + documentInfo.getDocumentID() +
					"&query=" + Coder.encodeURL(query),
					documentInfo.getName(), "_blank"));
			for (int[] result : results) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < result.length - 1; i++)
					buf.append(result[i]).append(',');
				buf.append(result[result.length - 1]);
				talbe.addRow(new HTMLLink(
						"/show?id=" + documentInfo.getDocumentID() +
						"&pos=" + buf + "#anchorp0",
						'[' + buf.toString() + ']', "_blank"));
			}
		}
	}

	private void positionalSearch(HTMLTable talbe, String query, SearchDataManager manager) {
		talbe.addRow("<b>Type: </b>Positional search");
		HTMLTableCell timeCell = talbe.addRow(new HTMLTableCell());

		Calendar start = Calendar.getInstance();
		PositionalSearch.positionalSearch(
				query, manager, new PositionalSearchResultWriter(talbe, query, manager));
		Calendar end = Calendar.getInstance();

		timeCell.addChild("<b>Time used (ms):</b> " + (end.getTime().getTime() - start.getTime().getTime()));
	}
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Boolean">

	private class BooleanSearchResultWriter implements BooleanSearch.BooleanSearchResultWriter {

		private HTMLTable talbe;
		private HTMLTableCell expressionCell;
		private String query;
		private SearchDataManager manager;

		public BooleanSearchResultWriter(HTMLTable talbe, HTMLTableCell expressionCell, String query, SearchDataManager manager) {
			this.talbe = talbe;
			this.expressionCell = expressionCell;
			this.query = query;
			this.manager = manager;
		}

		@Override
		public void writeBooleanExpression(ExpressionNode expression) {
			expressionCell.addChild("<b>Expression: </b>" + expression);
		}

		@Override
		public void writeBooleanResult(long documentID, ArrayList<Boolean> values) {
			DocumentInfo documentInfo = manager.getDocumentInfo(documentID);
			talbe.addRow("<hr/>");
			HTMLTableCell cell = new HTMLTableCell();
			cell.addChild(new HTMLLink(
					"/show?id=" + documentInfo.getDocumentID() +
					"&query=" + Coder.encodeURL(query),
					documentInfo.getName(), "_blank"));
			cell.addChild("<br/>");
			for (int i = 0; i < values.size(); i++)
				cell.addChild(" [" + i + "]=" + values.get(i));
			talbe.addRow(cell);
		}
	}

	private void booleanSearch(HTMLTable talbe, String query, SearchDataManager manager) {
		talbe.addRow("<b>Type: </b>Boolean search");
		HTMLTableCell expressionCell = talbe.addRow(new HTMLTableCell());
		HTMLTableCell timeCell = talbe.addRow(new HTMLTableCell());

		Calendar start = Calendar.getInstance();
		BooleanSearch.booleanSearch(
				query, manager, new BooleanSearchResultWriter(talbe, expressionCell, query, manager));
		Calendar end = Calendar.getInstance();

		timeCell.addChild("<b>Time used (ms):</b> " + (end.getTime().getTime() - start.getTime().getTime()));
	}
	//</editor-fold>
}
