package ui.page;

import html.HTMLElement;
import html.builder.HTMLAnchor;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;
import http.Coder;
import searchengine.TypeTokenizer;
import html.builder.HTMLLink;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import searchengine.data.DocumentInfo;

/**
 *
 * @author ZHS
 */
public class ShowDocumentPage extends MainFrame {

	private TreeSet<Integer> getPositions(String pos) {
		TreeSet<Integer> positions = new TreeSet<>();
		if (pos != null) {
			TypeTokenizer tokenizer = new TypeTokenizer(pos);
			tokenizer.addType("0123456789");
			for (;;) {
				String token = tokenizer.getNext(2);
				if (token == null)
					break;
				positions.add(Integer.parseInt(token));
			}
		}
		return positions;
	}

	private TreeSet<String> getKeyWords(String query) {
		TreeSet<String> keyWords = new TreeSet<>();
		if (query != null) {
			TypeTokenizer tokenizer = new TypeTokenizer(query);
			for (String string : tokenizer.getStrings(1))
				keyWords.add(string.toLowerCase());
		}
		return keyWords;
	}

	public ShowDocumentPage(DocumentInfo info, InputStream inputStream,
			String pos, String query) {
		super("Document - " + info.getName());
		TreeSet<Integer> positions = getPositions(pos);
		TreeSet<String> keyWords = getKeyWords(query);
		getContent().addChild("<p><b>Document - " + info.getName() + "</b><br/>[");
		getContent().addChild(new HTMLLink(info.getUrl(), info.getUrl(), "_blank"));
		getContent().addChild("]<br/>[");
		getContent().addChild(new HTMLLink("/show?id=" + info.getDocumentID() + "&download=download", "download"));
		getContent().addChild("]</p>");
		HTMLElement keyWordsDiv = getContent().addChild(new HTMLElement("div"));
		getContent().addChild("<hr/>");
		int anchorQID = 0;
		int anchorPID = 0;
		try (TypeTokenizer tokenizer = new TypeTokenizer(inputStream)) {
			int position = 0;
			StringBuilder builder = new StringBuilder();
			for (;;) {
				String string = tokenizer.getNext();
				if (string == null)
					break;
				if (tokenizer.getStringType() == 1) {
					if (positions.contains(position) || keyWords.contains(string.toLowerCase())) {
						if (positions.contains(position))
							builder.append(new HTMLAnchor("anchorp" + (anchorPID++)));
						if (keyWords.contains(string.toLowerCase())) {
							builder.append(new HTMLAnchor("anchorq" + anchorQID));
							keyWordsDiv.addChild(new HTMLLink("#anchorq" + anchorQID, string));
							keyWordsDiv.addChild(" ");
							anchorQID++;
						}
						builder.append("<b style=\"background-color:yellow\">").append(string).append("</b>");
					}
					else
						builder.append(string);
					position++;
				}
				else
					builder.append(Coder.escapeHTML(string));
			}
			getContent().addChild(builder.toString());
		}
	}
}
