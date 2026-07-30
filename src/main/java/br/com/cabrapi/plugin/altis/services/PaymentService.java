package br.com.cabrapi.plugin.altis.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cabrapi.plugin.altis.ActivatorPlugin;
import br.com.cabrapi.sdk.caBRAPI;
import br.com.cabrapi.sdk.client.HttpClient;
import br.com.cabrapi.sdk.model.payment.FilterPaymentsRequest;
import br.com.cabrapi.sdk.model.payment.FilterPaymentsResponse;
import br.com.cabrapi.sdk.model.payment.Payment;
import br.com.cabrapi.sdk.model.payment.PaymentShipmentStatus;
import br.com.cabrapi.sdk.model.payment.PaymentStatus;
import br.com.cabrapi.sdk.model.payment.UpdatePaymentRequest;
import br.com.cabrapi.sdk.util.Json;

/**
 * Serviço responsável por consultas e atualizações de pagamentos na API.
 * <p>
 * Oferece funcionalidades para filtrar pagamentos pendentes de um jogador,
 * extrair itens do pagamento, atualizar o status de entrega e recuperar
 * metadados associados.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class PaymentService {

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
     * Cria uma nova instância do serviço de pagamentos.
     *
     * @param plugin Instância principal do plugin.
     * @param api Instância da SDK da caBRAPI.
     * @param storeId Identificador da loja.
     */
    public PaymentService(ActivatorPlugin plugin, caBRAPI api, String storeId) {
        this.plugin = plugin;
        this.api = api;
        this.storeId = storeId;
    }

    /**
     * Filtra pagamentos aprovados e pendentes de entrega para um jogador.
     *
     * @param nickname Nome do jogador (nickname).
     * @return Resultado da filtragem ou {@code null} caso não haja pagamentos.
     * @throws Exception caso ocorra um erro na requisição à API.
     */
    public FilterResult filterPendingPayments(String nickname) throws Exception {
        FilterPaymentsRequest filterReq = new FilterPaymentsRequest();
        if (nickname != null) {
            filterReq.setMetadataKey("nickname");
            filterReq.setMetadataValue(nickname);
        }
        filterReq.setStatus(PaymentStatus.APPROVED);
        filterReq.setShipment(PaymentShipmentStatus.PENDING);
        filterReq.setLimit(100);

        HttpClient.HttpResponse response = api.payments().filter(storeId, filterReq);

        if (response.getStatus() != 200) {
            plugin.getPluginLogger().warning(plugin.getPluginConfig().getMessageApiRequestError()
                    .replace("{status}", String.valueOf(response.getStatus()))
                    .replace("{player}", nickname != null ? nickname : "*"));
            return null;
        }

        FilterPaymentsResponse filterResp = response.parse(FilterPaymentsResponse.class);
        if (filterResp == null || !filterResp.isStatus()) {
            if (nickname != null) {
                plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageNoPendingPayments()
                        .replace("{player}", nickname));
            }
            return null;
        }
        if (filterResp.getPayments() == null || filterResp.getPayments().isEmpty()) {
            if (nickname != null) {
                plugin.getPluginLogger().info(plugin.getPluginConfig().getMessageNoPendingPayments()
                        .replace("{player}", nickname));
            }
            return null;
        }

        Map<String, List<Map<String, Object>>> paymentItemsMap = new HashMap<>();
        Map<String, Object> rawBody = Json.fromJson(response.getBody(), Map.class);
        List<Map<String, Object>> rawPayments = (List<Map<String, Object>>) rawBody.get("payments");

        if (rawPayments != null) {
            for (Map<String, Object> raw : rawPayments) {
                String rawId = (String) raw.get("id");
                if (rawId != null) {
                    List<Map<String, Object>> items = extractItems(raw);
                    if (items != null && !items.isEmpty()) {
                        paymentItemsMap.put(rawId, items);
                    }
                }
            }
        }

        return new FilterResult(filterResp, paymentItemsMap);
    }

    /**
     * Extrai a lista de itens de um pagamento bruto retornado pela API.
     *
     * @param rawPayment Mapa com os dados brutos do pagamento.
     * @return Lista de itens ou {@code null} caso não haja itens.
     */
    private List<Map<String, Object>> extractItems(Map<String, Object> rawPayment) {
        Object itemsObj = rawPayment.get("items");
        if (itemsObj instanceof List) {
            List<Map<String, Object>> rootItems = (List<Map<String, Object>>) itemsObj;
            if (!rootItems.isEmpty()) return rootItems;
        }

        Object metaObj = rawPayment.get("metadata");
        if (metaObj instanceof Map) {
            Object metaItems = ((Map<String, Object>) metaObj).get("items");
            if (metaItems instanceof List) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Object metaItem : (List<?>) metaItems) {
                    if (metaItem instanceof Map) {
                        result.add(normalizeItem((Map<String, Object>) metaItem));
                    }
                }
                if (!result.isEmpty()) return result;
            }
        }

        return null;
    }

    /**
     * Normaliza os dados de um item, garantindo que o campo {@code productId}
     * esteja presente.
     *
     * @param rawItem Mapa com os dados brutos do item.
     * @return Mapa normalizado com o {@code productId} garantido.
     */
    private Map<String, Object> normalizeItem(Map<String, Object> rawItem) {
        Map<String, Object> normalized = new HashMap<>(rawItem);

        if (!normalized.containsKey("productId")) {
            Object productObj = rawItem.get("product");
            if (productObj instanceof Map) {
                Object productId = ((Map<String, Object>) productObj).get("id");
                if (productId != null) {
                    normalized.put("productId", productId.toString());
                }
            }
        }

        return normalized;
    }

    /**
     * Atualiza o status de entrega de um pagamento na API.
     * <p>
     * Preserva os metadados existentes e adiciona informações sobre
     * a ativação realizada pelo plugin.
     *
     * @param paymentId Identificador do pagamento.
     * @param playerName Nome do jogador que recebeu a entrega.
     * @param playerUUID UUID do jogador que recebeu a entrega.
     * @return {@code true} caso a atualização tenha sido bem-sucedida.
     */
    public boolean updatePaymentDelivered(String paymentId, String playerName, String playerUUID) {
        try {
            Map<String, Object> existingMetadata = fetchPaymentMetadata(paymentId);

            UpdatePaymentRequest updateReq = new UpdatePaymentRequest();
            updateReq.setShipment(PaymentShipmentStatus.DELIVERED);

            if (existingMetadata != null) {
                existingMetadata.put("activatedAt", new Date().toString());
                existingMetadata.put("activatedBy", "plugin");
                existingMetadata.put("playerName", playerName);
                existingMetadata.put("playerUUID", playerUUID);
                updateReq.setMetadata(existingMetadata);
            } else {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("activatedAt", new Date().toString());
                metadata.put("activatedBy", "plugin");
                metadata.put("playerName", playerName);
                metadata.put("playerUUID", playerUUID);
                updateReq.setMetadata(metadata);
            }

            HttpClient.HttpResponse updateResponse = api.payments().update(storeId, paymentId, updateReq);

            if (updateResponse.getStatus() == 200) {
                return true;
            }

            plugin.getPluginLogger().warning(plugin.getPluginConfig().getMessagePaymentUpdateFailed()
                    .replace("{paymentId}", paymentId)
                    .replace("{status}", String.valueOf(updateResponse.getStatus())));
        } catch (Exception e) {
            plugin.getPluginLogger().warning(plugin.getPluginConfig().getMessagePaymentUpdateError()
                    .replace("{paymentId}", paymentId)
                    .replace("{error}", e.getMessage()));
        }
        return false;
    }

    /**
     * Busca os metadados de um pagamento específico na API.
     *
     * @param paymentId Identificador do pagamento.
     * @return Mapa com os metadados ou {@code null} caso não sejam encontrados.
     */
    private Map<String, Object> fetchPaymentMetadata(String paymentId) {
        try {
            FilterPaymentsRequest req = new FilterPaymentsRequest();
            req.setId(paymentId);
            req.setLimit(1);
            HttpClient.HttpResponse response = api.payments().filter(storeId, req);
            if (response.getStatus() == 200) {
                Map<String, Object> body = Json.fromJson(response.getBody(), Map.class);
                List<Map<String, Object>> payments = (List<Map<String, Object>>) body.get("payments");
                if (payments != null && !payments.isEmpty()) {
                    Object metaObj = payments.get(0).get("metadata");
                    if (metaObj instanceof Map) {
                        return new HashMap<>((Map<String, Object>) metaObj);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getPluginLogger().warning(plugin.getPluginConfig().getMessageMetadataFetchError()
                    .replace("{paymentId}", paymentId)
                    .replace("{error}", e.getMessage()));
        }
        return null;
    }

    /**
     * Classe que encapsula o resultado de uma filtragem de pagamentos.
     */
    public static class FilterResult {

        /**
         * Resposta da API com a lista de pagamentos.
         */
        private final FilterPaymentsResponse response;

        /**
         * Mapa de identificador do pagamento para seus respectivos itens.
         */
        private final Map<String, List<Map<String, Object>>> paymentItemsMap;

        /**
         * Cria uma nova instância do resultado da filtragem.
         *
         * @param response Resposta da API.
         * @param paymentItemsMap Mapa de itens por pagamento.
         */
        public FilterResult(FilterPaymentsResponse response, Map<String, List<Map<String, Object>>> paymentItemsMap) {
            this.response = response;
            this.paymentItemsMap = paymentItemsMap;
        }

        /**
         * Retorna a resposta da API.
         *
         * @return Resposta da filtragem de pagamentos.
         */
        public FilterPaymentsResponse getResponse() {
            return response;
        }

        /**
         * Retorna o mapa de itens por pagamento.
         *
         * @return Mapa de itens.
         */
        public Map<String, List<Map<String, Object>>> getPaymentItemsMap() {
            return paymentItemsMap;
        }
    }
}
