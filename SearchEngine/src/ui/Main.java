package ui;

import java.io.File;
import java.util.Scanner;
import file.FileSearchDataManager;
import http.HTTPServer;
import ui.servlet.ServletRequestDeliver;

/**
 * This is for test
 *
 * @author ZHS
 */
public class Main
{

	private static void printUsage()
	{
		System.out.println("Usage: searchengine <dictionary_dir> <document_dir> <action>");
		System.out.println("<action> can be one of the following:");
		System.out.println("  loaddir");
		System.out.println("  httpserver <server_dir> <port>");
//		System.out.println("  inputfile <input_file>");
//		System.out.println("  cmdline");
	}

	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			printUsage();
			return;
		}
		File dictionaryDirFile = new File(args[0]);
		File documentFile = new File(dictionaryDirFile, "document");
		File documentIndexFile = new File(dictionaryDirFile, "document_index");
		File termFile = new File(dictionaryDirFile, "term");
		File termIndexFile = new File(dictionaryDirFile, "term_index");
		File documentDirFile = new File(args[1]);
		switch (args[2])
		{
			case "loaddir":
			{
				try (FileSearchDataManager manager = new FileSearchDataManager(documentDirFile,
						documentFile, documentIndexFile, termFile, termIndexFile, "rw"))
				{
					manager.loadDocument(documentDirFile);
					long documentCount = manager.getDocumentCount();
					System.out.println("Loaded " + documentCount + " documents");
					Scanner scanner = new Scanner(System.in);
					scanner.nextLine();
//					for (int i = 1; i <= documentCount; i++)
//						System.out.println(manager.getDocumentInfo(i));
				}
			}
			break;
			case "httpserver":
			{
				if (args.length < 5)
				{
					printUsage();
					return;
				}
				File serverDirFile = new File(args[3]);
				int port = Integer.parseInt(args[4]);
				try (FileSearchDataManager manager = new FileSearchDataManager(documentDirFile,
						documentFile, documentIndexFile, termFile, termIndexFile, "r"))
				{
					HTTPServer server = new HTTPServer(port, new ServletRequestDeliver(serverDirFile, manager));
					server.start();
					Scanner scanner = new Scanner(System.in);
					scanner.nextLine();
					server.stop();
				}
			}
			break;
//			case "inputfile":
//			{
//				if (args.length < 3)
//				{
//					printUsage();
//					return;
//				}
//				File inputFile = new File(args[2]);
//			}
//			break;
//			case "cmdline":
//			{
//			}
//			break;
			default:
				printUsage();
		}
	}
}
