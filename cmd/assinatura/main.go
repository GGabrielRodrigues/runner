package main

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

// version é preenchida em tempo de build via -ldflags.
// O valor padrão "dev" é usado para execuções locais (go run).
var version = "dev"

func main() {
	// Comando principal
	rootCmd := &cobra.Command{
		Use: "assinatura",
		Short: "Sistema Runner = Utilitário de Assinatura Digital",
	}

	// Subcomando version
	versionCmd := &cobra.Command{
		Use: "version",
		Short: "Exibe a versão atual do CLI",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println(version)
		},
	}

	// Adiciona o subcomando ao principal
	rootCmd.AddCommand(versionCmd)

	// Executa o CLI
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
