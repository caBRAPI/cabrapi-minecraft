package br.com.cabrapi.plugin.altis.util;

import org.bukkit.ChatColor;

/**
 * Classe utilitária para manipulação de cores em mensagens do Minecraft.
 * <p>
 * Oferece suporte a:
 * <ul>
 *     <li>Códigos de cor utilizando '&'.</li>
 *     <li>Cores HEX no formato {@code &#RRGGBB}.</li>
 *     <li>Gradientes utilizando
 *     {@code {gradient:#RRGGBB-#RRGGBB}Texto{/gradient}}.</li>
 *     <li>Conversão para ANSI, permitindo exibição colorida no console.</li>
 * </ul>
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public final class ColorUtil {

    /**
     * Caractere utilizado pelo Minecraft para representar códigos de cor.
     */
    private static final char SECTION = '\u00A7';

    /**
     * Impede a instanciação desta classe utilitária.
     */
    private ColorUtil() {
    }

    /**
     * Aplica cores Minecraft ao texto informado.
     * <p>
     * São processados:
     * <ul>
     *     <li>Códigos '&'.</li>
     *     <li>Cores HEX.</li>
     *     <li>Gradientes.</li>
     * </ul>
     *
     * @param text Texto a ser colorido.
     * @return Texto formatado para utilização no Minecraft.
     */
    public static String colorize(String text) {
        text = translateGradients(text, false);
        text = translateHexColors(text, false);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Aplica cores ANSI ao texto para exibição no console.
     *
     * @param text Texto a ser formatado.
     * @return Texto contendo códigos ANSI.
     */
    public static String colorizeConsole(String text) {
        text = translateGradients(text, true);
        text = translateHexColors(text, true);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Remove todos os códigos de cor de um texto.
     *
     * @param text Texto de origem.
     * @return Texto sem formatação.
     */
    public static String strip(String text) {
        return ChatColor.stripColor(text);
    }

    /**
     * Converte todas as cores HEX encontradas no texto.
     *
     * @param text Texto de origem.
     * @param ansi {@code true} para ANSI; {@code false} para Minecraft.
     * @return Texto convertido.
     */
    private static String translateHexColors(String text, boolean ansi) {
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (i + 6 < chars.length
                    && chars[i] == '&'
                    && chars[i + 1] == '#'
                    && isHexChar(chars[i + 2])
                    && isHexChar(chars[i + 3])
                    && isHexChar(chars[i + 4])
                    && isHexChar(chars[i + 5])
                    && isHexChar(chars[i + 6])) {

                String hex = ""
                        + chars[i + 2]
                        + chars[i + 3]
                        + chars[i + 4]
                        + chars[i + 5]
                        + chars[i + 6];

                result.append(ansi ? toAnsiHex(hex) : toMinecraftHex(hex));
                i += 6;
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }

    /**
     * Traduz todas as marcações de gradiente presentes no texto.
     *
     * @param text Texto de origem.
     * @param ansi {@code true} para ANSI; {@code false} para Minecraft.
     * @return Texto convertido.
     */
    private static String translateGradients(String text, boolean ansi) {

        String pattern = "{gradient:";
        int start = 0;

        while ((start = text.indexOf(pattern, start)) != -1) {

            int colorEnd = text.indexOf("}", start);

            if (colorEnd == -1) {
                break;
            }

            String colorSpec = text.substring(start + pattern.length(), colorEnd);
            String[] colors = colorSpec.split("-");

            if (colors.length < 2) {
                break;
            }

            int textEnd = text.indexOf("{/gradient}", colorEnd + 1);

            if (textEnd == -1) {
                break;
            }

            String content = text.substring(colorEnd + 1, textEnd);

            String gradient = applyGradient(
                    content,
                    colors[0].trim(),
                    colors[1].trim(),
                    ansi
            );

            text = text.substring(0, start)
                    + gradient
                    + text.substring(textEnd + "{/gradient}".length());

            start = 0;
        }

        return text;
    }

    /**
     * Aplica um gradiente entre duas cores HEX.
     *
     * @param text Texto que receberá o gradiente.
     * @param hexFrom Cor inicial.
     * @param hexTo Cor final.
     * @param ansi {@code true} para ANSI; {@code false} para Minecraft.
     * @return Texto com gradiente aplicado.
     */
    private static String applyGradient(String text, String hexFrom, String hexTo, boolean ansi) {

        String stripped = ChatColor.stripColor(text);

        if (stripped.isEmpty()) {
            return text;
        }

        int[] from = parseHex(hexFrom);
        int[] to = parseHex(hexTo);

        int length = stripped.length();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {

            int r = lerp(from[0], to[0], i, length);
            int g = lerp(from[1], to[1], i, length);
            int b = lerp(from[2], to[2], i, length);

            String hex = String.format("%02X%02X%02X", r, g, b);

            result.append(ansi ? toAnsiHex(hex) : toMinecraftHex(hex));
            result.append(stripped.charAt(i));
        }

        return result.toString();
    }

    /**
     * Converte uma cor HEX para o formato utilizado pelo Minecraft.
     *
     * @param hex Cor hexadecimal.
     * @return Cor formatada.
     */
    private static String toMinecraftHex(String hex) {

        StringBuilder builder = new StringBuilder();

        builder.append(SECTION).append('x');

        for (char character : hex.toCharArray()) {
            builder.append(SECTION).append(character);
        }

        return builder.toString();
    }

    /**
     * Converte uma cor HEX para ANSI.
     *
     * @param hex Cor hexadecimal.
     * @return Código ANSI correspondente.
     */
    private static String toAnsiHex(String hex) {

        int rgb = Integer.parseInt(hex, 16);

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return "\033[38;2;" + red + ";" + green + ";" + blue + "m";
    }

    /**
     * Converte uma cor hexadecimal em componentes RGB.
     *
     * @param hex Cor hexadecimal.
     * @return Vetor contendo vermelho, verde e azul.
     */
    private static int[] parseHex(String hex) {

        hex = hex.replace("#", "");

        int rgb = Integer.parseInt(hex, 16);

        return new int[]{
                (rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                rgb & 0xFF
        };
    }

    /**
     * Calcula uma interpolação linear entre dois valores.
     *
     * @param from Valor inicial.
     * @param to Valor final.
     * @param index Índice atual.
     * @param total Total de posições.
     * @return Valor interpolado.
     */
    private static int lerp(int from, int to, int index, int total) {

        if (total <= 1) {
            return from;
        }

        return from + (to - from) * index / (total - 1);
    }

    /**
     * Verifica se um caractere pertence ao conjunto hexadecimal.
     *
     * @param character Caractere a ser verificado.
     * @return {@code true} caso seja um caractere hexadecimal.
     */
    private static boolean isHexChar(char character) {
        return (character >= '0' && character <= '9')
                || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }
}