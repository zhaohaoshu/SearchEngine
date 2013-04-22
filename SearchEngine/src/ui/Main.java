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
		System.out.println("Usage: searchengine <dictionary_dir> <action>");
		System.out.println("<action> can be one of the following:");
		System.out.println("  inputdir <input_dir> ");
		System.out.println("  httpserver <server_dir> <port>");
//		System.out.println("  inputfile <input_file>");
//		System.out.println("  cmdline");
	}

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			printUsage();
			return;
		}
		//File dictionaryDirFile = new File("E:\\File\\School\\p\\2\\网络信息体系结构\\dictionary");
		File dictionaryDirFile = new File(args[0]);
		File documentFile = new File(dictionaryDirFile, "document");
		File documentIndexFile = new File(dictionaryDirFile, "document_index");
		File termFile = new File(dictionaryDirFile, "term");
		File termIndexFile = new File(dictionaryDirFile, "term_index");
		switch (args[1])
		{
			case "inputdir":
			{
				if (args.length < 3)
				{
					printUsage();
					return;
				}
				//File inputDirFile = new File("E:\\File\\School\\p\\2\\网络信息体系结构\\resource\\input");
				File inputDirFile = new File(args[2]);
				try (FileSearchDataManager manager = new FileSearchDataManager(
						documentFile, documentIndexFile, termFile, termIndexFile, "rw"))
				{
					for (File file : inputDirFile.listFiles())
					{
						System.out.println("reading: " + file);
						manager.addDocument(file);
					}
					long documentCount = manager.getDocumentCount();
					for (int i = 1; i <= documentCount; i++)
						System.out.println(manager.getDocumentInfo(i));
				}
			}
			break;
			case "httpserver":
			{
				if (args.length < 4)
				{
					printUsage();
					return;
				}
				//File serverDirFile = new File("E:/File/Program/SearchEngine/");
				File serverDirFile = new File(args[2]);
				int port = Integer.parseInt(args[3]);
				try (FileSearchDataManager manager = new FileSearchDataManager(
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
