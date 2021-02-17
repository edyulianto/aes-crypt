/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.quinos.pos;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author ediy6
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AES decrypt/encrypt");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        
        Text scenetitle = new Text("AES decrypt/encrypt");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label saltLabel = new Label("Salt:");
        grid.add(saltLabel, 0, 1);

        TextField saltTextField = new TextField();
        grid.add(saltTextField, 1, 1);

        Label textCrypt = new Label("Text to crypt:");
        grid.add(textCrypt, 0, 2);

        TextArea textCryptTextField = new TextArea();
        textCryptTextField.setWrapText(true);
        grid.add(textCryptTextField, 1, 2);
        
        
        Button btnEncrypt = new Button("Encrypt");
        HBox hbBtnEncrypt = new HBox(10);
        hbBtnEncrypt.setAlignment(Pos.BOTTOM_CENTER);
        hbBtnEncrypt.getChildren().add(btnEncrypt);
        grid.add(hbBtnEncrypt, 1, 4);
        
        Button btnDencrypt = new Button("Dencrypt");
        HBox hbBtnDencrypt = new HBox(10);
        hbBtnDencrypt.setAlignment(Pos.BOTTOM_CENTER);
        hbBtnDencrypt.getChildren().add(btnDencrypt);
        grid.add(btnDencrypt, 1, 4);
        
        Label result = new Label("Result :");
        grid.add(result, 0, 5);

        TextArea resultCrypt = new TextArea();
        resultCrypt.setWrapText(true);
        grid.add(resultCrypt, 1, 5);

        primaryStage.show();
        
        btnEncrypt.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {                                
                try {
                    resultCrypt.setText("");
                    System.out.println("Salt "+textCryptTextField.getText());
                    System.out.println("Text "+saltTextField.getText());
                    String passwordEnc = Cryptography.encrypt(textCryptTextField.getText(), saltTextField.getText());                    
                    resultCrypt.setText(passwordEnc);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    Alert a = new Alert(AlertType.ERROR); 
                    a.setContentText("Encrypt process failed :"+ex.getMessage());
                    a.show(); 
                }
            }
        });
        
        btnDencrypt.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {                                
                try {
                    resultCrypt.setText("");
                    System.out.println("Salt "+textCryptTextField.getText());
                    System.out.println("Text "+saltTextField.getText());
                    String passwordDec = Cryptography.decrypt(textCryptTextField.getText(), saltTextField.getText());                    
                    System.out.println("passwordDec "+passwordDec);
                    resultCrypt.setText(passwordDec);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    Alert a = new Alert(AlertType.ERROR); 
                    a.setContentText("decrypt process failed :"+ex.getMessage());
                    a.show();
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
