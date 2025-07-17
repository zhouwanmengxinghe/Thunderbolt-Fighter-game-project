//-------------------------------------------------------
// Yifu Hou 23009975
// Yuze Liang 23010065
// Xuran Li 23010041
// Shanfei Mo 23010080
//-------------------------------------------------------
package src;
import java.awt.*;

public class LaserWeapon {
    private GameEngine engine;
    private AudioManager audioManager;

    // Image of the laser
    private Image laserImage;

    // Maximum number of lasers
    private int maxLasers;
    
    // Difficulty level
    private int difficultyLevel = 0; // 0: Easy, 1: Normal, 2: Hard

    // Laser position
    private double[] laserPositionX;
    private double[] laserPositionY;

    // Laser velocity
    private double[] laserVelocityX;
    private double[] laserVelocityY;

    // Laser angle
    private double[] laserAngle;

    // Laser active status
    private boolean[] laserActive;

    // Laser mode
    private int laserMode;
    private double rapidFireDelay;
    private double rapidFireTimer;

    // Constructor
    public LaserWeapon(GameEngine engine, AudioManager audioManager, Image laserImage) {
        this.engine = engine;
        this.audioManager = audioManager;
        this.laserImage = laserImage;
        
this.difficultyLevel = Game.getDifficultyLevel();
        init();
    }

    // Initialize Laser
    private void init() {
        // Set maximum number of lasers based on difficulty
        switch (difficultyLevel) {
            case 1: // Normal
                maxLasers = 5;
                break;
            case 2: // Hard
                maxLasers = 1;
                break;
            default: // Easy
                maxLasers = 9;
                break;
        }

        // Set Laser Mode
        laserMode = 0;

        // Set Rapid-Fire Delay
        rapidFireDelay = 0.1;

        // Allocate arrays
        laserPositionX = new double[maxLasers];
        laserPositionY = new double[maxLasers];
        laserVelocityX = new double[maxLasers];
        laserVelocityY = new double[maxLasers];
        laserAngle = new double[maxLasers];
        laserActive = new boolean[maxLasers];

        // Make all lasers inactive initially
        for (int i = 0; i < maxLasers; i++) {
            laserActive[i] = false;
        }
    }

    // Function to create a laser with given position and angle
    private void fireLaserAt(double x, double y, double angle) {
        // For all lasers
        for (int i = 0; i < maxLasers; i++) {
            // Can only fire a laser if there isn't already one active
            if (laserActive[i] == false) {
                // Set the laser position to the current spaceship position
                laserPositionX[i] = x;
                laserPositionY[i] = y - 30; // Fire from the top of the spaceship

                // Laser always moves upwards
                laserVelocityX[i] = 0;
                laserVelocityY[i] = -400; // Increase laser speed

                // Laser always faces upwards
                laserAngle[i] = 0;

                // Set it to active
                laserActive[i] = true;

                // Break the loop
                break;
            }
        }
    }

    // Function to shoot a new laser
    public void fireLaser(double spaceshipX, double spaceshipY) {
        // Play shooting sound
        audioManager.playShootSound();

        if (laserMode == 0) {
            // Normal mode - Single straight laser
            fireLaserAt(spaceshipX, spaceshipY, 0);
        } else if (laserMode == 1) {
            // Scatter mode - Three scattering lasers
            int inactiveLasers = 0;

            // Count the number of available lasers
            for (int i = 0; i < maxLasers; i++) {
                if (laserActive[i] == false) {
                    inactiveLasers++;
                }
            }

            // Check if at least 3 lasers are available
            if (inactiveLasers >= 3) {
                // Fire three scattering lasers
                // Middle one is straight, and the sides are slightly angled
                fireLaserAt(spaceshipX - 15, spaceshipY, 0);
                fireLaserAt(spaceshipX, spaceshipY, 0);
                fireLaserAt(spaceshipX + 15, spaceshipY, 0);
            }
        } else if (laserMode == 2) {
            // Rapid-fire mode - Lasers with slight random offsets
            fireLaserAt(spaceshipX + engine.rand(10.0) - 5, spaceshipY, 0);
        }
    }

    // Function to draw the laser
    public void draw() {
        for (int i = 0; i < maxLasers; i++) {
            // Only draw the laser if it's active
            if (laserActive[i]) {
                // Save the current transform
                engine.saveCurrentTransform();

                // Translate to the position of the laser
                engine.translate(laserPositionX[i], laserPositionY[i]);

                // Rotate the drawing context around the angle of the laser
                engine.rotate(laserAngle[i]);

                // Draw the actual laser
                engine.drawImage(laserImage, -30, -30, 60, 60);

                // Restore the last transform to undo the rotate and translate transforms
                engine.restoreLastTransform();
            }
        }
    }

    // Function to update 'move' the laser
    public void update(double dt, boolean spacePressed) {
        // If in Rapid-Fire Mode
        if (laserMode == 2 && spacePressed) {
            // Increment Timer
            rapidFireTimer += dt;

            // If Timer is greater than delay
            if (rapidFireTimer > rapidFireDelay) {
                // Decrement delay
                rapidFireTimer -= rapidFireDelay;
            }
        }

        for (int i = 0; i < maxLasers; i++) {
            if (!laserActive[i]) continue;

            // Move the Laser
            laserPositionX[i] += laserVelocityX[i] * dt;
            laserPositionY[i] += laserVelocityY[i] * dt;

            // If the laser reaches any edge of the screen, destroy it
            if (laserPositionX[i] < 0 || laserPositionX[i] >= engine.width() ||
                laserPositionY[i] < 0 || laserPositionY[i] >= engine.height()) {
                laserActive[i] = false;
            }
        }
    }

    // Getters and setters
    public void setLaserMode(int mode) {
        this.laserMode = mode;
    }

    public int getLaserMode() {
        return laserMode;
    }

    // Get laser position and status for collision detection
    public double getLaserX(int index) {
        return laserPositionX[index];
    }

    public double getLaserY(int index) {
        return laserPositionY[index];
    }

    public boolean isLaserActive(int index) {
        return laserActive[index];
    }

    public void deactivateLaser(int index) {
        laserActive[index] = false;
    }

    public int getMaxLasers() {
        return maxLasers;
    }

    // Get and set difficulty level
    public void setDifficultyLevel(int level) {
        if (level >= 0 && level <= 2) {
            this.difficultyLevel = level;
            init(); // Reinitialize with new difficulty
        }
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }
}