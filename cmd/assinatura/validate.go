package main

import (
	"fmt"
	"os"
	"time"

	"github.com/GGabrielRodrigues/runner/internal/client"
	"github.com/GGabrielRodrigues/runner/internal/invoker"
	"github.com/spf13/cobra"
)
var validateInput string

var validateCmd = &cobra.Command{
	Use:   "validate",
	Short: "Valida a assinatura de um payload JSON",
	Run: func(cmd *cobra.Command, args []string) {
		if validateInput == "" {
			fmt.Println("Erro: A flag --input é obrigatória.")
			os.Exit(1)
		}
		if localMode {
			fmt.Println("[Modo Local] Executando processo efêmero para validação...")
			saida, err := invoker.ExecutarAssinador("validate", validateInput)
			if err != nil {
				fmt.Printf("Erro: %v\n", err)
				os.Exit(1)
			}
			fmt.Println(saida)
			return
		}
		state, vivo := invoker.ChecarServidor()
		
		if !vivo {
			fmt.Println("Iniciando motor criptográfico em background para validação...")
			err := invoker.IniciarServidor(port, timeout, pkcs11Lib)
			if err != nil {
				fmt.Printf("Erro ao iniciar servidor: %v\n", err)
				os.Exit(1)
			}
			fmt.Print("Aguardando servidor ficar online")
			for i := 0; i < 10; i++ {
				time.Sleep(1 * time.Second)
				fmt.Print(".")
				state, vivo = invoker.ChecarServidor()
				if vivo {
					break
				}
			}
			fmt.Println() 
			
			if !vivo {
				fmt.Println("Erro: O servidor falhou ao ficar online após 10 segundos de espera.")
				os.Exit(1)
			}
		}

		fmt.Printf("Enviando requisição de validação (Porta: %d)...\n", state.Port)
		httpClient := client.NewSignatureClient(state.Port)
		
		resultado, err := httpClient.EnviarRequisicao("validate", validateInput)
		if err != nil {
			fmt.Printf("Erro na validação: %v\n", err)
			os.Exit(1)
		}

		fmt.Println("\nResultado da validação:")
		fmt.Println(resultado)
	},
}

func init() {
	validateCmd.Flags().StringVarP(&validateInput, "input", "i", "", "JSON de entrada contendo a assinatura a ser validada")
	validateCmd.MarkFlagRequired("input")
}