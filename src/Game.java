//-------------------------------------------------------
	// Yifu Hou 23009975
	// Yuze Liang 23010065
	// Xuran Li 23010041
	// Shanfei Mo 23010080
	//-------------------------------------------------------
package src;
import java.awt.*;
import java.awt.event.*;
public class Game extends GameEngine {
	// Main Function
	public static void main(String args[]) {
		// Warning: Only call createGame in this function
		// Create a new Lab8
		createGame(new Game());
	}

	
	public static int getDifficultyLevel() {
		switch (difficulty) {
			case Medium:
				return 1;
			case Hard:
				return 2;
			default:
				return 0;
		}
	}

	//-------------------------------------------------------
	// Game
	//-------------------------------------------------------

	// Game State
	enum GameState {Menu, Options, Play, Rules};
	GameState state = GameState.Menu;

	// Difficulty
	enum Difficulty {Easy, Medium, Hard};
	static Difficulty difficulty = Difficulty.Easy;

	// Menu
	int menuOption = 0;

	// Score and Lives
	int score = 0;
	int lives = 3; // Initial 3 lives
	double gameTimer = 0.0; // Tracks game time for difficulty scaling

	final double BASE_ASTEROID_VERTICAL_SPEED = 50.0; // Base vertical speed for asteroids
	final double BASE_ALIEN_SPEED = 50.0;

	//-------------------------------------------------------
	// PowerUp
	//-------------------------------------------------------
	Image heartImage;
	Image explosionImage; // Add explosion power-up image variable
	Image coinImage; // Add coin power-up image variable
	PowerUp powerUp;
	
	//-------------------------------------------------------
	// Audio Manager
	//-------------------------------------------------------
	private AudioManager audioManager;

	//-------------------------------------------------------
	// Spaceship
	//-------------------------------------------------------

	// Image of the spaceship
	Image spaceshipImage;
	Image spaceshipEngine;
	Image spaceshipLeft;
	Image spaceshipRight;

	// Spaceship position
	double spaceshipPositionX;
	double spaceshipPositionY;
	
	// Spaceship velocity
	double spaceshipVelocityX;
	double spaceshipVelocityY;

	// Spaceship angle
	double spaceshipAngle;
	Image spaceship;

	// Initialize spaceship function
	public void initSpaceship() {
		// Load spaceship sprite images
		spaceshipImage    = subImage(spritesheet,   0,   0, 240, 240);
		spaceshipEngine   = subImage(spritesheet,   0, 240, 240, 240);
		spaceshipLeft     = subImage(spritesheet, 240, 240, 240, 240);
		spaceshipRight    = subImage(spritesheet, 480, 240, 240, 240);

		// Set the initial position of the spaceship at the bottom center of the screen
		spaceshipPositionX = width()/2;
		spaceshipPositionY = height() - 100; // Near the bottom of the screen
		spaceshipVelocityX = 0;
		spaceshipVelocityY = 0;
		spaceshipAngle     = 0; // Always facing upwards
	}

	// Function to draw the spaceship
	public void drawSpaceship() {
		// Save the current transform
		saveCurrentTransform();

		// Translate to the position of the spaceship
		translate(spaceshipPositionX, spaceshipPositionY);

		// The spaceship always faces upwards and does not rotate
		// Fix the angle to 0 degrees (facing upwards)
		spaceshipAngle = 0;

		// Draw the actual spaceship
		drawImage(spaceship, -30, -30, 80, 80);

		// Display thruster effects based on movement direction
		// Left thruster - Display when moving right
		if(right) {
			drawImage(spaceshipLeft, -30, -30, 60, 60);
		}

		// Right thruster - Display when moving left
		if(left) {
			drawImage(spaceshipRight, -30, -30, 60, 60);
		}

		// Main engine - Display when moving upwards
		if(up) {
			drawImage(spaceshipEngine, -30, -15, 60, 60);
		}

		// Restore the last transform to undo the rotate and translate transforms
		restoreLastTransform();
	}

	// Code to update 'move' the spaceship
	public void updateSpaceship(double dt) {
		// Set the spaceship movement speed
		double moveSpeed = 200;
		
		// Reset velocity
		spaceshipVelocityX = 0;
		spaceshipVelocityY = 0;
		
		// Use WASD to control spaceship movement
		// W key - Move upwards
		if(up == true) {
			spaceshipVelocityY -= moveSpeed;
		}
		
		// S key - Move downwards
		if(down == true) {
			spaceshipVelocityY += moveSpeed;
		}
		
		// A key - Move left
		if(left == true) {
			spaceshipVelocityX -= moveSpeed;
		}
		
		// D key - Move right
		if(right == true) {
			spaceshipVelocityX += moveSpeed;
		}
		
		// Move the spaceship
		spaceshipPositionX += spaceshipVelocityX * dt;
		spaceshipPositionY += spaceshipVelocityY * dt;
		
		// Restrict the spaceship within the screen boundaries
		// If the spaceship reaches the right boundary, restrict it within the boundary
		if(spaceshipPositionX > width() - 30)  {spaceshipPositionX = width() - 30;}
		
		// If the spaceship reaches the left boundary, restrict it within the boundary
		if(spaceshipPositionX < 30)  {spaceshipPositionX = 30;}
		
		// If the spaceship reaches the bottom boundary, restrict it within the boundary
		if(spaceshipPositionY > height() - 30) {spaceshipPositionY = height() - 30;}
		
		// If the spaceship reaches the top boundary, restrict it within the boundary
		if(spaceshipPositionY < 30)  {spaceshipPositionY = 30;}
	}


