package game.objects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import biuoop.DrawSurface;
import game.geometry.Point;
import game.levels.GameLevel;
import game.objects.bullet.Bullet;
import game.objects.bullet.Velocity;
import game.objects.sprite.Sprite;

/**
 * @author Yuval Ezra.
 * A squad of Aliens.
 */
public class AlienSquad implements Sprite {

    private Block[][] aliens;
    private int leftCol;
    private int rightCol;
    private int bottomRow;
    private double speed;

    // initial values for a reset
    private double initialSpeed;
    private double initialX;
    private double initialY;

    private int screenWidth;
    private GameLevel g;
    private long lastShot;

    /**
     * An AlienSquad constructor.
     * @param aliens the aliens that are squad-ed together.
     * @param speed the aliens' speed.
     * @param screenWidth the screen's width.
     * @param g the Game in which this AlienSquad resides.
     */
    public AlienSquad(Block[][] aliens, double speed, int screenWidth, GameLevel g) {
        this.aliens = aliens.clone();
        this.leftCol = 0;
        this.rightCol = aliens[0].length - 1;
        this.bottomRow = aliens.length - 1;
        this.speed = speed;

        this.initialSpeed = speed;
        this.initialX = aliens[0][0].getX();
        this.initialY = aliens[0][0].getY();

        this.screenWidth = screenWidth;
        this.g = g;
        this.lastShot = 0;
    }

    @Override
    public void drawOn(DrawSurface d) {
        for (int i = 0; i <= this.bottomRow; i++) {
            for (int j = this.leftCol; j <= this.rightCol; j++) {
                if (this.aliens[i][j] != null && this.aliens[i][j].getHitPoints() != 0) {
                    this.aliens[i][j].drawOn(d);
                }
            }
        }
    }

    @Override
    public void timePassed(double dt) {
        this.move(dt);
        long time = System.currentTimeMillis();
        if (time - this.lastShot >= 500) {
            this.lastShot = time;
            this.shoot();
        }
    }

    /**
     * Makes this AlienSquad shoot a bullet.
     */
    private void shoot() {
        int col = this.generateCol();

        // all aliens are dead - this would probably not happen
        if (col == -1) {
            return;
        }
        int lowest = -1;
        for (int i = 0; i <= this.bottomRow; i++) {
            if (this.exists(this.aliens[i][col])) {
                if (lowest == -1 || i > lowest) {
                    lowest = i;
                }
            }
        }
        double radius = 4;
        double x = this.aliens[lowest][col].getX() + this.aliens[lowest][col].getWidth() / 2;
        double y = this.aliens[lowest][col].getY() + this.aliens[lowest][col].getHeight() + (radius + 1);
        Bullet b = new Bullet(new Point(x, y), 4, Color.RED);
        b.setVelocity(new Velocity(0, 100));
        // Ball b = this.aliens[lowest][col].createShot("enemy");
        b.addToGame(this.g);
        b.enemy(true);
    }

    /**
     * @return a random column in which there's an Alien.
     */
    private int generateCol() {

        List<Integer> toCheck = new ArrayList<>();
        for (int i = this.leftCol; i <= this.rightCol; i++) {
            toCheck.add(i);
        }
        while (toCheck.size() > 0) {

            // randomize a column, and if it was already checked, continue
            int num = new Random().nextInt(toCheck.size());
            int col = toCheck.get(num);
            if (!toCheck.contains(col)) {
                continue;
            }

            // else, don't check it anymore
            toCheck.remove(num);

            // look for an alien in this column
            for (int i = 0; i <= this.bottomRow; i++) {
                if (this.exists(this.aliens[i][col])) {
                    return col;
                }
            }
        }
        return -1;
    }

    /**
     * Moves the Aliens.
     * @param dt the interval of time between draws.
     */
    public void move(double dt) {
        this.setLeftAndRight();
        if (!this.handleDown(dt)) {
            this.moveNormal(dt);
        }
    }

    /**
     * Moves the Aliens normally - using 'speed'.
     * @param dt the interval of time between draws.
     */
    private void moveNormal(double dt) {
        for (int i = 0; i <= this.bottomRow; i++) {
            for (int j = this.leftCol; j <= this.rightCol; j++) {
                if (this.exists(this.aliens[i][j])) {
                    this.aliens[i][j].move(this.aliens[i][j].getX() + this.speed * dt, this.aliens[i][j].getY());
                }
            }
        }
    }

