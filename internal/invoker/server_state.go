package invoker

import (
	"encoding/json"
	"errors"
	"os"
	"path/filepath"
)

type ServerState struct {
	PID  int `json:"pid"`
	Port int `json:"port"`
}

func getPathState() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	return filepath.Join(home, ".hubsaude", "server.json"), nil
}
func SalvarEstado(pid int, port int) error {
	path, err := getPathState()
	if err != nil {
		return err
	}

	state := ServerState{PID: pid, Port: port}
	data, err := json.MarshalIndent(state, "", "  ")
	if err != nil {
		return err
	}

	return os.WriteFile(path, data, 0644)
}
func LerEstado() (*ServerState, error) {
	path, err := getPathState()
	if err != nil {
		return nil, err
	}

	data, err := os.ReadFile(path)
	if err != nil {
		if os.IsNotExist(err) {
			return nil, errors.New("nenhum servidor ativo encontrado")
		}
		return nil, err
	}

	var state ServerState
	if err := json.Unmarshal(data, &state); err != nil {
		return nil, err
	}

	return &state, nil
}
func LimparEstado() error {
	path, err := getPathState()
	if err != nil {
		return err
	}
	return os.Remove(path)
}