# Planejamento do Sistema Runner - Status do Projeto

Este documento registra o progresso atual do desenvolvimento do Sistema Runner, as funcionalidades implementadas e os próximos passos necessários para a conclusão do projeto.

## ✅ Funcionalidades Implementadas

### 1. Infraestrutura e CLI em Go
- [x] Estrutura base do projeto Go com módulos e pacotes (`cmd/`, `internal/`).
- [x] Implementação do CLI `assinatura` usando Cobra com comando `version`.
- [x] Implementação do CLI `simulador` com comandos `start`, `stop`, `status` e `version`.
- [x] Lógica de detecção de ambiente e porta (Verificação de porta 8443 e 9090).
- [x] Gerenciamento de processos em background (PID storage em `~/.hubsaude/`).

### 2. Gestão de Ambiente e Artefatos (Automação)
- [x] **Provisionamento Automático do JDK**: Download, extração e configuração automática do Java 21 (Adoptium) em `~/.hubsaude/jdk/`.
- [x] **Sistema de Updates Dinâmicos**: Uso de `release.json` hospedado no GitHub para controle de versões e URLs de download.
- [x] **Download Dinâmico de JARs**: O CLI baixa automaticamente o `assinador.jar` e o `simulador.jar` conforme a necessidade.
- [x] Redirecionamento de logs dos processos Java para `~/.hubsaude/simulador.log`.

### 3. Assinador Java (Spring Boot)
- [x] Migração para **Spring Boot 3.2.4** com suporte a Java 21.
- [x] Entry point híbrido: Funciona como CLI (Invocação Direta/Cold Start) ou Servidor (Warm Start).
- [x] Documentação automática via **Swagger UI** (OpenAPI) em `http://localhost:9090/swagger-ui.html`.
- [x] **Modelagem FHIR Avançada**:
    - [x] DTOs completos para Criação (8 componentes: Bundle, Provenance, CryptoMaterial, etc.).
    - [x] DTOs completos para Validação (Assinatura JWS, Revogação, TrustStore de hashes).
- [x] **Validador FHIR**:
    - [x] Verificação de campos obrigatórios conforme o Guia da SES-GO.
    - [x] Validação de **Timestamp Drift** (limite de ±5 minutos em relação ao servidor).
    - [x] Validação de janela temporal absoluta (2025-2099).
- [x] Tratamento de erros padronizado retornando o recurso **OperationOutcome**.
- [x] **Desligamento Automático**: Implementação do cronômetro de inatividade (`--timeout`).

### 4. Pipeline CI/CD e Segurança
- [x] Workflow de Build automatizado no GitHub Actions.
- [x] **Release Automatizada**: Geração de binários para Windows, Linux e macOS.
- [x] **Segurança da Cadeia de Suprimentos**: Assinatura de artefatos com **Cosign/Sigstore** (geração de `.sig` e `.pem`).
- [x] Geração automática de `checksums.txt` (SHA-256).

---

## 🚀 O que ainda falta implementar (Pendências)

### 1. Evolução do CLI Go
- [ ] Implementar a invocação via **HTTP** no CLI `assinatura` (Atualmente ele faz apenas invocação direta via `java -jar`).
- [ ] Implementar detecção automática de servidor ativo (Warm Start) no CLI Go.
- [ ] Criar flags no CLI Go para facilitar o envio dos 8 componentes FHIR (atualmente é necessário passar o JSON bruto).

### 2. Material Criptográfico (PKCS#11)
- [ ] Finalizar a integração funcional com o Provider `SunPKCS11` para uso de Tokens e Smartcards físicos.
- [ ] Validar o fluxo com o simulador de hardware **SoftHSM2**.
d`).

---

## 🛠 Comandos Úteis para Teste

### Iniciar Simulador
`go run ./cmd/simulador start`

### Verificar Status do Simulador
`go run ./cmd/simulador status`

### Iniciar Assinador (Modo Servidor)
`java -jar assinador-java/target/assinador.jar server`

### Testar Criação (Direto)
`java -jar assinador-java/target/assinador.jar sign '{ "bundle": {...}, ... }'`
