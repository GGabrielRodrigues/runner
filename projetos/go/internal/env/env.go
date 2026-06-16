package env

import (
	"os"
	"path/filepath"
	"runtime"
)

// GetHubSaudeDir returns the path to the hubsaude directory.
// It prioritizes standard OS data directories and falls back to the user's home directory.
func GetHubSaudeDir() string {
	var baseDir string

	switch runtime.GOOS {
	case "linux":
		baseDir = os.Getenv("XDG_DATA_HOME")
		if baseDir == "" {
			home, _ := os.UserHomeDir()
			if home != "" {
				baseDir = filepath.Join(home, ".local", "share")
			}
		}
	case "windows":
		baseDir = os.Getenv("LOCALAPPDATA")
	case "darwin":
		home, _ := os.UserHomeDir()
		if home != "" {
			baseDir = filepath.Join(home, "Library", "Application Support")
		}
	}

	if baseDir != "" {
		// Use .hubsaude as requested by the user
		target := filepath.Join(baseDir, ".hubsaude")
		if err := os.MkdirAll(target, 0755); err == nil {
			return target
		}
	}

	// Fallback to HOME
	home, _ := os.UserHomeDir()
	if home == "" {
		return ".hubsaude" // Extreme fallback
	}
	target := filepath.Join(home, ".hubsaude")
	_ = os.MkdirAll(target, 0755)
	return target
}
