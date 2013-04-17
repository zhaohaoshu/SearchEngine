package searchengine;

import java.util.Scanner;
import java.util.TreeSet;
import searchengine.search.BooleanSearch;
import searchengine.search.PositionalSearch;

/**
 *
 * @author ZHS
 */
public class TestMain
{

	public static void main(String[] args)
	{
		//PositionalSearch.positionalSearch("this is", null, null);
		BooleanSearch.booleanSearch("a !b ", null, null);
	}
}
