# Repositório dedicado à disciplina de Implementação e Integração de Software

# Membros da dupla:
- Gabriel Rodrigues da Silva
- Kesley Soares

# Atividades realizadas até o momento:

---

## Sprint 1 -- Tarefas Operacionais

### US-01.1 — Estrutura base do CLI em Go

#### T-01.1.0 — Instalar Go 1.25

- Baixar o instalador de [https://go.dev/dl/](https://go.dev/dl/) para a plataforma de desenvolvimento
- Instalar e verificar com `go version` — saída esperada: `go version go1.25 <os>/<arch>`
- Confirmar que `GOPATH` e `GOROOT` estão configurados corretamente
- Garantir que o binário `go` está no `PATH` (necessário para todas as tarefas seguintes)

#### T-01.1.1 — Inicializar repositório e módulo Go

- Garantir que a branch padrão do repositório GitHub seja `main` (DT-02)
- Executar `go mod init github.com/kyriosdata/runner` na raiz (DT-01)
- Confirmar que `go.mod` foi gerado corretamente

#### T-01.1.2 — Criar estrutura de diretórios

- Criar os diretórios conforme DT-06
- Criar arquivos `.gitkeep` nos diretórios `internal/*` ainda vazios para preservá-los no Git
- Criar `assinador/` com `.gitkeep` (será populado na Sprint 2)

#### T-01.1.3 — Implementar comando `assinatura version`

- Criar `cmd/assinatura/main.go` com CLI mínima usando a biblioteca [cobra](https://github.com/spf13/cobra)
- Adicionar `cobra` como dependência: `go get github.com/spf13/cobra`
- Declarar a versão como **variável** (não constante): `var version = "dev"` — obrigatório para que `-ldflags` consiga sobrescrever o valor em tempo de build; se declarada como `const`, o linker ignora silenciosamente a injeção
- Implementar subcomando `version` que imprime o valor de `version`
- A versão é injetada pela pipeline CI via `-ldflags "-X main.version=<tag>"`; localmente exibe `dev`

#### T-01.1.4 — Criar stub do binário `simulador`

- Criar `cmd/simulador/main.go` com `main()` minimalista que imprime `"simulador v<versão> — em construção"`
- Não é necessário lógica funcional; o objetivo é garantir que o repositório compile dois binários desde o início

#### T-01.1.5 — Verificar compilação local

- Executar `go build ./...` e confirmar que não há erros
- Executar `go vet ./...` e corrigir eventuais warnings

#### T-01.1.6 — Teste de aceitação do comando `version`

- Criar `cmd/assinatura/version_test.go` com um teste de integração que:
  1. Usa `os/exec` para executar `go run . version` a partir do diretório `cmd/assinatura`
  2. Verifica que a saída contém a string `"dev"` (valor padrão da variável `version` sem injeção de ldflags)
- Usar `go run` em vez de compilar binário temporário: mais simples, sem necessidade de gerenciar arquivo temporário ou extensão `.exe` por plataforma
- Executar com `go test ./cmd/assinatura/...` e confirmar que passa

---

## US-05.1 — Pipeline CI/CD multiplataforma

### T-05.1.1 — Criar workflow de build

- Criar `.github/workflows/build.yml`
- Trigger: `push` e `pull_request` restritos à branch `main` (`branches: [main]`) — sem essa restrição, o workflow também dispararia ao criar tags `v*`, colidindo com `release.yml`
- Usar `actions/checkout@v4` e `actions/setup-go@v5` com `go-version: '1.25'`

### T-05.1.2 — Configurar job de testes multiplataforma

O teste de aceitação (T-01.1.6) executa o binário real via `os/exec`, exigindo runner nativo de cada plataforma. Estruturar um job `test` separado do job de build:

- Definir matrix de runners: `ubuntu-latest`, `windows-latest`, `macos-latest`
- Para cada runner, executar em sequência:
  1. `go vet ./...`
  2. `go test ./...` (inclui o teste de aceitação do comando `version`)
- Este job não gera artefatos; seu único objetivo é garantir que o código passa em todas as plataformas

### T-05.1.3 — Configurar job de cross-compilation

Job separado do de testes, responsável pelos artefatos distribuíveis:

- Roda em um único runner (`ubuntu-latest`)
- Depende do job `test` (`needs: test`) — só executa se todos os testes passarem
- Para cada plataforma de DT-03, executar:
  ```
  GOOS=<os> GOARCH=<arch> go build -o dist/assinatura-<os>-<arch> ./cmd/assinatura
  ```
- Para Windows, o binário deve ter extensão `.exe`

### T-05.1.4 — Publicar artefatos do workflow

- Usar `actions/upload-artifact@v4` para disponibilizar os binários como artifacts de cada execução
- Um único artifact por plataforma, nomeado conforme DT-04 (sem versão no nome do artifact; versão vai no release)

---

## US-05.2 — Publicação de releases com versionamento semântico

### T-05.2.1 — Criar workflow de release

- Criar `.github/workflows/release.yml`
- Trigger: `push` de tags no padrão `v*` (ex.: `v0.1.0`)
- O workflow tem três jobs em sequência: `test` → `build` → `publish`

### T-05.2.2 — Job `test`: testes de aceitação nas 3 plataformas

- Reutilizar a mesma estrutura do job `test` de `build.yml` (matrix: `ubuntu-latest`, `windows-latest`, `macos-latest`)
- Executar `go vet ./...` e `go test ./...` em cada runner
- O release só avança se todos os testes passarem nas 3 plataformas (`needs: test`)

### T-05.2.3 — Job `build`: gerar binários com versão injetada

- Depende do job `test` (`needs: test`)
- Roda em `ubuntu-latest`
- Extrair a versão da tag via `${{ github.ref_name }}` (ex.: `v0.1.0`)
- Para cada plataforma de DT-03, compilar com `-ldflags "-X main.version=<tag>"`
- Nomear binários conforme DT-04: `assinatura-<tag>-<os>-<arch>[.exe]`
- Gerar `checksums.txt` com SHA256 de cada binário no formato:
  ```
  <hash>  assinatura-v0.1.0-linux-amd64
  <hash>  assinatura-v0.1.0-windows-amd64.exe
  ...
  ```
- Usar `actions/upload-artifact@v4` para publicar os binários e `checksums.txt` como artifact — necessário para o job `publish` acessá-los, pois jobs rodam em VMs separadas

### T-05.2.4 — Job `publish`: publicar no GitHub Releases

- Depende do job `build` (`needs: build`)
- Usar `actions/download-artifact@v4` para baixar os binários e `checksums.txt` gerados pelo job `build`
- Usar `softprops/action-gh-release@v2` para criar o release automaticamente
- Anexar todos os binários e `checksums.txt` ao release (DT-05)
- Usar o corpo da tag como descrição do release (release notes)
- Requer permissão `contents: write` no workflow

### T-05.2.5 — Validar o fluxo completo

- Criar tag `v0.1.0` no repositório
- Confirmar que os três jobs (`test` → `build` → `publish`) executam com sucesso
- Confirmar que os artefatos estão disponíveis no GitHub Releases com `checksums.txt`


## Definição de Pronto (DoD) da Sprint 1

- [x] `go build ./...` passa sem erros localmente
- [x] `go vet ./...` passa sem warnings
- [x] `assinatura version` exibe a versão correta
- [x] Workflow de build (`build.yml`) executa com sucesso em push para `main`
- [x] Binários para as 3 plataformas (DT-03) são gerados como artifacts
- [x] Workflow de release (`release.yml`) executa ao criar uma tag `v*`
- [x] Release `v0.1.0` publicado no GitHub com binários e `checksums.txt`

---

## Sprint 2 — Assinatura Digital Simulada (modo local)

### US-02.1 & US-02.2 — Desenvolvimento do `assinador.jar` (Java 21)

#### T-02.1.0 — Setup do projeto Java com Maven

- [x] Criar estrutura no diretório `projetos/assinador-java`
- [x] Configurar `pom.xml` para Java 21 e adicionar plugin `maven-assembly-plugin` para gerar o **Fat JAR** (necessário para o CLI invocar um arquivo único)
- [x] Definir o nome do artefato final como `assinador.jar`

#### T-02.1.1 — Implementar Modelo de Dados (POJOs) e Interface

- [x] Criar pacotes `br.ufg.inf.hubsaude.model` e `br.ufg.inf.hubsaude.service`
- [x] Implementar POJOs baseados na referência FHIR (ex.: `SignatureRequest`, `SignatureResponse`)
- [x] Definir a interface `SignatureService` com os métodos `sign(payload)` e `validate(payload, signature)`

#### T-02.1.2 — Implementar `FakeSignatureService` e Validações

- [x] Criar classe `FakeSignatureService` que implementa a interface definida
- [x] Implementar lógica de assinatura: retornar string `SIMULATED_SIG_<hash>` para entradas válidas
- [x] Criar classe `RequestValidator` para verificar presença de campos obrigatórios e formatos de hash
- [x] Garantir que o validador lance exceções customizadas que capturem mensagens de erro legíveis

#### T-02.1.3 — Implementar Interface CLI (Main) do JAR

- [x] Criar `Main.java` para interpretar argumentos de linha de comando: `java -jar assinador.jar <comando> <json>`
- [x] Comandos suportados: `sign` e `validate`
- [x] Garantir que erros de validação sejam impressos no `System.err` e resultados no `System.out`

#### T-02.1.4 — Testes Unitários do Assinador

- [x] Criar testes com JUnit 5 para `FakeSignatureService`
- [x] Validar cenários de erro (parâmetros nulos ou vazios) e cenário de sucesso

---

### US-01.2, US-01.3 & US-01.4 — Evolução do CLI `assinatura` (Go 1.25)

#### T-01.2.1 — Implementar Parser de Comandos com Cobra

- [x] Adicionar subcomandos `sign` e `validate` ao comando `assinatura`
- [x] Configurar flags `--input` (caminho do arquivo ou string) e `--local` (bool, default true para esta sprint)
- [x] Atualizar o comando `help` para documentar os novos parâmetros

#### T-01.3.1 — Implementar Invocador de Processo (Executor)

- [x] Criar pacote `internal/executor` no projeto Go (Implementado como `internal/invoker`)
- [x] Implementar lógica usando `os/exec` para montar e rodar: `java -jar <path_to_jar> <cmd> <params>`
- [x] Criar função para localizar o executável `java` (buscando primeiro em `~/.hubsaude/jdk` e depois no `PATH`)

#### T-01.4.1 — Implementar Formatador de Saída e Tratamento de Erros

- [x] Criar lógica para capturar o `stderr` do processo Java e exibir como erro amigável no terminal
- [x] Formatar o JSON de resposta do `assinador.jar` para uma exibição em texto simples e legível (ex.: "Assinatura gerada com sucesso: [HASH]")

---

### US-04.1 — Provisionamento Automático do JDK

#### T-04.1.1 — Implementar Detector de Ambiente

- [x] Criar pacote `internal/env` (Implementado como `internal/jdk`)
- [x] Implementar função `IsJava21Present()` que executa `java -version` e analisa a string de retorno
- [x] Implementar persistência de estado em `~/.hubsaude/config.json` para armazenar o caminho do JDK gerenciado

#### T-04.1.2 — Implementar Downloader e Extrator do JDK

- [x] Mapear URLs de download da API Adoptium (Eclipse Temurin) para as 3 plataformas (Windows, Linux, macOS)
- [x] Implementar download via `net/http` com barra de progresso simples no terminal
- [x] Implementar extração de `.zip` (Windows) ou `.tar.gz` (Unix) para o diretório `~/.hubsaude/jdk/`

---

### Testes de Integração da Sprint 2

#### T-11.1.1 — Script de Integração Ponta-a-Ponta

- [x] Criar script `test/integration_local_test.go`
- [x] Cenário:
    1. Limpar diretório `~/.hubsaude`
    2. Executar `assinatura sign --input "teste"`
    3. Verificar se o JDK foi baixado automaticamente
    4. Verificar se a saída contém uma assinatura simulada válida
    5. Executar `assinatura validate` com a assinatura gerada e verificar o status positivo

---

## ## Definição de Pronto (DoD) da Sprint 2

- [x] `assinador.jar` compila com todas as dependências (Fat JAR)
- [x] CLI `assinatura` invoca o JAR localmente e exibe resultados sem erros de Java
- [x] O sistema provisiona o JDK 21 automaticamente em máquinas "virgens"
- [x] Testes unitários em Java e testes de integração em Go passam com > 80% de cobertura nos fluxos principais
- [x] Binário da Sprint 2 publicado via Tag `v0.2.0` no GitHub Releases (via pipeline herdada da Sprint 1)

---

## Sprint 3 - Modo Servidor e Material Criptográfico

#### T-02.4.1 — Implementar Servidor HTTP e Endpoints

* [x] Configurar microframework leve (ex: Javalin) ou o `com.sun.net.httpserver.HttpServer` nativo no `pom.xml` para não inflar o tamanho do Fat JAR
* [x] Implementar classe `SignatureController` expondo as rotas `POST /sign` e `POST /validate`
* [x] Adaptar chamadas dos endpoints para utilizar a lógica da interface `SignatureService` já existente
* [x] Mapear as exceções do `RequestValidator` para códigos de status HTTP corretos (ex: `400 Bad Request`, `500 Internal Server Error`)

#### T-02.5.1 — Implementar Suporte a PKCS#11

* [x] Implementar leitura do argumento de linha de comando (ex: `--pkcs11-lib=<path>`) no `Main.java` para receber o caminho dinâmico do driver do host
* [x] Criar nova implementação `PKCS11SignatureService` configurando dinamicamente o provider `SunPKCS11` com o caminho da biblioteca recebido
* [x] Implementar a lógica para realizar operações criptográficas delegando para o dispositivo (token, smart card ou SoftHSM2)
* [x] Adicionar fallback ou mensagens de erro padronizadas caso a biblioteca PKCS#11 (ex: SoftHSM2) não seja encontrada
* [x] Escrever arquivo de documentação (`docs/pkcs11-setup.md`) explicando como instalar e configurar o SoftHSM2 para os testes

#### T-02.4.2 — Testes de Integração e Timeouts do Assinador

* [ ] Implementar captura do argumento `--timeout` no `Main.java`
* [ ] Implementar lógica de atualização de *timestamp* a cada requisição HTTP recebida nas rotas
* [ ] Criar *thread* em *background* no Java que checa o *timestamp* a cada minuto; se a inatividade ultrapassar o limite, executar `System.exit(0)`
* [ ] Criar testes de integração no JUnit subindo o servidor HTTP e testando requisições contra `/sign` e `/validate`
* [ ] Criar testes garantindo que operações envolvendo chaves simuladas via SoftHSM2 funcionem adequadamente

#### T-01.5.1 — Implementar Gerenciamento de Ciclo de Vida (Servidor)

* [ ] Alterar o pacote `internal/invoker` para suportar inicialização de processos Java em *background* (desatrelados do terminal do usuário)
* [ ] Adicionar as flags globais no Cobra: `--port` (default: 8080), `--timeout` (default: 15) e `--pkcs11-lib` (para repassar ao processo Java)
* [ ] Implementar lógica para registrar o PID e a Porta do processo em um arquivo de estado (ex: `~/.hubsaude/server.json`) após o *start*
* [ ] Adicionar saída no CLI informando que o servidor iniciou em segundo plano

#### T-01.7.1 — Implementar Detecção de Instância (Health Check)

* [ ] Criar função no pacote `internal/invoker` para ler os dados (porta/PID) registrados em `~/.hubsaude/server.json`
* [ ] Implementar requisição HTTP rápida na porta registrada (ex: `GET /health` ou ping genérico) para validar se o processo está vivo
* [ ] Criar lógica para limpar o arquivo `server.json` silenciosamente caso o PID não exista mais ou o ping falhe (ex: quando o Java morre por timeout)

#### T-01.6.1 — Implementar Cliente HTTP no CLI

* [ ] Criar pacote `internal/client` (ou adaptar o invoker) para enviar payloads via requisição HTTP
* [ ] Modificar o comando raiz para que, caso o modo local esteja desabilitado e um servidor ativo seja detectado, o CLI faça um `POST /sign` ou `POST /validate` em vez de executar o JAR diretamente
* [ ] Tratar os retornos HTTP, parsear o JSON e manter a formatação amigável de erro no terminal (a mesma implementada na Sprint 2)

#### T-01.8.1 — Implementar Comando de Interrupção

* [ ] Criar novo comando no Cobra: `assinatura stop`
* [ ] Implementar leitura da flag `--port` (ou ler a porta ativa padrão de `~/.hubsaude/server.json`)
* [ ] Implementar encerramento enviando sinal `SIGTERM` para o PID listado (ou via endpoint de shutdown, se o dev A tiver implementado)
* [ ] Excluir o arquivo de registro do processo (`~/.hubsaude/server.json`) e exibir mensagem de confirmação

#### T-11.2.1 — Script de Integração Cliente-Servidor (Go)

*(Pode ser liderado pelo Desenvolvedor B, mas exige que o executável Java do Desenvolvedor A esteja pronto)*

* [ ] Criar novo arquivo de teste no pacote principal (ex: `cmd/assinatura/server_integration_test.go`)
* [ ] Implementar Cenário de Teste:
1. Executar `assinatura stop` (garantir estado limpo)
2. Executar `assinatura sign --input "teste" --pkcs11-lib="<caminho_mock>"` (deve subir o servidor em background e responder via HTTP)
3. Verificar se `~/.hubsaude/server.json` foi criado corretamente
4. Executar `assinatura validate` verificando os logs para garantir que foi servido pelo servidor HTTP ativo
5. Executar `assinatura stop` e checar se o processo morreu e o `.json` sumiu

## Definição de Pronto (DoD) da Sprint 3

* [ ] `assinador.jar` levanta servidor HTTP com rotas funcionais e gerencia limite de inatividade independentemente.
* [ ] Interação com dispositivo via PKCS#11 está funcional através da injeção do caminho da biblioteca pelo CLI.
* [ ] CLI gerencia ciclo de vida do Java (roda em background, reaproveita processo existente, encerra processo).
* [ ] Comunicação entre CLI e JAR ocorre via HTTP por padrão.
* [ ] Testes de ponta-a-ponta refletem o novo modelo com > 80% de cobertura nos fluxos principais.
