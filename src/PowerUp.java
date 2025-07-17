//-------------------------------------------------------
// Yifu Hou 23009975
// Yuze Liang 23010065
// Xuran Li 23010041
// Shanfei Mo 23010080
//-------------------------------------------------------
package src;
import java.awt.*;

public class PowerUp {
    // Power-up type
    public enum PowerUpType { HEART, EXPLOSION, COIN }
    private PowerUpType type;
    // Power-up images
    private Image heartImage;
    private Image explosionImage;
    private Image coinImage;
    private GameEngine engine;

    // Power-up position and state
    private double positionX;
    private double positionY;
    private boolean active;
    private double radius;

    // Power-up spawn timing control
    private double nextSpawnTime;
    private final double MIN_SPAWN_INTERVAL = 5.0;  // Minimum spawn interval
    private final double MAX_SPAWN_INTERVAL = 15.0; // Maximum spawn interval

    // Constructor
    public PowerUp(GameEngine engine, Image heartImage, Image explosionImage, Image coinImage) {
        this.engine = engine;
        this.heartImage = heartImage;
        this.explosionImage = explosionImage;
        this.coinImage = coinImage;
        this.radius = 20;
        this.active = false;
        this.nextSpawnTime = MIN_SPAWN_INTERVAL;
    }

    // Spawn a new power-up
    private void spawn() {
        // Randomly select power-up type
        double rand = engine.rand(100);
        if (rand < 30) { // 30% chance to spawn a heart power-up
            type = PowerUpType.HEART;
        } else if (rand < 60) { // 30% chance to spawn an explosion power-up
            type = PowerUpType.EXPLOSION;
        } else { // 40% chance to spawn a coin power-up
            type = PowerUpType.COIN;
        }
        // Spawn at a random position above the screen
        positionX = engine.rand(engine.width() - 60) + 30; // Ensure it doesn't spawn on the edge
        positionY = -30; // Enter from the top of the screen
        active = true;
    }

    // Update power-up state
    public void update(double dt, double gameTimer) {
        if (!active) {
            // Check if it's time to spawn
            if (gameTimer >= nextSpawnTime) {
                spawn();
                // Set the next spawn time
                nextSpawnTime = gameTimer + MIN_SPAWN_INTERVAL + 
                               engine.rand(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
            }
            return;
        }

        // Move the power-up downwards
        positionY += 100 * dt; // Movement speed

        // If the power-up goes off the bottom of the screen, deactivate it
        if (positionY > engine.height() + 30) {
            active = false;
        }
    }

    // Draw the power-up
    public void draw() {
        if (!active) return;

        engine.saveCurrentTransform();
        engine.translate(positionX, positionY);
        Image currentImage;
        if (type == PowerUpType.HEART) {
            currentImage = heartImage;
        } else if (type == PowerUpType.EXPLOSION) {
            currentImage = explosionImage;
        } else {
            currentImage = coinImage;
        }
        engine.drawImage(currentImage, -20, -20, 40, 40); // Draw a smaller power-up
        engine.restoreLastTransform();
    }

    // Check for collision with the ship
    public PowerUpType checkCollision(double shipX, double shipY) { // Modify return type
        if (!active) return null; // No collision returns null

        // Calculate the distance to the ship
        double dx = shipX - positionX;
        double dy = shipY - positionY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // If the distance is less than the sum of the radii, a collision occurs
        if (distance < (radius + 30)) { // 30 is the radius of the ship
            active = false; // The power-up disappears after collision
            return type; // Return the type of power-up after collision
        }
        return null; // No collision returns null
    }

    // Getter methods
    public boolean isActive() { return active; }
    public double getPositionX() { return positionX; }
    public double getPositionY() { return positionY; }
    public PowerUpType getType() { return type; } // Add method to get power-up type
}