package com.example.examplefeature;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Component
public class ReportGenerator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ReportGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Set this to TRUE to run the generator
    private boolean isEnabled = true;

    @Override
    public void run(String... args) throws Exception {
        if (!isEnabled) return;

        System.out.println(">>> GENERATING HOMEWORK REPORT...");

        // 1. Read the queries from file
        String content = new String(Files.readAllBytes(Paths.get("queries.sql")));

        // Split by semicolon to get individual chunks
        String[] chunks = content.split(";");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; font-size: 10px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 4px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append("h3 { background-color: #333; color: white; padding: 5px; margin-top: 30px; font-size: 12px; }");
        html.append("</style></head><body>");
        html.append("<h1>Assignment 1: Wildcard Search Results</h1>");

        int count = 1;

        for (String chunk : chunks) {
            // NEW LOGIC: Remove comment lines, keep query lines
            String executableQuery = chunk.lines()
                    .filter(line -> !line.trim().startsWith("--")) // Remove lines starting with --
                    .collect(Collectors.joining(" ")) // Join back into one string
                    .trim();

            // Skip if nothing is left after removing comments
            if (executableQuery.isEmpty()) continue;

            try {
                // 2. Execute Clean Query
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(executableQuery);

                // 3. Add to Report
                html.append("<h3>Q").append(count++).append(": ").append(executableQuery).append("</h3>");

                // 4. Generate Table
                if (rows.isEmpty()) {
                    html.append("<p><em>No results found (0 rows).</em></p>");
                } else {
                    html.append("<table><thead><tr>");
                    for (String col : rows.get(0).keySet()) {
                        html.append("<th>").append(col).append("</th>");
                    }
                    html.append("</tr></thead><tbody>");

                    int rowCount = 0;
                    for (Map<String, Object> row : rows) {
                        if (rowCount++ >= 5) break;
                        html.append("<tr>");
                        for (Object val : row.values()) {
                            html.append("<td>").append(val != null ? val.toString() : "").append("</td>");
                        }
                        html.append("</tr>");
                    }
                    html.append("</tbody></table>");
                    if (rows.size() > 5) html.append("<p><em>... (" + (rows.size() - 5) + " more rows hidden) ...</em></p>");
                }

            } catch (Exception e) {
                System.err.println("FAILED QUERY: " + executableQuery);
                System.err.println("Error: " + e.getMessage());
            }
        }

        html.append("</body></html>");

        Files.write(Paths.get("Assignment_Report.html"), html.toString().getBytes());
        System.out.println(">>> REPORT GENERATED: Assignment_Report.html");
        System.out.println(">>> Total Queries Processed: " + (count - 1));
    }
}