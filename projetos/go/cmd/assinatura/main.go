package main

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var version = "dev"

func main() {
	rootCmd := &cobra.Command{
		Use:   "assinatura",
		Short: "Sistema Runner = Utilitário de Assinatura Digital",
	}

	versionCmd := &cobra.Command{
		Use:   "version",
		Short: "Exibe a versão atual do CLI",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println(version)
		},
	}

	rootCmd.AddCommand(versionCmd)

	rootCmd.AddCommand(signCmd)
	rootCmd.AddCommand(validateCmd)

	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
