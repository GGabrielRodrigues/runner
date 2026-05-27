package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

type SignatureClient struct {
	BaseURL    string
	HTTPClient *http.Client
}

func NewSignatureClient(port int) *SignatureClient {
	return &SignatureClient{
		BaseURL: fmt.Sprintf("http://localhost:%d", port),
		HTTPClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}
func (c *SignatureClient) EnviarRequisicao(endpoint string, payload string) (string, error) {
	url := fmt.Sprintf("%s/%s", c.BaseURL, endpoint)
	req, err := http.NewRequest("POST", url, bytes.NewBuffer([]byte(payload)))
	if err != nil {
		return "", fmt.Errorf("erro ao criar requisição: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("falha ao comunicar com o servidor: %w", err)
	}
	defer resp.Body.Close()

	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("erro ao ler resposta: %w", err)
	}
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return "", fmt.Errorf("erro do servidor (Status %d): %s", resp.StatusCode, string(bodyBytes))
	}

	return formatarSaidaJSON(bodyBytes)
}
func formatarSaidaJSON(data []byte) (string, error) {
	var out bytes.Buffer
	if err := json.Indent(&out, data, "", "  "); err != nil {
		return string(data), nil
	}
	return out.String(), nil
}