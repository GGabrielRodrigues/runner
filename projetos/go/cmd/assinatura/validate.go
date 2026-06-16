package main

import (
	"encoding/json"
	"fmt"
	"strings"

	"github.com/GGabrielRodrigues/runner/internal/invoker"
	"github.com/spf13/cobra"
)

var (
	validateInput string
	validateHash  string
	validateLocal bool
)

type RespostaValidacao struct {
	Status        string `json:"status"`
	SignatureHash string `json:"signatureHash"`
	Timestamp     string `json:"timestamp"`
}

type ValidationRequest struct {
	PayloadBase64 string `json:"payloadBase64"`
	SignerName    string `json:"signerName"`
	SignatureHash string `json:"signatureHash"`
}

var validateCmd = &cobra.Command{
	Use:   "validate",
	Short: "Valida uma assinatura digital",
	Long:  `Invoca o assinador para validar uma assinatura digital baseada no input fornecido.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		req := ValidationRequest{
			PayloadBase64: validateInput,
			SignerName:    "Usuario CLI",
			SignatureHash: validateHash,
		}

		reqJSON, err := json.Marshal(req)
		if err != nil {
			return fmt.Errorf("erro ao preparar requisição: %w", err)
		}

		saidaTerminal, err := invoker.ExecutarAssinador("validate", string(reqJSON))

		if err != nil {
			return fmt.Errorf("Erro na validação da assinatura:\n%w", err)
		}

		var resp RespostaValidacao
		errParse := json.Unmarshal([]byte(saidaTerminal), &resp)
		if errParse != nil {
			saidaLimpa := strings.TrimSpace(saidaTerminal)
			fmt.Printf("Resultado da validação: %s\n", saidaLimpa)
			return nil
		}

		fmt.Printf("Resultado da validação: [%s] (Hash: %s)\n", resp.Status, resp.SignatureHash)

		return nil
	},
}

func init() {
	validateCmd.Flags().StringVar(&validateInput, "input", "", "Caminho do arquivo ou string de entrada (Obrigatório)")
	validateCmd.Flags().StringVar(&validateHash, "hash", "", "Hash da assinatura para validar (Obrigatório)")
	validateCmd.Flags().BoolVar(&validateLocal, "local", true, "Define se a execução será local (default true para esta sprint)")
	validateCmd.MarkFlagRequired("input")
	validateCmd.MarkFlagRequired("hash")
}

