package br.ufg.inf.hubsaude.exception;

/**
 * Exceção para capturar erros de regra de negócio e validação.
 * O 'errorCode' será usado para montar o JSON de erro que o Go vai ler.
 */
public class ValidationException extends RuntimeException {
    private final String errorCode;

    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
