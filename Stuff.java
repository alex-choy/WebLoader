import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class Stuff {
	public Button addButton(String desc) {
		Button button = new Button(desc);
		button.setOnAction(new EventHandler() {
			@Override
			public void handle(Event arg0) {
				Thread thread = new Thread(new WebWorker(

				));

			}

		});

		return button;
	}
}
