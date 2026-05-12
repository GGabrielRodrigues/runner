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
	Hash string `json:"hash"`
}

var signCmd = &cobra.Command{
	Use:   "sign",
	Short: "Gera uma assinatura digital",
	Long:  `Invoca o assinador para gerar uma assinatura digital a partir de um arquivo ou string de entrada.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		jarPath := "assinador/assinador.jar"

		saidaTerminal, err := invoker.ExecutarAssinador(jarPath, "sign", signInput)

		if err != nil {
			return fmt.Errorf("Erro na execução do assinador:\n%w", err)
		}

		var resp RespostaAssinatura

		errParse := json.Unmarshal([]byte(saidaTerminal), &resp)
		if errParse != nil || resp.Hash == "" {
			saidaLimpa := strings.TrimSpace(saidaTerminal)
			fmt.Printf("Assinatura gerada com sucesso: [%s]\n", saidaLimpa)
			return nil
		}

		fmt.Printf("Assinatura gerada com sucesso: [%s]\n", resp.Hash)
		return nil
	},
}

func init() {
	signCmd.Flags().StringVar(&signInput, "input", "", "Caminho do arquivo ou string de entrada (Obrigatório)")
	signCmd.Flags().BoolVar(&signLocal, "local", true, "Define se a execução será local (default true para esta sprint)")
	signCmd.MarkFlagRequired("input")
}
