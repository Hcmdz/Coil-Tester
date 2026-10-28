import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DbGenerator {
    public static void main(String[] args) throws Exception {
        String seedDir = args[0];
        String outputPath = args[1];

        Files.createDirectories(Paths.get(outputPath).getParent());
        Files.deleteIfExists(Paths.get(outputPath));

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + outputPath);
        Statement stmt = conn.createStatement();

        // Create tables with Room's exact schema (NOT NULL + indices)
        stmt.execute("CREATE TABLE products (id TEXT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, description TEXT, specsJson TEXT NOT NULL, imageAssetPath TEXT, sparePartIds TEXT NOT NULL, troubleshootingGuideId TEXT, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))");
        stmt.execute("CREATE INDEX index_products_category ON products (category)");
        stmt.execute("CREATE INDEX index_products_name ON products (name)");

        stmt.execute("CREATE TABLE spare_parts (id TEXT NOT NULL, productId TEXT NOT NULL, partNumber TEXT NOT NULL, description TEXT NOT NULL, compatibility TEXT, imageAssetPath TEXT, PRIMARY KEY(id))");
        stmt.execute("CREATE INDEX index_spare_parts_productId ON spare_parts (productId)");
        stmt.execute("CREATE INDEX index_spare_parts_partNumber ON spare_parts (partNumber)");

        stmt.execute("CREATE TABLE troubleshooting_guides (id TEXT NOT NULL, productId TEXT NOT NULL, stepsJson TEXT NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))");

        stmt.execute("CREATE TABLE refrigerants (name TEXT NOT NULL, gwp INTEGER NOT NULL, safetyClass TEXT NOT NULL, pressureTempChartJson TEXT NOT NULL, applications TEXT NOT NULL, phaseOutDate TEXT, PRIMARY KEY(name))");
        stmt.execute("CREATE INDEX index_refrigerants_name ON refrigerants (name)");

        stmt.execute("CREATE TABLE campaigns (id TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, validFrom INTEGER NOT NULL, validTo INTEGER NOT NULL, imageAssetPath TEXT, linkUrl TEXT, PRIMARY KEY(id))");

        stmt.execute("CREATE TABLE db_info (key TEXT NOT NULL, value INTEGER NOT NULL, PRIMARY KEY(key))");

        long now = System.currentTimeMillis();

        // Insert products
        List<String[]> products = readCsv(seedDir + "/products.csv");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO products (id,name,category,description,specsJson,imageAssetPath,sparePartIds,troubleshootingGuideId,updatedAt) VALUES (?,?,?,?,?,?,?,?,?)");
        for (String[] r : products) {
            ps.setString(1, r[0]);
            ps.setString(2, r[1]);
            ps.setString(3, r[2]);
            ps.setString(4, r[3].isEmpty() ? null : r[3]);
            ps.setString(5, r[4]);
            ps.setString(6, r[5].isEmpty() ? null : r[5]);
            ps.setString(7, r[6].replace(";", ","));
            ps.setString(8, r[7].isEmpty() ? null : r[7]);
            ps.setLong(9, now);
            ps.execute();
        }
        ps.close();

        // Insert spare_parts
        List<String[]> spares = readCsv(seedDir + "/spare_parts.csv");
        ps = conn.prepareStatement("INSERT INTO spare_parts (id,productId,partNumber,description,compatibility,imageAssetPath) VALUES (?,?,?,?,?,?)");
        for (String[] r : spares) {
            ps.setString(1, r[0]);
            ps.setString(2, r[1]);
            ps.setString(3, r[2]);
            ps.setString(4, r[3]);
            ps.setString(5, r[4].isEmpty() ? null : r[4]);
            ps.setString(6, r[5].isEmpty() ? null : r[5]);
            ps.execute();
        }
        ps.close();

        // Insert refrigerants
        List<String[]> refs = readCsv(seedDir + "/refrigerants.csv");
        ps = conn.prepareStatement("INSERT INTO refrigerants (name,gwp,safetyClass,pressureTempChartJson,applications,phaseOutDate) VALUES (?,?,?,?,?,?)");
        for (String[] r : refs) {
            ps.setString(1, r[0]);
            ps.setInt(2, Integer.parseInt(r[1]));
            ps.setString(3, r[2]);
            ps.setString(4, r[3]);
            ps.setString(5, r[4].replace(";", ","));
            ps.setString(6, r[5].isEmpty() ? null : r[5]);
            ps.execute();
        }
        ps.close();

        // Insert db_info
        stmt.execute("INSERT INTO db_info (key, value) VALUES ('version', 1)");
        stmt.execute("INSERT INTO db_info (key, value) VALUES ('update_date_tick', " + now + ")");

        conn.close();
        System.out.println("Database created: " + outputPath);
    }

    // Simple CSV parser handling quoted fields
    static List<String[]> readCsv(String path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path));
        if (lines.isEmpty()) return rows;
        // skip header
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            rows.add(parseCsvLine(line));
        }
        return rows;
    }

    static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}
