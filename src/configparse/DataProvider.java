/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package configparse;

import java.util.*;


/**
 *
 * @author Administrator
 */
public class DataProvider {

    public static String buildStringFromStringArray(String func, int sheetNum, String[][] content) {
        String buildedContent = "";
        if ("UPGRADE_BUILDING".equals(func)) {//建筑升级相关配置
            buildedContent += buildFinalUpgradeBuildingStringFromStringArray(func, sheetNum, content);
        } 
        return buildedContent;
    }

    public static String[][] getStringArrayByField(String[][] content, String value, int index) {
        int rowCount = content.length;
        int colCount = content[0].length;
        String[][] stringArray = new String[rowCount][colCount];
        int j = 0;
        for (int row = 0; row < rowCount; row++) {
            String indexValue = content[row][index];
            if (indexValue.equals(value)) {
                stringArray[j++] = content[row];
            }
        }
        return stringArray;
    }

    public static String[] getModelNamesFromStringArray(String[][] content) {
        String[] model;
        int i = 0;
        for (; i < content[0].length; i++) {
            if (content[0][i].isEmpty()) {
                break;
            }
        }
        model = new String[i];
        System.arraycopy(content[0], 0, model, 0, i);
        return model;
    }

    public static String buildFinalUpgradeBuildingStringFromStringArray(String func, int sheetNum, String[][] content) {
        String buildedContent = DataParse.parseData(content);
        return buildedContent;
    }
}
