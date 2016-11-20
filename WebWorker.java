import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class WebWorker extends Thread {
	/*
	 * This is the core web/download i/o code...
	 */
	ObservableList<WebFrame.URLStrings> data;
	String urlString;
	int rowNumber;
	boolean interrupted;
	ProgressBar bar;
	double incrementBar;
	TableView tv;
	ProgressIndicator indicator;

	public WebWorker(ObservableList<WebFrame.URLStrings> data, String url, int rowNumber, double incrementBar,
			ProgressBar bar, ProgressIndicator indicator, TableView tv) {
		this.data = data;
		urlString = url;
		this.rowNumber = rowNumber;
		interrupted = false;
		this.bar = bar;
		this.incrementBar = incrementBar;
		this.tv = tv;
		this.indicator = indicator;

	}

	public synchronized void run() {
		System.out.println("Fetching...." + urlString);
		InputStream input = null;
		StringBuilder contents = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);

			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				System.out.println("Fetching...." + urlString + len);
				contents.append(array, 0, len);
				Thread.sleep(100);
			}

			System.out.print(contents.toString());

		}
		// Otherwise control jumps to a catch...
		catch (MalformedURLException ignored) {
			System.out.println("Exception: " + ignored.toString());
			interrupted = true;
		} catch (InterruptedException exception) {
			// YOUR CODE HERE
			// deal with interruption
			System.out.println("The program has been interrupted");
			interrupted = true;
		} catch (IOException ignored) {
			System.out.println("Exception: " + ignored.toString());
			interrupted = true;
		} catch (Exception e) {
			System.out.println("Exception: " + e.toString());
			interrupted = true;
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}
		}
		if (!interrupted) {
			bar.setProgress(bar.getProgress() + incrementBar);
			if (bar.getProgress() > .999)
				bar.setProgress(1);

			indicator.setProgress(bar.getProgress());
			data.get(rowNumber).setStatus("Complete");
			tv.refresh();
		} else {
			bar.setProgress(bar.getProgress() + incrementBar);
			if (bar.getProgress() > .9999999)
				bar.setProgress(1);
			indicator.setProgress(bar.getProgress());
			data.get(rowNumber).setStatus("Interrupted");
			tv.refresh();
		}

	}

}
