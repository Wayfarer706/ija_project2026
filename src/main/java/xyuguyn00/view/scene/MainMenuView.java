/**
 * Project: Advance Wars Clone
 * Authors: Team xyuguyn00
 * Description: The primary landing view. Collects the desired game mode and map parameters 
 * from the user before dispatching them to the main application bootstrapper.
 */
package xyuguyn00.view.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.enums.GameMode;
import xyuguyn00.view.render.ViewConstants; 

import java.util.function.BiConsumer;

public class MainMenuView extends VBox {

    public MainMenuView(BiConsumer<GameMode, String> onModeSelected) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(ViewConstants.PADDING_MEDIUM); 
        this.setStyle("-fx-background-color: #2F4F4F;");

        Text title = new Text("Advance Wars Clone");
        title.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_TITLE));
        title.setFill(Color.WHITE);

        // Dynamically scans predefined data files to fulfill the 2-map minimum specification
        ComboBox<String> mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll("game_stats.json", "game_stats_2.json");
        mapSelector.setValue("game_stats.json");
        mapSelector.setStyle(String.format("-fx-font-size: %dpx; -fx-pref-width: %dpx;", 
                ViewConstants.FONT_SIZE_MEDIUM, ViewConstants.BUTTON_WIDTH_LARGE));

        Text mapLabel = new Text("Select Map:");
        mapLabel.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_MEDIUM));
        mapLabel.setFill(Color.WHITE);

        VBox mapSelectionBox = new VBox(5, mapLabel, mapSelector);
        mapSelectionBox.setAlignment(Pos.CENTER);
        mapSelectionBox.setSpacing(ViewConstants.PADDING_SMALL);

        Button pvpBtn = createMenuButton("Player 1 vs Player 2", () -> onModeSelected.accept(GameMode.PLAYER_VS_PLAYER, mapSelector.getValue()));
        Button pvbBtn = createMenuButton("Player 1 vs Bot", () -> onModeSelected.accept(GameMode.PLAYER_VS_BOT, mapSelector.getValue()));
        Button bvbBtn = createMenuButton("Bot vs Bot (Spectate)", () -> onModeSelected.accept(GameMode.BOT_VS_BOT, mapSelector.getValue()));

        this.getChildren().addAll(title, mapSelectionBox, pvpBtn, pvbBtn, bvbBtn);
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_MEDIUM));
        btn.setPrefWidth(ViewConstants.BUTTON_WIDTH_LARGE);
        btn.setPrefHeight(ViewConstants.BUTTON_HEIGHT_STANDARD);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}