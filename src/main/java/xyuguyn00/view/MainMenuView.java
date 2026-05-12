package xyuguyn00.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import xyuguyn00.common.enums.GameMode;

import java.util.function.Consumer;

public class MainMenuView extends VBox {

    public MainMenuView(Consumer<GameMode> onModeSelected) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #2F4F4F;");

        Text title = new Text("Advance Wars: Java Edition");
        title.setFont(Font.font("Arial", 36));
        title.setStyle("-fx-fill: white; -fx-font-weight: bold;");

        Button pvpBtn = createMenuButton("Player 1 vs Player 2", () -> onModeSelected.accept(GameMode.PLAYER_VS_PLAYER));
        Button pvbBtn = createMenuButton("Player 1 vs Bot", () -> onModeSelected.accept(GameMode.PLAYER_VS_BOT));
        Button bvbBtn = createMenuButton("Bot vs Bot (Spectate)", () -> onModeSelected.accept(GameMode.BOT_VS_BOT));

        this.getChildren().addAll(title, pvpBtn, pvbBtn, bvbBtn);
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 18));
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}