package com.project.legendsofleague.util.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ExcelReader {

    public List<Map<String, String>> readTeam(String filePath) throws Exception {
        List<Map<String, String>> teamData = new ArrayList<>();
        InputStream inputStream = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(inputStream);

        Map<String, Integer> columnIndexMap = new HashMap<>();
        columnIndexMap.put("Team", 0);
        columnIndexMap.put("UniqueLine", 5);

        Sheet sheet = workbook.getSheet("Team");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            Map<String, String> rowData = new HashMap<>();
            for (String columnName : columnIndexMap.keySet()) {
                int columnIndex = columnIndexMap.get(columnName);
                Cell cell = row.getCell(columnIndex);
                String cellValue = cell.getStringCellValue();
                rowData.put(columnName, cellValue);
            }
            teamData.add(rowData);
        }

        return teamData;
    }

    public List<Map<String, String>> readPlayer(String filePath) throws Exception {

        Map<String, Integer> columnIndexMap = new HashMap<>();
        columnIndexMap.put("Team", 0);
        columnIndexMap.put("N_PlayerInTeam", 1);
        columnIndexMap.put("Player", 3);
        columnIndexMap.put("Role", 4);

        List<Map<String, String>> rosters = new ArrayList<>();

        InputStream inputStream = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet sheet = workbook.getSheet("Rosters");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            Map<String, String> rowData = new HashMap<>();
            for (String columnName : columnIndexMap.keySet()) {
                int columnIndex = columnIndexMap.get(columnName);
                Cell cell = row.getCell(columnIndex);
                String cellValue = cell.getStringCellValue();
                rowData.put(columnName, cellValue);
            }
            rosters.add(rowData);
        }

        workbook.close();
        inputStream.close();
        return rosters;
    }

    public List<Map<String, String>> readMatchSchedule(String filePath) throws Exception {

        Map<String, Integer> columnIndexMap = new HashMap<>();
        columnIndexMap.put("Team1", 0);
        columnIndexMap.put("Team2", 1);
        columnIndexMap.put("Winner", 2);
        columnIndexMap.put("Team1Score", 3);
        columnIndexMap.put("Team2Score", 4);
        columnIndexMap.put("MatchDay", 5);
        columnIndexMap.put("DateTime_UTC", 6);
        columnIndexMap.put("MatchId", 7);
        columnIndexMap.put("UniqueMatch", 10);

        List<Map<String, String>> matchScheduleData = new ArrayList<>();

        InputStream inputStream = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet sheet = workbook.getSheet("MatchSchedule");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            Map<String, String> rowData = new HashMap<>();
            for (String columnName : columnIndexMap.keySet()) {
                int columnIndex = columnIndexMap.get(columnName);
                Cell cell = row.getCell(columnIndex);
                String cellValue = (cell == null) ? "0" : cell.toString();
                rowData.put(columnName, cellValue);
            }
            matchScheduleData.add(rowData);
        }

        workbook.close();
        inputStream.close();
        return matchScheduleData;
    }


    public List<Map<String, String>> readRostersByGame(String filePath) throws Exception {

        Map<String, Integer> columnIndexMap = new HashMap<>();
        columnIndexMap.put("Team1", 0);
        columnIndexMap.put("Team2", 1);
        columnIndexMap.put("Team1Players", 3);
        columnIndexMap.put("Team2Players", 4);
        columnIndexMap.put("GameId", 5);
        columnIndexMap.put("MatchId", 6);

        List<Map<String, String>> rostersByGame = new ArrayList<>();

        InputStream inputStream = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet sheet = workbook.getSheet("Rosters_By_Game");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowData = new HashMap<>();
            for (String columnName : columnIndexMap.keySet()) {
                int columnIndex = columnIndexMap.get(columnName);
                Cell cell = row.getCell(columnIndex);
                String cellValue = cell.getStringCellValue();
                rowData.put(columnName, cellValue);
            }
            rostersByGame.add(rowData);
        }

        workbook.close();
        inputStream.close();
        return rostersByGame;
    }

}
