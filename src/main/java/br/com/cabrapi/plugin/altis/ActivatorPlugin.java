package br.com.cabrapi.plugin.altis;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import br.com.cabrapi.plugin.altis.command.CabraCommandExecutor;
import br.com.cabrapi.plugin.altis.config.PluginConfig;
import br.com.cabrapi.plugin.altis.listener.LoginListener;
import br.com.cabrapi.plugin.altis.listener.NLoginListener;
import br.com.cabrapi.plugin.altis.manager.ActivationManager;
import br.com.cabrapi.plugin.altis.util.Logger;
import br.com.cabrapi.plugin.altis.util.VersionChecker;
import br.com.cabrapi.sdk.caBRAPI;
import br.com.cabrapi.sdk.client.CoreClient;

/**
 * Classe principal do plugin Altis.
 * <p>
 * Responsável por gerenciar todo o ciclo de vida do plugin, incluindo
 * carregamento das configurações, inicialização da SDK da caBRAPI,
 * registro de comandos, listeners, métricas e verificação de atualizações.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class ActivatorPlugin extends JavaPlugin {

    /**
     * Versão atual esperada do config.yml.
     */
    private static final int CONFIG_VERSION = 1;

    /**
     * Banner exibido durante a inicialização do plugin.
     */
    private static final String[] BANNER = {
            "----------------------",
            "          /\\          ",
            "         /  \\         ",
            "        / /\\ \\        ",
            "       / /  \\ \\       ",
            "      / / /\\ \\ \\      ",
            "     /_/ /  \\ \\_\\     ",
            "   ALTIS by caBRAPI   ",
            "----------------------"
    };

    /**
     * Configuração principal do plugin.
     */
    private PluginConfig pluginConfig;

    /**
     * Logger personalizado utilizado pelo plugin.
     */
    private Logger logger;

    /**
     * Instância da SDK da caBRAPI.
     */
    private caBRAPI api;

    /**
     * Gerenciador responsável pela ativação dos produtos.
     */
    private ActivationManager activationManager;

    /**
     * Inicializa o plugin.
     * <p>
     * Durante a inicialização são executadas as seguintes etapas:
     * <ul>
     *     <li>Inicialização do bStats.</li>
     *     <li>Carregamento da configuração.</li>
     *     <li>Exibição do banner.</li>
     *     <li>Verificação de atualizações (opcional).</li>
     *     <li>Validação das credenciais da API.</li>
     *     <li>Inicialização da SDK da caBRAPI.</li>
     *     <li>Registro dos listeners.</li>
     *     <li>Registro dos comandos.</li>
     * </ul>
     */
    @Override
    public void onEnable() {

        new Metrics(this, 32403);

        saveDefaultConfig();
        reloadConfig();

        pluginConfig = new PluginConfig(this);
        logger = new Logger(this);

        printBanner();

        if (pluginConfig.isCheckUpdate()) {
            new VersionChecker(this, logger).check();
        }

         if (pluginConfig.getConfigVersion() < CONFIG_VERSION) {
            logger.warning(pluginConfig.getMessageConfigOutdated()
                    .replace("{current}", String.valueOf(pluginConfig.getConfigVersion()))
                    .replace("{latest}", String.valueOf(CONFIG_VERSION)));
        } else if (pluginConfig.getConfigVersion() > CONFIG_VERSION) {
            logger.warning(pluginConfig.getMessageConfigNewer()
                    .replace("{current}", String.valueOf(pluginConfig.getConfigVersion())));
        }

        String apiKeyStatus = hasValue(pluginConfig.getApiKey())
                ? pluginConfig.getMessageApiKeyConfigured()
                : pluginConfig.getMessageApiKeyNotConfigured();

        String storeIdStatus = hasValue(pluginConfig.getStoreId())
                ? pluginConfig.getMessageStoreIdConfigured()
                : pluginConfig.getMessageStoreIdNotConfigured();

        logger.info("&fAPI Key: " + apiKeyStatus);
        logger.info("&fStore ID: " + storeIdStatus);

        if (!pluginConfig.hasValidCredentials()) {
            logger.severe(pluginConfig.getMessageConfigureCredentials());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeApi();

        boolean nLoginPresent = Bukkit.getPluginManager().getPlugin("nLogin") != null;

        if (nLoginPresent) {
            logger.info(pluginConfig.getMessageNloginDetected());
            new NLoginListener(this).register();
        } else {
            logger.info(pluginConfig.getMessageNloginNotFound());
            getServer().getPluginManager().registerEvents(
                    new LoginListener(this),
                    this
            );
        }

        getCommand("cabrapi").setExecutor(new CabraCommandExecutor(this));

        startPeriodicCheck();
    }

    /**
     * Inicia a verificação periódica de pagamentos pendentes (modo console).
     */
    private void startPeriodicCheck() {
        double interval = pluginConfig.getCheckInterval();
        if (interval <= 0) return;

        long ticks = (long) (interval * 60.0 * 20.0);
        if (ticks < 1) ticks = 1;

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (activationManager != null) {
                activationManager.processConsole();
            }
        }, ticks, ticks);
    }

    /**
     * Executado quando o plugin é desabilitado.
     * <p>
     * Atualmente não existe nenhuma rotina necessária durante o desligamento.
     */
    @Override
    public void onDisable() {

    }

    /**
     * Recarrega a configuração do plugin e reinicializa
     * a conexão com a SDK da caBRAPI.
     */
    public void reload() {
        pluginConfig.reload();
        initializeApi();
    }

    /**
     * Exibe o banner de inicialização no console.
     */
    private void printBanner() {
        logger.info("&8)&r &7--------------------");

        for (String line : BANNER) {
            logger.info(line);
        }

        logger.info("&8)&r &7--------------------");
    }

    /**
     * Inicializa a SDK da caBRAPI utilizando as credenciais
     * definidas na configuração do plugin.
     */
    private void initializeApi() {

        CoreClient.Options options = new CoreClient.Options(CoreClient.Mode.PRIVATE)
                .apiKey(pluginConfig.getApiKey());

        api = new caBRAPI(options);

        activationManager = new ActivationManager(
                this,
                api,
                pluginConfig.getStoreId()
        );
    }

    /**
     * Verifica se um valor foi informado.
     *
     * @param value Valor a ser verificado.
     * @return {@code true} caso o valor não seja nulo e não esteja vazio;
     *         caso contrário, {@code false}.
     */
    private boolean hasValue(String value) {
        return value != null && !value.isEmpty();
    }

    /**
     * Retorna a configuração principal do plugin.
     *
     * @return Configuração do plugin.
     */
    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    /**
     * Retorna o gerenciador de ativações.
     *
     * @return Gerenciador de ativações.
     */
    public ActivationManager getActivationManager() {
        return activationManager;
    }

    /**
     * Retorna o logger personalizado do plugin.
     *
     * @return Logger do plugin.
     */
    public Logger getPluginLogger() {
        return logger;
    }
}