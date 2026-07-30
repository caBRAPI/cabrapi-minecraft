package br.com.cabrapi.plugin.altis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import br.com.cabrapi.sdk.caBRAPI;
import br.com.cabrapi.sdk.client.HttpClient;
import br.com.cabrapi.sdk.util.Json;

/**
 * Serviço responsável por consultar detalhes de produtos na API.
 * <p>
 * Oferece funcionalidades para buscar informações de um produto,
 * extrair comandos e mensagens configurados no metadado {@code minecraft}
 * e verificar se a verificação de inventário está habilitada.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class ProductService {

    /**
     * Instância principal do plugin.
     */
    private final ActivatorPlugin plugin;

    /**
     * Instância da SDK da caBRAPI.
     */
    private final caBRAPI api;

    /**
     * Identificador da loja.
     */
    private final String storeId;

    /**
     * Cria uma nova instância do serviço de produtos.
     *
     * @param plugin Instância principal do plugin.
     * @param api Instância da SDK da caBRAPI.
     * @param storeId Identificador da loja.
     */
    public ProductService(ActivatorPlugin plugin, caBRAPI api, String storeId) {
        this.plugin = plugin;
        this.api = api;
        this.storeId = storeId;
    }

    /**
     * Busca os detalhes de um produto pelo seu identificador.
     *
     * @param productId Identificador do produto.
     * @return Mapa com os dados do produto ou {@code null} caso não seja encontrado.
     */
    public Map<String, Object> fetchProductDetail(String productId) {
        try {
            HttpClient.HttpResponse response = api.products().getById(storeId, productId);
            if (response.getStatus() == 200) {
                return Json.fromJson(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            plugin.getPluginLogger().warning(plugin.getPluginConfig().getMessageProductFetchError()
                    .replace("{error}", e.getMessage()));
        }
        return null;
    }

    /**
     * Retorna a lista de comandos configurados no metadado {@code minecraft}
     * do produto.
     *
     * @param product Mapa com os dados do produto.
     * @return Lista de comandos ou {@code null} caso não haja comandos configurados.
     */
    public List<String> getProductCommands(Map<String, Object> product) {
        Object metaObj = product.get("metadata");
        if (!(metaObj instanceof Map)) return null;

        Object minecraftObj = ((Map<?, ?>) metaObj).get("minecraft");
        if (!(minecraftObj instanceof Map)) return null;

        Object cmdObj = ((Map<?, ?>) minecraftObj).get("command");
        if (!(cmdObj instanceof List)) return null;

        List<String> commands = new ArrayList<>();
        for (Object cmd : (List<?>) cmdObj) {
            if (cmd != null) commands.add(cmd.toString());
        }
        return commands.isEmpty() ? null : commands;
    }

    /**
     * Retorna a mensagem configurada no metadado {@code minecraft} do produto.
     *
     * @param product Mapa com os dados do produto.
     * @return Mensagem configurada ou {@code null} caso não haja mensagem.
     */
    public String getProductMessage(Map<String, Object> product) {
        Object metaObj = product.get("metadata");
        if (!(metaObj instanceof Map)) return null;

        Object minecraftObj = ((Map<?, ?>) metaObj).get("minecraft");
        if (!(minecraftObj instanceof Map)) return null;

        Object msgObj = ((Map<?, ?>) minecraftObj).get("message");
        return msgObj != null ? msgObj.toString() : null;
    }

    /**
     * Retorna o modo de execução configurado no metadado {@code minecraft}.
     * <p>
     * O campo {@code run} define quem executa o comando:
     * <ul>
     *     <li>{@code console} - Executa como console (ignora inventário).</li>
     *     <li>{@code player} - Executa como o jogador (verifica inventário).</li>
     * </ul>
     *
     * @param product Mapa com os dados do produto.
     * @return Modo de execução ou {@code "console"} como padrão.
     */
    public String getProductRunMode(Map<String, Object> product) {
        Object metaObj = product.get("metadata");
        if (!(metaObj instanceof Map)) return "console";

        Object minecraftObj = ((Map<?, ?>) metaObj).get("minecraft");
        if (!(minecraftObj instanceof Map)) return "console";

        Object runObj = ((Map<?, ?>) minecraftObj).get("run");
        if (runObj == null) return "console";

        String mode = runObj.toString().toLowerCase();
        return mode.equals("player") ? "player" : "console";
    }

    /**
     * Verifica se a verificação de inventário está habilitada para o produto.
     * <p>
     * O campo é lido de {@code metadata.minecraft.inventory.enable} e aceita
     * valores booleano, numérico (1 para true) ou string ("true"/"false").
     *
     * @param product Mapa com os dados do produto.
     * @return {@code true} caso a verificação de inventário esteja habilitada.
     */
    public boolean shouldCheckInventory(Map<String, Object> product) {
        Object metaObj = product.get("metadata");
        if (!(metaObj instanceof Map)) return false;

        Object minecraftObj = ((Map<?, ?>) metaObj).get("minecraft");
        if (!(minecraftObj instanceof Map)) return false;

        Object invObj = ((Map<?, ?>) minecraftObj).get("inventory");
        if (!(invObj instanceof Map)) return false;

        Object enableObj = ((Map<?, ?>) invObj).get("enable");

        if (enableObj instanceof Boolean) return (Boolean) enableObj;
        if (enableObj instanceof Number) return ((Number) enableObj).intValue() == 1;
        if (enableObj instanceof String) return Boolean.parseBoolean((String) enableObj);
        return false;
    }

    /**
     * Retorna a quantidade mínima de slots vazios necessários no inventário.
     * <p>
     * O campo é lido de {@code metadata.minecraft.inventory.slot}.
     * Valores abaixo de 1 são ajustados para 1 (mínimo).
     * Valores acima de 36 são ajustados para 36 (máximo do inventário do jogador).
     *
     * @param product Mapa com os dados do produto.
     * @return Quantidade mínima de slots vazios (entre 1 e 36).
     */
    public int getInventorySlot(Map<String, Object> product) {
        Object metaObj = product.get("metadata");
        if (!(metaObj instanceof Map)) return 1;

        Object minecraftObj = ((Map<?, ?>) metaObj).get("minecraft");
        if (!(minecraftObj instanceof Map)) return 1;

        Object invObj = ((Map<?, ?>) minecraftObj).get("inventory");
        if (!(invObj instanceof Map)) return 1;

        Object slotObj = ((Map<?, ?>) invObj).get("slot");
        if (slotObj == null) return 1;

        int slot = 1;
        if (slotObj instanceof Number) {
            slot = ((Number) slotObj).intValue();
        } else {
            try {
                slot = Integer.parseInt(slotObj.toString());
            } catch (NumberFormatException e) {
                return 1;
            }
        }

        if (slot < 1) return 1;
        if (slot > 36) return 36;
        return slot;
    }
}
