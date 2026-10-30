package me.TechsCode.UltraPermissions.base.security;

import me.TechsCode.UltraPermissions.base.mysql.MySQLCredentials;

public final class JdbcPolicy {
    private JdbcPolicy() {}

    public static void requireIdentityVerification(String url) throws java.sql.SQLException {
        java.sql.Driver driver = java.sql.DriverManager.getDriver(url);
        for (java.sql.DriverPropertyInfo property : driver.getPropertyInfo(url, new java.util.Properties())) {
            if ("sslMode".equals(property.name) && property.choices != null
                    && java.util.Arrays.asList(property.choices).contains("VERIFY_IDENTITY")) return;
        }
        // Old drivers may silently ignore sslMode. Reject them before sending credentials.
        throw new java.sql.SQLException("JDBC driver must support sslMode=VERIFY_IDENTITY");
    }

    public static String url(MySQLCredentials credentials, boolean tls) {
        String host = credentials.getHostname();
        if (host == null || !host.matches("[A-Za-z0-9.-]{1,253}") || host.startsWith(".") || host.endsWith(".")) {
            throw new IllegalArgumentException("MySQL hostname must be a plain DNS name or IPv4 address");
        }
        int port;
        try { port = Integer.parseInt(credentials.getPort()); }
        catch (NumberFormatException invalid) { throw new IllegalArgumentException("Invalid MySQL port"); }
        if (port < 1 || port > 65535 || !credentials.getDatabase().matches("[A-Za-z0-9_$-]{1,64}")) {
            throw new IllegalArgumentException("Invalid MySQL database/port");
        }
        boolean loopback = host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
        if (!loopback && !tls) throw new IllegalArgumentException("Remote MySQL requires ssl=true and verified TLS");
        return "jdbc:mysql://" + host + ':' + port + '/' + credentials.getDatabase()
            + "?sslMode=" + (tls ? "VERIFY_IDENTITY" : "DISABLED")
            + "&useSSL=" + tls + "&requireSSL=" + tls + "&verifyServerCertificate=" + tls
            + "&allowPublicKeyRetrieval=false&allowMultiQueries=false&allowLoadLocalInfile=false"
            + "&connectTimeout=5000&socketTimeout=10000&characterEncoding=utf-8";
    }
}
