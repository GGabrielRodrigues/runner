package jdk

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
)

type Config struct {
	ManagedJDK string `json:"managed_jdk"`
}

func saveConfig(jdkPath string) error {
	home, err := os.UserHomeDir()
	if err != nil {
		return err
	}
	configDir := filepath.Join(home, ".hubsaude")
	configPath := filepath.Join(configDir, "config.json")
	
	if err := os.MkdirAll(configDir, 0755); err != nil {
		return err
	}
	
	config := Config{ManagedJDK: jdkPath}
	data, err := json.MarshalIndent(config, "", "  ")
	if err != nil {
		return err
	}

	return os.WriteFile(configPath, data, 0644)
}

func loadConfig() (*Config, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return nil, err
	}
	configPath := filepath.Join(home, ".hubsaude", "config.json")

	data, err := os.ReadFile(configPath)
	if err != nil {
		return nil, err
	}

	var config Config
	if err := json.Unmarshal(data, &config); err != nil {
		return nil, err
	}
	return &config, nil
}
func IsJava21Present() (string, bool) {
	if cfg, err := loadConfig(); err == nil && cfg.ManagedJDK != "" {
		if verifyJavaVersion(cfg.ManagedJDK) {
			return cfg.ManagedJDK, true
		}
	}
	managedPath, err := getManagedJavaPath()
	if err == nil {
		if verifyJavaVersion(managedPath) {
			saveConfig(managedPath)
			return managedPath, true
		}
	}
	pathJava, err := exec.LookPath("java")
	if err == nil {
		if verifyJavaVersion(pathJava) {
			saveConfig(pathJava)
			return pathJava, true
		}
	}

	return "", false
}

func getManagedJavaPath() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}

	javaBin := "java"
	if runtime.GOOS == "windows" {
		javaBin = "java.exe"
	}
	return filepath.Join(home, ".hubsaude", "jdk", "bin", javaBin), nil
}

func verifyJavaVersion(javaPath string) bool {
	cmd := exec.Command(javaPath, "-version")
	output, err := cmd.CombinedOutput()
	if err != nil {
		return false
	}
	return strings.Contains(string(output), "21.")
}
func GetJDKURL() string {
	osName := runtime.GOOS
	arch := runtime.GOARCH
	adoptOS := osName
	if osName == "darwin" {
		adoptOS = "mac"
	}

	adoptArch := arch
	if arch == "amd64" {
		adoptArch = "x64"
	}

	return fmt.Sprintf("https://api.adoptium.net/v3/binary/latest/21/ga/%s/%s/jdk/hotspot/normal/eclipse", adoptOS, adoptArch)
}
func ProvisionJDK() error {
	home, err := os.UserHomeDir()
	if err != nil {
		return err
	}

	baseDir := filepath.Join(home, ".hubsaude")
	jdkDir := filepath.Join(baseDir, "jdk")

	if err := os.MkdirAll(baseDir, 0755); err != nil {
		return err
	}

	url := GetJDKURL()
	fmt.Printf("Baixando JDK 21 de: %s\n", url)

	resp, err := http.Get(url)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("falha ao baixar JDK: status %s", resp.Status)
	}
	tmpFile, err := os.CreateTemp("", "jdk-download-*.tmp")
	if err != nil {
		return err
	}
	defer os.Remove(tmpFile.Name())
	defer tmpFile.Close()

	_, err = io.Copy(tmpFile, resp.Body)
	if err != nil {
		return err
	}
	_, err = tmpFile.Seek(0, 0)
	if err != nil {
		return err
	}
	os.RemoveAll(jdkDir)
	if err := os.MkdirAll(jdkDir, 0755); err != nil {
		return err
	}

	fmt.Println("Extraindo JDK...")
	var errExt error
	if strings.HasSuffix(resp.Request.URL.Path, ".zip") || runtime.GOOS == "windows" {
		errExt = extractZip(tmpFile, jdkDir)
	} else {
		errExt = extractTarGz(tmpFile, jdkDir)
	}

	if errExt != nil {
		return errExt
	}

	managedPath, _ := getManagedJavaPath()
	return saveConfig(managedPath)
}

func extractTarGz(r io.Reader, dest string) error {
	gzr, err := gzip.NewReader(r)
	if err != nil {
		return err
	}
	defer gzr.Close()

	tr := tar.NewReader(gzr)
	var commonPrefix string

	for {
		header, err := tr.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		if commonPrefix == "" {
			parts := strings.Split(header.Name, "/")
			if len(parts) > 1 {
				commonPrefix = parts[0]
			}
		}

		target := header.Name
		if commonPrefix != "" && strings.HasPrefix(target, commonPrefix+"/") {
			target = strings.TrimPrefix(target, commonPrefix+"/")
		}

		if target == "" {
			continue
		}

		targetPath := filepath.Join(dest, target)

		switch header.Typeflag {
		case tar.TypeDir:
			if err := os.MkdirAll(targetPath, 0755); err != nil {
				return err
			}
		case tar.TypeReg:
			f, err := os.OpenFile(targetPath, os.O_CREATE|os.O_RDWR, os.FileMode(header.Mode))
			if err != nil {
				return err
			}
			if _, err := io.Copy(f, tr); err != nil {
				f.Close()
				return err
			}
			f.Close()
		}
	}
	return nil
}

func extractZip(f *os.File, dest string) error {
	info, err := f.Stat()
	if err != nil {
		return err
	}

	zr, err := zip.NewReader(f, info.Size())
	if err != nil {
		return err
	}

	var commonPrefix string
	for _, file := range zr.File {
		if commonPrefix == "" {
			parts := strings.Split(file.Name, "/")
			if len(parts) > 1 {
				commonPrefix = parts[0]
			} else {
				parts = strings.Split(file.Name, "\\")
				if len(parts) > 1 {
					commonPrefix = parts[0]
				}
			}
		}

		target := file.Name
		if commonPrefix != "" && (strings.HasPrefix(target, commonPrefix+"/") || strings.HasPrefix(target, commonPrefix+"\\")) {
			target = target[len(commonPrefix)+1:]
		}

		if target == "" {
			continue
		}

		targetPath := filepath.Join(dest, target)

		if file.FileInfo().IsDir() {
			os.MkdirAll(targetPath, 0755)
			continue
		}

		if err := os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
			return err
		}

		dstFile, err := os.OpenFile(targetPath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, file.Mode())
		if err != nil {
			return err
		}

		srcFile, err := file.Open()
		if err != nil {
			dstFile.Close()
			return err
		}

		if _, err := io.Copy(dstFile, srcFile); err != nil {
			dstFile.Close()
			srcFile.Close()
			return err
		}

		dstFile.Close()
		srcFile.Close()
	}
	return nil
}

