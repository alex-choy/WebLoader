
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author narayan
 */
public class WebFrame extends Application {

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage) throws FileNotFoundException {
		ObservableList<URLStrings> data = FXCollections.observableArrayList();
		TableView<URLStrings> tv = new TableView<URLStrings>();
		double count = 0;

		Button fetchButt = new Button("Fetch");
		Button multiButt = new Button("Multiple Fetch");
		Button resetButt = new Button("Reset");
		Button stopButt = new Button("Stop");
		stopButt.setDisable(true);
		resetButt.setDisable(true);

		Label label = new Label("Limit to # of Threads:");
		TextField text = new TextField();

		text.setMaxWidth(50);

		List<Thread> theWorkers = new ArrayList<Thread>();

		TableColumn URLCol = new TableColumn("URL");
		URLCol.setMinWidth(100);
		URLCol.setCellValueFactory(new PropertyValueFactory<URLStrings, String>("URL"));

		ProgressBar bar = new ProgressBar(0);
		ProgressIndicator indicator = new ProgressIndicator(bar.getProgress());

		Scanner in = new Scanner(new File("URL.txt"));

		while (in.hasNextLine()) {
			String url = in.nextLine();
			String status = "";

			URLStrings string = new URLStrings(url, status);
			data.add(string);
			count++; // Increment count so we can update the status bar
		}
		double addToProgress = 1 / count;
		final int numOfURLs = (int) count;
		in.close();

		TableColumn statusCol = new TableColumn("Status");
		statusCol.setMinWidth(100);
		statusCol.setCellValueFactory(new PropertyValueFactory<URLStrings, String>("status"));

		tv.getColumns().addAll(URLCol, statusCol);
		tv.setItems(data);
		tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		fetchButt.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				runningState(fetchButt, multiButt, stopButt, resetButt, text);

				Semaphore sem1 = new Semaphore(1);

				try {
					startWorkers(tv, data, theWorkers, sem1, addToProgress, bar, indicator);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				/*
				 * int countCompleted = 0; while(countCompleted < data.size()){
				 * //System.out.
				 * println("NOT DONE YET YA DUMB BITCH HURRY THE FUCK UP YOU STUPID CUNT"
				 * ); int temp = 0; for(URLStrings url: data){
				 * if(url.getStatus().equals("Complete")) temp++; }
				 * countCompleted = temp; } //resetButt.setDisable(false);
				 */
			}

		});

		multiButt.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				boolean correct = true;
				Semaphore sem = new Semaphore(0);

				
				String newText = text.getText().replaceAll("[^\\d\\-]", "");
				String oldText = text.getText();
				
				if(newText.equals("") || oldText.length() != newText.length() || newText.equals("0")){
					System.out.println("Please enter a positive, non-zero number");
					text.setText("");
				}
				else if (Integer.parseInt(newText) >= 1) {
					runningState(fetchButt, multiButt, stopButt, resetButt, text);
					try {
						sem = new Semaphore(Integer.parseInt(newText));
					} catch (Exception e) {
						System.out.println(e);
						correct = false;
					}
					try {
						if (correct)
							startWorkers(tv, data, theWorkers, sem, addToProgress, bar, indicator);
						else
							resetState(fetchButt, multiButt, stopButt, resetButt, text);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Please enter a valid input");
				}
			}

		});

		stopButt.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				for (Thread worker : theWorkers)
					worker.interrupt();

				interruptedState(fetchButt, multiButt, stopButt, resetButt);

			}

		});

		resetButt.setOnAction(new EventHandler() {

			@Override
			public void handle(Event event) {

				for (Thread worker : theWorkers) {

					worker.interrupt();
					try {
						worker.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				for (URLStrings url : data) {
					url.setStatus("");
				}
				text.setText("");
				tv.refresh();
				readyState(fetchButt, multiButt, stopButt, resetButt, bar, indicator, text);

			}

		});

		Scene scene = new Scene(new Group());
		stage.setTitle("Table View Sample");
		stage.setHeight(545);
		stage.setWidth(360);

		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));

		HBox hbox = new HBox();
		hbox.setSpacing(5);
		hbox.setPadding(new Insets(10, 0, 0, 10));

		HBox hbox2 = new HBox();
		hbox2.setSpacing(5);
		hbox2.setPadding(new Insets(10, 0, 0, 10));

		hbox.getChildren().addAll(fetchButt, label, text, multiButt);
		hbox2.getChildren().addAll(stopButt, resetButt, indicator, bar);
		vbox.getChildren().addAll(tv, hbox, hbox2);
		((Group) scene.getRoot()).getChildren().addAll(vbox);

		stage.setScene(scene);
		stage.show();

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	public static class URLStrings {
		private StringProperty URL;
		private StringProperty status;

		URLStrings(String URL, String status) {
			this.URL = new SimpleStringProperty(URL);
			this.status = new SimpleStringProperty(status);
			// this.runningThreadsCount = runningThreadsCount;
		}

		public String getURL() {
			return URL.get();
		}

		public String getStatus() {
			return status.get();
		}

		public synchronized void setStatus(String status) {
			this.status = new SimpleStringProperty(status);
		}

	}

	private static void startWorkers(TableView tv, ObservableList<URLStrings> data, List<Thread> theWorkers,
			Semaphore sem1, double addToProgress, ProgressBar bar, ProgressIndicator indicator)
			throws InterruptedException {
		long start = System.currentTimeMillis();
		for (int i = 0; i < data.size(); i++) {

			URLStrings url = data.get(i);
			WebWorker worker = new WebWorker(data, url.getURL(), i, addToProgress, bar, indicator, tv);
			url.setStatus("Running");
			tv.refresh();
			Thread t1 = new Thread(worker);
			theWorkers.add(t1);
			try {
				sem1.acquire();
				t1.start();

				sem1.release();
			} catch (Exception e) {

			}
		}
		long stop = System.currentTimeMillis();

		System.out.println("\n\n\n\nTime to run: " + (stop - start) + "ms");
	}

	private static void runningState(Button fetchButt, Button multiButt, Button stopButt, Button resetButt, TextField text) {
		fetchButt.setDisable(true);
		multiButt.setDisable(true);
		stopButt.setDisable(false);
		resetButt.setDisable(false);
		text.setDisable(true);
	}

	private static void interruptedState(Button fetchButt, Button multiButt, Button stopButt, Button resetButt) {
		fetchButt.setDisable(true);
		multiButt.setDisable(true);
		stopButt.setDisable(true);
		resetButt.setDisable(false);
	}

	private static void resetState(Button fetchButt, Button multiButt, Button stopButt, Button resetButt, TextField text) {
		fetchButt.setDisable(false);
		multiButt.setDisable(false);
		stopButt.setDisable(true);
		resetButt.setDisable(true);
		text.setDisable(false);
	}

	private static void readyState(Button fetchButt, Button multiButt, Button stopButt, Button resetButt,
			ProgressBar bar, ProgressIndicator indicator, TextField text) {
		fetchButt.setDisable(false);
		multiButt.setDisable(false);
		stopButt.setDisable(true);
		resetButt.setDisable(true);
		text.setDisable(false);
		bar.setProgress(0);
		indicator.setProgress(bar.getProgress());

	}
}