	//-------------------------------------------------------
// Laser Weapon
//-------------------------------------------------------
LaserWeapon laserWeapon;

// Initialize Laser
public void initLaser() {
	// Load laser image
	Image laserImage = subImage(spritesheet, 240, 0, 240, 240);
	// Create new laser weapon system
	laserWeapon = new LaserWeapon(this, audioManager, laserImage);
}

// Function to draw the laser
public void drawLaser() {
	laserWeapon.draw();
}

// Function to update 'move' the laser
public void updateLaser(double dt) {
	laserWeapon.update(dt, space);
	if (space && laserWeapon.getLaserMode() == 2) {
		laserWeapon.fireLaser(spaceshipPositionX, spaceshipPositionY);
	}
}

//-------------------------------------------------------
// Asteroid
//-------------------------------------------------------

// Asteroid-related configurations
final int MAX_ASTEROIDS = 7; // Maximum number of asteroids
final double MIN_SPAWN_INTERVAL = 1.0; // Minimum spawn interval (seconds)
final double MAX_SPAWN_INTERVAL = 3.0; // Maximum spawn interval (seconds)
double nextSpawnTime = 0.0; // Time for the next asteroid spawn

// Number of asteroids for different difficulties
final int EASY_ASTEROIDS = 3;
final int MEDIUM_ASTEROIDS = 5;
final int HARD_ASTEROIDS = 7;

// Images of the asteroids
Image[] asteroidImages = new Image[3];
Image[] currentAsteroidImages;

// Asteroid arrays
double[] asteroidPositionX;
double[] asteroidPositionY;
double[] asteroidVelocityX;
double[] asteroidVelocityY;
double[] asteroidAngle;
double[] asteroidRadius;
boolean[] asteroidActive; // Mark whether the asteroid is active
	// Initialise Asteroid
public void initAsteroid() {
	// Load three different asteroid images from spritesheet
	asteroidImages[0] = subImage(spritesheet, 480, 0, 240, 240);
	asteroidImages[1] = subImage(spritesheet, 720, 0, 240, 240);
	asteroidImages[2] = subImage(spritesheet, 960, 0, 240, 240);

	// Initialize asteroid arrays
	currentAsteroidImages = new Image[MAX_ASTEROIDS];
	asteroidPositionX = new double[MAX_ASTEROIDS];
	asteroidPositionY = new double[MAX_ASTEROIDS];
	asteroidVelocityX = new double[MAX_ASTEROIDS];
	asteroidVelocityY = new double[MAX_ASTEROIDS];
	asteroidAngle = new double[MAX_ASTEROIDS];
	asteroidRadius = new double[MAX_ASTEROIDS];
	asteroidActive = new boolean[MAX_ASTEROIDS];

	// Initialize all asteroids as inactive
	for (int i = 0; i < MAX_ASTEROIDS; i++) {
		asteroidActive[i] = false;
	}

	// Initialize the initial number of asteroids based on difficulty
	int initialAsteroids = EASY_ASTEROIDS;
	if (difficulty == Difficulty.Medium) initialAsteroids = MEDIUM_ASTEROIDS;
	if (difficulty == Difficulty.Hard) initialAsteroids = HARD_ASTEROIDS;

	// Spawn initial asteroids
	for (int i = 0; i < initialAsteroids; i++) {
		randomAsteroid(i);
	}
}

// Spawn an asteroid from the top of the screen
public void randomAsteroid(int index) {
	// Check if the index is valid
	if (index < 0 || index >= MAX_ASTEROIDS) return;

	// Calculate time-based speed multiplier (same as in update for consistency)
	double timeSpeedMultiplier = 1.0 + (gameTimer / 2) * 0.1;

	// Difficulty multiplier for asteroid
	double asteroidDifficultyMultiplier = 1.0;
	if (difficulty == Difficulty.Easy) asteroidDifficultyMultiplier = 0.8;
	else if (difficulty == Difficulty.Hard) asteroidDifficultyMultiplier = 1.2;

	// Spawn the asteroid at a random position at the top of the screen
	asteroidPositionX[index] = rand(width());
	asteroidPositionY[index] = -50; // Above the screen

	// Random horizontal velocity, but vertical velocity is always downward
	asteroidVelocityX[index] = (-30 + rand(60)) * asteroidDifficultyMultiplier * timeSpeedMultiplier;
	asteroidVelocityY[index] = (BASE_ASTEROID_VERTICAL_SPEED + rand(50)) * asteroidDifficultyMultiplier * timeSpeedMultiplier; // Move downward

	// Random rotation angle
	asteroidAngle[index] = rand(360);

	// Fixed radius
	asteroidRadius[index] = 30;

	// Randomly select an asteroid image
	int randomIndex = (int) rand(3);
	currentAsteroidImages[index] = asteroidImages[randomIndex];

	// Activate the asteroid
	asteroidActive[index] = true;

	// Set the next spawn time
	nextSpawnTime = gameTimer + MIN_SPAWN_INTERVAL + rand(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
}

// Find an inactive asteroid index
private int findInactiveAsteroid() {
	for (int i = 0; i < MAX_ASTEROIDS; i++) {
		if (!asteroidActive[i]) return i;
	}
	return -1;
}

// Function to update 'move' the asteroid
public void updateAsteroid(double dt) {
	// Get the maximum number of asteroids for the current difficulty
	int maxCurrentAsteroids = EASY_ASTEROIDS;
	if (difficulty == Difficulty.Medium) maxCurrentAsteroids = MEDIUM_ASTEROIDS;
	if (difficulty == Difficulty.Hard) maxCurrentAsteroids = HARD_ASTEROIDS;

	// Check if a new asteroid needs to be spawned
	if (gameTimer >= nextSpawnTime) {
		// Count the number of currently active asteroids
		int activeCount = 0;
		for (int i = 0; i < MAX_ASTEROIDS; i++) {
			if (asteroidActive[i]) activeCount++;
		}

		// If the number of active asteroids is less than the maximum for the current difficulty, try to spawn a new asteroid
		if (activeCount < maxCurrentAsteroids) {
			int newIndex = findInactiveAsteroid();
			if (newIndex != -1) {
				randomAsteroid(newIndex);
			}
		}
	}

	// Update all active asteroids
	for (int i = 0; i < MAX_ASTEROIDS; i++) {
		if (!asteroidActive[i]) continue;

		// Rotate the asteroid
		asteroidAngle[i] += 30 * dt;

		// Move the asteroid
		asteroidPositionX[i] += asteroidVelocityX[i] * dt;
		asteroidPositionY[i] += asteroidVelocityY[i] * dt;

		// If the asteroid reaches the left edge of the screen, bounce back
		if (asteroidPositionX[i] < 30) {
			asteroidPositionX[i] = 30;
			asteroidVelocityX[i] = -asteroidVelocityX[i];
		}

		// If the asteroid reaches the right edge of the screen, bounce back
		if (asteroidPositionX[i] >= width() - 30) {
			asteroidPositionX[i] = width() - 30;
			asteroidVelocityX[i] = -asteroidVelocityX[i];
		}

		// If the asteroid reaches the bottom of the screen, deactivate it
		if (asteroidPositionY[i] >= height() + 50) {
			asteroidActive[i] = false;
		}
	}
}

// Function to draw the asteroid
public void drawAsteroid() {
	// Draw all active asteroids
	for (int i = 0; i < MAX_ASTEROIDS; i++) {
		if (!asteroidActive[i]) continue;

		// Save the current transform
		saveCurrentTransform();

		// Translate to the position of the asteroid
		translate(asteroidPositionX[i], asteroidPositionY[i]);

		// Rotate the drawing context around the angle of the asteroid
		rotate(asteroidAngle[i]);

		// Draw the actual asteroid
		drawImage(currentAsteroidImages[i], -30, -30, 80, 80);

		// Restore the last transform to undo the rotate and translate transforms
		restoreLastTransform();
	}
}

//-------------------------------------------------------
// Explosion
//-------------------------------------------------------
// Images for the explosion
Image[] explosionImages = new Image[32];

// Support multiple explosion effects
static final int MAX_EXPLOSIONS = 10;

// Position of the explosions
double[] explosionPositionX = new double[MAX_EXPLOSIONS];
double[] explosionPositionY = new double[MAX_EXPLOSIONS];

// Timer for the explosions
double[] explosionTimer = new double[MAX_EXPLOSIONS];
double[] explosionDuration = new double[MAX_EXPLOSIONS];

boolean[] explosionActive = new boolean[MAX_EXPLOSIONS];

// Initialise variables for explosion
public void initExplosion() {
	// Load explosion sprites
	int n = 0;
	for (int y = 960; y < 1920; y += 240) {
		for (int x = 0; x < 1920; x += 240) {
			explosionImages[n] = subImage(spritesheet, x, y, 240, 240);
			n++;
		}
	}
}

// Create an explosion at position x,y
public void createExplosion(double x, double y) {
	// Play explosion sound
	audioManager.playExplosionSound();

	// Find an inactive explosion slot
	for (int i = 0; i < MAX_EXPLOSIONS; i++) {
		if (!explosionActive[i]) {
			// Position the explosion
			explosionPositionX[i] = x;
			explosionPositionY[i] = y;

			// Start a new explosion
			explosionTimer[i] = 0;
			explosionDuration[i] = 1.0;
			explosionActive[i] = true;
			break;
		}
	}
}

// Function to update the explosion
public void updateExplosion(double dt) {
	// Update all active explosions
	for (int i = 0; i < MAX_EXPLOSIONS; i++) {
		// If the explosion is active
		if (explosionActive[i]) {
			// Increment timer
			explosionTimer[i] += dt;

			// Check if the explosion has finished
			if (explosionTimer[i] >= explosionDuration[i]) {
				explosionActive[i] = false;
			}
		}
	}
}

// Function to get the frame of animation
public int getAnimationFrame(double timer, double duration, int numFrames) {
	// Get the frame
	int i = (int) floor(((timer % duration) / duration) * numFrames);
	// Check the range
	if (i >= numFrames) {
		i = numFrames - 1;
	}
	// Return the frame
	return i;
}
	// Function to draw the explosion
public void drawExplosion() {
	// Draw all active explosions
	for (int i = 0; i < MAX_EXPLOSIONS; i++) {
		// Select the right image
		if (explosionActive[i]) {
			// Save the current transform
			saveCurrentTransform();

			// Translate to the position of the explosion
			translate(explosionPositionX[i], explosionPositionY[i]);

			// Draw the explosion frame
			int frame = getAnimationFrame(explosionTimer[i], explosionDuration[i], 30);
			drawImage(explosionImages[frame], -30, -30, 80, 80);

			// Restore the last transform to undo the rotate and translate transforms
			restoreLastTransform();
		}
	}
}

//-------------------------------------------------------
// Alien
//-------------------------------------------------------
static final int MAX_ALIENS = 3; // 最大外星人数量
double[] alienPositionX = new double[MAX_ALIENS];
double[] alienPositionY = new double[MAX_ALIENS];
double[] alienVelocityX = new double[MAX_ALIENS];
double[] alienVelocityY = new double[MAX_ALIENS];
double[] alienAngle = new double[MAX_ALIENS];
double[] alienHealth = new double[MAX_ALIENS]; // 外星人血量
boolean[] alienActive = new boolean[MAX_ALIENS]; // 外星人是否激活
Image alienImage;
double alienSpeed; // Current speed, will be set dynamically

//-------------------------------------------------------
// Alien Laser
//-------------------------------------------------------
Image alienLaserImage;
static final int MAX_ALIEN_LASERS = 5;
double[] alienLaserPositionX = new double[MAX_ALIEN_LASERS];
double[] alienLaserPositionY = new double[MAX_ALIEN_LASERS];
double[] alienLaserVelocityX = new double[MAX_ALIEN_LASERS];
double[] alienLaserVelocityY = new double[MAX_ALIEN_LASERS];
boolean[] alienLaserActive = new boolean[MAX_ALIEN_LASERS];
double alienLaserTimer = 0; // Timer to control laser firing intervals
double alienLaserInterval = 2.0; // Base firing interval (seconds)
double nextAlienLaserTime = 0; // Time for the next laser firing

// Initialise Alien
public void initAlien() {
	// Load Image
	alienImage = loadImage("et.png");

	// Load alien laser image
	alienLaserImage = loadImage("attack.png");

	// Initialize alien laser arrays
	for (int i = 0; i < MAX_ALIEN_LASERS; i++) {
		alienLaserActive[i] = false;
	}

	// Initialize aliens
	for (int i = 0; i < MAX_ALIENS; i++) {
		alienActive[i] = false;
	}

	// Spawn initial alien
	randomAlien();

	// Set the time for the next laser firing
	nextAlienLaserTime = 2.0 + rand(1.0);
}

// Spawn an alien from the top of the screen
public void randomAlien() {
	// Find an inactive alien slot
	for (int i = 0; i < MAX_ALIENS; i++) {
		if (!alienActive[i]) {
			// Spawn the alien at a random position at the top of the screen
			alienPositionX[i] = rand(width());
			alienPositionY[i] = -50; // Above the screen

			// Initial velocity is downward
			alienVelocityX[i] = 0;
			alienVelocityY[i] = 30; // Initial downward movement

			// Set initial health
			alienHealth[i] = 100; // 设置初始血量为100

			// Activate the alien
			alienActive[i] = true;
			break;
		}
	}
}

// Update the Alien
public void updateAlien(double dt) {
	// Update all active aliens
	for (int j = 0; j < MAX_ALIENS; j++) {
		if (!alienActive[j]) continue;

		// When the alien enters the screen, start tracking the player
		if (alienPositionY[j] > 0) {
			// Calculate the direction from the alien to the player
			double dirX = spaceshipPositionX - alienPositionX[j];
			double dirY = spaceshipPositionY - alienPositionY[j];

			// Calculate the length of the direction vector
			double length = length(dirX, dirY);

			// If the length is not zero, normalize the direction vector
			if (length > 0) {
				// Set the alien's speed based on difficulty
				alienVelocityX[j] = dirX / length * alienSpeed;
				alienVelocityY[j] = dirY / length * alienSpeed;
			}

			// Calculate the alien's orientation angle
			alienAngle[j] = -atan2(alienVelocityX[j], alienVelocityY[j]);
		}

		}

// Get time passed since last frame
// 使用不同的变量名避免重复声明
double deltaTime = measureTime() / 1000.0;

// Update alien positions
for (int j = 0; j < MAX_ALIENS; j++) {
	if (alienActive[j]) {
		// Move the alien
		alienPositionX[j] += alienVelocityX[j] * dt;
		alienPositionY[j] += alienVelocityY[j] * dt;

		// Check if alien is out of screen bounds
		if (alienPositionY[j] > height() + 50) {
			alienActive[j] = false;
		}
	}
}

// Update the laser firing timer
alienLaserTimer += dt;

// Check if it's time to fire a laser
if (alienLaserTimer >= nextAlienLaserTime) {
		// Find an active alien to fire
		for (int j = 0; j < MAX_ALIENS; j++) {
			if (alienActive[j] && alienPositionY[j] > 0) {
				// Fire a laser
				audioManager.playShootSound();
				for (int i = 0; i < MAX_ALIEN_LASERS; i++) {
					if (!alienLaserActive[i]) {
						// Set the laser position and velocity
						alienLaserPositionX[i] = alienPositionX[j];
						alienLaserPositionY[i] = alienPositionY[j];

						// Calculate the direction towards the player
						double laserDirX = spaceshipPositionX - alienPositionX[j];
						double laserDirY = spaceshipPositionY - alienPositionY[j];
						double laserLength = length(laserDirX, laserDirY);

						// Set the laser speed
						double laserSpeed = 300.0; // Laser speed
						alienLaserVelocityX[i] = laserDirX / laserLength * laserSpeed;
						alienLaserVelocityY[i] = laserDirY / laserLength * laserSpeed;

						// Activate the laser
						alienLaserActive[i] = true;
						break;
					}
				}
				break;
			}
		}

		// Reset the timer and set the next firing time
			alienLaserTimer = 0;
			nextAlienLaserTime = alienLaserInterval;
	}

	// Update alien lasers
	for (int i = 0; i < MAX_ALIEN_LASERS; i++) {
		if (alienLaserActive[i]) {
			// Move the laser
			alienLaserPositionX[i] += alienLaserVelocityX[i] * dt;
			alienLaserPositionY[i] += alienLaserVelocityY[i] * dt;

			// Check if the laser hits the player
			double dx = spaceshipPositionX - alienLaserPositionX[i];
			double dy = spaceshipPositionY - alienLaserPositionY[i];
			double distance = length(dx, dy);

			if (distance < 30) { // Player collision radius
				// Player takes damage
				lives--;
				// Create an explosion effect
				createExplosion(spaceshipPositionX, spaceshipPositionY);
				// Deactivate the laser
				alienLaserActive[i] = false;

				// Check if the game is over
				if (lives <= 0) {
					gameOver = true;
				}
			}

			// Check if the laser is out of the screen
			if (alienLaserPositionX[i] < 0 || alienLaserPositionX[i] >= width() ||
				alienLaserPositionY[i] < 0 || alienLaserPositionY[i] >= height()) {
				alienLaserActive[i] = false;
			}
		} else {
			// 如果激光超出屏幕范围则停用
			alienLaserActive[i] = false;
		} }// 结束 if (alienLaserActive[i]) 块
	}

	// Draw Alien on the screen
private void drawAlien() {
	// Draw all active aliens
	for (int i = 0; i < MAX_ALIENS; i++) {
		if (alienActive[i]) {
			// Save the current transform
			saveCurrentTransform();

			// Translate to the position of the alien
			translate(alienPositionX[i], alienPositionY[i]);

			// Rotate the drawing context around the angle of the alien
			rotate(alienAngle[i]);

			// Draw the actual alien
			drawImage(alienImage, -30, -30, 75, 75);

			// Restore the transform for health bar (so it's not rotated)
			restoreLastTransform();

			// Draw health bar
			double healthBarWidth = 60;
			double healthBarHeight = 8;
			double healthPercentage = alienHealth[i] / 100.0;

			// Draw background (gray)
			changeColor(128, 128, 128);
			drawSolidRectangle(alienPositionX[i] - healthBarWidth/2, 
								alienPositionY[i] + 35, 
								healthBarWidth, 
								healthBarHeight);

			// Draw current health (red)
			changeColor(255, 0, 0);
			drawSolidRectangle(alienPositionX[i] - healthBarWidth/2, 
								alienPositionY[i] + 35, 
								healthBarWidth * healthPercentage, 
								healthBarHeight);
		}
	}

	// Draw alien lasers
	for (int i = 0; i < MAX_ALIEN_LASERS; i++) {
		if (alienLaserActive[i]) {
			// Save the current transform
			saveCurrentTransform();

			// Translate to the laser position
			translate(alienLaserPositionX[i], alienLaserPositionY[i]);

			// Calculate the laser angle
			double laserAngle = atan2(alienLaserVelocityY[i], alienLaserVelocityX[i]);
			rotate(laserAngle);

			// Draw the laser
			drawImage(alienLaserImage, -15, -15, 30, 30);

			// Restore the transform
			restoreLastTransform();
		}
	}
}

//-------------------------------------------------------
// Game Variables
//-------------------------------------------------------

// Spritesheet and Background Images
private Image spritesheet;
private Image backgroundImage;
private Image hudImage;
private Image back;
// Keep track of keys
private boolean left, right, up, down, space;
private boolean w, a, s, d; // WASD keys
private boolean mousePressed, enter; // Mouse and Enter key states
private boolean gameOver;

//-------------------------------------------------------
// Game Methods
//-------------------------------------------------------

// Function to initialise the game
@Override
public void init() {
	// Initialize AudioManager
	audioManager = new AudioManager();
	audioManager.initAudio();

	// Load sprites and background images
	spritesheet = loadImage("spritesheet.png");
	backgroundImage = loadImage("background.png");
	hudImage = loadImage("hud.png");
	heartImage = loadImage("aixin.png");
	explosionImage = loadImage("daojv.png"); // Load explosion power-up image
	coinImage = loadImage("jinbi.png"); // Load coin power-up image
	spaceship = loadImage("spaceship.png"); // Load spaceship image
	back = loadImage("back.jpg"); 

	// Set window size
	setWindowSize(1000, 1000);

	// Setup booleans
	left  = false;
	right = false;
	up    = false;
	down  = false;
	w     = false;
	a     = false;
	s     = false;
	d     = false;

	gameOver = false;
	score = 0; // Reset score
	gameTimer = 0.0; // Reset game timer

	// Initialise Spaceship
	initSpaceship();

	// Setup Laser
	initLaser();

	// Init Asteroid
	initAsteroid();

	// Initialize asteroids based on difficulty
	int initialAsteroids = EASY_ASTEROIDS;
	if (difficulty == Difficulty.Medium) initialAsteroids = MEDIUM_ASTEROIDS;
	if (difficulty == Difficulty.Hard) initialAsteroids = HARD_ASTEROIDS;

	// Spawn initial asteroids
	for (int i = 0; i < initialAsteroids; i++) {
		randomAsteroid(i);
	}

	// Init Explosion
	initExplosion();

	// Init Alien
	initAlien();
	alienSpeed = BASE_ALIEN_SPEED; // Initialize with base speed
	randomAlien(); // Spawn initial alien

	// Init PowerUp
	powerUp = new PowerUp(this, heartImage, explosionImage, coinImage); // Pass all power-up images
}

// Updates the display
public void update(double dt) {
	if (state == GameState.Play) {
		// If the game is over
		if (gameOver == true) {
			// Don't try to update anything.
			return;
		}

		gameTimer += dt; // Increment game timer

		// Update PowerUp
		powerUp.update(dt, gameTimer);

		// Check PowerUp collision
		PowerUp.PowerUpType collectedPowerUpType = powerUp.checkCollision(spaceshipPositionX, spaceshipPositionY); // Get the type of collected power-up
		if (collectedPowerUpType != null) {
			audioManager.playPowerUpSound();
			if (collectedPowerUpType == PowerUp.PowerUpType.HEART) {
				lives++; // Increase lives
			} else if (collectedPowerUpType == PowerUp.PowerUpType.COIN) {
				score += 100; // Increase score by 100
			} else if (collectedPowerUpType == PowerUp.PowerUpType.EXPLOSION) {
				// Explosion power-up effect: Clear all enemies and add points
				int destroyedEnemies = 0; // Track the number of destroyed enemies

				// Clear all active asteroids
				for (int i = 0; i < MAX_ASTEROIDS; i++) {
					if (asteroidActive[i]) {
						createExplosion(asteroidPositionX[i], asteroidPositionY[i]); // Trigger explosion effect
						asteroidActive[i] = false;
						destroyedEnemies++;
					}
				}

				// Check if any alien is on-screen or about to enter
for (int i = 0; i < MAX_ALIENS; i++) {
	if (alienActive[i] && alienPositionY[i] > -100 && alienPositionY[i] < height() + 100) {
		createExplosion(alienPositionX[i], alienPositionY[i]); // Trigger explosion effect
		alienActive[i] = false; // Deactivate the alien
		destroyedEnemies++;
	}
}

				// Increase score based on the number of destroyed enemies
				score += destroyedEnemies * 10; // 10 points per enemy
			}
		}

		// Dynamic speed scaling based on time
		double timeSpeedMultiplier = 1.0 + (gameTimer / 2) * 0.1; // Increases by 10% every 20 seconds

		// Difficulty multiplier for alien
		double alienDifficultyMultiplier = 1.0;
		if (difficulty == Difficulty.Easy) alienDifficultyMultiplier = 0.8;
		else if (difficulty == Difficulty.Hard) alienDifficultyMultiplier = 1.2;
		alienSpeed = BASE_ALIEN_SPEED * alienDifficultyMultiplier * timeSpeedMultiplier;

		// Update the spaceship
		updateSpaceship(dt);

		// Update the laser
		updateLaser(dt);

		// Update Asteroid
		updateAsteroid(dt);

		// Update Explosion
		updateExplosion(dt);

		// Update Alien
		updateAlien(dt);

		// Detect Collision between Laser and Asteroids
		for (int i = 0; i < laserWeapon.getMaxLasers(); i++) {
			if (laserWeapon.isLaserActive(i)) {
				for (int j = 0; j < MAX_ASTEROIDS; j++) {
					if (asteroidActive[j]) {
						if (distance(laserWeapon.getLaserX(i), laserWeapon.getLaserY(i), asteroidPositionX[j], asteroidPositionY[j]) < asteroidRadius[j] * 1.2) {
							// Destroy the laser
							laserWeapon.deactivateLaser(i);

							// Create an explosion
							createExplosion(asteroidPositionX[j], asteroidPositionY[j]);

							// Add score based on difficulty
							switch (difficulty) {
								case Easy:
									score += 1;
									break;
								case Medium:
									score += 2;
									break;
								case Hard:
									score += 4;
									break;
							}

							// Deactivate the asteroid
							asteroidActive[j] = false;
							break; // One laser can only hit one asteroid
						}
					}
				}
			}
		}
			// Detect Collision between Laser and Alien
for (int i = 0; i < laserWeapon.getMaxLasers(); i++) {
	if (laserWeapon.isLaserActive(i)) {
		for (int j = 0; j < MAX_ALIENS; j++) {
			if (alienActive[j] && distance(laserWeapon.getLaserX(i), laserWeapon.getLaserY(i), alienPositionX[j], alienPositionY[j]) < 30 * 1.2) {
				// Destroy the laser
				laserWeapon.deactivateLaser(i);

				// Reduce alien health
				alienHealth[j] -= 25; // 每次击中减少25点血量

				// Create an explosion if alien is destroyed
				if (alienHealth[j] <= 0) {
					createExplosion(alienPositionX[j], alienPositionY[j]);

					// Add score based on difficulty
					switch (difficulty) {
						case Easy:
							score += 1;
							break;
						case Medium:
							score += 2;
							break;
						case Hard:
							score += 4;
							break;
					}

					// Deactivate the alien
					alienActive[j] = false;

					// Create a new random Alien
					randomAlien();
				}
				break;
			}
		}
	}
}

// Detect Collision between Alien and Asteroids
for (int j = 0; j < MAX_ALIENS; j++) {
	if (alienActive[j]) {
		for (int i = 0; i < MAX_ASTEROIDS; i++) {
			if (asteroidActive[i]) {
				if (distance(alienPositionX[j], alienPositionY[j], asteroidPositionX[i], asteroidPositionY[i]) < asteroidRadius[i] + 30) {
					// Create an explosion
					createExplosion(alienPositionX[j], alienPositionY[j]);

					// Deactivate the alien
					alienActive[j] = false;

					// Create a new random Alien
					randomAlien();

					// Deactivate the asteroid
					asteroidActive[i] = false;
				}
			}
		}
	}
}

// Detect Collision between Spaceship and Asteroids
for (int i = 0; i < MAX_ASTEROIDS; i++) {
	if (asteroidActive[i]) {
		if (distance(spaceshipPositionX, spaceshipPositionY, asteroidPositionX[i], asteroidPositionY[i]) < asteroidRadius[i] + 30) {
			// Reduce lives when collision occurs
			lives--;
			// Create an explosion effect
			createExplosion(spaceshipPositionX, spaceshipPositionY);
			// Reset spaceship position
			spaceshipPositionX = width() / 2;
			spaceshipPositionY = height() - 100;
			// Destroy the collided asteroid
			asteroidActive[i] = false;
			// If lives are zero, game over
			if (lives <= 0) {
				gameOver = true;
			}
			break;
		}
	}
}

// Detect Collision between Spaceship and Alien
for (int j = 0; j < MAX_ALIENS; j++) {
	if (alienActive[j]) {
		if (distance(spaceshipPositionX, spaceshipPositionY, alienPositionX[j], alienPositionY[j]) < 60) {
			// Reduce lives when collision occurs
			lives--;
			// Create an explosion effect
			createExplosion(spaceshipPositionX, spaceshipPositionY);
			// Reset spaceship position
			spaceshipPositionX = width() / 2;
			spaceshipPositionY = height() - 100;
			// Deactivate the alien
			alienActive[j] = false;
			// Spawn a new alien
			randomAlien();
			// If lives are zero, game over
			if (lives <= 0) {
				gameOver = true;
			}
		}
	}
}
	}
}

// This gets called any time the Operating System
// tells the program to paint itself
public void paintComponent() {
	// Draw the background image
	drawImage(backgroundImage, 0, 0, width(), height());

	// Menu
	if (state == GameState.Menu) {
			// Draw menu background
			drawImage(back, 0, 0, width(), height());
		// Show Main Menu
		drawMenu();
	} else if (state == GameState.Options) {
		// Show Options Menu
		drawOptions();
	} else if (state == GameState.Play) {
		// Show Game
		drawGame();
	} else if (state == GameState.Rules) {
		drawRules();
	}
}

// Draw the Game
public void drawGame() {
	// Draw the HUD
	drawImage(hudImage, 0, 0, width(), height());

	// Draw the score and lives
	changeColor(white);
	drawText(width() - 60, height() - 30, String.valueOf(score), "Arial", 20);
	drawText(30, 30, "LIVES: " + lives, "Arial", 20);

	// If the game is not over yet
	if (gameOver == false) {
		// Draw the Asteroid
		drawAsteroid();

		// Draw the laser (if it's active)
		drawLaser();

		// Draw the Spaceship
		drawSpaceship();

		// Draw the Explosion
		drawExplosion();

		// Draw the Alien
		drawAlien();

		// Draw PowerUp
		powerUp.draw();
	} else {
		// If the game is over
		// Display GameOver text
		changeColor(white);
		drawText(width() / 2 - 165, height() / 2, "GAME OVER!", "Arial", 50);
		// Show final score and continue prompt
		drawText(width() / 2 - 165, height() / 2 + 60, "Final Score: " + score, "Arial", 40);
		drawText(width() / 2 - 165, height() / 2 + 100, "Press any key to continue", "Arial", 30);
	}
}
	// Draw the Main Menu
public void drawMenu() {
	// Draw game title using image
	Image titleImage = loadImage("图层 1.png");
	drawImage(titleImage, width()/2 - 250, 400, 500, 200);

	// Play
	if (menuOption == 0) {
		changeColor(white);
		drawText(50, 50, "Play");
		drawImage(spaceshipImage, 0, 0, 50, 50);
	} else {
		changeColor(black);
		drawText(50, 50, "Play");
	}

	// Options
	if (menuOption == 1) {
		changeColor(white);
		drawText(50, 100, "Options");
		drawImage(spaceshipImage, 0, 50, 50, 50);
	} else {
		changeColor(black);
		drawText(50, 100, "Options");
	}

	// Rules
	if (menuOption == 2) {
		changeColor(white);
		drawText(50, 150, "Rules");
		drawImage(spaceshipImage, 0, 100, 50, 50);
		// Check mouse click or keyboard selection
		if (mousePressed || space || enter) {
			state = GameState.Rules;
			menuOption = 0;
		}
	} else {
		changeColor(black);
		drawText(50, 150, "Rules");
	}

	// Exit
	if (menuOption == 3) {
		changeColor(white);
		drawText(50, 200, "Exit");
		drawImage(spaceshipImage, 0, 150, 50, 50);
	} else {
		changeColor(black);
		drawText(50, 200, "Exit");
	}
}

// Draw the Options Menu
public void drawOptions() {
	// Easy
	if (menuOption == 0) {
		changeColor(white);
		drawText(50, 50, "Easy      - 9 Lasers, Slow Enemy", "Arial", 24);
		drawImage(spaceshipImage, 0, 0, 50, 50);
	} else {
		if (difficulty == Difficulty.Easy) {
			changeColor(200, 200, 200);
			drawText(50, 50, "Easy      - 9 Lasers, Slow Enemy", "Arial", 24);
		} else {
			changeColor(150, 150, 150);
			drawText(50, 50, "Easy      - 9 Lasers, Slow Enemy", "Arial", 24);
		}
	}

	// Medium
	if (menuOption == 1) {
		changeColor(white);
		drawText(50, 100, "Medium - 5 Lasers, Regular Enemy", "Arial", 24);
		drawImage(spaceshipImage, 0, 50, 50, 50);
	} else {
		if (difficulty == Difficulty.Medium) {
			changeColor(200, 200, 200);
			drawText(50, 100, "Medium - 5 Lasers, Regular Enemy", "Arial", 24);
		} else {
			changeColor(150, 150, 150);
			drawText(50, 100, "Medium - 5 Lasers, Regular Enemy", "Arial", 24);
		}
	}

	// Hard
	if (menuOption == 2) {
		changeColor(white);
		drawText(50, 150, "Hard      - 1 Laser, Fast Enemy", "Arial", 24);
		drawImage(spaceshipImage, 0, 100, 50, 50);
	} else {
		if (difficulty == Difficulty.Hard) {
			changeColor(200, 200, 200);
			drawText(50, 150, "Hard      - 1 Laser, Fast Enemy", "Arial", 24);
		} else {
			changeColor(150, 150, 150);
			drawText(50, 150, "Hard      - 1 Laser, Fast Enemy", "Arial", 24);
		}
	}
}

//-------------------------------------------------------
// Keyboard functions
//-------------------------------------------------------

// Called whenever a key is pressed
public void keyPressed(KeyEvent e) {
	if (state == GameState.Menu) {
			// Draw menu background
			drawImage(back, 0, 0, width(), height());
		// Call keyPressed for Main Menu
		keyPressedMenu(e);
	} else if (state == GameState.Options) {
		// Call keyPressed for Options Menu
		keyPressedOptions(e);
	} else if (state == GameState.Play) {
		// Call keyPressed for Game
		keyPressedGame(e);
	} else if (state == GameState.Rules) {
		state = GameState.Menu;
	}
}

// KeyPressed for Game
public void keyPressedGame(KeyEvent e) {
	if (gameOver) {
		state = GameState.Menu;
	}

	// WASD controls
	// W key - Move up
	if (e.getKeyCode() == KeyEvent.VK_W) {
		up = true;
		w = true;
	}
	// S key - Move down
	if (e.getKeyCode() == KeyEvent.VK_S) {
		down = true;
		s = true;
	}
	// A key - Move left
	if (e.getKeyCode() == KeyEvent.VK_A) {
		left = true;
		a = true;
	}
	// D key - Move right
	if (e.getKeyCode() == KeyEvent.VK_D) {
		right = true;
		d = true;
	}

	// Arrow keys as an alternative
	// Left arrow - Move left
	if (e.getKeyCode() == KeyEvent.VK_LEFT) {
		left = true;
	}
	// Right arrow - Move right
	if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
		right = true;
	}
	// Up arrow - Move up
	if (e.getKeyCode() == KeyEvent.VK_UP) {
		up = true;
	}
	// Down arrow - Move down
	if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		down = true;
	}

