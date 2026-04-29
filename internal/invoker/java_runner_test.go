package invoker 

import (
	"strings"
	"testing"
)

func TestLocalizarJava(t *testing.T) {
	caminho, err := LocalizarJava()
	
	if err != nil {
		t.Fatalf("Esperava encontrar o executável do Java, mas retornou erro: %v", err)
	}

	if caminho == "" {
		t.Errorf("O caminho retornado para o Java está vazio")
	}

	if !strings.HasSuffix(caminho, "java") && !strings.HasSuffix(caminho, "java.exe") {
		t.Errorf("O caminho retornado não parece ser um executável Java válido: %s", caminho)
	}
	
	t.Logf("Java encontrado com sucesso em: %s", caminho)
}