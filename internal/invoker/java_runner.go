package invoker 

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
)

func LocalizarJava() (string, error) {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		return "", fmt.Errorf("erro ao buscar o diretório home: %w", err)
	}

	javaCmd := "java"
	if runtime.GOOS == "windows" {
		javaCmd = "java.exe"
	}

	hubsaudeJavaPath := filepath.Join(homeDir, ".hubsaude", "jdk", "bin", javaCmd)
	if info, err := os.Stat(hubsaudeJavaPath); err == nil && !info.IsDir() {
		return hubsaudeJavaPath, nil
	}

	systemJavaPath, err := exec.LookPath("java")
	if err == nil {
		return systemJavaPath, nil
	}

	return "", fmt.Errorf("java não encontrado. Verifique se está instalado ou em %s", hubsaudeJavaPath)
}

func ExecutarAssinador(jarPath, comando, input string) (string, error) {
	javaPath, err := LocalizarJava()
	if err != nil {
		return "", err
	}

	cmd := exec.Command(javaPath, "-jar", jarPath, comando, "--input", input)

	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	err = cmd.Run()
	if err != nil {
		return "", fmt.Errorf("falha ao executar processo Java: %w\nDetalhes (stderr): %s", err, stderr.String())
	}

	return stdout.String(), nil
}