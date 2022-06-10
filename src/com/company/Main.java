package com.company;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Перенесём данные из CSV в DB (~3 min)
        // MoveCSVToDB();
        // Создадим график стран по их показателям щедрости и сохраним его в "histogram.png" (1 задание)
        // CreateAndSaveHistogram();
        // Выведем в консоль страну с самым низким показателем щедрости среди "Middle East and Northern Africa" и "Central and Eastern Europe" (2 задание)
        // PrintResultForQuery2();
    }

    private static void PrintResultForQuery2() throws SQLException, ClassNotFoundException, IOException {
        Database.Connect("db.s3db");
        var query2 = String.join("\n", Files.readAllLines(Paths.get("query2.txt")));
        var res = Database.CustomSelectFirst(query2, new String[]{"region", "generosity"});
        System.out.println(res);
    }

    private static void CreateAndSaveHistogram() throws ClassNotFoundException, SQLException, IOException {
        Database.Connect("db.s3db");
        // Берём показатели стран
        var data = Database
                .SelectFromTable("country_generosity", "generosity")
                .stream()
                .mapToDouble(d -> Double.parseDouble(d[1]))
                .toArray();
        // Создаём гистограмму с 17 столбцами (каждый столбик будет занимать примерно 0.05)
        var dataset = new HistogramDataset();
        dataset.addSeries("country", data, 17);
        JFreeChart histogram = ChartFactory.createHistogram("График щедрости стран",
                "Показатели щедрости", "Количество стран", dataset, PlotOrientation.VERTICAL, false, true, false);
        // Сохраняем
        ChartUtilities.saveChartAsPNG(new File("histogram.png"), histogram, 800, 600);
    }

    public static void MoveCSVToDB() throws SQLException, ClassNotFoundException, IOException {
        Database.Connect("db.s3db");
        System.out.println("БД подключена");

        // Для каждой пары "СТРАНА - ПОЛЕ" создаём собственную таблицу в БД
        CreateTables();
        System.out.println("Таблицы созданы");
        // Для каждой строчки в csv каждое поле добавляем в соответствующую таблицу БД
        var csvPath = Paths.get("table.csv");
        var columns = new String[]{"country", "region", "happyRank", "happiness", "lowerConfidence", "upperConfidence", "economy", "family", "health", "freedom", "trust", "generosity", "dystopia"};
        AddEntries(EntryParser.DictParse(columns, Files.readAllLines(csvPath)));
    }

    public static void CreateTables() throws SQLException {
        var queries = new ArrayList<String>();

        // Региону присвоим тип строки
        queries.add("CREATE TABLE 'country_region' ('country' VARCHAR(30) PRIMARY KEY, 'region' VARCHAR(30) NOT NULL);");
        // Создадим таблицы "страна-поле" на основании столбцов csv
        var fields = new String[]{"happyRank", "happiness", "lowerConfidence", "upperConfidence", "economy", "family", "health", "freedom", "trust", "generosity", "dystopia"};
        Arrays.stream(fields)
                .map(f -> String.format("CREATE TABLE 'country_%s' ('country' VARCHAR(30) PRIMARY KEY, '%s' FLOAT NOT NULL);", f, f))
                .forEach(queries::add);

        Database.Execute(queries);
    }

    public static void AddEntries(List<DictEntry> entries) throws SQLException {
        var queries = new ArrayList<String>();

        var fields = new String[]{"region", "happyRank", "happiness", "lowerConfidence", "upperConfidence", "economy", "family", "health", "freedom", "trust", "generosity", "dystopia"};
        // Для каждой строчки проходимся по каждому полю и добавляем его значение в соответствующую таблицу БД
        for (String f : fields) {
            for (DictEntry e : entries) {
                var table = "country_" + f;
                var cols = new String[]{"country", f};
                var values = new String[]{e.getValue("country"), e.getValue(f)};
                queries.add(Database.GetInsertQuery(table, cols, values));
            }
        }

        Database.Execute(queries);
    }
}

