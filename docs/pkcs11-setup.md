# Configuração de Material Criptográfico (PKCS#11) com SoftHSM2

Este documento descreve como configurar o SoftHSM2 para simular um dispositivo criptográfico (token/smart card) e testar o suporte a PKCS#11 no Assinador.

## 1. Instalação do SoftHSM2

### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install softhsm2
```

### macOS
```bash
brew install softhsm
```

### Windows
Baixe os binários do [site oficial do OpenDNSSEC](https://www.opendnssec.org/softhsm/) ou use o gerenciador de pacotes apropriado.

## 2. Inicialização do Token

O SoftHSM2 precisa de um "slot" inicializado para funcionar.

```bash
# Crie um diretório para o banco de dados do SoftHSM2
mkdir -p $HOME/softhsm2/tokens
echo "directories.tokendir = $HOME/softhsm2/tokens" > $HOME/softhsm2/softhsm2.conf
export SOFTHSM2_CONF=$HOME/softhsm2/softhsm2.conf

# Inicialize o slot 0
softhsm2-util --init-token --slot 0 --label "RunnerToken" --pin 1234 --so-pin 1234
```

## 3. Geração de Par de Chaves e Certificado

Para que o Java (via provider SunPKCS11) consiga enxergar a chave privada dentro do dispositivo, é estritamente necessário que ela esteja associada a um certificado. O jeito mais fácil de fazer isso é usando a própria ferramenta `keytool` que já vem instalada com o JDK.

Crie um arquivo de configuração temporário chamado `p11.cfg`:
```bash
echo -e "name = Runner\nlibrary = /usr/lib/softhsm/libsofthsm2.so" > p11.cfg
```

E gere o par de chaves com o certificado autoassinado usando o `keytool`:
```bash
keytool -genkeypair -alias RunnerKey \
        -keyalg RSA -keysize 2048 \
        -keystore NONE -storetype PKCS11 \
        -providerClass sun.security.pkcs11.SunPKCS11 \
        -providerArg p11.cfg \
        -storepass 1234 \
        -dname "CN=Runner"
```

## 4. Uso com o Assinador

Ao executar o Assinador, passe o caminho da biblioteca do SoftHSM2:

### Via CLI Direto
```bash
java -jar assinador.jar sign \
     --pkcs11-lib=/usr/lib/softhsm/libsofthsm2.so \
     --pin=1234 \
     '{"payloadBase64": "YWJj", "signerName": "Runner"}'
```

### Via Modo Servidor
```bash
java -jar assinador.jar server \
     --pkcs11-lib=/usr/lib/softhsm/libsofthsm2.so \
     --pin=1234 \
     --port=8080
```

## 5. Localização da Biblioteca (.so / .dylib / .dll)

- **Ubuntu/Debian:** `/usr/lib/softhsm/libsofthsm2.so`
- **macOS (Homebrew):** `/usr/local/lib/softhsm/libsofthsm2.so` ou `/opt/homebrew/lib/softhsm/libsofthsm2.so`
- **Windows:** `C:\Program Files\SoftHSM2\lib\softhsm2.dll`
