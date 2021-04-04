package log_manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogFileManager {

	File logFile;
	FileWriter fileWritter;
	BufferedWriter bufferWritter;

	final String FILE_PATH = "journal.log";
	final DateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss dd/mm/yyyy");

	public LogFileManager() {
		try {
			logFile = new File(FILE_PATH);
			logFile.createNewFile(); // Create if not exists
		} catch (IOException e) {
		}
	}

	public void addLog(String message) {
		try {
			bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(getCurrentDate() + " " + message);
			bufferWritter.newLine();
			bufferWritter.close();
		} catch (IOException e) {
		}
	}

	String getCurrentDate() {
		Date date = Calendar.getInstance().getTime();
		return DATE_FORMAT.format(date);
	}

	public void close() {
		try {
			fileWritter.close();
		} catch (IOException e) {
		}
	}
}
