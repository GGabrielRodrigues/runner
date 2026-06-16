package release

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"github.com/GGabrielRodrigues/runner/internal/env"
)

const ReleaseURL = "https://raw.githubusercontent.com/GGabrielRodrigues/runner/main/release.json"

type ArtifactInfo struct {
	Version string `json:"version"`
	URL     string `json:"url"`
}

type ReleaseInfo struct {
	Assinador ArtifactInfo `json:"assinador"`
	Simulador ArtifactInfo `json:"simulador"`
}

func FetchReleaseInfo() (*ReleaseInfo, error) {
	resp, err := http.Get(ReleaseURL)
	if err != nil {
		return nil, fmt.Errorf("falha ao buscar release.json: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("falha ao buscar release.json: status %s", resp.Status)
	}

	var info ReleaseInfo
	if err := json.NewDecoder(resp.Body).Decode(&info); err != nil {
		return nil, fmt.Errorf("falha ao decodificar release.json: %w", err)
	}

	return &info, nil
}

func EnsureArtifact(name, customURL string) (string, error) {
	baseDir := env.GetHubSaudeDir()
	artifactPath := filepath.Join(baseDir, name+".jar")
	versionPath := filepath.Join(baseDir, name+".version")

	if err := os.MkdirAll(baseDir, 0755); err != nil {
		return "", err
	}

	// Se customURL for fornecida, ignoramos a checagem de versão e baixamos direto
	if customURL != "" {
		fmt.Printf("Forçando download de %s a partir de: %s\n", name, customURL)
		if err := downloadFile(customURL, artifactPath); err != nil {
			return "", err
		}
		return artifactPath, nil
	}

	info, err := FetchReleaseInfo()
	if err != nil {
		// Se não conseguir buscar a release, mas o arquivo já existir localmente, usamos o local
		if _, errStat := os.Stat(artifactPath); errStat == nil {
			fmt.Printf("Aviso: Não foi possível verificar atualizações para %s. Usando versão local.\n", name)
			return artifactPath, nil
		}
		return "", err
	}

	var target ArtifactInfo
	switch name {
	case "assinador":
		target = info.Assinador
	case "simulador":
		target = info.Simulador
	default:
		return "", fmt.Errorf("artefato desconhecido: %s", name)
	}

	localVersionBytes, _ := os.ReadFile(versionPath)
	localVersion := string(localVersionBytes)

	if localVersion == target.Version {
		if _, err := os.Stat(artifactPath); err == nil {
			return artifactPath, nil
		}
	}

	// Se já existe uma versão local diferente, pergunta antes de atualizar
	if localVersion != "" {
		if _, err := os.Stat(artifactPath); err == nil {
			fmt.Printf("Uma nova versão de '%s' está disponível: %s -> %s\n", name, localVersion, target.Version)
			fmt.Print("Deseja atualizar agora? (s/N): ")
			var response string
			fmt.Scanln(&response)
			response = strings.ToLower(strings.TrimSpace(response))
			if response != "s" && response != "sim" {
				fmt.Printf("Mantendo versão local %s de %s.\n", localVersion, name)
				return artifactPath, nil
			}
		}
	}

	fmt.Printf("Atualizando %s para a versão %s...\n", name, target.Version)
	if err := downloadFile(target.URL, artifactPath); err != nil {
		return "", err
	}

	os.WriteFile(versionPath, []byte(target.Version), 0644)
	return artifactPath, nil
}

func downloadFile(url, dest string) error {
	resp, err := http.Get(url)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("falha no download: status %s", resp.Status)
	}

	out, err := os.Create(dest)
	if err != nil {
		return err
	}
	defer out.Close()

	_, err = io.Copy(out, resp.Body)
	return err
}
