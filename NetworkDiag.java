import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkDiag {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Network Interface Diagnostic ===");
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            System.out.println("Display Name: " + iface.getDisplayName());
            System.out.println("Name: " + iface.getName());
            System.out.println("Up? " + iface.isUp());
            System.out.println("Loopback? " + iface.isLoopback());
            System.out.println("Virtual? " + iface.isVirtual());

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                System.out.println("  - Address: " + addr.getHostAddress());
            }
            System.out.println("-----------------------------------");
        }
    }
}
