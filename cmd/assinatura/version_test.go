package main

import (
	"os/exec"
	"strings"
	"testing"
)

func TestVersionCommand(t *testing.T) {
	// Executa "go run . version" dentro da pasta do comando assinatura
	// O comando exec.Command prepara a chamada ao sistema operacional
	cmd := exec.Command("go", "run", ".", "version")
	
	// Captura a saída combinada (stdout + stderr)
	out, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Erro ao executar comando: %v. Saída: %s", err, string(out))
	}

	// Remove espaços em branco e verifica se contém "dev"
	got := strings.TrimSpace(string(out))
	want := "dev"

	if got != want {
		t.Errorf("O comando version deveria retornar %q, mas retornou %q", want, got)
	}
}
