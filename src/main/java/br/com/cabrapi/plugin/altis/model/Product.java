package br.com.cabrapi.plugin.altis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um produto da caBRAPI que será entregue ao jogador.
 * <p>
 * Um produto pode executar um ou mais comandos, enviar uma mensagem ao jogador
 * e, opcionalmente, verificar se o inventário possui espaço antes da entrega.
 *
 * @author Sebastian Jn <sebastianjnuwu@gmail.com>
 * @since 1.0.0
 */
public class Product {

    /**
     * Identificador único do produto.
     */
    private String id;

    /**
     * Nome do produto.
     */
    private String name;

    /**
     * Lista de comandos executados durante a entrega.
     */
    private List<String> commands = new ArrayList<String>();

    /**
     * Mensagem enviada ao jogador após a entrega.
     */
    private String message;

    /**
     * Indica se o inventário deve ser verificado antes da entrega.
     */
    private boolean inventoryCheck;

    /**
     * Cria um produto vazio.
     */
    public Product() {
    }

    /**
     * Cria um produto com identificador e nome.
     *
     * @param id Identificador do produto.
     * @param name Nome do produto.
     */
    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Retorna o identificador do produto.
     *
     * @return Identificador do produto.
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador do produto.
     *
     * @param id Novo identificador.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retorna o nome do produto.
     *
     * @return Nome do produto ou {@code "Unknown"} caso não tenha sido definido.
     */
    public String getName() {
        return name == null ? "Unknown" : name;
    }

    /**
     * Define o nome do produto.
     *
     * @param name Novo nome.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna os comandos do produto.
     *
     * @return Lista de comandos.
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Define os comandos do produto.
     *
     * @param commands Lista de comandos.
     */
    public void setCommands(List<String> commands) {
        this.commands = commands != null ? commands : new ArrayList<String>();
    }

    /**
     * Adiciona um comando ao produto.
     *
     * @param command Comando a ser adicionado.
     */
    public void addCommand(String command) {
        if (command != null && !command.trim().isEmpty()) {
            commands.add(command);
        }
    }

    /**
     * Retorna a mensagem enviada ao jogador.
     *
     * @return Mensagem configurada.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Define a mensagem enviada ao jogador.
     *
     * @param message Nova mensagem.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Indica se o inventário deve ser verificado antes da entrega.
     *
     * @return {@code true} caso a verificação esteja habilitada.
     */
    public boolean isInventoryCheck() {
        return inventoryCheck;
    }

    /**
     * Define se o inventário deve ser verificado antes da entrega.
     *
     * @param inventoryCheck {@code true} para habilitar a verificação.
     */
    public void setInventoryCheck(boolean inventoryCheck) {
        this.inventoryCheck = inventoryCheck;
    }

    /**
     * Retorna uma representação textual do produto.
     *
     * @return Informações básicas do produto.
     */
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }
}