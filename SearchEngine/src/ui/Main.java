package ui;

import file.FilePostingReader;
import java.io.File;
import java.util.Scanner;
import file.FileSearchDataManager;
import http.HTTPServer;
import searchengine.data.Posting;
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
		System.out.println("  loaddir <max_posting_count>");
		System.out.println("  httpserver <server_dir> <port>");
		System.out.println("  test");
//		System.out.println("  inputfile <input_file>");
//		System.out.println("  cmdline");
	}

	public static void main(String[] args)
	{
//		Scanner testScanner = new Scanner(System.in);
//		switch (testScanner.nextLine())
//		{
//			case "r":
//				args = new String[]
//				{
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\dictionary",
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\part",
//					"loaddir",
//					"1000",
//				};
//				break;
//			case "t":
//				args = new String[]
//				{
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\dictionary",
//					"E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\gov\\part",
//					"test",
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
		File postingFile = new File(dictionaryDirFile, "posting");
		File postingIndexFile = new File(dictionaryDirFile, "posting_index");
		File documentDirFile = new File(args[argIndex++]);
		switch (args[argIndex++])
		{
			case "loaddir":
			{
				if (argIndex + 1 > args.length)
				{
					printUsage();
					return;
				}
				long maxPostingCount = Long.parseLong(args[argIndex++]);
				try (FileSearchDataManager manager = new FileSearchDataManager(documentDirFile,
						documentFile, documentIndexFile, postingFile, postingIndexFile, "rw"))
				{
					manager.loadDocument(documentDirFile, maxPostingCount);
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
						documentFile, documentIndexFile, postingFile, postingIndexFile, "r"))
				{
					HTTPServer server = new HTTPServer(port, new ServletRequestDeliver(serverDirFile, manager));
					server.start();
					Scanner scanner = new Scanner(System.in);
					scanner.nextLine();
					server.stop();
				}
			}
			break;
			case "test":
				try (FileSearchDataManager manager = new FileSearchDataManager(documentDirFile,
						documentFile, documentIndexFile, postingFile, postingIndexFile, "r"))
				{
					Scanner scanner = new Scanner(System.in);
					for (;;)
					{
						String nextLine = scanner.nextLine();
						if (nextLine.isEmpty())
							break;
						FilePostingReader reader = manager.getPostingReader(nextLine.toLowerCase());
						System.out.println("\tcount: " + reader.getCount());
						for (;;)
						{
							Posting posting = reader.read();
							if (posting == null)
								break;
							System.out.println("\t\t" + posting);
							reader.moveNext();
						}
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
