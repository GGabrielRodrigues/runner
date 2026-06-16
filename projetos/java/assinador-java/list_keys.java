import java.security.Provider;
import java.security.Security;
import java.security.KeyStore;
import java.util.Enumeration;

public class list_keys {
    public static void main(String[] args) throws Exception {
        String lib = "/usr/lib/softhsm/libsofthsm2.so";
        String configContent = "--name=Runner\nlibrary=" + lib;
        Provider sunPKCS11 = Security.getProvider("SunPKCS11");
        Provider p = sunPKCS11.configure(configContent);
        Security.addProvider(p);
        
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, "1234".toCharArray());
        
        Enumeration<String> aliases = ks.aliases();
        if (!aliases.hasMoreElements()) {
            System.out.println("No aliases found!");
        }
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            System.out.println("Found alias: " + alias);
            System.out.println("Is key? " + ks.isKeyEntry(alias));
            System.out.println("Key: " + ks.getKey(alias, null));
        }
    }
}
