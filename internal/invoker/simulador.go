package invoker

import (
	"crypto/tls"
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

	cmd := exec.Command(javaPath, "-jar", jarPath)
	
	// Redirecionar logs para arquivo ou descartar para rodar em background
	// Para simplificar agora, vamos descartar. Em produção seria bom logar em ~/.hubsaude/simulador.log
	cmd.Stdout = nil
	cmd.Stderr = nil

	if err := cmd.Start(); err != nil {
		return fmt.Errorf("falha ao iniciar simulador: %w", err)
	}

	pidPath, _ := GetSimulatorPIDPath()
	os.WriteFile(pidPath, []byte(strconv.Itoa(cmd.Process.Pid)), 0644)

	fmt.Printf("Simulador iniciado em background (PID: %d) na porta %d\n", cmd.Process.Pid, SimulatorPort)
	return nil
}

func GetSimuladorStatus() (string, error) {
	client := &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
		Timeout: 5 * time.Second,
	}

	resp, err := client.Get(fmt.Sprintf("https://localhost:%d/api/info", SimulatorPort))
	if err != nil {
		return "", fmt.Errorf("simulador offline ou não responde: %w", err)
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	return string(body), nil
}

func StopSimulador() error {
	client := &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
		Timeout: 5 * time.Second,
	}

	// O Spring Boot Actuator geralmente usa POST para /shutdown, mas o enunciado diz "requisição HTTP"
	// Vamos tentar POST primeiro, se falhar tentamos GET ou sinal de sistema.
	resp, err := client.Post(fmt.Sprintf("https://localhost:%d/shutdown", SimulatorPort), "application/json", nil)
	if err != nil {
		// Fallback: tentar matar o processo pelo PID
		return killSimuladorByPID()
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusOK {
		fmt.Println("Comando de shutdown enviado com sucesso.")
		cleanupPIDFile()
		return nil
	}

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
