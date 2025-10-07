
package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import entities.Player;
import levels.LevelManager;
import main.Game;
import ui.PauseOverlay;
import utilz.LoadSave;

import static utilz.Constants.Environment.*;

public class Playing extends State implements Statemethods {

    private Player player;
    private LevelManager levelManager;
    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    private int xLvlOffset;
    private int lvlTilesWide = LoadSave.GetLevelData()[0].length;
    private int maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
    private int maxLvlOffsetX = maxTilesOffset * Game.TILES_SIZE;

    private BufferedImage backgroundImg, bigCloud, smallCloud;
    private int[] smallCloudsPos;
    private Random rnd = new Random();

    public Playing(Game game) {
        super(game);
        initClasses();

        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
        bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
        smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);

        // Random vertical positions for small clouds
        smallCloudsPos = new int[8];
        for (int i = 0; i < smallCloudsPos.length; i++)
            smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));
    }

    private void initClasses() {
        levelManager = new LevelManager(game);
        player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE));
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        pauseOverlay = new PauseOverlay(this);
    }

    @Override
    public void update() {
        if (!paused) {
            levelManager.update();
            player.update();
            updateCameraOffset();
        } else {
            pauseOverlay.update();
        }
    }

    // Camera follows the player
    private void updateCameraOffset() {
        xLvlOffset = (int) player.getHitbox().x - Game.GAME_WIDTH / 2;

        // Clamp to level bounds
        if (xLvlOffset < 0) xLvlOffset = 0;
        if (xLvlOffset > maxLvlOffsetX) xLvlOffset = maxLvlOffsetX;
    }

    @Override
    public void draw(Graphics g) {
        drawBackground(g);   // scrolling background
        drawClouds(g);       // moving clouds
        levelManager.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            pauseOverlay.draw(g);
        }
    }

    // Background with parallax and no cutting at the end
    private void drawBackground(Graphics g) {
        int parallaxOffset = (int) (xLvlOffset * 0.5); // adjust parallax speed

        // Main background
        g.drawImage(backgroundImg, -parallaxOffset, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        // Draw extra copy if needed to fill space
        if (-parallaxOffset + Game.GAME_WIDTH < Game.GAME_WIDTH) {
            g.drawImage(backgroundImg, -parallaxOffset + Game.GAME_WIDTH, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        }
    }

    private void drawClouds(Graphics g) {
        // Big clouds (slower than player)
        for (int i = 0; i < 3; i++) {
            g.drawImage(
                bigCloud,
                i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3),
                (int) (204 * Game.SCALE),
                BIG_CLOUD_WIDTH,
                BIG_CLOUD_HEIGHT,
                null
            );
        }

        // Small clouds (faster)
        for (int i = 0; i < smallCloudsPos.length; i++) {
            g.drawImage(
                smallCloud,
                SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7),
                smallCloudsPos[i],
                SMALL_CLOUD_WIDTH,
                SMALL_CLOUD_HEIGHT,
                null
            );
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            player.setAttacking(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> player.setLeft(true);
            case KeyEvent.VK_D -> player.setRight(true);
            case KeyEvent.VK_SPACE -> player.setJump(true);
            case KeyEvent.VK_ESCAPE -> paused = !paused;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> player.setLeft(false);
            case KeyEvent.VK_D -> player.setRight(false);
            case KeyEvent.VK_SPACE -> player.setJump(false);
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (paused) pauseOverlay.mouseDragged(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (paused) pauseOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (paused) pauseOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused) pauseOverlay.mouseMoved(e);
    }

    public void unpauseGame() {
        paused = false;
    }

    public void windowFocusLost() {
        player.resetDirBooleans();
    }

    public Player getPlayer() {
        return player;
    }
}

