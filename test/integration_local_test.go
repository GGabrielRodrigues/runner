package test

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
)

func TestIntegrationLocal(t *testing.T) {
	tempHome, err := os.MkdirTemp("", "runner-test-home-*")
	if err != nil {
		t.Fatalf("falha ao criar HOME temporário: %v", err)
	}
	defer os.RemoveAll(tempHome)
	originalHome := os.Getenv("HOME")
	os.Setenv("HOME", tempHome)
	defer os.Setenv("HOME", originalHome)
	
	originalUserProfile := os.Getenv("USERPROFILE")
	os.Setenv("USERPROFILE", tempHome)
	defer os.Setenv("USERPROFILE", originalUserProfile)
	projectRoot, err := os.Getwd()
	if err != nil {
		t.Fatalf("falha ao obter diretório atual: %v", err)
	}
	projectRoot = filepath.Dir(projectRoot)
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
	hubsaudeDir := filepath.Join(tempHome, ".hubsaude")
	if _, err := os.Stat(hubsaudeDir); os.IsNotExist(err) {
		t.Errorf("Diretório .hubsaude não foi criado em %s", tempHome)
	}
	start := strings.Index(output, "[")
	end := strings.Index(output, "]")
	if start == -1 || end == -1 || end <= start {
		t.Fatalf("Não foi possível extrair o hash da saída: %s", output)
	}
	hash := output[start+1 : end]
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
