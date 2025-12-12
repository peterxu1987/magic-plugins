package org.ssssssss.magicapi.tcp.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 端口检测工具类（线程安全）
 */
public class PortChecker {

    // 使用缓存避免重复检测（可选）
    private static final ConcurrentHashMap<Integer, Boolean> portCache = new ConcurrentHashMap<>();
    // 使用锁保证同一端口的检测互斥
    private static final ConcurrentHashMap<Integer, ReentrantLock> portLocks = new ConcurrentHashMap<>();

    /**
     * 检测端口是否被占用
     * @param port 要检测的端口号 (0-65535)
     * @return true=已被占用, false=未占用
     * @throws IllegalArgumentException 端口号不合法
     */
    public static boolean isPortOccupied(int port) {
        // 验证端口范围
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port range: " + port);
        }

        // 检查缓存（可选优化）
        Boolean cached = portCache.get(port);
        if (cached != null) return cached;

        // 获取端口专用锁
        ReentrantLock lock = portLocks.computeIfAbsent(port, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双检锁避免重复检测
            if (portCache.containsKey(port)) {
                return portCache.get(port);
            }

            boolean occupied;
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                // 设置SO_REUSEADDR可避免TIME_WAIT状态干扰（重要！）
                serverSocket.setReuseAddress(true);
                occupied = false;
            } catch (IOException e) {
                // 端口绑定失败视为占用
                occupied = true;
            }

            // 缓存结果（有效期5秒）
            portCache.put(port, occupied);
            scheduleCacheCleanup(port, 1000);  // 5秒后清除缓存

            return occupied;
        } finally {
            lock.unlock();
        }
    }

    // 定时清除缓存（避免长期状态不一致）
    private static void scheduleCacheCleanup(int port, long delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
            portCache.remove(port);
            portLocks.remove(port);
        }).start();
    }

    // 测试用例
    public static void main(String[] args) {
        int testPort = 9999;
        System.out.println("Port " + testPort + " status: " + 
                (isPortOccupied(testPort) ? "Occupied" : "Available"));
    }
}