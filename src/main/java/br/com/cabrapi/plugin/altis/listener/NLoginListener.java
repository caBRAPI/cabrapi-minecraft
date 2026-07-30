package br.com.cabrapi.plugin.altis.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nickuc.login.api.event.bukkit.auth.LoginEvent;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;

/**
 * Listener que escuta eventos de autenticação do plugin nLogin.
 * <p>
 * Responsável por acionar o processamento de pagamentos pendentes
 * assim que o jogador completa o login via nLogin. Este listener
 * só é registrado caso o plugin nLogin esteja presente no servidor.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class NLoginListener implements Listener {

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Cria uma nova instância do listener do nLogin.
     *
     * @param plugin Instância principal do plugin.
     */
    public NLoginListener(ActivatorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Processa o evento de login do nLogin e aciona a ativação
     * de pagamentos pendentes para o jogador.
     *
     * @param event Evento de autenticação do nLogin.
     */
    @EventHandler
    public void onLogin(LoginEvent event) {
        plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageNloginEventReceived()
                .replace("{player}", event.getPlayer().getName()));
        plugin.getActivationManager().processPlayer(event.getPlayer());
    }

    /**
     * Registra este listener no servidor Bukkit.
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}