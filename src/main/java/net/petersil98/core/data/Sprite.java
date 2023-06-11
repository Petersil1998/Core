package net.petersil98.core.data;

import net.petersil98.core.Core;
import net.petersil98.core.constant.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Sprite {

    private String sprite;
    private String group;
    private int x;
    private int y;
    private int width;
    private int height;

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

    public BufferedImage getImageFromSprite() {
        String path = String.format("%scdn/%s/img/sprite/%s", Constants.DDRAGON_BASE_PATH, Constants.DDRAGON_VERSION, this.getSprite());
        try {
            return ImageIO.read(new URL(path)).getSubimage(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        } catch (IOException e) {
            Core.LOGGER.error("Couldn't load sprite from image", e);
        }
        return null;
    }
}
