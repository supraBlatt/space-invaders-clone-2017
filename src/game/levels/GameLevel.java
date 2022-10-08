package game.levels;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import biuoop.DrawSurface;
import biuoop.KeyboardSensor;
import game.animation.Animation;
import game.animation.AnimationRunner;
import game.animation.CountdownAnimation;
import game.animation.PauseScreen;
import game.collections.GameEnvironment;
import game.collections.SpriteCollection;
import game.decorators.KeyPressStoppableAnimation;
import game.geometry.Point;
import game.geometry.Rectangle;
import game.hitListeners.BallRemover;
import game.hitListeners.BlockRemover;
import game.hitListeners.ScoreTrackingListener;
import game.misc.Counter;
import game.misc.Fill;
import game.objects.AlienSquad;
import game.objects.Block;
import game.objects.SpaceShip;
import game.objects.bullet.Bullet;
import game.objects.collidable.Collidable;
import game.objects.sprite.LevelIndicator;
import game.objects.sprite.LivesIndicator;
import game.objects.sprite.ScoreIndicator;
import game.objects.sprite.Sprite;

/**
 * @author Yuval Ezra An Arkanoid game.
 */
public class GameLevel implements Animation {
    private SpriteCollection sprites;
    private GameEnvironment environment;
    private int wid;
    private int len;
    private SpaceShip paddle;
    private KeyboardSensor keyboard;

    private Counter remainingBlocks;
    private Counter remainingBalls;
    private Counter score;
    private Counter lives;

    private AnimationRunner runner;

    private LevelInformation level;
    private AlienSquad a;
    private double shieldHeight;
    private int battleNo;

    /**
     * A constructor for a GameLevel.
     * @param level the level we would play.
     * @param keyboard the GameLevel keyboard.
     * @param ar the AnimationRunner.
     * @param lives the lives.
     * @param score the score.
     * @param battleNo the number of level.
     */
    public GameLevel(LevelInformation level, KeyboardSensor keyboard, AnimationRunner ar, Counter lives,
                     Counter score, int battleNo) {
        this.level = level;
        this.score = score;
        this.lives = lives;
        this.keyboard = keyboard;
        this.wid = 800;
        this.len = 600;
        this.runner = ar;
        this.shieldHeight = this.len;
        this.battleNo = battleNo;
    }

    /**
     * Add a Collidable to the Game.
     * @param c a Collidable to add to the Game.
     */
    public void addCollidable(Collidable c) {
        this.environment.addCollidable(c);
    }

    /**
     * Add a Sprite to the Game.
     * @param s a Sprite to add to the Game.
     */
    public void addSprite(Sprite s) {
        this.sprites.addSprite(s);
    }

    /**
     * Removes Collidable c from this Game.
     * @param c the Collidable to remove from this Game.
     */
    public void removeCollidable(Collidable c) {
        this.environment.removeCollidable(c);
    }

    /**
     * Removes Sprite s from this Game.
     * @param s the Sprite to remove from this Game.
     */
    public void removeSprite(Sprite s) {
        this.sprites.removeSprite(s);
    }

    /**
     * @return this GameEnvironment.
     */
    public GameEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * @return this remainingBlocks.
     */
    public Counter getRemainingBlocks() {
        return this.remainingBlocks;
    }

