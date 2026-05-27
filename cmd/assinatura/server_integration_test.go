package main_test

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
	"time"
)
func getRootDir() string {
	cwd, _ := os.Getwd()
	return filepath.Join(cwd, "..", "..")
}

func TestClientServerIntegration(t *testing.T) {
	rootDir := getRootDir()
	cmdLimpeza := exec.Command("go", "run", "./cmd/assinatura", "stop")
	cmdLimpeza.Dir = rootDir
	cmdLimpeza.Run()

	t.Run("Deve iniciar servidor em background e responder via HTTP", func(t *testing.T) {
cmdSign := exec.Command("go", "run", "./cmd/assinatura", "sign", "--input", `{"payloadBase64":"dGVzdGU=", "signerName":"Teste Unitario"}`)
		cmdSign.Dir = rootDir
		out, err := cmdSign.CombinedOutput()
		
		if err != nil {
			t.Fatalf("Comando sign falhou: %v\nSaída: %s", err, string(out))
		}

		saidaString := string(out)
		if !strings.Contains(saidaString, "Iniciando motor criptográfico em background") {
			t.Errorf("Esperava iniciar o servidor, mas a saída foi:\n%s", saidaString)
		}

		if !strings.Contains(saidaString, "Assinatura gerada com sucesso") {
			t.Errorf("Esperava sucesso na assinatura, mas a saída foi:\n%s", saidaString)
		}
		home, _ := os.UserHomeDir()
		statePath := filepath.Join(home, ".hubsaude", "server.json")
		if _, err := os.Stat(statePath); os.IsNotExist(err) {
			t.Error("Arquivo ~/.hubsaude/server.json não foi criado")
		}
		time.Sleep(1 * time.Second)
		cmdStop := exec.Command("go", "run", "./cmd/assinatura", "stop")
		cmdStop.Dir = rootDir
		stopOut, err := cmdStop.CombinedOutput()
		if err != nil {
			t.Fatalf("Falha ao parar o servidor: %v\nSaída: %s", err, string(stopOut))
		}

		if !strings.Contains(string(stopOut), "Servidor interrompido com sucesso") {
			t.Errorf("Falha na mensagem de interrupção. Saída:\n%s", string(stopOut))
		}
		if _, err := os.Stat(statePath); !os.IsNotExist(err) {
			t.Error("Arquivo ~/.hubsaude/server.json deveria ter sido deletado pelo stop")
		}
	})
}