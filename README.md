# Sistema Runner

[![Release - Sistema Runner](https://github.com/GGabrielRodrigues/runner/actions/workflows/release.yml/badge.svg)](https://github.com/GGabrielRodrigues/runner/actions/workflows/release.yml)

O **Sistema Runner** é uma solução integrada desenvolvida para facilitar a execução e gestão de aplicações Java voltadas à interoperabilidade e segurança em saúde. Ele atua como uma "ponte" entre integradores da Plataforma HubSaúde (SES-GO) e as complexidades de assinatura digital e validação de recursos FHIR.

---

## 🚀 Visão Geral

O projeto consiste em um ecossistema multiplataforma (Windows, Linux, macOS) composto por:
1.  **CLI Assinatura (Go)**: Interface de linha de comando para criar e validar assinaturas digitais.
2.  **CLI Simulador (Go)**: Orquestrador para gestão do ciclo de vida do ambiente de testes do HubSaúde.
3.  **Assinador.jar (Java/Spring Boot)**: O motor de regras de negócio que realiza a validação rigorosa de parâmetros FHIR e operações criptográficas (JAdES/JWS).

---

## ✨ Funcionalidades Principais

### 🔧 Automação de Ambiente
- **Provisionamento Zero-Config**: O CLI detecta a ausência do Java 21 e realiza o download/instalação automática do JDK (Adoptium) em um diretório isolado (`~/.hubsaude/`).
- **Updates Dinâmicos**: O sistema consulta um arquivo `release.json` remoto para garantir que o usuário sempre utilize a versão mais recente dos componentes sem precisar reinstalar o CLI.

### 🔐 Assinatura Digital FHIR (SES-GO)
- **Modelagem Avançada**: Suporte integral aos 8 componentes de entrada exigidos pelo Guia de Implementação da SES-GO.
- **Validação de Segurança**:
    - Verificação de **Timestamp Drift** (limite de ±5 min para prevenir replays).
    - Suporte a múltiplos materiais criptográficos: **PEM**, **PKCS#12**, **Smartcards** e **Tokens** (PKCS#11).
    - Respostas padronizadas via recurso FHIR **OperationOutcome**.

### 🧪 Gestão do Simulador
- Controle total via CLI (`start`, `stop`, `status`).
- Monitoramento de saúde via endpoints Actuator.
- Execução em background com persistência de PID.

---

## 🛠 Como Usar

### Pré-requisitos
- Apenas o binário do CLI correspondente ao seu sistema operacional (disponível em [Releases](https://github.com/GGabrielRodrigues/runner/releases)).
- O sistema cuidará do resto (Java, JARs, etc).

### Comandos do Simulador
```bash
# Iniciar o ambiente de testes do HubSaúde
./simulador start

# Verificar se o simulador está online e saudável
./simulador status

# Parar o simulador de forma limpa
./simulador stop
```

### Comandos de Assinatura
O assinador pode ser usado de duas formas:

**1. Modo Direto (Cold Start):**
```bash
./assinatura sign --input "caminho/do/seu/bundle.json"
```

**2. Modo Servidor (Warm Start):**
Para alta performance e baixa latência, inicie o assinador como um serviço:
```bash
# Inicia o servidor na porta 9090
java -jar assinador.jar server
```
Acesse a documentação interativa em: `http://localhost:9090/swagger-ui.html`

---

## 🏗 Arquitetura Técnica

- **Linguagem CLI**: Go 1.25 (pela facilidade de cross-compiling e binários estáticos).
- **Linguagem Motor**: Java 21 com Spring Boot 3.2.
- **Segurança CI/CD**: Artefatos assinados digitalmente com **Cosign/Sigstore** e verificáveis via `checksums.txt`.
- **Padrão de Dados**: HL7 FHIR R4.

---

## 📂 Estrutura do Repositório

```text
runner/
├── .github/workflows/   # Pipelines de CI/CD (Build, Release, Segurança)
├── assinador-java/      # Projeto Java (Spring Boot) - O motor FHIR
├── cmd/
│   ├── assinatura/      # Código-fonte do CLI de assinatura (Go)
│   └── simulador/       # Código-fonte do CLI do simulador (Go)
├── internal/            # Lógica privada (JDK, Invocação, Updates)
├── release.json         # Metadados de versão e distribuição
└── planejamento.md      # Roadmap e status detalhado do projeto
```

---

## 🤝 Contribuição

1.  Consulte o arquivo `planejamento.md` para ver as tarefas pendentes.
2.  Crie uma branch para sua feature: `git checkout -b feature/minha-melhoria`.
3.  Garanta que o build passe: `go build ./...` e `mvn clean package`.
4.  Abra um Pull Request.

---

## 📄 Licença

Este projeto é de interesse da Secretaria de Estado de Saúde de Goiás (SES) e da Universidade Federal de Goiás (UFG). Consulte os termos de uso específicos da disciplina de Implementação e Integração de Software.