    /**
     * Initialize a new game: create the Blocks and Ball (and Paddle) and add them
     * to the game.
     */
    public void initialize() {

        this.sprites = new SpriteCollection();
        this.environment = new GameEnvironment();

        // initialize the Counters
        this.remainingBlocks = new Counter(0);
        this.remainingBalls = new Counter(0);

        // initialize the HitListeners
        BlockRemover blockRemover = new BlockRemover(this, this.remainingBlocks);
        BallRemover ballRemover = new BallRemover(this, this.remainingBalls);
        ScoreTrackingListener scoreTracker = new ScoreTrackingListener(score);

        // special Blocks creation - the borders and the death border
        java.util.List<Block> specialBlocks = new java.util.LinkedList<>();
        int thickness = 26;

        // creating the border blocks, screen and scoreboard
        this.addSprite(new Block(new Rectangle(new Point(0, 0), this.wid, this.len), Color.BLUE.darker().darker(), -1));

        // right border
        specialBlocks.add(new Block(new Rectangle(new Point(this.wid, 0), thickness, this.len), Color.GRAY, -1));

        // left border
        specialBlocks.add(new Block(new Rectangle(new Point(0, 0), 0, this.len), java.awt.Color.GRAY, -1));

        // scoreboard
        int scoreboardThickness = 20;
        Block scoreblock = new Block(new Rectangle(new Point(0, 0), this.wid, scoreboardThickness), Color.WHITE, -1);
        ScoreIndicator scoreboard = new ScoreIndicator(score, scoreblock);

        // live counter
        LivesIndicator liveIndicator = new LivesIndicator(lives, scoreblock);

        // level indicator
        LevelIndicator levelIndicator = new LevelIndicator(this.level.levelName(), scoreblock);

        // upper border
        specialBlocks.add(new Block(new Rectangle(new Point(0, scoreboardThickness), this.wid, 0), Color.GRAY, -1));

        // death border
        Block deathBorder = new Block(new Rectangle(new Point(0, len), this.wid, thickness), Color.GRAY, -1);

        deathBorder.addHitListener(ballRemover);
        specialBlocks.add(deathBorder);

        this.addSprite(this.level.getBackground());
        java.util.List<Block> blocks = this.level.blocks();

        // adding the special Blocks to this Game
        for (Block b : specialBlocks) {
            b.addToGame(this);
            b.addHitListener(ballRemover);
        }

        // adding the inner Blocks to this Game
        for (Block b : blocks) {
            b.addHitListener(blockRemover);
            b.addHitListener(scoreTracker);
            b.addToGame(this);
        }

        this.createShields();

        this.createAliens();

        // adding the score and life counters
        this.addSprite(scoreboard);
        this.addSprite(liveIndicator);
        this.addSprite(levelIndicator);
    }

    /**
     * Creates this Game's Aliens.
     */
    private void createAliens() {
        BlockRemover blockRemover = new BlockRemover(this, this.remainingBlocks);
        BallRemover ballRemover = new BallRemover(this, this.remainingBalls);
        ScoreTrackingListener scoreTracker = new ScoreTrackingListener(score);
        int alienWid = 10, alienLen = 5;
        double multiplier = 1.1;
        double startX = 25, startY = 60, speed = 100 * Math.pow(multiplier, this.battleNo - 1);
        int lineSpace = 45, widSpace = 8, hitPoints = 1;
        int height = 30, width = 40;

        Image image = null;
        String imagePath = "block_images/enemy.png";
        try {
            image = ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream(imagePath));
        } catch (IOException e) {
            System.out.println("Couldn't open image " + imagePath);
            e.printStackTrace();
        }
        Fill f = new Fill(image);

        Block[][] aliens = new Block[alienLen][alienWid];
        for (int i = 0; i < alienLen; i++) {
            double x = startX;
            for (int j = 0; j < alienWid; j++) {
                Rectangle r = new Rectangle(new Point(x + j * width, startY), width, height);
                aliens[i][j] = new Block(r, f, null, hitPoints);
                aliens[i][j].addHitListener(blockRemover);
                aliens[i][j].addHitListener(ballRemover);
                aliens[i][j].addHitListener(scoreTracker);
                aliens[i][j].enemy(true);
                this.remainingBlocks.increase(1);
                x += widSpace;
            }

            startY += lineSpace;
        }

