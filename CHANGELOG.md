# 📝 Changelog - Sistema Runner

Todos os marcos e alterações relevantes do projeto serão documentados aqui. Este projeto segue o [Versionamento Semântico (SemVer)](https://semver.org/lang/pt-BR/).

## [0.2.0] - 2026-06-16

### 🛠️ Sprint 2: Refatoração, Modo Local e Padrões de SO

Nesta iteração, focamos na organização do projeto em um monorepo, na conformidade com os padrões de diretórios de dados dos sistemas operacionais e no aprimoramento da qualidade do código Java.

-----

### **Adicionado**

  - **Lógica de Diretório Específica por SO (Go):**
      - Implementação do pacote `internal/env` para gerenciar a pasta `.hubsaude`.
      - Priorização de caminhos padrão: `XDG_DATA_HOME` (Linux), `LOCALAPPDATA` (Windows) e `Application Support` (macOS).
      - Fallback robusto para o diretório HOME do usuário.
  - **Testes de Unidade para Ambiente:** Adição de testes em `internal/env/env_test.go` validando a descoberta de diretórios em múltiplas plataformas.
  - **Qualidade de Código Java:**
      - Integração de anotações `@SuppressFBWarnings` (SpotBugs) para melhor análise estática.
      - Implementação de cópias defensivas em modelos Java (`SignatureRequest`, `ValidationConfig`) para evitar exposição de representação interna.

-----

### **Alterado**

  - **Refatoração para Monorepo:** Movimentação do projeto Java de `assinador-java/` para `projetos/java/assinador-java/`, consolidando a estrutura de subprojetos.
  - **Invocador de Processos (Go):** Atualização do `invoker` para localizar automaticamente o `assinador.jar` no novo layout de desenvolvimento.
  - **Provisionamento de JDK:** Centralização do caminho do JDK gerenciado para seguir a nova lógica de diretórios do SO.
  - **Testes de Integração:** Atualização de `test/integration_local_test.go` para suportar mocks de ambiente (`XDG_DATA_HOME`, etc.) e validar a nova estrutura de pastas.

-----

### **Rastreabilidade de Requisitos (Sprint 2)**

  - [x] **US-01.1 (Refinamento):** Estrutura do monorepo e descoberta de artefatos.
  - [x] **US-04.1:** Provisionamento do JDK seguindo padrões de sistema.
  - [x] **US-02.1 & 02.2:** Modelos Java aprimorados e integrados ao fluxo local.

-----

## [0.1.0] - 2026-04-14

### 🚀 Sprint 1: Fundação e Entrega Contínua

Nesta sprint inicial, estabelecemos a infraestrutura de desenvolvimento, o layout do projeto e a automação de build e release para múltiplas plataformas.

-----

### **Adicionado**

  - **Estrutura de Diretórios (Layout de Pacotes):** Implementação do padrão monorepo com separação clara entre binários (`cmd/`), lógica interna protegida (`internal/`) e projeto Java (`assinador/`).
  - **Módulo Go:** Inicialização do módulo `github.com/kyriosdata/runner` utilizando **Go 1.25**.
  - **CLI `assinatura`:**
      - Implementação base utilizando a biblioteca **Cobra**.
      - Adição do subcomando `version` para exibir a versão atual.
      - Mecanismo de injeção de versão em tempo de compilação via `-ldflags`.
  - **CLI `simulador` (Stub):** Binário minimalista para garantir a compilação de múltiplos artefatos no repositório.
  - **Teste de Aceitação:** Implementação de teste de integração em `version_test.go` que valida a execução do binário e a saída do comando de versão.
  - **Pipeline de CI (Build):**
      - Workflow `.github/workflows/build.yml` configurado.
      - Testes automatizados executados em **Linux, Windows e macOS**.
      - Geração de artefatos temporários após cada push na branch `main`.
  - **Pipeline de CD (Release):**
      - Workflow `.github/workflows/release.yml` acionado por tags `v*`.
      - **Cross-compilation** nativa para as três plataformas alvo.
      - Geração automática de **Checksums SHA256** para garantia de integridade.
      - Publicação automática no **GitHub Releases** com anexos de binários e hashes.

-----

### **Decisões Técnicas (DT)**

| ID | Decisão | Descrição |
| :--- | :--- | :--- |
| **DT-01** | Módulo Go | `github.com/kyriosdata/runner` |
| **DT-03** | Plataformas-alvo | `windows/amd64`, `linux/amd64`, `darwin/amd64` |
| **DT-04** | Convenção de Nomes | `assinatura-<tag>-<os>-<arch>` |
| **DT-05** | Checksums | Uso de hashes SHA256 para cada artefato distribuído. |

-----

### **Rastreabilidade de Requisitos (Sprint 1)**

  - [x] **US-01.1:** Estrutura base do CLI em Go concluída.
  - [x] **US-05.1:** Pipeline CI/CD multiplataforma funcional.
  - [x] **US-05.2:** Publicação de releases com versionamento semântico automatizada.

-----

### **Status dos Artefatos Disponíveis**

Os seguintes artefatos podem ser encontrados na aba de [Releases](https://www.google.com/search?q=https://github.com/GGabrielRodrigues/runner/releases):

  * `assinatura-v0.1.0-linux-amd64`
  * `assinatura-v0.1.0-windows-amd64.exe`
  * `assinatura-v0.1.0-macos-amd64`
  * `checksums.txt`

> **Nota:** Todos os binários foram validados via Teste de Aceitação local e remoto.