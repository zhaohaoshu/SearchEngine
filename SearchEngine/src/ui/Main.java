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
		System.out.println("  loaddir <max_posting_count> <max_position_count>");
		System.out.println("  httpserver <server_dir> <port>");
//		System.out.println("  inputfile <input_file>");
//		System.out.println("  cmdline");
	}

	public static void main(String[] args)
	{
//		switch (new Scanner(System.in).nextLine())
//		{
//			case "r":
//				args = new String[]
//				{
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\dictionary",
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\part",
//					"loaddir",
//					"-1",
//					"1000",
//				};
//				break;
//			default:
//				args = new String[]
//				{
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\dictionary",
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\part",
//					"httpserver",
//					"E:\\File\\Program\\SearchEngine\\httpserver",
//					"8888"
//				};
//				break;
//		}
		int argIndex = 0;
		if (argIndex + 2 > args.length)
		{
			printUsage();
			return;
		}
		File dictionaryDirFile = new File(args[argIndex++]);
		File documentFile = new File(dictionaryDirFile, "document");
		File documentIndexFile = new File(dictionaryDirFile, "document_index");
		File termFile = new File(dictionaryDirFile, "term");
		File termIndexFile = new File(dictionaryDirFile, "term_index");
		File positionFile = new File(dictionaryDirFile, "position");
		File documentDirFile = new File(args[argIndex++]);
		switch (args[argIndex++])
		{
			case "loaddir":
			{
				if (argIndex + 2 > args.length)
				{
					printUsage();
					return;
				}
				long maxPostingCount = Long.parseLong(args[argIndex++]);
				long maxPositionCount = Long.parseLong(args[argIndex++]);
				try (FileSearchDataManager manager = new FileSearchDataManager(documentDirFile,
						documentFile, documentIndexFile, termFile, termIndexFile, positionFile, "rw"))
				{
					manager.loadDocument(documentDirFile, maxPostingCount, maxPositionCount);
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
				if (argIndex + 2 > args.length)
				{
					printUsage();
					return;
				}
				File serverDirFile = new File(args[argIndex++]);
				int port = Integer.parseInt(args[argIndex++]);
				try (FileSearchDataManager manager = new FileSearchDataManager(documentDirFile,
						documentFile, documentIndexFile, termFile, termIndexFile, positionFile, "r"))
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