        this.a = new AlienSquad(aliens, speed, this.wid, this);
        a.addToGame(this);
        this.addSprite(a);
    }

    /**
     * Creates this Game's shields.
     */
    private void createShields() {
        BlockRemover shieldRemover = new BlockRemover(this, new Counter(0));
        BallRemover ballRemover = new BallRemover(this, this.remainingBalls);
        double startX = 100;
        this.shieldHeight = this.len - 100;
        Color c = Color.CYAN;
        double shieldWid = 140;
        double shieldSpace = 90;

        double width = 5;
        // creating the shields
        for (int i = 1; i <= 3; i++) {
            double totalWid = 0;
            for (int j = 0; j < shieldWid; j += width, totalWid += width) {
                for (int k = 0; k < width * 3; k += width) {
                    Rectangle r = new Rectangle(new Point(startX + j, this.shieldHeight + k), width, width);
                    Block b = new Block(r, c, 1);
                    b.addHitListener(shieldRemover);
                    b.addHitListener(ballRemover);
                    b.addToGame(this);
                }
            }

            startX += shieldSpace + totalWid;
        }
    }

    /**
     * Creates the Paddle. A conveniecnce function.
     */
    public void placePaddle() {

        // Paddle initialization
        int paddleWidth = this.level.paddleWidth();
        int paddleHeight = 19, thickness = 23;
        int paddleSpeed = this.level.paddleSpeed();

        // the middle of the screen, X axis wise
        double paddleX = (this.wid - paddleWidth) / 2;
        double paddleY = this.len - thickness - paddleHeight;
        Rectangle paddleRect = new Rectangle(new Point(paddleX, paddleY), paddleWidth, paddleHeight);

        if (this.paddle == null) {
            List<Fill> fills = new ArrayList<>();
            fills.add(new Fill(Color.ORANGE));
            /*
             * try { image =
             * ImageIO.read(ClassLoader.getSystemClassLoader().getResourceAsStream(
             * "block_images/space.jpg")); Fill f = new Fill(image); fills.add(f); } catch
             * (IOException e) { System.out.println("Couldn't open spaceship image");
             * e.printStackTrace(); fills.add(new Fill(Color.ORANGE)); }
             */
            Block b = new Block(paddleRect, fills, new Fill(Color.black), -1);
            b.addHitListener(new BallRemover(this, this.remainingBalls));
            this.paddle = new SpaceShip(b, this.keyboard, paddleSpeed, this);
        } else {
            this.paddle.getBlock().setCollisionRectangle(paddleRect);
        }

        // setting this Paddle's environment
        this.paddle.setEnvironment(this.environment);
        this.paddle.addToGame(this);
    }

    /**
     * Creates the Balls. A convenience function.
     */
    public void createBallsOnTopOfPaddle() {

        // creating the Balls
        List<Bullet> balls = new ArrayList<>();
        Rectangle c = this.paddle.getCollisionRectangle();
        double x = c.getUpperLeft().getX() + c.getWidth() / 2;
        double y = c.getUpperLeft().getY() - 10;
        Point sharedCenter = new Point(x, y);

        for (int i = 0; i < this.level.numberOfBalls(); i++) {
            balls.add(new Bullet(sharedCenter, 5, java.awt.Color.WHITE));
            balls.get(i).setVelocity(this.level.initialBallVelocities().get(i));
        }

        // adds the Balls to this Game
        for (Bullet ball : balls) {
            ball.addToGame(this);
            this.remainingBalls.increase(1);
        }
    }

    /**
     * Play the game -- start the animation loop for a single life.
     */
    public void playOneTurn() {

        // creates the balls the places the Paddle
        this.placePaddle();
        this.createBallsOnTopOfPaddle();
        this.runner.run(new CountdownAnimation(2, 3, this.sprites));

        this.runner.run(this);
        this.paddle.removeFromGame(this);
    }

    @Override
    public void doOneFrame(DrawSurface d, double dt) {
        if (this.keyboard.isPressed("p")) {
            this.runner.run(new KeyPressStoppableAnimation(this.keyboard, KeyboardSensor.SPACE_KEY, new PauseScreen()));
        }

        this.sprites.drawAllOn(d);
        this.sprites.notifyAllTimePassed(dt);
    }

    @Override
    public boolean shouldStop() {

        // if the player died or finished the level
        if (this.lives.getValue() == 0 || this.a.isEmpty() || this.remainingBlocks.getValue() == 0) {
            return true;
        }

        if (this.a.shouldRestart(this.shieldHeight) || this.paddle.isDead()) {
            if (this.paddle.isDead()) {
                this.a.restart();
                this.paddle.ressurect();
            }
            this.lives.decrease(1);
            this.removeBalls();
            return true;
        }
        return false;
    }

    /**
     * Removes all Balls from this game.
     */
    private void removeBalls() {
        this.sprites.removeBalls();
    }
}
