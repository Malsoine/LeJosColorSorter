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

	File logFile;
	FileOutputStream output;
	PrintStream outputStream;

	final String FILE_PATH = "journal.log";
	final DateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");

	public LogFileManager() {
		try {
			logFile = new File(FILE_PATH);
			logFile.createNewFile(); // Create if not exists
			output = new FileOutputStream(logFile);
			outputStream = new PrintStream(output);
		} catch (IOException e) {
		}
	}
	
	public void addLog(String message) {
		addLog(message, "\n");
	}
	
	public void addLog(String message, String endChar) {
		outputStream.print(getCurrentDate() + " " + message + endChar);
	}

	String getCurrentDate() {
		Date date = Calendar.getInstance().getTime();
		return DATE_FORMAT.format(date);
	}

	public void close() {
		try {
			outputStream.close();
			output.close();
		} catch (IOException e) {
		}
	}
}
