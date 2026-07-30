package br.com.cabrapi.plugin.altis.listener;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener que escuta o evento de entrada de jogadores no servidor.
 * <p>
 * Responsável por acionar o processamento de pagamentos pendentes do jogador
 * que acabou de entrar. Este listener só é registrado quando o plugin nLogin
 * não está presente no servidor.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class LoginListener implements Listener {

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Cria uma nova instância do listener de login.
     *
     * @param plugin Instância principal do plugin.
     */
    public LoginListener(ActivatorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Processa o evento de entrada do jogador.
     * <p>
     * O processamento é adiado em 2 segundos (40 ticks) para garantir
     * que o jogador esteja totalmente conectado.
     *
     * @param event Evento de entrada do jogador.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getActivationManager().processPlayer(player);
            }
        }, 40L);
    }
}
