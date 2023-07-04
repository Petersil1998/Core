package net.petersil98.core.data;

import net.petersil98.core.Core;
import net.petersil98.core.constant.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * This class represents a Sprite as returned by the Data Dragon (DDragon)
 */
public class Sprite {

    private String sprite;
    private String group;
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Constructor
     * @param sprite The filename of the Sprite
     * @param group The folder name
     * @param x The x-Coordinate of the Image within the Sprite
     * @param y The y-Coordinate of the Image within the Sprite
     * @param width The width of the Image within the Sprite
     * @param height The height of the Image within the Sprite
     */
    public Sprite(String sprite, String group, int x, int y, int width, int height) {
        this.sprite = sprite;
        this.group = group;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Sprite() {}

    public String getSprite() {
        return sprite;
    }

    public String getGroup() {
        return group;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Utility Method to get the actual Image from the referenced Sprite
     * @return The Image given by this Sprite
     */
    public BufferedImage getImageFromSprite() {
        String path = String.format("%scdn/%s/img/sprite/%s", Constants.DDRAGON_BASE_PATH, Constants.DDRAGON_VERSION, this.getSprite());
        try {
            return ImageIO.read(URI.create(path).toURL()).getSubimage(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        } catch (IOException e) {
            Core.LOGGER.error("Couldn't load sprite from image", e);
        }
        return null;
    }
}
