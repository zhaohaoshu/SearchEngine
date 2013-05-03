package ui;

import file.FilePostingReader;
import java.io.File;
import java.util.Scanner;
import file.FileSearchDataManager;
import http.HTTPServer;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import searchengine.data.Posting;
import ui.servlet.ServletRequestDeliver;

/**
 * This is for test
 *
 * @author ZHS
 */
public class Main {

	private static File dictionaryDirFile;
	private static File documentDirFile;

	private static void printUsage() {
		System.out.println("Usage: searchengine <dictionary_dir> <document_dir> <action>");
		System.out.println("<action> can be one of the following:");
		System.out.println("  loaddir <max_posting_count>");
		System.out.println("  httpserver <server_dir> <port>");
		System.out.println("  cmdline");
//		System.out.println("  inputfile <input_file>");
	}

	private static void excute(LinkedList<String> argList) {
		switch (argList.poll()) {
			case "l":
			case "loaddir": {
				if (argList.size() < 1) {
					printUsage();
					return;
				}
				int maxPostingCount = Integer.parseInt(argList.poll());
				try (FileSearchDataManager manager = new FileSearchDataManager(
						documentDirFile, dictionaryDirFile, "rw")) {
					manager.loadDocuments(documentDirFile, maxPostingCount);
					long documentCount = manager.getDocumentCount();
					System.out.println("Loaded " + documentCount + " documents");
					Scanner scanner = new Scanner(System.in);
					scanner.nextLine();
				}
				catch (IOException ex) {
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			break;
			case "s":
			case "httpserver": {
				if (argList.size() < 2) {
					printUsage();
					return;
				}
				File serverDirFile = new File(argList.poll());
				int port = Integer.parseInt(argList.poll());
				try (FileSearchDataManager manager = new FileSearchDataManager(
						documentDirFile, dictionaryDirFile, "r")) {
					HTTPServer server = new HTTPServer(port, new ServletRequestDeliver(serverDirFile, manager));
					server.start();
					Scanner scanner = new Scanner(System.in);
					scanner.nextLine();
					server.stop();
				}
			}
			break;
			case "p":
			case "post":
			case "posting": {
				try (FileSearchDataManager manager = new FileSearchDataManager(
						documentDirFile, dictionaryDirFile, "r")) {
					Scanner scanner = new Scanner(System.in);
					for (;;) {
						System.out.print("posting>");
						String nextLine = scanner.nextLine();
						if (nextLine.isEmpty())
							break;
						FilePostingReader reader = manager.getPostingReader(nextLine.toLowerCase());
						System.out.println("\tcount: " + reader.getCount());
						for (;;) {
							Posting posting = reader.read();
							if (posting == null)
								break;
							System.out.println("\t\t" + posting);
							reader.moveNext();
						}
						System.out.println("\tcount: " + reader.getCount());
					}
				}
			}
			break;
			case "d":
			case "doc":
			case "document": {
				try (FileSearchDataManager manager = new FileSearchDataManager(
						documentDirFile, dictionaryDirFile, "r")) {
					Scanner scanner = new Scanner(System.in);
					for (;;) {
						System.out.print("doc>");
						String nextLine = scanner.nextLine();
						if (nextLine.isEmpty())
							break;
						System.out.println(manager.getDocumentInfo(Long.parseLong(nextLine)));
					}
				}
			}
			break;
			case "cmd":
			case "cmdline": {
				Scanner scanner = new Scanner(System.in);
				for (;;) {
					System.out.print("cmdline>");
					String nextLine = scanner.nextLine();
					if (nextLine.isEmpty())
						break;
					LinkedList<String> cmdList = new LinkedList<>();
					StringTokenizer tokenizer = new StringTokenizer(nextLine);
					while (tokenizer.hasMoreTokens())
						cmdList.add(tokenizer.nextToken());
					excute(cmdList);
				}
			}
			break;
			case "clr":
			case "clear": {
				for (File file : dictionaryDirFile.listFiles())
					file.delete();
				System.out.println("Dictionary cleared");
			}
			break;
			case "c": {
				testCode();
			}
			break;
			default:
				printUsage();
		}
	}

	private static void testCode() {
	}

	public static void main(String[] args) {
		LinkedList<String> argList = new LinkedList<>(Arrays.asList(args));
		if (argList.size() < 3) {
			printUsage();
			return;
		}
		dictionaryDirFile = new File(argList.poll());
		if (!dictionaryDirFile.exists())
			dictionaryDirFile.mkdir();
		documentDirFile = new File(argList.poll());
		excute(argList);
	}
}
