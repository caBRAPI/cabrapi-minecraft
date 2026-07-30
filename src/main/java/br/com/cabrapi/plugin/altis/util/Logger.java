package br.com.cabrapi.plugin.altis.util;

import org.bukkit.command.ConsoleCommandSender;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;

/**
 * Logger personalizado do plugin Altis.
 * <p>
 * Responsável por enviar mensagens formatadas para o console do servidor,
 * utilizando o prefixo configurado e cores compatíveis com o Minecraft.
 * Também oferece suporte a mensagens de depuração, que são exibidas
 * apenas quando o modo debug está habilitado.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class Logger {

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Console do servidor utilizado para envio das mensagens.
     */
    private final ConsoleCommandSender console;

    /**
     * Cria uma nova instância do logger.
     *
     * @param plugin Instância principal do plugin.
     */
    public Logger(ActivatorPlugin plugin) {
        this.plugin = plugin;
        this.console = plugin.getServer().getConsoleSender();
    }

    /**
     * Envia uma mensagem informativa ao console.
     *
     * @param message Mensagem a ser enviada.
     */
    public void info(String message) {
        log(message);
    }

    /**
     * Envia uma mensagem de aviso ao console.
     *
     * @param message Mensagem a ser enviada.
     */
    public void warning(String message) {
        log(message);
    }

    /**
     * Envia uma mensagem de erro ao console.
     *
     * @param message Mensagem a ser enviada.
     */
    public void severe(String message) {
        log(message);
    }

    /**
     * Envia uma mensagem de depuração ao console.
     * <p>
     * A mensagem será exibida apenas se a opção
     * {@code debug} estiver habilitada na configuração.
     *
     * @param message Mensagem de depuração.
     */
    public void debug(String message) {
        if (!plugin.getPluginConfig().isDebug()) {
            return;
        }

        log("&8[DEBUG] &7" + message);
    }

    /**
     * Envia uma mensagem formatada ao console.
     *
     * @param message Conteúdo da mensagem.
     */
    private void log(String message) {
        String prefix = ColorUtil.colorize(plugin.getPluginConfig().getLogPrefix());
        String text = ColorUtil.colorize(message);

        console.sendMessage(prefix + text);
    }
}