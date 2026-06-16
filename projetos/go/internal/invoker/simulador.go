package invoker

import (
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/GGabrielRodrigues/runner/internal/release"
)

const SimulatorPort = 8443

func GetSimulatorPIDPath() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	return filepath.Join(home, ".hubsaude", "simulador.pid"), nil
}

func IsPortAvailable(port int) bool {
	ln, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		return false
	}
	ln.Close()
	return true
}

func StartSimulador(customURL string) error {
	if !IsPortAvailable(SimulatorPort) {
		return fmt.Errorf("porta %d já está em uso", SimulatorPort)
	}

	javaPath, err := LocalizarJava()
	if err != nil {
		return err
	}

	jarPath, err := release.EnsureArtifact("simulador", customURL)
	if err != nil {
		return fmt.Errorf("falha ao garantir simulador.jar: %w", err)
	}

	home, _ := os.UserHomeDir()
	logPath := filepath.Join(home, ".hubsaude", "simulador.log")
	// Usar O_TRUNC para limpar o log a cada nova tentativa de start, facilitando o debug
	logFile, err := os.OpenFile(logPath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		return fmt.Errorf("falha ao criar arquivo de log: %w", err)
	}

	// Forçamos a porta definida na constante SimulatorPort (8443)
	cmd := exec.Command(javaPath, "-jar", jarPath, "--server.port="+strconv.Itoa(SimulatorPort))
	cmd.Stdout = logFile
	cmd.Stderr = logFile

	if err := cmd.Start(); err != nil {
		logFile.Close()
		return fmt.Errorf("falha ao iniciar simulador: %w", err)
	}

	pidPath, _ := GetSimulatorPIDPath()
	os.WriteFile(pidPath, []byte(strconv.Itoa(cmd.Process.Pid)), 0644)

	fmt.Printf("Simulador iniciado em background (PID: %d). Logs em: %s\n", cmd.Process.Pid, logPath)
	return nil
}

func GetSimuladorStatus() (string, error) {
	client := &http.Client{
		Timeout: 5 * time.Second,
	}

	// Tentamos o endpoint da especificação
	endpoints := []string{"/api/info", "/actuator/info", "/actuator/health"}
	var lastErr error

	for _, ep := range endpoints {
		resp, err := client.Get(fmt.Sprintf("http://localhost:%d%s", SimulatorPort, ep))
		if err != nil {
			lastErr = err
			continue
		}
		defer resp.Body.Close()

		if resp.StatusCode == http.StatusOK {
			body, _ := io.ReadAll(resp.Body)
			return string(body), nil
		}
		lastErr = fmt.Errorf("endpoint %s retornou status %d", ep, resp.StatusCode)
	}

	return "", fmt.Errorf("não foi possível obter o status: %v", lastErr)
}

func StopSimulador() error {
	client := &http.Client{
		Timeout: 5 * time.Second,
	}

	// Tentamos os caminhos prováveis de shutdown
	endpoints := []string{"/shutdown", "/actuator/shutdown"}

	for _, ep := range endpoints {
		resp, err := client.Post(fmt.Sprintf("http://localhost:%d%s", SimulatorPort, ep), "application/json", nil)
		if err == nil {
			defer resp.Body.Close()
			if resp.StatusCode == http.StatusOK {
				fmt.Printf("Comando de shutdown enviado para %s com sucesso.\n", ep)
				cleanupPIDFile()
				return nil
			}
		}
	}

	// Se falhar via HTTP, matamos pelo PID
	return killSimuladorByPID()
}

func killSimuladorByPID() error {
	pidPath, _ := GetSimulatorPIDPath()
	data, err := os.ReadFile(pidPath)
	if err != nil {
		return fmt.Errorf("não foi possível encontrar o PID do simulador: %w", err)
	}

	pid, err := strconv.Atoi(strings.TrimSpace(string(data)))
	if err != nil {
		return fmt.Errorf("PID inválido no arquivo: %w", err)
	}

	process, err := os.FindProcess(pid)
	if err != nil {
		return fmt.Errorf("processo não encontrado: %w", err)
	}

	if err := process.Kill(); err != nil {
		return fmt.Errorf("falha ao matar processo: %w", err)
	}

	fmt.Printf("Processo %d encerrado forçadamente.\n", pid)
	cleanupPIDFile()
	return nil
}

func cleanupPIDFile() {
	pidPath, _ := GetSimulatorPIDPath()
	os.ReadFile(pidPath) // dummy read
	os.Remove(pidPath)
}
