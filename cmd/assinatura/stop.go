package main

import (
	"fmt"
	"os"

	"github.com/GGabrielRodrigues/runner/internal/invoker"
	"github.com/spf13/cobra"
)

var stopCmd = &cobra.Command{
	Use:   "stop",
	Short: "Interrompe o servidor de assinatura que está rodando em background",
	Run: func(cmd *cobra.Command, args []string) {
		state, err := invoker.LerEstado()
		if err != nil {
			fmt.Println("Nenhum servidor ativo encontrado (ou arquivo de estado inexistente).")
			return
		}

		fmt.Printf("Encerrando servidor Java (PID: %d)...\n", state.PID)
		
		process, err := os.FindProcess(state.PID)
		if err == nil {
			_ = process.Kill() 
		}
		_ = invoker.LimparEstado()
		
		fmt.Println("Servidor interrompido com sucesso.")
	},
}