	// Space bar - Fire laser
	if (e.getKeyCode() == KeyEvent.VK_SPACE) {
		space = true;
		// Fire a laser
		laserWeapon.fireLaser(spaceshipPositionX, spaceshipPositionY);
	}

	// Laser mode switching
	if (e.getKeyCode() == KeyEvent.VK_1) {
		laserWeapon.setLaserMode(0);
	}
	if (e.getKeyCode() == KeyEvent.VK_2) {
		laserWeapon.setLaserMode(1);
	}
	if (e.getKeyCode() == KeyEvent.VK_3) {
		laserWeapon.setLaserMode(2);
	}
}

public void resetGame() {
	// Reset game state
	gameOver = false;
	score = 0;
	lives = 3; // Reset lives
	gameTimer = 0.0; // Reset game timer

	// Reset spaceship state
	spaceshipPositionX = width() / 2;
	spaceshipPositionY = height() - 100; // Near the bottom of the screen
	spaceshipVelocityX = 0;
	spaceshipVelocityY = 0;
	spaceshipAngle = 0; // Always facing upwards
	left  = false;
	right = false;
	up    = false;
	down  = false;
	w     = false;
	a     = false;
	s     = false;
	d     = false;
	space = false;

	// Reset laser system
	laserWeapon.setLaserMode(0); // Default to scatter mode

	// Reset all lasers
	// Initialize laser weapon
	initLaser();

	// Reset all asteroids to inactive
	for (int i = 0; i < MAX_ASTEROIDS; i++) {
		asteroidActive[i] = false;
	}

	// Spawn initial asteroids based on current difficulty
	int initialAsteroids = EASY_ASTEROIDS;
	if (difficulty == Difficulty.Medium) initialAsteroids = MEDIUM_ASTEROIDS;
	if (difficulty == Difficulty.Hard) initialAsteroids = HARD_ASTEROIDS;

	// Spawn initial asteroids
	for (int i = 0; i < initialAsteroids; i++) {
		int newIndex = findInactiveAsteroid();
		if (newIndex != -1) {
			randomAsteroid(newIndex);
		}
	}

	// Reset alien
	alienSpeed = BASE_ALIEN_SPEED; // Reset alien speed
	randomAlien(); // Respawn alien

	// Reset power-up system
	powerUp = new PowerUp(this, heartImage, explosionImage, coinImage); // Reinitialize power-up object
}

