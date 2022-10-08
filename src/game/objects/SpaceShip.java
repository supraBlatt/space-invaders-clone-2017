package game.objects;
import java.awt.Color;

import biuoop.DrawSurface;
import game.collections.GameEnvironment;
import game.geometry.Line;
import game.geometry.Point;
import game.geometry.Rectangle;
import game.levels.GameLevel;
import game.misc.Fill;
import game.objects.bullet.Bullet;
import game.objects.bullet.Velocity;
import game.objects.collidable.Collidable;
import game.objects.collidable.CollisionInfo;
import game.objects.sprite.Sprite;
/**
 * @author Yuval Ezra
 * A paddle.
 */
public class SpaceShip implements Sprite, Collidable {
   private biuoop.KeyboardSensor keyboard;
   private Block block;
   private double speed;
   private GameEnvironment env;
   private long lastShot;
   private GameLevel g;
   private boolean dead;

   /**
    * A Paddle constructor.
    * @param block the Paddle's Block.
    * @param keyboard the keyboard sensor to sense the Paddle's movement via
    * the keyboard.
    * @param speed the Paddle's speed.
    * @param g the GameLevel that the Paddle resides in.
    */
   public SpaceShip(Block block, biuoop.KeyboardSensor keyboard, double speed, GameLevel g) {
       this.block = block;
       this.keyboard = keyboard;
       this.speed = speed;
       this.lastShot = 0;
       this.g = g;
       this.dead = false;
   }

   /**
    * Moves the Paddle to the left.
    * @param dt the time interval between frames.
    */
   public void moveLeft(double dt) {
       double newSpeed = this.speed * dt;
       Point newCorner;
       Line trajectory = new Line(new Point(this.getX(), this.getY()),
                                  new Point(this.getX() - newSpeed, this.getY()));
       CollisionInfo c = this.env.getClosestCollision(trajectory);

       // If the Paddle hit something on the left, don't allow it to continue in that direction.
       if (c != null && this.getX() - newSpeed < c.collisionPoint().getX()) {
           newCorner = new Point(c.collisionPoint().getX(), this.getY());
       } else {
           newCorner = new Point(this.getX() - newSpeed, this.getY());
       }
       Rectangle collisionRectangle = new Rectangle(newCorner, this.getWidth(), this.getHeight());
       this.block = new Block(collisionRectangle, this.block.getColor(), new Fill(Color.BLACK),
                              this.block.getHitPoints());
   }

   /**
    * Moves the Paddle to the right.
    * @param dt the time interval between frames.
    */
   public void moveRight(double dt) {
       double newSpeed = this.speed * dt;
       Point newCorner;
       Line trajectory = new Line(new Point(this.getX() + this.getWidth(), this.getY()),
                                  new Point(this.getX() + this.getWidth() + newSpeed, this.getY()));
       CollisionInfo c = this.env.getClosestCollision(trajectory);

       // If the Paddle hit something on its right, don't allow it to continue in that direction.
       // Also if the Paddle is going to cross it on the next move, and is on its left before moving.
       if (c != null && (this.getX() + this.getWidth()) + newSpeed > c.collisionPoint().getX()
           && this.getX() < c.collisionPoint().getX()) {
           newCorner = new Point(c.collisionPoint().getX() - this.getWidth(), this.getY());
       } else {
           newCorner = new Point(this.getX() + newSpeed,
                                 this.getY());
       }

       Rectangle collisionRectangle = new Rectangle(newCorner, this.getWidth(), this.getHeight());
       this.block = new Block(collisionRectangle, this.block.getColor(), new Fill(Color.BLACK),
                              this.block.getHitPoints());
   }

   /**
    * Notifies the Paddle that time has passed, and moves it.
    * @param dt the time interval between drawings.
    */
   public void timePassed(double dt) {
       if (this.keyboard.isPressed(biuoop.KeyboardSensor.LEFT_KEY)) {
           this.moveLeft(dt);
       }
       if (this.keyboard.isPressed(biuoop.KeyboardSensor.RIGHT_KEY)) {
           this.moveRight(dt);
       }

       // shooting
       long time = System.currentTimeMillis();
       if (this.keyboard.isPressed(biuoop.KeyboardSensor.SPACE_KEY) && (time - this.lastShot >= 350)) {
           this.lastShot = time;
           this.shoot();
       }
   }

   /**
    * Makes the paddle shoot a bullet.
    */
   private void shoot() {
       int radius = 3, ballSpeed = -600;
       double x = this.getX() + this.getWidth() / 2;
       double y = this.getY() - (radius + 3);
       Bullet b = new Bullet(new Point(x, y), radius, Color.WHITE);
       b.setVelocity(new Velocity(0, ballSpeed));
       b.addToGame(this.g);
   }

   /**
    * Draws the Paddle on the given DrawSurface 'd'.
    * @param d  the given DrawSurface.
    */
   public void drawOn(DrawSurface d) {
       this.block.drawOn(d);
   }

   /**
    * @return the Paddle's collision Rectangle.
    */
   public Rectangle getCollisionRectangle() {
       return this.block.getCollisionRectangle();
   }

   /**
   * Changes the Ball's speed after it hit the Paddle.
   * @param hitter the hitting Ball.
   * @param collisionPoint the Ball's collision point with this Paddle.
   * @param currentVelocity the Paddle's current velocity.
   * @return a new Velocity for the Ball, after hitting this Paddle
   * at the collision point 'collisionPoint'.
   */
   public Velocity hit(Bullet hitter, Point collisionPoint, Velocity currentVelocity) {
       Velocity newVelocity = this.block.hit(hitter, collisionPoint, currentVelocity);
       this.dead = true;
       return newVelocity;
   }

   /**
    * @return the Paddle's collision rectangle width.
    */
   public double getWidth() {
       return this.block.getCollisionRectangle().getWidth();
   }

   /**
    * @return the Paddle's collision rectangle height.
    */
   public double getHeight() {
       return this.block.getCollisionRectangle().getHeight();
   }

   /**
    * @return the Paddle's collision rectangle's corner X coordinate.
    */
   public double getX() {
       return this.block.getCollisionRectangle().getUpperLeft().getX();
   }

   /**
    * @return the Paddle's collision rectangle's corner Y coordiate.
    */
   public double getY() {
       return this.block.getCollisionRectangle().getUpperLeft().getY();
   }

   /**
    * @return this Paddle's Block.
    */
   public Block getBlock() {
       return this.block;
   }

   /**
    * Sets the Paddles GameEnvironment to 'environment'.
    * @param environment the new GameEnvironemnt for the Paddle.
    */
   public void setEnvironment(GameEnvironment environment) {
       this.env = new GameEnvironment(environment.getObstacles());
   }

   /**
    * Add this Paddle to the game.
    * @param game the game to add this Paddle to
    */
   public void addToGame(GameLevel game) {
       game.addSprite(this);
       game.addCollidable(this);
   }

   /**
    * Removes this Paddle from the Game 'game'.
    * @param game the Game to remove this Paddle from.
    */
   public void removeFromGame(GameLevel game) {
       game.removeCollidable(this);
       game.removeSprite(this);
   }

   /**
    * @return true if this Paddle 'should die'. False otherwise.
    */
   public boolean isDead() {
       return this.dead;
   }

   /**
    * 'Revives' the paddle -- sets 'dead' to false.
    */
   public void ressurect() {
       this.dead = false;
   }
}