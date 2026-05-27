package invoker

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"

	"github.com/GGabrielRodrigues/runner/internal/jdk"
)

func LocalizarJar() (string, error) {
	home, err := os.UserHomeDir()
	if err == nil {
		managedPath := filepath.Join(home, ".hubsaude", "assinador.jar")
		if _, err := os.Stat(managedPath); err == nil {
			return managedPath, nil
		}
	}
	if _, err := os.Stat("assinador/assinador.jar"); err == nil {
		return "assinador/assinador.jar", nil
	}
	devPath := "projetos/assinador-java/target/assinador.jar"
	if _, err := os.Stat(devPath); err == nil {
		return devPath, nil
	}

	return "", fmt.Errorf("assinador.jar não encontrado")
}

func LocalizarJava() (string, error) {
	javaPath, found := jdk.IsJava21Present()
	if found {
		return javaPath, nil
	}

	fmt.Println("Java 21 não encontrado. Iniciando provisionamento automático...")
	err := jdk.ProvisionJDK()
	if err != nil {
		return "", fmt.Errorf("falha ao provisionar JDK 21: %w", err)
	}

	javaPath, found = jdk.IsJava21Present()
	if found {
		return javaPath, nil
	}

	return "", fmt.Errorf("java 21 não encontrado mesmo após provisionamento")
}

func ExecutarAssinador(comando, input string) (string, error) {
	javaPath, err := LocalizarJava()
	if err != nil {
		return "", err
	}

	jarPath, err := LocalizarJar()
	if err != nil {
		return "", err
	}

	cmd := exec.Command(javaPath, "-jar", jarPath, comando, input)

	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	err = cmd.Run()
	if err != nil {
		return "", fmt.Errorf("falha ao executar processo Java: %w\nDetalhes (stderr): %s", err, stderr.String())
	}

	return stdout.String(), nil
}
func IniciarServidor(port int, timeout int, pkcs11Lib string) error {
	javaPath, err := LocalizarJava()
	if err != nil {
		return err
	}

	jarPath, err := LocalizarJar()
	if err != nil {
		return err
	}
	args := []string{
		"-jar", jarPath,
		"server",
		fmt.Sprintf("--port=%d", port),
		fmt.Sprintf("--timeout=%d", timeout),
	}
	
	if pkcs11Lib != "" {
		args = append(args, fmt.Sprintf("--pkcs11-lib=%s", pkcs11Lib))
	}

	cmd := exec.Command(javaPath, args...)
	cmd.Stdin = nil
	cmd.Stdout = nil
	cmd.Stderr = nil
	err = cmd.Start()
	if err != nil {
		return fmt.Errorf("falha ao iniciar o servidor Java em background: %w", err)
	}
	err = SalvarEstado(cmd.Process.Pid, port)
	if err != nil {
		return fmt.Errorf("servidor iniciou (PID %d), mas falhou ao salvar estado: %w", cmd.Process.Pid, err)
	}

	return nil
}