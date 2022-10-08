package game.hitListeners;

import game.misc.Counter;
import game.objects.Block;
import game.objects.bullet.Bullet;

/**
 * @author Yuval Ezra.
 * A listener that tracks score.
 */
public class ScoreTrackingListener implements HitListener {
   private Counter currentScore;

   /**
    * A constructor for a ScoreTrackingListener.
    * @param scoreCounter the score counter.
    */
   public ScoreTrackingListener(Counter scoreCounter) {
      this.currentScore = scoreCounter;
   }

   /**
    * Adding score for a successful hit.
    * @param beingHit the Block that's being hit by 'hitter'.
    * @param hitter the Ball that hit 'beingHit'.
    */
   public void hitEvent(Block beingHit, Bullet hitter) {
       if (beingHit.getHitPoints() == 0) {
           this.currentScore.increase(100);
       }
   }
}