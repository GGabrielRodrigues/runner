import java.security.Provider;
import java.security.Security;

public class TestPKCS11 {
    public static void main(String[] args) throws Exception {
        String lib = "/usr/lib/softhsm/libsofthsm2.so";
        String configContent = "--name=Runner\nlibrary=" + lib;
        try {
            Provider sunPKCS11 = Security.getProvider("SunPKCS11");
            Provider p = sunPKCS11.configure(configContent);
            Security.addProvider(p);
            System.out.println("Success without slotListIndex!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        configContent = "--name=Runner\nlibrary=" + lib + "\nslotListIndex=0";
        try {
            Provider sunPKCS11 = Security.getProvider("SunPKCS11");
            Provider p = sunPKCS11.configure(configContent);
            Security.addProvider(p);
            System.out.println("Success with slotListIndex=0!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
