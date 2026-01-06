package com.lancast.lancast.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages network-related functionality: IP addresses, peer tracking, device
 * detection.
 */
public class NetworkService {

    private static final int PORT = 8000;
    // Map of IP address -> Last seen timestamp (ms)
    private static final Map<String, Long> activePeersMap = new ConcurrentHashMap<>();
    private static Consumer<Integer> peerCountListener;
    private static final long PEER_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // Periodic cleanup task
        scheduler.scheduleAtFixedRate(NetworkService::cleanupPeers, 1, 1, TimeUnit.MINUTES);
    }

    public static int getPort() {
        return PORT;
    }

    public static void setPeerCountListener(Consumer<Integer> listener) {
        peerCountListener = listener;
    }

    public static int getActivePeers() {
        cleanupPeers();
        return activePeersMap.size();
    }

    public static void registerActivity(String ipAddress) {
        boolean isNew = !activePeersMap.containsKey(ipAddress);
        activePeersMap.put(ipAddress, System.currentTimeMillis());

        if (isNew) {
            notifyListener();
        }
    }

    private static void cleanupPeers() {
        long now = System.currentTimeMillis();
        boolean changed = activePeersMap.entrySet().removeIf(entry -> (now - entry.getValue()) > PEER_TIMEOUT_MS);

        if (changed) {
            notifyListener();
        }
    }

    private static void notifyListener() {
        if (peerCountListener != null) {
            peerCountListener.accept(activePeersMap.size());
        }
    }

    // Keep old methods for backward compatibility but redirect to new logic
    public static void incrementPeers() {
        // No-op or potentially could be used to force update, but specific IP is better
    }

    public static void decrementPeers() {
        // No-op
    }

    public static String getIpAddress() {
        String bestIp = "Unavailable";
        int bestPriority = -1;

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        String name = iface.getDisplayName().toLowerCase();
                        int priority = 0;

                        if (name.contains("vmware") || name.contains("virtual") || name.contains("vbox")
                                || name.contains("wsl") || name.contains("hyper-v")) {
                            priority = 0;
                        } else if (name.contains("wi-fi") || name.contains("wlan") || name.contains("wireless")) {
                            priority = 4;
                        } else if (name.contains("eth") || name.contains("ethernet")) {
                            priority = 3;
                        } else {
                            priority = 1;
                        }

                        if (addr.isSiteLocalAddress()) {
                            priority += 1;
                        }

                        if (priority > bestPriority) {
                            bestPriority = priority;
                            bestIp = "http://" + ip + ":" + PORT + "/";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestIp;
    }

    public static void printIpAddresses() {
        try {
            System.out.println("Available LAN IP Addresses:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        System.out.println("  - http://" + addr.getHostAddress() + ":" + PORT + " ("
                                + iface.getDisplayName() + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing IP addresses: " + e.getMessage());
        }
    }

    public static String getDeviceType(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        if (userAgent.contains("Android"))
            return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
            return "iOS";
        if (userAgent.contains("Windows"))
            return "Windows";
        if (userAgent.contains("Macintosh"))
            return "macOS";
        if (userAgent.contains("Linux"))
            return "Linux";
        return "Unknown";
    }
}
