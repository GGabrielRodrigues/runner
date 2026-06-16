package test

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"

	"github.com/GGabrielRodrigues/runner/internal/env"
)

func TestIntegrationLocal(t *testing.T) {
	// Criar diretório temporário para simular o HOME e dados
	tempDir, err := os.MkdirTemp("", "runner-test-*")
	if err != nil {
		t.Fatalf("falha ao criar diretório temporário: %v", err)
	}
	defer os.RemoveAll(tempDir)

	// Configurar variáveis de ambiente para isolar o teste
	envs := map[string]string{
		"HOME":          tempDir,
		"USERPROFILE":   tempDir,
		"XDG_DATA_HOME": filepath.Join(tempDir, "share"),
		"LOCALAPPDATA":  filepath.Join(tempDir, "local"),
	}

	for k, v := range envs {
		original := os.Getenv(k)
		defer os.Setenv(k, original)
		os.Setenv(k, v)
	}

	// Pegar o diretório raiz do projeto para rodar o 'go run'
	projectRoot, err := os.Getwd()
	if err != nil {
		t.Fatalf("falha ao obter diretório atual: %v", err)
	}
	// Como estamos em 'test/', a raiz é o pai
	projectRoot = filepath.Dir(projectRoot)

	// 1. Executar sign
	cmdSign := exec.Command("go", "run", "./cmd/assinatura", "sign", "--input", "teste-integracao")
	cmdSign.Dir = projectRoot
	
	outSign, err := cmdSign.CombinedOutput()
	if err != nil {
		t.Fatalf("Erro no sign: %v. Saída: %s", err, string(outSign))
	}

	output := string(outSign)
	if !strings.Contains(output, "Assinatura gerada com sucesso") {
		t.Errorf("Saída inesperada do sign: %s", output)
	}

	// Verificar se o diretório hubsaude foi criado (usando a nova lógica)
	hubsaudeDir := env.GetHubSaudeDir()
	if _, err := os.Stat(hubsaudeDir); os.IsNotExist(err) {
		t.Errorf("Diretório hubsaude não foi criado em %s", hubsaudeDir)
	}

	// Extrair o hash para o teste de validação
	// Formato esperado: "Assinatura gerada com sucesso: [HASH]"
	start := strings.Index(output, "[")
	end := strings.Index(output, "]")
	if start == -1 || end == -1 || end <= start {
		t.Fatalf("Não foi possível extrair o hash da saída: %s", output)
	}
	hash := output[start+1 : end]

	// 2. Executar validate
	cmdVal := exec.Command("go", "run", "./cmd/assinatura", "validate", "--input", "teste-integracao", "--hash", hash)
	cmdVal.Dir = projectRoot
	outVal, err := cmdVal.CombinedOutput()
	if err != nil {
		t.Fatalf("Erro no validate: %v. Saída: %s", err, string(outVal))
	}

	if !strings.Contains(string(outVal), "[VALID]") {
		t.Errorf("Validação deveria ser bem sucedida. Saída: %s", string(outVal))
	}
}
