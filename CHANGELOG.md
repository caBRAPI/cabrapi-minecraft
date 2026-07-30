
# CHANGELOG

All significant changes to this project will be documented in this file.

## [1.1.0] - 2026-07-14
### Added
- **Sistema dual de execução de produtos**: Campo `run` no metadata do produto (`"console"` ou `"player"`)
  - `console` (padrão): Comandos executados pelo console, sem verificação de inventário
  - `player`: Comandos executados pelo jogador, com verificação de inventário quando habilitada
- **Verificação periódica de pagamentos console**: Task assíncrona roda a cada `check-interval` minutos (padrão 5) processando pagamentos pendentes de produtos modo console
- **Persistência de pagamentos processados**: Arquivo `data/processed.yml` registra todas as compras processadas com detalhes completos
- **Javadocs completos**: Documentação Java adicionada a todos os arquivos do projeto (7 classes)
- **Slot configurável para inventário**: Campo `inventory.slot` define slots vazios mínimos necessários
  - Valores abaixo de 1 são ajustados para 1 (mínimo)
  - Valores acima de 36 são ajustados para 36 (máximo inventário jogador)
- **Log detalhado de comandos**: Cada comando executado é logado com nome, quantidade e executor
- **Suporte a decimais no check-interval**: Aceita `0.5` (30 segundos), `0.25` (15 segundos), etc.

### Changed
- **Configuração 100% mensagens**: Todas as mensagens do plugin são configuráveis via `config.yml` (25+ chaves)
- **Logger sem cores forçadas**: Removidos códigos `&e`, `&c`, &f` do Logger; cores vêm exclusivamente do config.yml
- **LoginListener condicional**: Listener registrado apenas quando nLogin NÃO está presente (antes era sempre registrado)
- **VersionChecker atualizado**: Construtor agora recebe `ActivatorPlugin` para acessar configuração
- **Webhook mensagem clarificada**: Mensagem "Webhook notificação enviada ao Discord: {player}" (sobre o jogador, não para ele)
- **Verificação de inventário na main thread**: Inventory check roda via `callSyncMethod` para acesso seguro ao inventário
- **Race condition corrigida**: `CountDownLatch` garante que produtos são despachados antes de marcar API como entregue
- **Produtos primeiro, API depois**: Dispatch de produtos acontece antes da atualização na API (evita perda em caso de crash)
- **Pagamento duplicado prevenido**: ID salvo no arquivo mesmo se API update falhar (evita entrega dupla)

### Fixed
- **Thread safety**: `ConcurrentHashMap.newKeySet()` para `processedPayments` (antes era HashSet comum)
- **Fallback nickname para console**: `filterPendingPayments(null)` agora aceita null para buscar todos pendentes
- **Produto sem run mode**: Default `getProductRunMode()` retorna `"console"` quando metadata não define `run`

### Removed
- **Emojis das mensagens**: Removidos ✔ e ✘ de todas as mensagens
- **Campos冗antes do Logger**: Removidos campos e métodos não utilizados

### Config
```yaml
# Novas chaves adicionadas ao config.yml
check-interval: 5  # Intervalo em minutos para verificação periódica
```

### Product Metadata
```json
{
  "minecraft": {
    "run": "console",       // "console" (padrão) ou "player"
    "commands": ["give {player} diamond 1"],
    "message": "&aVocê recebeu um diamante!",
    "inventory": {
      "enable": false,      // Só funciona quando run = "player"
      "slot": 4             // Mínimo de slots vazios (1-36, padrão 1)
    }
  }
}
```

### Processed Payments Record (data/processed.yml)
```yaml
payments:
  "pagamento-abc123":
    player: "Steve"
    uuid: "uuid-do-jogador"     # Vazio para modo console
    api-updated: true
    timestamp: "14/07/2026 15:30:00"
    products:
      - "1x VIP"
      - "2x Caixa de Surpresa"
  "pagamento-def456":
    player: "Alex"
    uuid: ""                     # Vazio (processado pelo console)
    api-updated: true
    timestamp: "14/07/2026 15:35:00"
    products:
      - "1x Coins"
```
