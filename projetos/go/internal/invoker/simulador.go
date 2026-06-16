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

	"github.com/GGabrielRodrigues/runner/internal/env"
	"github.com/GGabrielRodrigues/runner/internal/release"
)

const DefaultSimulatorPort = 8443

func GetSimulatorPIDPath() (string, error) {
	return filepath.Join(env.GetHubSaudeDir(), "simulador.pid"), nil
}

func IsPortAvailable(port int) bool {
	ln, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		return false
	}
	ln.Close()
	return true
}

func StartSimulador(customURL string, port int) error {
	if !IsPortAvailable(port) {
		return fmt.Errorf("porta %d já está em uso", port)
	}

	javaPath, err := LocalizarJava()
	if err != nil {
		return err
	}

	jarPath, err := release.EnsureArtifact("simulador", customURL)
	if err != nil {
		return fmt.Errorf("falha ao garantir simulador.jar: %w", err)
	}

	logPath := filepath.Join(env.GetHubSaudeDir(), "simulador.log")
	// Usar O_TRUNC para limpar o log a cada nova tentativa de start, facilitando o debug
	logFile, err := os.OpenFile(logPath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		return fmt.Errorf("falha ao criar arquivo de log: %w", err)
	}

	// Forçamos a porta definida
	cmd := exec.Command(javaPath, "-jar", jarPath, "--server.port="+strconv.Itoa(port))
	cmd.Stdout = logFile
	cmd.Stderr = logFile

	if err := cmd.Start(); err != nil {
		logFile.Close()
		return fmt.Errorf("falha ao iniciar simulador: %w", err)
	}

	pidPath, _ := GetSimulatorPIDPath()
	os.WriteFile(pidPath, []byte(strconv.Itoa(cmd.Process.Pid)), 0644)

	fmt.Printf("Simulador iniciado em background (PID: %d). Logs em: %s\n", cmd.Process.Pid, logPath)
	fmt.Println("Aguardando o simulador estar pronto (readiness check)...")

	if err := WaitForReadiness(30*time.Minute, port); err != nil {
		return fmt.Errorf("simulador iniciado mas não ficou pronto a tempo: %w", err)
	}

	fmt.Println("Simulador está pronto para uso.")
	return nil
}

// IsReady checks if the simulator is fully ready to handle requests.
func IsReady(port int) bool {
	client := &http.Client{
		Timeout: 2 * time.Second,
	}

	// Endpoints prioritários para readiness
	readinessEndpoints := []string{
		"/actuator/health/readiness", // Padrão Spring Boot 2.3+
		"/actuator/health",           // Fallback comum
		"/api/info",                  // Mencionado na especificação
	}

	for _, ep := range readinessEndpoints {
		resp, err := client.Get(fmt.Sprintf("http://localhost:%d%s", port, ep))
		if err != nil {
			continue
		}
		defer resp.Body.Close()

		if resp.StatusCode == http.StatusOK {
			// No caso do /actuator/health, verificamos se o status é "UP"
			if strings.Contains(ep, "health") {
				body, _ := io.ReadAll(resp.Body)
				if strings.Contains(strings.ToUpper(string(body)), "\"UP\"") {
					return true
				}
				continue
			}
			return true
		}
	}
	return false
}

// WaitForReadiness waits for the simulator to be ready using exponential backoff.
func WaitForReadiness(timeout time.Duration, port int) error {
	start := time.Now()
	interval := 1 * time.Second
	maxInterval := 30 * time.Second

	for time.Since(start) < timeout {
		if IsReady(port) {
			return nil
		}

		fmt.Printf("Simulador ainda não está pronto na porta %d. Tentando novamente em %v...\n", port, interval)
		time.Sleep(interval)

		interval *= 2
		if interval > maxInterval {
			interval = maxInterval
		}
	}

	return fmt.Errorf("timeout de %v atingido aguardando o simulador na porta %d", timeout, port)
}

func GetSimuladorStatus(port int) (string, error) {
	client := &http.Client{
		Timeout: 5 * time.Second,
	}

	// Tentamos o endpoint da especificação
	endpoints := []string{"/api/info", "/actuator/info", "/actuator/health"}
	var lastErr error

	for _, ep := range endpoints {
		resp, err := client.Get(fmt.Sprintf("http://localhost:%d%s", port, ep))
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

func StopSimulador(port int) error {
	client := &http.Client{
		Timeout: 5 * time.Second,
	}

	// Tentamos os caminhos prováveis de shutdown
	endpoints := []string{"/shutdown", "/actuator/shutdown"}

	for _, ep := range endpoints {
		resp, err := client.Post(fmt.Sprintf("http://localhost:%d%s", port, ep), "application/json", nil)
		if err == nil {
			defer resp.Body.Close()
			if resp.StatusCode == http.StatusOK {
				fmt.Printf("Comando de shutdown enviado para %s com sucesso na porta %d.\n", ep, port)
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
