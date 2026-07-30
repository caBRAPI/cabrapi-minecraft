package br.com.cabrapi.plugin.altis.services;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

/**
 * Serviço responsável pelo envio de notificações para webhooks.
 * <p>
 * Atualmente, o serviço suporta o envio de mensagens para webhooks do
 * Discord utilizando a biblioteca {@code discord-webhooks}. O envio é
 * realizado de forma assíncrona para evitar bloqueios na thread principal
 * do servidor.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class WebhookService {

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Cria uma nova instância do serviço de webhooks.
     *
     * @param plugin Instância principal do plugin.
     */
    public WebhookService(ActivatorPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Envia uma notificação para o webhook configurado.
     * <p>
     * A mensagem pode utilizar os seguintes placeholders:
     * <ul>
     *     <li>{@code {player}} - Nome do jogador.</li>
     *     <li>{@code {products}} - Lista de produtos entregues.</li>
     * </ul>
     * O envio ocorre de forma assíncrona.
     *
     * @param player Jogador que recebeu os produtos.
     * @param products Lista de produtos entregues.
     */
    public void sendWebhook(Player player, List<Map<String, Object>> products) {
        sendWebhookByName(player.getName(), products);
    }

    /**
     * Envia uma notificação para o webhook configurado (modo console).
     * <p>
     * Utilizado quando o pagamento é processado sem um jogador online.
     *
     * @param playerName Nome do jogador.
     * @param products Lista de produtos entregues.
     */
    public void sendWebhookConsole(String playerName, List<Map<String, Object>> products) {
        sendWebhookByName(playerName, products);
    }

    /**
     * Envia uma notificação para o webhook configurado.
     *
     * @param playerName Nome do jogador.
     * @param products Lista de produtos entregues.
     */
    private void sendWebhookByName(String playerName, List<Map<String, Object>> products) {
        if (!plugin.getPluginConfig().isWebhookEnabled()) {
            return;
        }

        String webhookUrl = plugin.getPluginConfig().getWebhookUrl();
        String webhookMessage = plugin.getPluginConfig().getWebhookMessage();

        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {

                StringBuilder productList = new StringBuilder();

                for (int i = 0; i < products.size(); i++) {

                    if (i > 0) {
                        productList.append(", ");
                    }

                    Map<String, Object> product = products.get(i);

                    Object quantity = product.get("quantity");
                    Object name = product.get("name");

                    if (quantity instanceof Number
                            && ((Number) quantity).intValue() > 1) {

                        productList.append(((Number) quantity).intValue())
                                .append("x ");
                    }

                    productList.append(name != null
                            ? name
                            : "Produto #" + (i + 1));
                }

                String message = webhookMessage
                        .replace("{player}", playerName)
                        .replace("{products}", productList.toString());

                WebhookClient client = WebhookClient.withUrl(webhookUrl);

                WebhookMessageBuilder builder = new WebhookMessageBuilder()
                        .setContent(message);

                client.send(builder.build());
                client.close();

                plugin.getPluginLogger().info(
                        plugin.getPluginConfig().getMessageWebhookSent()
                                .replace("{player}", playerName)
                );

            } catch (Exception exception) {

                plugin.getPluginLogger().warning(
                        plugin.getPluginConfig().getMessageWebhookFailed()
                                .replace("{error}", exception.getMessage())
                );
            }
        });
    }
}