public void keyPressedMenu(KeyEvent e) {
	audioManager.playButtonSound();
	// Move up in the menu
	if (e.getKeyCode() == KeyEvent.VK_UP) {
		menuOption--;
		if (menuOption < 0) menuOption = 3;
	}
	// Move down in the menu
	if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		menuOption++;
		if (menuOption > 3) menuOption = 0;
	}
	// Select an item
	if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		selectMenuOption();
	}
}

// Handle menu option selection
private void selectMenuOption() {
	switch (menuOption) {
		case 0: // Play
			resetGame();
			state = GameState.Play;
			break;
		case 1: // Options
			state = GameState.Options;
			break;
		case 2: // Rules
			state = GameState.Rules;
			break;
		case 3: // Exit
			System.exit(0);
			break;
	}
}

public void keyPressedOptions(KeyEvent e) {
	audioManager.playButtonSound();
	// Move up in the menu
	if (e.getKeyCode() == KeyEvent.VK_UP) {
		menuOption--;
		if (menuOption < 0) menuOption = 2;
	}
	// Move down in the menu
	if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		menuOption++;
		if (menuOption > 2) menuOption = 0;
	}
	// Select an item
	if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		switch (menuOption) {
			case 0: // Easy
				difficulty = Difficulty.Easy;
				state = GameState.Menu;
				break;
			case 1: // Medium
				difficulty = Difficulty.Medium;
				state = GameState.Menu;
				break;
			case 2: // Hard
				difficulty = Difficulty.Hard;
				state = GameState.Menu;
				break;
		}
	}
	// Add code to change difficulty or
	// return to the main menu
}

