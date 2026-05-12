package main

import (
	"fmt"
	"strings"

	"github.com/GGabrielRodrigues/runner/internal/invoker"
	"github.com/spf13/cobra"
)

var (
	validateInput string
	validateLocal bool
)

var validateCmd = &cobra.Command{
	Use:   "validate",
	Short: "Valida uma assinatura digital",
	Long:  `Invoca o assinador para validar uma assinatura digital baseada no input fornecido.`,
	RunE: func(cmd *cobra.Command, args []string) error {
		jarPath := "assinador/assinador.jar"

		saidaTerminal, err := invoker.ExecutarAssinador(jarPath, "validate", validateInput)

		if err != nil {
			return fmt.Errorf("Erro na validação da assinatura:\n%w", err)
		}

		saidaLimpa := strings.TrimSpace(saidaTerminal)
		fmt.Printf("Resultado da validação: %s\n", saidaLimpa)

		return nil
	},
}

func init() {
	validateCmd.Flags().StringVar(&validateInput, "input", "", "Caminho do arquivo ou string de entrada (Obrigatório)")
	validateCmd.Flags().BoolVar(&validateLocal, "local", true, "Define se a execução será local (default true para esta sprint)")
	validateCmd.MarkFlagRequired("input")
}
