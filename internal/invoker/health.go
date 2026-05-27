package invoker

import (
	"fmt"
	"net/http"
	"time"
)
func ChecarServidor() (*ServerState, bool) {
	state, err := LerEstado()
	if err != nil {
		return nil, false // Arquivo não existe ou erro de leitura
	}
	url := fmt.Sprintf("http://localhost:%d/", state.Port)
	client := http.Client{
		Timeout: 2 * time.Second,
	}

	resp, err := client.Get(url)
	if err != nil {
		LimparEstado()
		return nil, false
	}
	defer resp.Body.Close()
	return state, true
}