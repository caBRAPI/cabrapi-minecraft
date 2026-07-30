<div align="center">

# Altis

<img src="https://www.cabrapi.com.br/images/banner/banner-icon-name-dark.webp" alt="caBRAPI" width="320"/>

**Plugin oficial da caBRAPI para servidores Spigot/Paper.**

Integra automaticamente compras realizadas na plataforma **caBRAPI** ao seu servidor Minecraft.

<br>

<a href="https://github.com/caBRAPI/cabrapi-minecraft/releases">
    <img alt="Version" src="https://img.shields.io/badge/Version-1.0.0-white"/>
</a>

<a href="https://opensource.org/licenses/Apache-2.0">
    <img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-white.svg"/>
</a>

<img alt="Platform" src="https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper-white"/>

</div>

## ✨ Recursos

- Sincronização automática das compras da caBRAPI.
- Entrega automática de produtos.
- Execução de comandos personalizados.
- Compatível com **Spigot** e **Paper**.
- Compatível com **nLogin**.
- Verificação de inventário cheio.
- Notificações via Webhook (Discord).
- Compatível com as versões: `1.16.x`, `1.17.x`, `1.18.x`, `1.19.x`, `1.20.x`, `1.21.x`, `26.x`


## 📦 Instalação

1. Baixe a versão mais recente em **Releases**.
2. Coloque o arquivo `.jar` na pasta `plugins/`.
3. Inicie ou reinicie o servidor.
4. Configure o arquivo: `plugins/altis/config.yml`

## ⚙️ Configuração

```yaml
# ========================================
# Altis - Configuração
# ========================================

# Verificar se há atualizações disponíveis para o plugin.
check-update: true

# Versão da configuração (não alterar).
version: 1

# Intervalo em minutos para verificar pagamentos pendentes (modo console).
check-interval: 5

# Chave da API da sua loja.
# Você pode obtê-la no painel da caBRAPI.
api-key: ""

# ID da sua loja.
# Disponível no painel administrativo da caBRAPI.
store-id: ""

# ========================================
# Discord Webhook
# ========================================
# Variáveis disponíveis em "message":
#   {player}   -> Nome do jogador
#   {products} -> Nome(s) do(s) produto(s) (ex: "10x DEMOSTRAÇÃO")
webhook:
  enable: false
  url: ""
  message: "{player} comprou {products} na loja!"

# ========================================
# Mensagens do Jogo
# ========================================
# Variáveis disponíveis em todas as mensagens:
#   {player}   -> Nome do jogador
#   {product}  -> Nome do produto
#   {quantity} -> Quantidade comprada
#   {count}    -> Pagamentos ativados
#   {total}    -> Total de pagamentos encontrados
#   {status}   -> Código HTTP de retorno
#   {error}    -> Mensagem de erro
#   {current}  -> Versão atual do plugin
#   {latest}   -> Versão mais recente disponível
#   {paymentId} -> ID do pagamento
# Cores disponíveis:
#   &0 Preto    &1 Azul Escuro  &2 Verde Escuro &3 Ciano Escuro
#   &4 Vermelho &5 Roxo         &6 Dourado      &7 Cinza Claro
#   &8 Cinza    &9 Azul         &a Verde        &b Ciano
#   &c Vermelho &d Magenta     &e Amarelo      &f Branco
message:
  # ========================================
  # Mensagens para o Jogador
  # ========================================
  permission: "&c&l[ALTIS] &cVocê não possui permissão."
  inventory: "&e&l[ALTIS] &eSeu inventário está cheio! Precisa de pelo menos &f{slots} &eslot(s) vazio(s) para receber seus itens."
  reload: "&a&l[ALTIS] &aConfiguração recarregada com sucesso!"
  activation-success: "&a&l[ALTIS] &a{count} pagamento(s) ativado(s) com sucesso!"

  # ========================================
  # Status do Startup (Console)
  # ========================================
  api-key-configured: "&aConfigurada"
  api-key-not-configured: "&cNão configurada"
  store-id-configured: "&aConfigurada"
  store-id-not-configured: "&cNão configurada"
  configure-credentials: "&cObservação: &fConfigure &capi-key &fe &cstore-id &fno arquivo &bconfig.yml&f."
  nlogin-detected: "&fnLogin: &aDetectado"
  nlogin-not-found: "&fnLogin: &eNão encontrado &8- &7utilizando PlayerJoinEvent."

  # ========================================
  # Console - Ativação
  # ========================================
  activation-log: "&aAtivado(s) &f{count}/{total} &apagamento(s) para &f{player}"
  reload-by: "&aConfiguração recarregada por &f{player}"

  # ========================================
  # Console - Serviços
  # ========================================
  nlogin-event-received: "&aNLogin event recebido para &f{player}"
  no-pending-payments: "&eNenhum pagamento pendente para &f{player}"
  api-request-error: "&cObservação: &fAPI retornou &e{status} &fpara &b{player}"
  payment-update-failed: "&cObservação: &fFalha ao atualizar pagamento &e{paymentId}&f: HTTP &e{status}"
  payment-update-error: "&cObservação: &fErro ao atualizar pagamento &e{paymentId}&f: &7{error}"
  metadata-fetch-error: "&cObservação: &fErro ao buscar metadata do pagamento &e{paymentId}&f: &7{error}"
  product-fetch-error: "&cObservação: &fErro ao buscar produto: &7{error}"
  command-executed: "&aExecutando: &f{command} &7(x{quantity}) &apara &f{player} &7via &f{executor}"
  webhook-sent: "&aWebhook notificação enviada ao Discord: &f{player}"
  webhook-failed: "&cObservação: &fFalha ao enviar webhook: &7{error}"

  # ========================================
  # Console - Verificador de Versão
  # ========================================
  update-check-failed: "&cObservação: &fNão foi possível verificar atualizações (HTTP &e{status}&f)."
  update-check-invalid: "&cObservação: &fNão foi possível verificar atualizações. Resposta inválida."
  update-available: "&eObservação: &fUma nova versão está disponível! &a(v{current} → v{latest})"
  update-current: "&aVocê está utilizando a versão mais recente. &f(v{version})"
  update-download: "&fDownload: &bhttps://github.com/caBRAPI/cabrapi-minecraft/releases"
  update-check-error: "&cObservação: &fNão foi possível verificar atualizações. &7{error}"

  # ========================================
  # Console - Versão do Config
  # ========================================
  config-outdated: "&eObservação: &fSeu config.yml está desatualizado (v{current}). Recomenda-se atualizá-lo para a versão v{latest}."
  config-newer: "&eObservação: &fSeu config.yml parece ser de uma versão mais recente (v{current}). Caso haja problemas, verifique as configurações."

```

## 📜 Comandos

|    Permissão     |      Comando      |            Descrição                |
|------------------|-------------------|-------------------------------------|
| `cabrapi.admin`  | `/cabrapi reload` | Recarrega a configuração do plugin. | 

