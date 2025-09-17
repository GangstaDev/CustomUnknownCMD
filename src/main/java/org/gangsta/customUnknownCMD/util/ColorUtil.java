package org.gangsta.customUnknownCMD.util;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;

public class ColorUtil {
    public static String colorize(String text) {
        if (text == null) return "";

        // Handle gradients with &end& support
        if (text.contains("gradient:")) {
            int gradientStart = text.indexOf("&gradient:");
            if (gradientStart != -1) {
                String beforeGradient = text.substring(0, gradientStart);

                int textStart = text.indexOf("&", gradientStart + 10);
                if (textStart != -1) {
                    String gradientData = text.substring(gradientStart + 10, textStart);
                    String remaining = text.substring(textStart + 1);

                    // Look for &end& to terminate gradient
                    int endMarker = remaining.indexOf("&end&");
                    String gradientText;
                    String afterGradient = "";

                    if (endMarker != -1) {
                        gradientText = remaining.substring(0, endMarker);
                        afterGradient = remaining.substring(endMarker + 5); // Skip &end&
                    } else {
                        gradientText = remaining;
                    }

                    String[] hexColors = gradientData.split(":");
                    if (hexColors.length >= 2) {
                        String translatedBefore = ChatColor.translateAlternateColorCodes('&', beforeGradient);
                        String gradientResult = applyGradient(gradientText, hexColors);

                        StringBuilder formattedGradient = new StringBuilder();
                        for (int i = 0; i < gradientResult.length(); i++) {
                            char c = gradientResult.charAt(i);
                            if (c == '§') {
                                formattedGradient.append(c);
                            } else if (i > 0 && gradientResult.charAt(i - 1) == '§') {
                                formattedGradient.append(c);
                            } else {
                                formattedGradient.append(translatedBefore).append(c);
                            }
                        }

                        // Process the after gradient part (including single hex)
                        String processedAfter = colorize(afterGradient);
                        return formattedGradient + processedAfter;
                    }
                }
            }
        }

        // Handle single hex colors &#FF6B6B
        while (text.contains("&#")) {
            int hexStart = text.indexOf("&#");
            if (hexStart != -1 && text.length() >= hexStart + 8) {
                String hexCode = text.substring(hexStart + 2, hexStart + 8);
                try {
                    String replacement = ChatColor.of("#" + hexCode).toString();
                    text = text.substring(0, hexStart) + replacement + text.substring(hexStart + 8);
                } catch (Exception e) {
                    text = text.substring(0, hexStart) + text.substring(hexStart + 8);
                }
            } else {
                break;
            }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String applyGradient(String message, String... hexColors) {
        if (message.length() <= 1 || hexColors.length < 2) {
            // Ensure # prefix for fallback
            String fallbackColor = hexColors[0].startsWith("#") ? hexColors[0] : "#" + hexColors[0];
            return ChatColor.of(fallbackColor) + message;
        }

        StringBuilder gradientMessage = new StringBuilder();

        try {
            Color[] colors = new Color[hexColors.length];
            for (int i = 0; i < hexColors.length; i++) {
                // Add # prefix if missing
                String color = hexColors[i].startsWith("#") ? hexColors[i] : "#" + hexColors[i];
                colors[i] = Color.decode(color);
            }

            for (int i = 0; i < message.length(); i++) {
                float position = (float) i / (message.length() - 1);
                Color interpolatedColor = interpolateMultipleColors(colors, position);

                String hexColor = String.format("#%02x%02x%02x",
                        interpolatedColor.getRed(),
                        interpolatedColor.getGreen(),
                        interpolatedColor.getBlue());
                gradientMessage.append(ChatColor.of(hexColor)).append(message.charAt(i));
            }
        } catch (Exception e) {
            // Ensure # prefix for fallback
            String fallbackColor = hexColors[0].startsWith("#") ? hexColors[0] : "#" + hexColors[0];
            return ChatColor.of(fallbackColor) + message;
        }

        return gradientMessage.toString();
    }

    public static Color interpolateMultipleColors(Color[] colors, float position) {
        if (colors.length == 1) return colors[0];
        if (position <= 0) return colors[0];
        if (position >= 1) return colors[colors.length - 1];

        float scaledPosition = position * (colors.length - 1);
        int index = (int) Math.floor(scaledPosition);
        float localPosition = scaledPosition - index;

        if (index >= colors.length - 1) {
            return colors[colors.length - 1];
        }

        Color startColor = colors[index];
        Color endColor = colors[index + 1];

        int red = (int) (startColor.getRed() + localPosition * (endColor.getRed() - startColor.getRed()));
        int green = (int) (startColor.getGreen() + localPosition * (endColor.getGreen() - startColor.getGreen()));
        int blue = (int) (startColor.getBlue() + localPosition * (endColor.getBlue() - startColor.getBlue()));

        return new Color(red, green, blue);
    }

    public static String stripAllColors(String text) {
        if (text == null || text.isEmpty()) return text;

        // Remove BungeeCord hex format (§x§F§F§F§F§F§F)
        text = text.replaceAll("§x(§[0-9A-Fa-f]){6}", "");

        // Remove legacy section codes (§l, §a, etc.)
        text = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");

        // Remove hex codes (&#RRGGBB)
        text = text.replaceAll("&#[0-9A-Fa-f]{6}", "");

        // Remove legacy & codes (&l, &a, etc.)
        text = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");

        // Remove any remaining § symbols
        text = text.replaceAll("§", "");

        return text.trim();
    }
}
