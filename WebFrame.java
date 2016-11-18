/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
    public void start(Stage stage) {
        TableView<URLStrings> tv = new TableView();
        ObservableList<URLStrings> data = FXCollections.observableArrayList(
                new URLStrings("http://www.sjsu.edu/cs/", ""));
        
        
        TableColumn URLCol = new TableColumn("URL");
        URLCol.setMinWidth(100);
        URLCol.setCellValueFactory(
                new PropertyValueFactory<URLStrings, String>("URL"));
        
        Button fetchButt = new Button("Fetch");
        Button stopButt  = new Button("Stop");
        stopButt.setDisable(true);

		WebWorker worker = new WebWorker("http://www.sjsu.edu/cs/");
        fetchButt.setOnAction(new EventHandler(){
			@Override
			public void handle(Event event) {
				fetchButt.setDisable(true);
				stopButt.setDisable(false);
				worker.start();	
			}
        		
        });
        
        stopButt.setOnAction(new EventHandler(){
        	@Override	
        	public void handle(Event event){
        		stopButt.setDisable(true);
        		worker.interrupt();
        		fetchButt.setDisable(false);
        	}
        });
        	
        
        
        TableColumn statusCol = new TableColumn("Status");
        statusCol.setMinWidth(100);
        statusCol.setCellValueFactory(
                new PropertyValueFactory<URLStrings, String>("status"));
        
        tv.getColumns().addAll(URLCol, statusCol);
        tv.setItems(data);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setHeight(500);
        
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        
        HBox hbox = new HBox();
        hbox.setSpacing(5);
        hbox.setPadding(new Insets(10, 0, 0, 10));
        hbox.setSpacing(5);
        hbox.setPadding(new Insets(10, 0, 0, 10));
        
        vbox.getChildren().addAll(tv);
        hbox.getChildren().addAll(fetchButt, stopButt);
        vbox.getChildren().add(hbox);
        ((Group) scene.getRoot()).getChildren().addAll(vbox);
 
        stage.setScene(scene);
        stage.show();
        
      
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    


    public static class URLStrings {
        StringProperty URL;
        StringProperty status;
        URLStrings(String URL, String status) {
            this.URL = new SimpleStringProperty(URL);
            this.status = new SimpleStringProperty(status);
        }
        public String getURL() {
            return URL.get();
        }

        public String getStatus() {
            return status.get();
        }
    }
}