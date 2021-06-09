package log_manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogFileManager {

	static File logFile;
	static FileOutputStream output;
	static PrintStream outputStream;

	static final String FILE_PATH = "journal.log";
	static final DateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");

	public static void start() {
		try {
			logFile = new File(FILE_PATH);
			logFile.createNewFile(); // Create if not exists
			output = new FileOutputStream(logFile);
			outputStream = new PrintStream(output);
		} catch (IOException e) {
		}
	}
	
	public static void addError(String message) {
		//Displaying an arrow to highlight the issue in log file
		outputStream.print("-> " +getCurrentDate() + " " + message + "!\n");
	}
	
	public static void addLog(String message) {
		addLog(message, "\n");
	}
	
	public static void addLog(String message, String endChar) {
		outputStream.print(getCurrentDate() + " " + message + endChar);
	}

	static String getCurrentDate() {
		Date date = Calendar.getInstance().getTime();
		return DATE_FORMAT.format(date);
	}

	public static void close() {
		try {
			outputStream.close();
			output.close();
		} catch (IOException e) {
		}
	}
}
