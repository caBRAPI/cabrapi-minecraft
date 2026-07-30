package br.com.cabrapi.plugin.altis.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import br.com.cabrapi.sdk.util.Json;

/**
 * Responsável por verificar se existe uma nova versão do plugin disponível.
 * <p>
 * A verificação é realizada consultando um arquivo JSON hospedado no GitHub.
 * Caso uma versão mais recente seja encontrada, uma mensagem será exibida no
 * console informando o administrador.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public final class VersionChecker {

    /**
     * URL do arquivo JSON contendo a versão mais recente.
     */
    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/caBRAPI/cabrapi-minecraft/main/.github/version.json";

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Logger utilizado para enviar mensagens ao console.
     */
    private final Logger logger;

    /**
     * Cria uma nova instância do verificador de versões.
     *
     * @param plugin Instância principal do plugin.
     * @param logger Logger utilizado para registrar mensagens.
     */
    public VersionChecker(ActivatorPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    /**
     * Verifica se existe uma nova versão disponível.
     * <p>
     * Caso a versão instalada seja a mais recente, uma mensagem informativa será
     * enviada ao console. Caso contrário, será exibido um aviso contendo a
     * versão disponível e o link para download.
     */
    @SuppressWarnings("unchecked")
    public void check() {
        final String currentVersion = plugin.getDescription().getVersion();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(VERSION_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.warning(plugin.getPluginConfig().getMessageUpdateCheckFailed()
                        .replace("{status}", String.valueOf(connection.getResponseCode())));
                return;
            }

            StringBuilder response = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            Map<String, Object> json = Json.fromJson(response.toString(), Map.class);

            if (json == null) {
                logger.warning(plugin.getPluginConfig().getMessageUpdateCheckInvalid());
                return;
            }

            String latestVersion = (String) json.get("version");

            if (latestVersion == null) {
                logger.warning(plugin.getPluginConfig().getMessageUpdateCheckInvalid());
                return;
            }

            latestVersion = latestVersion.trim();

            if (latestVersion.isEmpty()) {
                logger.warning(plugin.getPluginConfig().getMessageUpdateCheckInvalid());
                return;
            }

            if (currentVersion.equals(latestVersion)) {
                logger.info(plugin.getPluginConfig().getMessageUpdateCurrent()
                        .replace("{version}", currentVersion));
            } else {
                logger.info(plugin.getPluginConfig().getMessageUpdateAvailable()
                        .replace("{current}", currentVersion)
                        .replace("{latest}", latestVersion));
                logger.info(plugin.getPluginConfig().getMessageUpdateDownload());
            }

        } catch (Exception exception) {
            logger.warning(plugin.getPluginConfig().getMessageUpdateCheckError()
                    .replace("{error}", exception.getMessage()));
        }
    }
}
