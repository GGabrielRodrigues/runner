package test

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
)

func TestIntegrationLocal(t *testing.T) {
	// Criar diretório temporário para simular o HOME
	tempHome, err := os.MkdirTemp("", "runner-test-home-*")
	if err != nil {
		t.Fatalf("falha ao criar HOME temporário: %v", err)
	}
	defer os.RemoveAll(tempHome)

	// Configurar variável de ambiente HOME (e USERPROFILE no Windows)
	originalHome := os.Getenv("HOME")
	os.Setenv("HOME", tempHome)
	defer os.Setenv("HOME", originalHome)
	
	originalUserProfile := os.Getenv("USERPROFILE")
	os.Setenv("USERPROFILE", tempHome)
	defer os.Setenv("USERPROFILE", originalUserProfile)

	// Pegar o diretório raiz do projeto para rodar o 'go run'
	projectRoot, err := os.Getwd()
	if err != nil {
		t.Fatalf("falha ao obter diretório atual: %v", err)
	}
	// Como estamos em 'test/', a raiz é o pai
	projectRoot = filepath.Dir(projectRoot)

	// 1. Executar sign (deve disparar download do JDK se não estiver no PATH, 
	// mas como estamos num ambiente de dev, o PATH provavelmente tem Java. 
	// No entanto, o teste garante que o fluxo completa.)
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

	// Verificar se ~/.hubsaude foi criado
	hubsaudeDir := filepath.Join(tempHome, ".hubsaude")
	if _, err := os.Stat(hubsaudeDir); os.IsNotExist(err) {
		t.Errorf("Diretório .hubsaude não foi criado em %s", tempHome)
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
