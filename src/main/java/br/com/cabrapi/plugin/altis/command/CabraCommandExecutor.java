package br.com.cabrapi.plugin.altis.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import br.com.cabrapi.plugin.altis.util.ColorUtil;

/**
 * Executor do comando {@code /cabrapi} do plugin.
 * <p>
 * Responsável por processar os subcomandos disponíveis, como
 * {@code reload} para recarregar a configuração do plugin.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class CabraCommandExecutor implements CommandExecutor {

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Cria uma nova instância do executor de comandos.
     *
     * @param plugin Instância principal do plugin.
     */
    public CabraCommandExecutor(ActivatorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Processa a execução do comando {@code /cabrapi}.
     *
     * @param sender Origem do comando.
     * @param command Comando executado.
     * @param label Rótulo utilizado para invocar o comando.
     * @param args Argumentos adicionais fornecidos.
     * @return {@code true} se o comando foi processado com sucesso.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            return false;
        }

        if (!sender.hasPermission("cabrapi.admin")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPluginConfig().prefixed(plugin.getPluginConfig().getMessagePermission())));
            return true;
        }

        plugin.reload();

        sender.sendMessage(ColorUtil.colorize(plugin.getPluginConfig().prefixed(plugin.getPluginConfig().getMessageReload())));
        plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageReloadBy()
                .replace("{player}", sender.getName()));

        return true;
    }
}
