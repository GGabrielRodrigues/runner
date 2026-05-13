package main

import (
	"encoding/json"
	"fmt"
	"strings"

	"github.com/GGabrielRodrigues/runner/internal/invoker"
	"github.com/spf13/cobra"
)

var (
	signInput string
	signLocal bool
)
type RespostaAssinatura struct {
	Status        string `json:"status"`
	SignatureHash string `json:"signatureHash"`
	Timestamp     string `json:"timestamp"`
}

type SignatureRequest struct {
	PayloadBase64 string `json:"payloadBase64"`
	SignerName    string `json:"signerName"`
}

var signCmd = &cobra.Command{
	Use:   "sign",
	Short: "Gera uma assinatura digital",
	Long:  `Invoca o assinador para gerar uma assinatura digital a partir de um arquivo ou string de entrada.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		req := SignatureRequest{
			PayloadBase64: signInput, // Por enquanto enviamos a string direto, a Sprint 3/4 pode exigir base64 real
			SignerName:    "Usuario CLI",
		}

		reqJSON, err := json.Marshal(req)
		if err != nil {
			return fmt.Errorf("erro ao preparar requisição: %w", err)
		}

		saidaTerminal, err := invoker.ExecutarAssinador("sign", string(reqJSON))

		if err != nil {
			return fmt.Errorf("Erro na execução do assinador:\n%w", err)
		}

		var resp RespostaAssinatura

		errParse := json.Unmarshal([]byte(saidaTerminal), &resp)
		if errParse != nil || resp.SignatureHash == "" {
			saidaLimpa := strings.TrimSpace(saidaTerminal)
			fmt.Printf("Assinatura gerada com sucesso: [%s]\n", saidaLimpa)
			return nil
		}

		fmt.Printf("Assinatura gerada com sucesso: [%s]\n", resp.SignatureHash)
		return nil
	},
}

func init() {
	signCmd.Flags().StringVar(&signInput, "input", "", "Caminho do arquivo ou string de entrada (Obrigatório)")
	signCmd.Flags().BoolVar(&signLocal, "local", true, "Define se a execução será local (default true para esta sprint)")
	signCmd.MarkFlagRequired("input")
}
