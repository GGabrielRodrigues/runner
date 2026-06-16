package main

import (
	"fmt"
	"os"

	"github.com/GGabrielRodrigues/runner/internal/invoker"
	"github.com/spf13/cobra"
)

var (
	version   = "dev"
	sourceURL string
	simPort   int
)

var rootCmd = &cobra.Command{
	Use:   "simulador",
	Short: "Gerenciador do Simulador HubSaúde",
	Long:  `O simulador é um orquestrador que gerencia o ciclo de vida do ambiente de testes do HubSaúde.`,
}

var startCmd = &cobra.Command{
	Use:   "start",
	Short: "Inicia o simulador em background",
	Run: func(cmd *cobra.Command, args []string) {
		err := invoker.StartSimulador(sourceURL, simPort)
		if err != nil {
			fmt.Printf("Erro ao iniciar simulador: %v\n", err)
			os.Exit(1)
		}
	},
}

var stopCmd = &cobra.Command{
	Use:   "stop",
	Short: "Para o simulador em execução",
	Run: func(cmd *cobra.Command, args []string) {
		err := invoker.StopSimulador(simPort)
		if err != nil {
			fmt.Printf("Erro ao parar simulador: %v\n", err)
			os.Exit(1)
		}
	},
}

var statusCmd = &cobra.Command{
	Use:   "status",
	Short: "Exibe o status atual do simulador",
	Run: func(cmd *cobra.Command, args []string) {
		status, err := invoker.GetSimuladorStatus(simPort)
		if err != nil {
			fmt.Printf("Status: OFFLINE na porta %d (%v)\n", simPort, err)
			return
		}
		fmt.Printf("Status: ONLINE na porta %d\n%s\n", simPort, status)
	},
}

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Exibe a versão do CLI simulador",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("simulador v%s\n", version)
	},
}

func init() {
	rootCmd.PersistentFlags().IntVar(&simPort, "port", invoker.DefaultSimulatorPort, "Porta para rodar/acessar o simulador")
	startCmd.Flags().StringVar(&sourceURL, "source", "", "URL alternativa para baixar o simulador.jar")
	rootCmd.AddCommand(startCmd, stopCmd, statusCmd, versionCmd)
}

func main() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