// Called whenever a key is released
public void keyReleased(KeyEvent e) {
	if (state == GameState.Play) {
		// Call handler for Game Play
		keyReleasedGame(e);
	}
}

// Handle mouse click events
public void mousePressed(MouseEvent e) {
	if (state == GameState.Menu) {
			// Draw menu background
			drawImage(back, 0, 0, width(), height());
		// Play button sound
		audioManager.playButtonSound();

		// Check if the click position is on a menu option
		int mouseY = e.getY();
		if (mouseY >= 30 && mouseY <= 70) { // Play
			menuOption = 0;
			selectMenuOption();
		} else if (mouseY >= 80 && mouseY <= 120) { // Options
			menuOption = 1;
			selectMenuOption();
		} else if (mouseY >= 130 && mouseY <= 170) { // Rules
			menuOption = 2;
			selectMenuOption();
		} else if (mouseY >= 180 && mouseY <= 220) { // Exit
			menuOption = 3;
			selectMenuOption();
		}
	} else if (state == GameState.Options) {
		// Play button sound
		audioManager.playButtonSound();

		// Check if the click position is on a difficulty option
		int mouseY = e.getY();
		if (mouseY >= 20 && mouseY <= 40) { // Easy
			difficulty = Difficulty.Easy;
			state = GameState.Menu;
		} else if (mouseY >= 60 && mouseY <= 90) { // Medium
			difficulty = Difficulty.Medium;
			state = GameState.Menu;
		} else if (mouseY >= 100 && mouseY <= 140) { // Hard
			difficulty = Difficulty.Hard;
			state = GameState.Menu;
		} else if (mouseY >= height() - 50 && mouseY <= height() - 10) { // Back
			state = GameState.Menu;
		}
	} else if (state == GameState.Rules) {
		// Play button sound
		audioManager.playButtonSound();

		// Click anywhere to return to the main menu
		state = GameState.Menu;
	}
}

