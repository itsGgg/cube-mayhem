package model.tools;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LoadSave {

    public static BufferedImage GetSprite(String imageName) {
        BufferedImage img = null;
        File is = new File(Constants.Path.RESSOURCES + imageName + ".png");
        try {
            img = ImageIO.read(is);

            GraphicsConfiguration gfxConfig = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
            
            BufferedImage optimized = gfxConfig.createCompatibleImage(
                img.getWidth(), img.getHeight(), img.getTransparency());
            
            optimized.getGraphics().drawImage(img, 0, 0, null);
    
            return optimized;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
