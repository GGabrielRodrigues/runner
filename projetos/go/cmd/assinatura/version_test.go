package main

import (
	"os/exec"
	"strings"
	"testing"
)

func TestVersionCommand(t *testing.T) {
	cmd := exec.Command("go", "run", ".", "version")

	out, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("Erro ao executar comando: %v. Saída: %s", err, string(out))
	}

	got := strings.TrimSpace(string(out))
	want := "dev"

	if got != want {
		t.Errorf("O comando version deveria retornar %q, mas retornou %q", want, got)
	}
}
