package xyuguyn00.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.enums.GameMode;

import java.util.function.BiConsumer;

public class MainMenuView extends VBox {

    public MainMenuView(BiConsumer<GameMode, String> onModeSelected) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #2F4F4F;");

        Text title = new Text("Advance Wars Clone");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);

        // Create the Map Selector
        ComboBox<String> mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll("game_stats.json", "game_stats_2.json");
        mapSelector.setValue("game_stats.json");
        mapSelector.setStyle("-fx-font-size: 16px; -fx-pref-width: 300px;");

        Text mapLabel = new Text("Select Map:");
        mapLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        mapLabel.setFill(Color.WHITE);

        VBox mapSelectionBox = new VBox(5, mapLabel, mapSelector);
        mapSelectionBox.setAlignment(Pos.CENTER);
        mapSelectionBox.setSpacing(10);

        // Pass the selected map value when a mode is clicked
        Button pvpBtn = createMenuButton("Player 1 vs Player 2", () -> onModeSelected.accept(GameMode.PLAYER_VS_PLAYER, mapSelector.getValue()));
        Button pvbBtn = createMenuButton("Player 1 vs Bot", () -> onModeSelected.accept(GameMode.PLAYER_VS_BOT, mapSelector.getValue()));
        Button bvbBtn = createMenuButton("Bot vs Bot (Spectate)", () -> onModeSelected.accept(GameMode.BOT_VS_BOT, mapSelector.getValue()));

        this.getChildren().addAll(title, mapSelectionBox, pvpBtn, pvbBtn, bvbBtn);
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}