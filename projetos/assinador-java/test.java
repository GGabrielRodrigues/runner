import java.security.Provider;
import java.security.Security;
import java.security.KeyStore;

public class test {
    public static void main(String[] args) throws Exception {
        String lib = "/usr/lib/softhsm/libsofthsm2.so";
        String configContent = "--name=Runner\nlibrary=" + lib + "\nslotListIndex=0";
        Provider sunPKCS11 = Security.getProvider("SunPKCS11");
        Provider p = sunPKCS11.configure(configContent);
        Security.addProvider(p);
        
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        try {
            ks.load(null, "9999".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