// Draw the game rules interface
public void drawRules() {
	changeColor(white);
	drawText(50, 50, "Game Rules", "Arial", 30);

	changeColor(200, 200, 200);
	drawText(50, 100, "Power-Ups:", "Arial", 24);

	// Draw power-up icons and descriptions
	drawImage(heartImage, 50, 130, 30, 30);
	drawText(100, 150, "Heart - Restores 1 life", "Arial", 20);

	drawImage(explosionImage, 50, 180, 30, 30);
	drawText(100, 200, "Explosion - Destroys all enemies", "Arial", 20);

	drawImage(coinImage, 50, 230,30, 30);
	drawText(100, 250, "Coin - Increases score by 100", "Arial", 20);

	// Add scoring rules for different difficulties
	changeColor(200, 200, 200);
	drawText(50, 300, "Difficulty Scoring:", "Arial", 24);
	drawText(50, 330, "Easy - Base score", "Arial", 20);
	drawText(50, 360, "Medium - Score x2", "Arial", 20);
	drawText(50, 390, "Hard - Score x4", "Arial", 20);

	changeColor(200, 200, 200);
	drawText(50, 430, "Controls:", "Arial", 24);
	drawText(50, 460, "WASD or Arrow Keys - Move ship", "Arial", 18);
	drawText(50, 490, "Space - Fire laser 123-Switch attack mode", "Arial", 18);

	drawText(50, 530, "Click the mouse or press any key to return", "Arial", 18);
}

public void keyReleasedGame(KeyEvent e) {
	// WASD controls
	// W key released
	if (e.getKeyCode() == KeyEvent.VK_W) {
		w = false;
		up = false;
	}
	// S key released
	if (e.getKeyCode() == KeyEvent.VK_S) {
		s = false;
		down = false;
	}
	// A key released
	if (e.getKeyCode() == KeyEvent.VK_A) {
		a = false;
		left = false;
	}
	// D key released
	if (e.getKeyCode() == KeyEvent.VK_D) {
		d = false;
		right = false;
	}

	// Arrow key controls
	// Left arrow released
	if (e.getKeyCode() == KeyEvent.VK_LEFT) {
		// Only set left to false if A is not pressed
		if (!a) left = false;
	}
	// Right arrow released
	if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
		// Only set right to false if D is not pressed
		if (!d) right = false;
	}
	// Up arrow released
	if (e.getKeyCode() == KeyEvent.VK_UP) {
		// Only set up to false if W is not pressed
		if (!w) up = false;
	}
	// Down arrow released
	if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		// Only set down to false if S is not pressed
		if (!s) down = false;
	}

	// Space bar released
	if (e.getKeyCode() == KeyEvent.VK_SPACE) {
		space = false;
	}
}
}
