package main

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var version = "dev"
var (
	port      int
	timeout   int
	pkcs11Lib string
	localMode bool
)
var rootCmd = &cobra.Command{
	Use:   "assinatura",
	Short: "Sistema Runner = Utilitário de Assinatura Digital",
}
var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Exibe a versão atual do CLI",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println(version)
	},
}

func init() {
	rootCmd.PersistentFlags().IntVarP(&port, "port", "p", 8080, "Porta para o servidor Java em background")
	rootCmd.PersistentFlags().IntVarP(&timeout, "timeout", "t", 15, "Tempo limite de inatividade em minutos para o servidor desligar")
	rootCmd.PersistentFlags().StringVar(&pkcs11Lib, "pkcs11-lib", "", "Caminho absoluto para a biblioteca PKCS#11 (ex: SoftHSM2)")
	rootCmd.PersistentFlags().BoolVar(&localMode, "local", false, "Executa o assinador de forma síncrona e efêmera (ignora o servidor)")
	rootCmd.AddCommand(versionCmd)
	rootCmd.AddCommand(signCmd)
	rootCmd.AddCommand(validateCmd)
	rootCmd.AddCommand(stopCmd) // Adicionando o comando de parada que criamos
}

func main() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}