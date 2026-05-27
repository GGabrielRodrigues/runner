package main

import (
	"fmt"
	"os"
	"time"

	"github.com/GGabrielRodrigues/runner/internal/client"
	"github.com/GGabrielRodrigues/runner/internal/invoker" // Ajuste o path de importação de acordo com seu module
	"github.com/spf13/cobra"
)

var input string

var signCmd = &cobra.Command{
	Use:   "sign",
	Short: "Assina um payload JSON",
	Run: func(cmd *cobra.Command, args []string) {
		if input == "" {
			fmt.Println("Erro: A flag --input é obrigatória.")
			os.Exit(1)
		}
		if localMode {
			fmt.Println("[Modo Local] Executando processo efêmero...")
			saida, err := invoker.ExecutarAssinador("sign", input)
			if err != nil {
				fmt.Printf("Erro: %v\n", err)
				os.Exit(1)
			}
			fmt.Println(saida)
			return
		}
		state, vivo := invoker.ChecarServidor()
		
		if !vivo {
			fmt.Println("Iniciando motor criptográfico em background...")
			err := invoker.IniciarServidor(port, timeout, pkcs11Lib)
			if err != nil {
				fmt.Printf("Erro ao iniciar servidor: %v\n", err)
				os.Exit(1)
			}
			time.Sleep(2 * time.Second)
			
			state, vivo = invoker.ChecarServidor()
			if !vivo {
				fmt.Println("Erro: O servidor falhou ao ficar online após a inicialização.")
				os.Exit(1)
			}
		}

		fmt.Printf("Enviando requisição (Porta: %d)...\n", state.Port)
		httpClient := client.NewSignatureClient(state.Port)
		
		resultado, err := httpClient.EnviarRequisicao("sign", input)
		if err != nil {
			fmt.Printf("Erro na assinatura: %v\n", err)
			os.Exit(1)
		}

		fmt.Println("\nAssinatura gerada com sucesso:")
		fmt.Println(resultado)
	},
}

func init() {
	signCmd.Flags().StringVarP(&input, "input", "i", "", "JSON de entrada para ser assinado")
	signCmd.MarkFlagRequired("input")
}