    /**
     * Checks if the Aliens need to move down, and if so, moves it.
     * @param dt the interval of time between draws.
     * @return true if the Aliens were moved down, false otherwise.
     */
    public boolean handleDown(double dt) {
        double leftX = 0;
        double rightX = 0;
        for (int i = 0; i <= bottomRow; i++) {
            if (this.exists(this.aliens[i][this.leftCol])) {
                leftX = this.aliens[i][this.leftCol].getCollisionRectangle().getUpperLeft().getX();
            }
            if (this.exists(this.aliens[i][this.rightCol])) {
                rightX = this.aliens[i][this.rightCol].getX() + this.aliens[i][this.rightCol].getWidth();
            }
        }
        double newX = leftX + this.speed * dt;

        // if we hit one of the edges, move the alien squad down and raise their speed
        if ((newX < 0 && this.speed < 0) || (rightX > this.screenWidth && this.speed > 0)) {
            this.speed = -1.1 * this.speed;
            this.moveDown();
            return true;
        }
        return false;
    }

    /**
     * Moves the Aliens down.
     */
    public void moveDown() {
        for (int i = 0; i <= this.bottomRow; i++) {
            for (int j = this.leftCol; j <= this.rightCol; j++) {
                if (this.exists(this.aliens[i][j])) {
                    this.aliens[i][j].move(this.aliens[i][j].getX(),
                            this.aliens[i][j].getY() + this.aliens[i][j].getHeight());
                }
            }
        }
    }

    /**
     * Adds this AlienSquad to GameLevel 'g'.
     * @param game the GameLevel to add this AlienSquad to.
     */
    public void addToGame(GameLevel game) {
        for (int i = 0; i <= this.bottomRow; i++) {
            for (int j = this.leftCol; j <= this.rightCol; j++) {
                if (this.exists(this.aliens[i][j])) {
                    game.addCollidable(this.aliens[i][j]);
                }
            }
        }
    }

    /**
     * Checks if an Alien in the squad is not 'dead'.
     * @param alien the Alien itself.
     * @return true if the Alien is alive, false otherwise.
     */
    public boolean exists(Block alien) {
        return alien != null && alien.getHitPoints() != 0;
    }

    /**
     * Updates this leftCol, rightCol and bottomRow.
     */
    public void setLeftAndRight() {
        this.leftCol = -1;
        this.rightCol = -1;
        this.bottomRow = -1;
        for (int i = 0; i < this.aliens.length; i++) {
            for (int j = 0; j < this.aliens[0].length; j++) {
                if (this.exists(this.aliens[i][j])) {
                    if (this.leftCol == -1 || j < this.leftCol) {
                        this.leftCol = j;
                    }
                    if (this.rightCol == -1 || this.rightCol < j) {
                        this.rightCol = j;
                    }
                    if (this.bottomRow == -1 || this.bottomRow < i) {
                        this.bottomRow = i;
                    }
                }
            }
        }
    }

    /**
     * @return the lowest Y coordinate of this AlienSquad.
     */
    public double getHeight() {
        for (int j = 0; j <= this.rightCol; j++) {
            if (this.exists(this.aliens[this.bottomRow][j])) {
                double y = this.aliens[this.bottomRow][j].getY();
                return y + this.aliens[this.bottomRow][j].getHeight();
            }
        }
        return 0;
    }

    /**
     * @return this aliens.
     */
    public Block[][] getAliens() {
        return this.aliens;
    }

    /**
     * Checks whether this AlienSquad should reset its position or not.
     * If so, it restarts their position.
     * @param shieldHeight the height of the shields in the game.
     * @return true if the aliens need to restart, false otherwise.
     */
    public boolean shouldRestart(double shieldHeight) {
        if (this.getHeight() >= shieldHeight) {
            this.restart();
            return true;
        }
        return false;
    }

    /**
     * Restart this aliens to their initian positions.
     */
    public void restart() {
        double startX = this.initialX, startY = this.initialY;
        int lineSpace = 45, widSpace = 8;

        for (int i = 0; i < aliens.length; i++) {
            double x = startX;
            for (int j = 0; j < aliens[0].length; j++) {
                double width = aliens[i][j].getCollisionRectangle().getWidth();
                aliens[i][j].move(x + j * width, startY);
                x += widSpace;
            }
            startY += lineSpace;
        }
        this.speed = this.initialSpeed;
    }

    /**
     * Checks if this AlienSquad contains Alien b.
     * @param b the Alien to check if contained in this AlienSquad.
     * @return true if b is in this AlienSquad, false otherwise.
     */
    public boolean contains(Block b) {
        for (int i = 0; i < aliens.length; i++) {
            for (int j = 0; j < aliens[0].length; j++) {
                if (this.aliens[i][j].equals(b)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return true if all aliens are dead, false otherwise.
     */
    public boolean isEmpty() {
        for (int i = 0; i <= this.bottomRow; i++) {
            for (int j = this.leftCol; j <= this.rightCol; j++) {
                if (this.exists(this.aliens[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }
}
