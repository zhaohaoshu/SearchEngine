package ui.page;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;
import http.Coder;
import searchengine.TypeTokenizer;
import html.builder.HTMLLink;
import http.HTTPRequest;
import java.io.StringReader;
import searchengine.data.DocumentInfo;

/**
 *
 * @author ZHS
 */
public class ShowDocumentPage extends MainFrame
{

	private TreeSet<Integer> getPositions(String pos)
	{
		TreeSet<Integer> positions = new TreeSet<>();
		if (pos != null)
		{
			TypeTokenizer tokenizer = new TypeTokenizer(new StringReader(pos));
			tokenizer.addType("0123456789");
			for (;;)
			{
				String token = tokenizer.getNext(2);
				if (token == null)
					break;
				positions.add(Integer.parseInt(token));
			}
		}
		return positions;
	}

	private TreeSet<String> getKeyWords(String query)
	{
		TreeSet<String> keyWords = new TreeSet<>();
		if (query != null)
		{
			TypeTokenizer tokenizer = new TypeTokenizer(new BufferedReader(new StringReader(query)));
			for (String string : tokenizer.getStrings(1))
				keyWords.add(string.toLowerCase());
		}
		return keyWords;
	}

	public ShowDocumentPage(DocumentInfo info, InputStream inputStream,
			String pos, String query)
	{
		super("Document - " + info.getName());
		TreeSet<Integer> positions = getPositions(pos);
		TreeSet<String> keyWords = getKeyWords(query);
		getContent().addChild("<p><b>Document - " + info.getName() + "</b><br/>[");
		getContent().addChild(new HTMLLink("/show?id=" + info.getDocumentID() + "&download=download", "download"));
		getContent().addChild("]</p>");
		boolean flag = false;
		try (TypeTokenizer tokenizer = new TypeTokenizer(new BufferedReader(new InputStreamReader(inputStream))))
		{
			int position = 0;
			StringBuilder builder = new StringBuilder();
			for (;;)
			{
				String string = tokenizer.getNext();
				if (string == null)
					break;
				if (tokenizer.getStringType() == 1)
				{
					if (positions.contains(position) || keyWords.contains(string.toLowerCase()))
					{
						if (!flag)
						{
							builder.append("<span id=\"anchor\"/>");
							flag = true;
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
