package br.com.cabrapi.plugin.altis.config;

import org.bukkit.configuration.file.FileConfiguration;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;

/**
 * Classe responsável por carregar e gerenciar todas as configurações do plugin.
 * <p>
 * As configurações são lidas a partir do arquivo {@code config.yml} e incluem
 * credenciais da API, opções de webhook, mensagens personalizadas e
 * preferências gerais do plugin.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class PluginConfig {

    /**
     * Prefixo padrão exibido nas mensagens do plugin.
     */
    private static final String LOG_PREFIX = "[altis] ";

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Versão da configuração carregada.
     */
    private int configVersion;

    /**
     * Chave de acesso à API da caBRAPI.
     */
    private String apiKey;

    /**
     * Identificador da loja na caBRAPI.
     */
    private String storeId;

    /**
     * Indica se a verificação automática de atualizações está habilitada.
     */
    private boolean checkUpdate;

    /**
     * Indica se o modo de depuração está habilitado.
     */
    private boolean debug;

    /**
     * Intervalo em minutos para verificar pagamentos pendentes.
     */
    private double checkInterval;

    /**
     * Indica se o envio de webhooks está habilitado.
     */
    private boolean webhookEnabled;

    /**
     * URL do webhook configurado.
     */
    private String webhookUrl;

    /**
     * Mensagem formatada enviada ao webhook.
     */
    private String webhookMessage;

    // ========================================
    // Mensagens para o Jogador
    // ========================================

    private String messagePermission;
    private String messageInventory;
    private String messageReload;
    private String messageActivationSuccess;

    // ========================================
    // Status do Startup
    // ========================================

    private String messageApiKeyConfigured;
    private String messageApiKeyNotConfigured;
    private String messageStoreIdConfigured;
    private String messageStoreIdNotConfigured;
    private String messageConfigureCredentials;
    private String messageNloginDetected;
    private String messageNloginNotFound;

    // ========================================
    // Console - Ativação
    // ========================================

    private String messageActivationLog;
    private String messageReloadBy;

    // ========================================
    // Console - Serviços
    // ========================================

    private String messageNloginEventReceived;
    private String messageNoPendingPayments;
    private String messageApiRequestError;
    private String messagePaymentUpdateFailed;
    private String messagePaymentUpdateError;
    private String messageMetadataFetchError;
    private String messageProductFetchError;
    private String messageCommandExecuted;
    private String messageWebhookSent;
    private String messageWebhookFailed;

    // ========================================
    // Console - Verificador de Versão
    // ========================================

    private String messageUpdateCheckFailed;
    private String messageUpdateCheckInvalid;
    private String messageUpdateAvailable;
    private String messageUpdateCurrent;
    private String messageUpdateDownload;
    private String messageUpdateCheckError;

    // ========================================
    // Console - Versão do Config
    // ========================================

    private String messageConfigOutdated;
    private String messageConfigNewer;

    /**
     * Cria uma nova instância da configuração e carrega os valores do arquivo.
     *
     * @param plugin Instância principal do plugin.
     */
    public PluginConfig(ActivatorPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    /**
     * Carrega todas as configurações a partir do arquivo {@code config.yml}.
     */
    public void load() {
        FileConfiguration config = plugin.getConfig();

        configVersion = config.getInt("version", 1);
        apiKey = config.getString("api-key", "");
        storeId = config.getString("store-id", "");
        checkUpdate = config.getBoolean("check-update", true);
        checkInterval = config.getDouble("check-interval", 5);
        debug = config.getBoolean("debug", false);

        webhookEnabled = config.getBoolean("webhook.enable", false);
        webhookUrl = config.getString("webhook.url", "");
        webhookMessage = config.getString("webhook.message", "");

        // Mensagens para o Jogador
        messagePermission = config.getString("message.permission",
                "&cVocê não possui permissão.");
        messageInventory = config.getString("message.inventory",
                "&eSeu inventário está cheio! Precisa de pelo menos {slots} slot(s) vazio(s) para receber seus itens.");
        messageReload = config.getString("message.reload",
                "&aConfiguração recarregada com sucesso!");
        messageActivationSuccess = config.getString("message.activation-success",
                "&a{count} pagamento(s) ativado(s) com sucesso!");

        // Status do Startup
        messageApiKeyConfigured = config.getString("message.api-key-configured",
                "&aConfigurada");
        messageApiKeyNotConfigured = config.getString("message.api-key-not-configured",
                "&cNão configurada");
        messageStoreIdConfigured = config.getString("message.store-id-configured",
                "&aConfigurada");
        messageStoreIdNotConfigured = config.getString("message.store-id-not-configured",
                "&cNão configurada");
        messageConfigureCredentials = config.getString("message.configure-credentials",
                "&cConfigure api-key e store-id no arquivo config.yml.");
        messageNloginDetected = config.getString("message.nlogin-detected",
                "&fnLogin: &aDetectado");
        messageNloginNotFound = config.getString("message.nlogin-not-found",
                "&fnLogin: &eNão encontrado &8- &7utilizando PlayerJoinEvent.");

        // Console - Ativação
        messageActivationLog = config.getString("message.activation-log",
                "&aAtivado(s) &f{count}/{total} &apagamento(s) para &f{player}");
        messageReloadBy = config.getString("message.reload-by",
                "&aConfiguração recarregada por {player}");

        // Console - Serviços
        messageNloginEventReceived = config.getString("message.nlogin-event-received",
                "&aNLogin event recebido para &f{player}");
        messageNoPendingPayments = config.getString("message.no-pending-payments",
                "&eNenhum pagamento pendente para &f{player}");
        messageApiRequestError = config.getString("message.api-request-error",
                "API retornou {status} para {player}");
        messagePaymentUpdateFailed = config.getString("message.payment-update-failed",
                "Falha ao atualizar pagamento {paymentId}: HTTP {status}");
        messagePaymentUpdateError = config.getString("message.payment-update-error",
                "Erro ao atualizar pagamento {paymentId}: {error}");
        messageMetadataFetchError = config.getString("message.metadata-fetch-error",
                "Erro ao buscar metadata do pagamento {paymentId}: {error}");
        messageProductFetchError = config.getString("message.product-fetch-error",
                "Erro ao buscar produto {error}");
        messageCommandExecuted = config.getString("message.command-executed",
                "Executando: {command} (x{quantity}) para {player} via {executor}");
        messageWebhookSent = config.getString("message.webhook-sent",
                "Webhook notificação enviada ao Discord: {player}");
        messageWebhookFailed = config.getString("message.webhook-failed",
                "Falha ao enviar webhook: {error}");

        // Console - Verificador de Versão
        messageUpdateCheckFailed = config.getString("message.update-check-failed",
                "Não foi possível verificar atualizações (HTTP {status}).");
        messageUpdateCheckInvalid = config.getString("message.update-check-invalid",
                "Não foi possível verificar atualizações. Resposta inválida.");
        messageUpdateAvailable = config.getString("message.update-available",
                "Uma nova versão está disponível! &e(v{current} → v{latest})");
        messageUpdateCurrent = config.getString("message.update-current",
                "Você está utilizando a versão mais recente. &a(v{version})");
        messageUpdateDownload = config.getString("message.update-download",
                "Download: &ehttps://github.com/caBRAPI/cabrapi-minecraft/releases");
        messageUpdateCheckError = config.getString("message.update-check-error",
                "Não foi possível verificar atualizações. &7{error}");

        // Console - Versão do Config
        messageConfigOutdated = config.getString("message.config-outdated",
                "&eSeu config.yml está desatualizado (v{current}). Recomenda-se atualizá-lo para a versão v{latest}.");
        messageConfigNewer = config.getString("message.config-newer",
                "&eSeu config.yml parece ser de uma versão mais recente (v{current}). Caso haja problemas, verifique as configurações.");
    }

    /**
     * Recarrega as configurações do arquivo {@code config.yml}.
     */
    public void reload() {
        plugin.reloadConfig();
        load();
    }

    /**
     * Retorna a versão da configuração.
     *
     * @return Versão da configuração.
     */
    public int getConfigVersion() {
        return configVersion;
    }

    /**
     * Retorna a chave de acesso à API.
     *
     * @return Chave da API.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Retorna o identificador da loja.
     *
     * @return Identificador da loja.
     */
    public String getStoreId() {
        return storeId;
    }

    /**
     * Verifica se a verificação automática de atualizações está habilitada.
     *
     * @return {@code true} caso a verificação esteja habilitada.
     */
    public boolean isCheckUpdate() {
        return checkUpdate;
    }

    /**
     * Verifica se o modo de depuração está habilitado.
     *
     * @return {@code true} caso o modo debug esteja habilitado.
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Retorna o intervalo em minutos para verificação de pagamentos pendentes.
     *
     * @return Intervalo em minutos.
     */
    public double getCheckInterval() {
        return checkInterval;
    }

    /**
     * Verifica se o envio de webhooks está habilitado.
     *
     * @return {@code true} caso os webhooks estejam habilitados.
     */
    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    /**
     * Retorna a URL do webhook configurado.
     *
     * @return URL do webhook.
     */
    public String getWebhookUrl() {
        return webhookUrl;
    }

    /**
     * Retorna a mensagem formatada enviada ao webhook.
     *
     * @return Mensagem do webhook.
     */
    public String getWebhookMessage() {
        return webhookMessage;
    }

    // ========================================
    // Mensagens para o Jogador
    // ========================================

    public String getMessagePermission() {
        return messagePermission;
    }

    public String getMessageInventory() {
        return messageInventory;
    }

    public String getMessageReload() {
        return messageReload;
    }

    public String getMessageActivationSuccess() {
        return messageActivationSuccess;
    }

    // ========================================
    // Status do Startup
    // ========================================

    public String getMessageApiKeyConfigured() {
        return messageApiKeyConfigured;
    }

    public String getMessageApiKeyNotConfigured() {
        return messageApiKeyNotConfigured;
    }

    public String getMessageStoreIdConfigured() {
        return messageStoreIdConfigured;
    }

    public String getMessageStoreIdNotConfigured() {
        return messageStoreIdNotConfigured;
    }

    public String getMessageConfigureCredentials() {
        return messageConfigureCredentials;
    }

    public String getMessageNloginDetected() {
        return messageNloginDetected;
    }

    public String getMessageNloginNotFound() {
        return messageNloginNotFound;
    }

    // ========================================
    // Console - Ativação
    // ========================================

    public String getMessageActivationLog() {
        return messageActivationLog;
    }

    public String getMessageReloadBy() {
        return messageReloadBy;
    }

    // ========================================
    // Console - Serviços
    // ========================================

    public String getMessageNloginEventReceived() {
        return messageNloginEventReceived;
    }

    public String getMessageNoPendingPayments() {
        return messageNoPendingPayments;
    }

    public String getMessageApiRequestError() {
        return messageApiRequestError;
    }

    public String getMessagePaymentUpdateFailed() {
        return messagePaymentUpdateFailed;
    }

    public String getMessagePaymentUpdateError() {
        return messagePaymentUpdateError;
    }

    public String getMessageMetadataFetchError() {
        return messageMetadataFetchError;
    }

    public String getMessageProductFetchError() {
        return messageProductFetchError;
    }

    public String getMessageCommandExecuted() {
        return messageCommandExecuted;
    }

    public String getMessageWebhookSent() {
        return messageWebhookSent;
    }

    public String getMessageWebhookFailed() {
        return messageWebhookFailed;
    }

    // ========================================
    // Console - Verificador de Versão
    // ========================================

    public String getMessageUpdateCheckFailed() {
        return messageUpdateCheckFailed;
    }

    public String getMessageUpdateCheckInvalid() {
        return messageUpdateCheckInvalid;
    }

    public String getMessageUpdateAvailable() {
        return messageUpdateAvailable;
    }

    public String getMessageUpdateCurrent() {
        return messageUpdateCurrent;
    }

    public String getMessageUpdateDownload() {
        return messageUpdateDownload;
    }

    public String getMessageUpdateCheckError() {
        return messageUpdateCheckError;
    }

    // ========================================
    // Console - Versão do Config
    // ========================================

    public String getMessageConfigOutdated() {
        return messageConfigOutdated;
    }

    public String getMessageConfigNewer() {
        return messageConfigNewer;
    }

    // ========================================
    // Utilitários
    // ========================================

    /**
     * Retorna o prefixo padrão das mensagens do plugin.
     *
     * @return Prefixo das mensagens.
     */
    public String getLogPrefix() {
        return LOG_PREFIX;
    }

    /**
     * Adiciona o prefixo padrão à mensagem informada.
     *
     * @param message Mensagem a ser prefixada.
     * @return Mensagem com o prefixo aplicado.
     */
    public String prefixed(String message) {
        return LOG_PREFIX + message;
    }

    /**
     * Verifica se as credenciais da API estão configuradas.
     *
     * @return {@code true} caso a API key e o store ID estejam preenchidos.
     */
    public boolean hasValidCredentials() {
        return apiKey != null && !apiKey.isEmpty() && storeId != null && !storeId.isEmpty();
    }
}
