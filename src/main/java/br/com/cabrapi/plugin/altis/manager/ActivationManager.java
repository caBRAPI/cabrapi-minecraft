package br.com.cabrapi.plugin.altis.manager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import br.com.cabrapi.plugin.altis.services.PaymentService;
import br.com.cabrapi.plugin.altis.services.PaymentService.FilterResult;
import br.com.cabrapi.plugin.altis.services.ProductService;
import br.com.cabrapi.plugin.altis.services.WebhookService;
import br.com.cabrapi.plugin.altis.util.ColorUtil;
import br.com.cabrapi.sdk.caBRAPI;
import br.com.cabrapi.sdk.model.payment.Payment;

/**
 * Gerenciador responsável por coordenar o fluxo de ativação de pagamentos.
 * <p>
 * Orquestra a busca de pagamentos pendentes, a validação de estoque,
 * a entrega de produtos ao jogador, a atualização do status de entrega
 * na API e o envio de notificações via webhook.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class ActivationManager {

    private final ActivatorPlugin plugin;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final WebhookService webhookService;
    private final Set<String> processedPayments = ConcurrentHashMap.newKeySet();
    private final File processedFile;
    private FileConfiguration processedConfig;

    public ActivationManager(ActivatorPlugin plugin, caBRAPI api, String storeId) {
        this.plugin = plugin;
        this.productService = new ProductService(plugin, api, storeId);
        this.paymentService = new PaymentService(plugin, api, storeId);
        this.webhookService = new WebhookService(plugin);
        this.processedFile = new File(plugin.getDataFolder(), "data" + File.separator + "processed.yml");
        loadProcessedPayments();
    }

    private void loadProcessedPayments() {
        if (!processedFile.exists()) return;
        processedConfig = YamlConfiguration.loadConfiguration(processedFile);
        if (processedConfig.contains("payments")) {
            processedPayments.addAll(processedConfig.getConfigurationSection("payments").getKeys(false));
        }
    }

    private void saveProcessedPayment(String paymentId, String playerName, String playerUuid,
                                      List<Map<String, Object>> products, boolean apiUpdated) {
        if (processedConfig == null) {
            processedConfig = new YamlConfiguration();
        }

        String path = "payments." + paymentId;
        processedConfig.set(path + ".player", playerName);
        processedConfig.set(path + ".uuid", playerUuid);
        processedConfig.set(path + ".api-updated", apiUpdated);
        processedConfig.set(path + ".timestamp", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

        List<String> productNames = new ArrayList<>();
        List<String> executedCommands = new ArrayList<>();

        for (Map<String, Object> product : products) {
            String productName = product.getOrDefault("name", "Unknown").toString();
            int quantity = 1;
            Object qty = product.get("quantity");
            if (qty instanceof Number && ((Number) qty).intValue() > 1) {
                quantity = ((Number) qty).intValue();
                productNames.add(quantity + "x " + productName);
            } else {
                productNames.add(productName);
            }

            List<String> commands = productService.getProductCommands(product);
            String runMode = productService.getProductRunMode(product);

            if (commands != null) {
                for (String cmd : commands) {
                    String resolved = cmd.replace("{player}", playerName)
                            .replace("@player", playerName)
                            .replace("{product}", productName)
                            .replace("{quantity}", String.valueOf(quantity));

                    if (quantity > 1) {
                        executedCommands.add(resolved + " (x" + quantity + ") [" + runMode + "]");
                    } else {
                        executedCommands.add(resolved + " [" + runMode + "]");
                    }
                }
            }
        }

        processedConfig.set(path + ".products", productNames);
        processedConfig.set(path + ".commands", executedCommands);

        try {
            processedFile.getParentFile().mkdirs();
            processedConfig.save(processedFile);
        } catch (Exception e) {
            plugin.getPluginLogger().warning("Erro ao salvar pagamento processado: " + e.getMessage());
        }
    }

    public void processPlayer(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                processPendingPayments(player);
            } catch (Exception e) {
                plugin.getPluginLogger().severe(player.getName() + ": " + e.getMessage());
            }
        });
    }

    public void processConsole() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                processPendingConsolePayments();
            } catch (Exception e) {
                plugin.getPluginLogger().severe("Console: " + e.getMessage());
            }
        });
    }

    private void processPendingPayments(Player player) throws Exception {
        String nickname = player.getName();

        FilterResult filterResult = paymentService.filterPendingPayments(nickname);
        if (filterResult == null) {
            return;
        }

        List<Payment> pendingPayments = filterResult.getResponse().getPayments();
        Map<String, List<Map<String, Object>>> paymentItemsMap = filterResult.getPaymentItemsMap();

        int activatedCount = 0;
        for (Payment payment : pendingPayments) {
            List<Map<String, Object>> items = paymentItemsMap.get(payment.getId());
            if (activatePayment(player, payment, items)) {
                activatedCount++;
            }
        }

        if (activatedCount > 0) {
            int total = pendingPayments.size();
            String msg = plugin.getPluginConfig().prefixed(plugin.getPluginConfig().getMessageActivationSuccess()
                    .replace("{count}", String.valueOf(activatedCount))
                    .replace("{total}", String.valueOf(total)));
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ColorUtil.colorize(msg)));
            plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageActivationLog()
                    .replace("{count}", String.valueOf(activatedCount))
                    .replace("{total}", String.valueOf(total))
                    .replace("{player}", nickname));
        }
    }

    private void processPendingConsolePayments() throws Exception {
        FilterResult filterResult = paymentService.filterPendingPayments(null);
        if (filterResult == null) {
            return;
        }

        List<Payment> pendingPayments = filterResult.getResponse().getPayments();
        Map<String, List<Map<String, Object>>> paymentItemsMap = filterResult.getPaymentItemsMap();

        int activatedCount = 0;
        for (Payment payment : pendingPayments) {
            String paymentId = payment.getId();
            if (paymentId == null || paymentId.isEmpty()) continue;
            if (processedPayments.contains(paymentId)) continue;

            String playerName = null;
            if (payment.getMetadata() != null) {
                Object nickObj = payment.getMetadata().get("nickname");
                if (nickObj != null) {
                    playerName = nickObj.toString();
                }
            }
            if (playerName == null || playerName.isEmpty()) continue;

            final String finalPlayerName = playerName;

            List<Map<String, Object>> items = paymentItemsMap.get(paymentId);
            List<Map<String, Object>> productDetails = buildProductDetails(payment, items);
            if (productDetails.isEmpty()) continue;

            boolean hasConsole = false;
            for (Map<String, Object> product : productDetails) {
                if ("console".equals(productService.getProductRunMode(product))) {
                    hasConsole = true;
                    break;
                }
            }
            if (!hasConsole) continue;

            CountDownLatch dispatchLatch = new CountDownLatch(1);

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    dispatchConsoleProducts(finalPlayerName, productDetails);
                } finally {
                    dispatchLatch.countDown();
                }
            });

            dispatchLatch.await();

            boolean updated = paymentService.updatePaymentDelivered(paymentId, finalPlayerName, "");

            processedPayments.add(paymentId);
            saveProcessedPayment(paymentId, finalPlayerName, "", productDetails, updated);

            if (updated) {
                webhookService.sendWebhookConsole(finalPlayerName, productDetails);
            }
            activatedCount++;
        }

        if (activatedCount > 0) {
            plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageActivationLog()
                    .replace("{count}", String.valueOf(activatedCount))
                    .replace("{total}", String.valueOf(activatedCount))
                    .replace("{player}", "console (entregas pendentes)"));
        }
    }

    private boolean activatePayment(Player player, Payment payment, List<Map<String, Object>> items) throws Exception {
        String paymentId = payment.getId();
        if (paymentId == null || paymentId.isEmpty()) return false;

        if (processedPayments.contains(paymentId)) return false;

        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        List<Map<String, Object>> productDetails = buildProductDetails(payment, items);

        if (productDetails.isEmpty()) return false;

        boolean needsInventoryCheck = false;
        for (Map<String, Object> product : productDetails) {
            String runMode = productService.getProductRunMode(product);
            if ("player".equals(runMode) && productService.shouldCheckInventory(product)) {
                needsInventoryCheck = true;
                break;
            }
        }

        if (needsInventoryCheck) {
            Future<Boolean> inventoryCheck = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                    isInventoryFullAndRequired(player, productDetails));
            if (inventoryCheck.get()) return false;
        }

        CountDownLatch dispatchLatch = new CountDownLatch(1);

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                dispatchProducts(player, playerName, productDetails);
            } finally {
                dispatchLatch.countDown();
            }
        });

        dispatchLatch.await();

        boolean updated = paymentService.updatePaymentDelivered(paymentId, playerName, playerUuid);

        processedPayments.add(paymentId);
        saveProcessedPayment(paymentId, playerName, playerUuid, productDetails, updated);

        if (updated) {
            webhookService.sendWebhook(player, productDetails);
            return true;
        }

        plugin.getPluginLogger().warning(plugin.getPluginConfig().prefixed(
                plugin.getPluginConfig().getMessagePaymentUpdateFailed()
                        .replace("{paymentId}", paymentId)
                        .replace("{status}", "FALHA AO ATUALIZAR - PRODUTO JÁ ENTREGUE")));
        return false;
    }

    private List<Map<String, Object>> buildProductDetails(Payment payment, List<Map<String, Object>> items) {
        List<Map<String, Object>> productDetails = new ArrayList<>();

        if (items != null) {
            for (Map<String, Object> item : items) {
                Object productIdObj = item.get("productId");
                if (productIdObj == null) continue;

                Object productObj = item.get("product");
                if (productObj instanceof Map) {
                    Map<String, Object> product = new HashMap<>((Map<String, Object>) productObj);
                    if (item.containsKey("quantity")) {
                        product.put("quantity", item.get("quantity"));
                    }
                    if (product.get("metadata") != null) {
                        productDetails.add(product);
                        continue;
                    }
                }

                Map<String, Object> detail = productService.fetchProductDetail(productIdObj.toString());
                if (detail != null) {
                    productDetails.add(detail);
                } else {
                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("id", productIdObj.toString());
                    fallback.put("name", productIdObj.toString());
                    productDetails.add(fallback);
                }
            }
        }

        if (productDetails.isEmpty() && payment.getMetadata() != null) {
            Object metaProductId = payment.getMetadata().get("productId");
            if (metaProductId != null) {
                Map<String, Object> detail = productService.fetchProductDetail(metaProductId.toString());
                if (detail != null) {
                    productDetails.add(detail);
                }
            }
        }

        return productDetails;
    }

    private boolean isInventoryFullAndRequired(Player player, List<Map<String, Object>> products) {
        int emptySlots = 0;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (ItemStack item : contents) {
            if (item == null) emptySlots++;
        }

        for (Map<String, Object> product : products) {
            String runMode = productService.getProductRunMode(product);
            if ("player".equals(runMode) && productService.shouldCheckInventory(product)) {
                int requiredSlots = productService.getInventorySlot(product);
                if (emptySlots < requiredSlots) {
                    player.sendMessage(ColorUtil.colorize(plugin.getPluginConfig().prefixed(plugin.getPluginConfig().getMessageInventory()
                            .replace("{slots}", String.valueOf(requiredSlots)))));
                    return true;
                }
            }
        }
        return false;
    }

    private void dispatchProducts(Player player, String playerName, List<Map<String, Object>> products) {
        CommandSender console = Bukkit.getConsoleSender();

        for (Map<String, Object> product : products) {
            String productName = product.getOrDefault("name", "Unknown").toString();
            int quantity = 1;
            Object qtyObj = product.get("quantity");
            if (qtyObj instanceof Number) {
                quantity = ((Number) qtyObj).intValue();
            }

            String runMode = productService.getProductRunMode(product);
            List<String> commands = productService.getProductCommands(product);
            String productMessage = productService.getProductMessage(product);

            CommandSender executor = "player".equals(runMode) ? player : console;

            if (commands != null) {
                for (String cmd : commands) {
                    String resolved = cmd.replace("{player}", playerName)
                            .replace("@player", playerName)
                            .replace("{product}", productName)
                            .replace("{quantity}", String.valueOf(quantity));

                    for (int i = 0; i < quantity; i++) {
                        Bukkit.dispatchCommand(executor, resolved);
                    }

                    plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageCommandExecuted()
                            .replace("{command}", resolved)
                            .replace("{quantity}", String.valueOf(quantity))
                            .replace("{player}", playerName)
                            .replace("{executor}", "player".equals(runMode) ? "jogador" : "console"));
                }
            }

            if (productMessage != null && "player".equals(runMode)) {
                player.sendMessage(ColorUtil.colorize(productMessage
                        .replace("{player}", playerName)
                        .replace("@player", playerName)
                        .replace("{product}", productName)
                        .replace("{quantity}", String.valueOf(quantity))));
            }
        }
    }

    private void dispatchConsoleProducts(String playerName, List<Map<String, Object>> products) {
        CommandSender console = Bukkit.getConsoleSender();

        for (Map<String, Object> product : products) {
            String productName = product.getOrDefault("name", "Unknown").toString();
            int quantity = 1;
            Object qtyObj = product.get("quantity");
            if (qtyObj instanceof Number) {
                quantity = ((Number) qtyObj).intValue();
            }

            String runMode = productService.getProductRunMode(product);
            if (!"console".equals(runMode)) continue;

            List<String> commands = productService.getProductCommands(product);

            if (commands != null) {
                for (String cmd : commands) {
                    String resolved = cmd.replace("{player}", playerName)
                            .replace("@player", playerName)
                            .replace("{product}", productName)
                            .replace("{quantity}", String.valueOf(quantity));

                    for (int i = 0; i < quantity; i++) {
                        Bukkit.dispatchCommand(console, resolved);
                    }

                    plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageCommandExecuted()
                            .replace("{command}", resolved)
                            .replace("{quantity}", String.valueOf(quantity))
                            .replace("{player}", playerName)
                            .replace("{executor}", "console"));
                }
            }
        }
    }
}
