package game.levels;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import biuoop.DialogManager;
import biuoop.GUI;
import biuoop.KeyboardSensor;
import game.animation.Animation;
import game.animation.AnimationRunner;
import game.animation.EndScreen;
import game.animation.MenuAnimation;
import game.decorators.KeyPressStoppableAnimation;
import game.highscores.HighScoresAnimation;
import game.highscores.HighScoresTable;
import game.highscores.ScoreInfo;
import game.menu.Menu;
import game.menu.ShowHiScoresTask;
import game.menu.Task;
import game.misc.Counter;
import game.misc.Fill;
import game.misc.Tuple;
import game.objects.Block;
import game.objects.bullet.Velocity;

/**
 * @author Yuval Ezra. A Game Flow.
 */
public class GameFlow {

    private AnimationRunner ar;
    private KeyboardSensor keyboard;
    private Counter lives;
    private Counter score;
    private GUI gui;

    /**
     * A constructor for GameFlow.
     * @param ar the AnimationRunner.
     * @param keyboard the Keyboard
     * @param lives the amount of lives.
     * @param gui the GUI to present the dialog manager on.
     */
    public GameFlow(AnimationRunner ar, KeyboardSensor keyboard, int lives, GUI gui) {
        this.ar = ar;
        this.keyboard = keyboard;
        this.lives = new Counter(lives);
        this.score = new Counter(0);
        this.gui = gui;
    }

    /**
     * Runs the levels.
     */
    public void runGame() {

        int battleNo = 1;
        Counter livesTemp = new Counter(this.lives.getValue());
        GameLevel level = new GameLevel(this.spaceInvadersLevel(battleNo), this.keyboard,
                                        this.ar, livesTemp, this.score, battleNo);

        level.initialize();
        boolean win = true;

        while (true) {
            while (livesTemp.getValue() > 0 && level.getRemainingBlocks().getValue() > 0) {
                level.playOneTurn();
            }
            if (livesTemp.getValue() == 0) {
                win = false;
                break;
            }
            battleNo++;
            level = new GameLevel(this.spaceInvadersLevel(battleNo), this.keyboard,
                                  this.ar, livesTemp, this.score, battleNo);
            level.initialize();
        }

        File f = new File("highscores.txt");
        HighScoresTable t = HighScoresTable.loadFromFile(f);
        if (t.getRank(score.getValue()) <= t.size()) {
            DialogManager dialog = this.gui.getDialogManager();
            String name = dialog.showQuestionDialog("Name", "What is your name?", "");
            t.add(new ScoreInfo(name, score.getValue()));
            try {
                t.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // displaying an end screen until space is pressed
        KeyPressStoppableAnimation end = new KeyPressStoppableAnimation(this.keyboard, KeyboardSensor.SPACE_KEY,
                new EndScreen(this.score, win));

        // HighScoresAnimation end = new HighScoresAnimation(t, "p", this.keyboard);
        while (!end.shouldStop()) {
            this.ar.run(end);
        }
    }

    /**
     * Initializing the Menu of the game and the GameFlow overall.
     * @return a new Menu for this Game.
     */
    public Menu<Task<Void>> initializeMenu() {
        Menu<Task<Void>> menu = new MenuAnimation<Task<Void>>(this.keyboard, this.ar);

        // create a new highscores file
        File f = new File("highscores.txt");
        HighScoresTable t = HighScoresTable.loadFromFile(f);
        Animation scores = new KeyPressStoppableAnimation(gui.getKeyboardSensor(), KeyboardSensor.SPACE_KEY,
                new HighScoresAnimation(t));

        menu.addSelection("h", "Hi scores", new ShowHiScoresTask(ar, scores));

        Task<Void> quitTask = new Task<Void>() {
            public Void run() {
                gui.close();
                System.exit(1);
                return null;
            }
        };
        menu.addSelection("q", "Quit", quitTask);

        Task<Void> tempPlay = new Task<Void>() {
            public Void run() {
                // creating the gameFlow
                GameFlow.this.runGame();
                try {
                    t.load(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // scores
                Animation scorez = new KeyPressStoppableAnimation(gui.getKeyboardSensor(), KeyboardSensor.SPACE_KEY,
                        new HighScoresAnimation(t));
                ar.run(scorez);
                return null;
            }
        };
        menu.addSelection("s", "Play", tempPlay);
        return menu;
    }

    /**
     * Creates a new SpaceInvaders level.
     * @param num the BattleNo.
     * @return a new SpaceInvaders level to run.
     */
    public LevelInformation spaceInvadersLevel(int num) {
        String levelName = "Battle no. " + num;

        Fill background = new Fill(Color.BLACK);
        int paddleSpeed = 650;
        int paddleWidth = 60;
        Tuple<Integer, Integer> scale = new Tuple<>(this.gui.getDrawSurface().getWidth(),
                                                    this.gui.getDrawSurface().getHeight());
        LevelInformation l = new GeneralLevelCreator(levelName, new ArrayList<Velocity>(), background, paddleSpeed,
                                                     paddleWidth, new ArrayList<Block>(), scale);
        return l;
    }
}