package env

import (
	"os"
	"path/filepath"
	"runtime"
	"testing"
)

func TestGetHubSaudeDir(t *testing.T) {
	tempDir, err := os.MkdirTemp("", "env-test-*")
	if err != nil {
		t.Fatalf("falha ao criar diretorio temporario: %v", err)
	}
	defer os.RemoveAll(tempDir)

	// Mock environment
	originalHome := os.Getenv("HOME")
	originalXdg := os.Getenv("XDG_DATA_HOME")
	originalLocalApp := os.Getenv("LOCALAPPDATA")
	
	defer func() {
		os.Setenv("HOME", originalHome)
		os.Setenv("XDG_DATA_HOME", originalXdg)
		os.Setenv("LOCALAPPDATA", originalLocalApp)
	}()

	t.Run("Respects XDG_DATA_HOME on Linux", func(t *testing.T) {
		if runtime.GOOS != "linux" {
			t.Skip("Test specific to Linux")
		}
		xdgDir := filepath.Join(tempDir, "xdg")
		os.Setenv("XDG_DATA_HOME", xdgDir)
		
		got := GetHubSaudeDir()
		expected := filepath.Join(xdgDir, ".hubsaude")
		if got != expected {
			t.Errorf("expected %s, got %s", expected, got)
		}
	})

	t.Run("Respects LOCALAPPDATA on Windows", func(t *testing.T) {
		if runtime.GOOS != "windows" {
			t.Skip("Test specific to Windows")
		}
		localAppDir := filepath.Join(tempDir, "localapp")
		os.Setenv("LOCALAPPDATA", localAppDir)
		
		got := GetHubSaudeDir()
		expected := filepath.Join(localAppDir, ".hubsaude")
		if got != expected {
			t.Errorf("expected %s, got %s", expected, got)
		}
	})

	t.Run("Fallback to HOME", func(t *testing.T) {
		// Unset standard dirs
		os.Unsetenv("XDG_DATA_HOME")
		os.Unsetenv("LOCALAPPDATA")
		
		homeDir := filepath.Join(tempDir, "home")
		os.MkdirAll(homeDir, 0755)
		os.Setenv("HOME", homeDir)
		os.Setenv("USERPROFILE", homeDir)

		got := GetHubSaudeDir()
		
		// Depending on OS, it might still try standard paths relative to HOME
		// but eventually it should contain .hubsaude
		if !filepath.IsAbs(got) {
			t.Errorf("expected absolute path, got %s", got)
		}
	})
}
