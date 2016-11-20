
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
import java.util.concurrent.CountDownLatch;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
		int runningThreadsCount = 0;
		
		Button fetchButt = new Button("Fetch");
		Button multiButt = new Button("Multiple Fetch");
		Button stopButt = new Button("Stop");
		stopButt.setDisable(true);
		
		List<Thread> theWorkers = new ArrayList<Thread>();

		TableColumn URLCol = new TableColumn("URL");
		URLCol.setMinWidth(100);
		URLCol.setCellValueFactory(new PropertyValueFactory<URLStrings, String>("URL"));

		

		ProgressBar bar = new ProgressBar(0);

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
				runningState(fetchButt, multiButt, stopButt);
				
				Semaphore sem1 = new Semaphore(1);
				CountDownLatch count = new CountDownLatch(numOfURLs);

				try {
					startWorkers(tv, data, theWorkers, sem1, addToProgress, bar, count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});

		multiButt.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				runningState(fetchButt, multiButt, stopButt);
				Semaphore sem = new Semaphore(4);
				CountDownLatch count = new CountDownLatch(numOfURLs);
				try {
					startWorkers(tv, data, theWorkers, sem, addToProgress, bar, count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});

		stopButt.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				for(Thread worker: theWorkers){
					
					worker.interrupt(); 	
				}
				readyState(fetchButt, multiButt, stopButt, bar);
				
			}

		});

		Scene scene = new Scene(new Group());
		stage.setTitle("Table View Sample");
		stage.setHeight(500);
		stage.setWidth(335);

		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));

		HBox hbox = new HBox();
		hbox.setSpacing(5);
		hbox.setPadding(new Insets(10, 0, 0, 10));

		hbox.getChildren().addAll(fetchButt, multiButt, stopButt, bar);
		vbox.getChildren().addAll(tv, hbox);
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

		public void setStatus(String status) {
			this.status = new SimpleStringProperty(status);
		}

	}

	private static void startWorkers(TableView tv, ObservableList<URLStrings> data, List<Thread> theWorkers, Semaphore sem1,
			double addToProgress, ProgressBar bar, CountDownLatch count) throws InterruptedException {
		
		for (int i = 0; i < data.size(); i++) {
			
			URLStrings url = data.get(i);
			WebWorker worker = new WebWorker(data, url.getURL(), i, addToProgress, bar, tv,count);
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
	}
	private static void runningState(Button fetchButt, Button multiButt, Button stopButt){
		fetchButt.setDisable(true);
		multiButt.setDisable(true);
		stopButt.setDisable(false);
	}
	
	private static void readyState(Button fetchButt, Button multiButt, Button stopButt, ProgressBar bar){
		fetchButt.setDisable(false);
		multiButt.setDisable(false);
		stopButt.setDisable(true);
		bar.setProgress(0);
		
		
	}
}