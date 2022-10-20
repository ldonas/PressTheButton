package com.loredo.pressthebutton;

import android.graphics.Color;

import java.util.Random;

public class GamesColor {
    private final static int[] _iCOLORS = {Color.parseColor("#faf271"), /* 1. Amarillo*/
            Color.parseColor("#888888"), /* 2. Gris Claro*/
            Color.parseColor("#e5b463"), /* 3. Naranja*/
            Color.parseColor("#9d6da5"), /* 4. Violeta*/
            Color.parseColor("#bf0811"), /* 5. Rojo*/
            Color.parseColor("#bdcf73"), /* 6. Verde Claro*/
            Color.parseColor("#77bae8"), /* 7. Azul*/
            Color.parseColor("#6c8dc4"), /* 8. Azul Medio*/
            Color.parseColor("#635e9f"), /* 9. Azul Oscuro*/
            Color.parseColor("#cf7aab"), /*10. Rosa*/
            Color.parseColor("#a28356"), /*11. Marrón Claro*/
            Color.parseColor("#5b400c"), /*12. Marrón Oscuro*/
            Color.parseColor("#c6baa2"), /*13. Marrón Muy Claro*/
            Color.parseColor("#cf734f"), /*14. Rojo Claro*/
            Color.parseColor("#75ad74"), /*15. Verde Oscuro*/
            Color.parseColor("#545454")  /*16. Gris Oscuro*/};

    public static int getColorCount(){return _iCOLORS.length;}

    public static int GetColor(int colorNumber) {
        if(colorNumber >= 0 && colorNumber < _iCOLORS.length)
            return _iCOLORS[colorNumber];
        else
            return Color.parseColor("#FF000000");
    }

    public static  int ColorVariant (int color){
        int Alpha = (color >> 24) & 0xff; // or color >>> 24
        int Red = (color >> 16) & 0xff;
        int Green = (color >>  8) & 0xff;
        int Blue = (color      ) & 0xff;

        Random r = new Random(System.currentTimeMillis());

        int variant = r.nextInt() % 5;

        Red += variant;
        Green += variant;
        Blue += variant;

        return ((Alpha & 0xff) << 24) | ((Red & 0xff) << 16) | ((Green & 0xff) << 8) | (Blue & 0xff);
    }

    public static int ColorLerp(int before, int after, int steps, int totalSteps)
    {
        if(steps <= 0) return before;
        if(steps >= totalSteps) return after;

        int beforeAlpha = (before >> 24) & 0xff; // or color >>> 24
        int beforeRed = (before >> 16) & 0xff;
        int beforeGreen = (before >>  8) & 0xff;
        int beforeBlue = (before      ) & 0xff;

        int afterAlpha = (after >> 24) & 0xff; // or color >>> 24
        int afterRed = (after >> 16) & 0xff;
        int afterGreen = (after >>  8) & 0xff;
        int afterBlue = (after      ) & 0xff;

        int lerpAlpha = beforeAlpha + (int)((float)(afterAlpha - beforeAlpha) * (float)steps / (float)totalSteps);
        int lerpRed = beforeRed + (int)(((float)afterRed - beforeRed) * (float)steps / (float)totalSteps);
        int lerpGreen = beforeGreen + (int)((float)(afterGreen - beforeGreen) * (float)steps / (float)totalSteps);
        int lerpBlue = beforeBlue + (int)((float)(afterBlue - beforeBlue) * (float)steps / (float)totalSteps);

        return ((lerpAlpha & 0xff) << 24) | ((lerpRed & 0xff) << 16) | ((lerpGreen & 0xff) << 8) | (lerpBlue & 0xff);
    }

    public static int TextColorInverted (int color)
    {
        int Alpha = (color >> 24) & 0xff; // or color >>> 24
        int Red = (color >> 16) & 0xff;
        int Green = (color >>  8) & 0xff;
        int Blue = (color      ) & 0xff;

        int grey = ((255 - Red) + (255 - Green) + (255 - Blue)) / 3;

        if(grey < (255 / 2))
            grey = 64;
        else
            grey = 240;

        return ((Alpha & 0xff) << 24) | ((grey & 0xff) << 16) | ((grey & 0xff) << 8) | (grey & 0xff);
    }
}
