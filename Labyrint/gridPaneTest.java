import java.io.File;
import java.io.FileNotFoundException;
import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class gridPaneTest extends Application{
	private GridPane gp;
	private File selectedFile;
	private Stage vindu;
	private Labyrint l;
	private HBox toppBoks;
	private VBox root;
	private String losning;
	private Button[][] knapper;
	private boolean[][] losningArray;
	
	public static void main(String[] args) {
		launch();
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.vindu = primaryStage;
		toppBoks = lagToppBoks();
		root = new VBox(toppBoks);
		Scene vindu = new Scene(root);
		primaryStage.setScene(vindu);
		primaryStage.show();
		
	}
	
	private GridPane lagGrid(Labyrint l){
		gp = new GridPane();
		gp.setAlignment(Pos.CENTER);
		gp.setHgap(0);
		gp.setVgap(0);
		System.out.println("Antall y rader: " + l.tester.length + "Antall x rader: " +l.tester[0].length );
		knapper = new Button[l.tester.length][l.tester[0].length];
		for(Rute[] a: l.tester){
			for(Rute b: a){
				if(b instanceof HvitRute){
					Button hButton = new Button(b.y+ " . " + b.x);
					hButton.setStyle("-fx-font: 22 arial; -fx-base: #ffffff; -fx-text-fill: white;");
					hButton.setMaxSize(10, 10);
					hButton.setMinSize(10, 10);
					hButton.setPrefSize(10, 10);
					knapper[b.y][b.x] = hButton;
					hButton.setOnAction(new EventHandler<ActionEvent>(){

						@Override
						public void handle(ActionEvent event) {
							losning = "";
							System.out.println("Trykket paa knapp: " + hButton.getText());
							losning = l.kortesteUtveiFraGUI(hButton.getText());
							System.out.println(losning);
							losningArray = losningStringTilTabell(losning, l.tester[0].length, l.tester.length);
							for(int y = 0; y < losningArray.length; y++){
								for(int x = 0; x < losningArray[0].length; x++){
									if(losningArray[y][x] == true){
										System.out.print("O");
										knapper[y][x].setStyle("-fx-base: #faff00; -fx-text-fill: yellow");
									} else{
										System.out.print("#");
									}
								}
								System.out.print("\n");
							}
						}
						
					});
					gp.add(hButton, b.x, b.y);
				} else{
					Button sButton = new Button(b.y+ " . " + b.x);
					sButton.setStyle("-fx-font: 22 arial; -fx-base: #000000; -fx-text-fill: black;");
					sButton.setMaxSize(10, 10);
					sButton.setMinSize(10, 10);
					sButton.setPrefSize(10, 10);
					gp.add(sButton, b.x, b.y);
					knapper[b.y][b.x] = sButton;
				}
			}
		}
		return gp;
	}
	
	private HBox lagToppBoks(){
		TextField filFelt = new TextField();
		Button velgFilKnapp = new Button("Velg fil. . .");
		velgFilKnapp.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event){
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Velg fil");
				selectedFile = fileChooser.showOpenDialog(vindu);
				if(selectedFile != null){
					filFelt.setText(selectedFile.getPath());
				} else{
					while(selectedFile == null){
						selectedFile = fileChooser.showOpenDialog(vindu);
					}
				}
			}
		});
		
		Button lastInnKnapp = new Button("Last inn");
		lastInnKnapp.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				try {
					l = Labyrint.lesFraFil(selectedFile);
					System.out.println("Leser fra fil nå");
					gp = lagGrid(l);
					System.out.println("lagde ny gp");
					VBox kk = new VBox(toppBoks, gp);
					Scene vindu2 = new Scene(kk);
					System.out.println();
					vindu.hide();
					vindu.setScene(vindu2);
					vindu.show();
					
				} catch (FileNotFoundException e) {e.printStackTrace();}
			}
			
		});
		return new HBox(50 , velgFilKnapp, filFelt, lastInnKnapp);
	}
	
	/**
	 * Konverterer losning-String fra oblig 5 til en boolean[][]-representasjon
	 * av losningstien.
	 * @param losningString String-representasjon av utveien
	 * @param bredde        bredde til labyrinten
	 * @param hoyde         hoyde til labyrinten
	 * @return              2D-representasjon av rutene der true indikerer at
	 *                      ruten er en del av utveien.
	 */
	private boolean[][] losningStringTilTabell(String losningString, int bredde, int hoyde) {
	    boolean[][] losningBoolean = new boolean[hoyde][bredde];
	    java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\(([0-9]+),([0-9]+)\\)");
	    java.util.regex.Matcher m = p.matcher(losningString.replaceAll("\\s",""));
	    while(m.find()) {
	        int x = Integer.parseInt(m.group(1))-1;
	        int y = Integer.parseInt(m.group(2))-1;
	        losningBoolean[y][x] = true;
	    }
	    return losningBoolean;
	}

}
