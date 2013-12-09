/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package configparse;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jxl.*;
import jxl.read.biff.BiffException;

/**
 *
 * @author Administrator
 */
public class FileProcessor {

    // 二维数组纵向合并  
    private static String[][] unite(String[][] content1, String[][] content2) {
        String[][] newArrey = new String[][]{};
        List<String[]> list = new ArrayList<String[]>();
        list.addAll(Arrays.<String[]>asList(content1));
        list.addAll(Arrays.<String[]>asList(content2));
        return list.toArray(newArrey);
    }
    public static Map< String, Map> fileMapping;
    public static String configBaseDir;
    public static String outputDirectory;

    public static String[][] parseXls(String filePath, int sheetNum) throws IOException, BiffException {
        String[][] finalContents = FileProcessor.parseXls(filePath, sheetNum, true);
        return finalContents;
    }

    public static WorkbookSettings getWorkbookSettings() {
        return FileProcessor.getWorkbookSettings("ISO-8859-1");
    }

    public static WorkbookSettings getWorkbookSettings(String encoding) {
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setEncoding(encoding); //关键代码，解决乱码
        return workbookSettings;
    }

    /**
     * 替换
     *
     * @param sheet
     * @param i
     * @param j
     * @return
     */
    public static String getTmpContent(Sheet sheet, int i, int j) {
        Cell cells = sheet.getCell(i, j);
        String finalContents;
        if (cells.getType() == CellType.DATE) {//对日期数据进行特殊处理，如果不处理的话 默认24h制会变成12小时制
            finalContents = FileProcessor.formatTime(sheet.getCell(i, j));
        } else {
            String tmpContent = sheet.getCell(i, j).getContents();
            if (!isNumeric(tmpContent)) {
                tmpContent = tmpContent.replace("\\'", "\'");//防止单引号已经被转义了 如果没有这一步骤的话 被转义的单引号就会出现问题
                tmpContent = tmpContent.replace("\'", "\\'");
            }
            finalContents = tmpContent;
        }
        return finalContents;
    }

    /**
     * 判断是否为 number类型
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0;) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化时间
     *
     * @param formatCell
     * @return
     */
    public static String formatTime(Cell formatCell) {
        java.util.Date date = null;
        DateCell dateCell = (DateCell) formatCell;
        date = dateCell.getDate();
        //long time = (date.getTime() / 1000) - 60 * 60 * 8;
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        //date.setTime(time * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(gmtZone);
        return formatter.format(date);

    }

    /**
     * 获得指定worksheet名称的顺序
     *
     * @param filePath
     * @param desSheetName
     * @return String
     * @throws IOException
     * @throws BiffException
     */
    public static int getSheetIndexBySheetName(String filePath, String desSheetName) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(new File(filePath));
        String[] sheetNames = workbook.getSheetNames();
        int sheetIndex = 0;
        int sheetNamesLen = sheetNames.length;
        for (int i = 0; i < sheetNamesLen; i++) {
            String sheetName = sheetNames[i];
            if (sheetName.equals(desSheetName)) {
                sheetIndex = i;
                break;
            }
        }
        return sheetIndex;
    }

    /**
     *
     * @param filePath
     * @return
     */
    public static HashMap getFileParseRuleConfigInfo(String filePath) {
        HashMap<String, HashMap> configHashMap = new HashMap<String, HashMap>();
        try {
            int configSheetIndex = getSheetIndexBySheetName(filePath, "parseRuleCfg");
            String[][] configInfoString = parseXls(filePath, configSheetIndex, true);
            int rowCount = configInfoString.length;
            for (int row = 1; row < rowCount; row++) {
                String[] rowInfo = configInfoString[row];
                if (!rowInfo[0].isEmpty()) {
                    String sheetName = rowInfo[0];
                    String cfgKey = rowInfo[1];
                    String cfgValue = rowInfo[2];
                    HashMap cfgInfo;
                    if (configHashMap.containsKey(sheetName)) {
                        cfgInfo = configHashMap.get(sheetName);
                    } else {
                        cfgInfo = new HashMap();
                    }
                    cfgInfo.put(cfgKey, cfgValue);
                    configHashMap.put(sheetName, cfgInfo);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BiffException ex) {
            Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(configHashMap.toString());
        return configHashMap;
    }

    /**
     * 获得指定worksheet的名称
     *
     * @param filePath
     * @param sheetIndex
     * @return String
     * @throws IOException
     * @throws BiffException
     */
    public static String getSheetNameBySheetIndex(String filePath, int sheetIndex) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(new File(filePath));
        String[] sheetNames = workbook.getSheetNames();
        String sheetName = sheetNames[sheetIndex];
        return sheetName;
    }

    /**
     * 获得指定excel文件的worksheet数量
     *
     * @param filePath
     * @return int
     * @throws IOException
     * @throws BiffException
     */
    public static int getSheetNumber(String filePath) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(new File(filePath));
        int sheetNumber = workbook.getNumberOfSheets();
        return sheetNumber;
    }

    /**
     * 加载setting文件
     *
     * @param file_path
     */
    public static void loadSetting(String file_path) {
        File f = new File(file_path);
        if (f.exists()) {
            Properties prop = new Properties();
            FileInputStream fis;
            try {
                fis = new FileInputStream(file_path);
                try {
                    prop.load(fis);
                } catch (IOException ex) {
                    Logger.getLogger(ConfigParseJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    showMessageDialogMessage(ex);
                }
                if (!prop.getProperty("configBaseDir", "").isEmpty()) {
                    try {
                        configBaseDir = new String(prop.getProperty("configBaseDir").getBytes("ISO-8859-1"), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ConfigParseJFrame.class.getName()).log(Level.SEVERE, null, ex);
                        showMessageDialogMessage(ex);
                    }

                }
                if (!prop.getProperty("outputDirectory", "").isEmpty()) {
                    try {
                        outputDirectory = new String(prop.getProperty("outputDirectory").getBytes("ISO-8859-1"), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        showMessageDialogMessage(ex);
                        Logger.getLogger(ConfigParseJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ConfigParseJFrame.class.getName()).log(Level.SEVERE, null, ex);
                showMessageDialogMessage(ex);
            }
        }
    }

    /**
     * 解析excel文件
     *
     * @param filePath 文件路径
     * @param sheetNum work sheet 索引
     * @param reverse 是否需要reverse所得二维数组
     * @return array[][]
     * @throws IOException
     * @throws BiffException
     */
    public static String[][] parseXls(String filePath, int sheetNum, boolean reverse) throws IOException, BiffException {
        //通过Workbook的静态方法getWorkbook选取Excel文件
        WorkbookSettings workbookSettings = getWorkbookSettings();
        Workbook workbook = Workbook.getWorkbook(new File(filePath), workbookSettings);
        //通过Workbook的getSheet方法选择第一个工作簿（从0开始）
        Sheet sheet = workbook.getSheet(sheetNum);
        int rows = sheet.getRows();
        int cols = sheet.getColumns();
        Cell cells[][] = new Cell[cols][rows];
        String[][] finalContents;
        if (reverse) {
            finalContents = new String[rows][cols];
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    finalContents[j][i] = getTmpContent(sheet, i, j);
                }
            }
        } else {
            finalContents = new String[cols][rows];
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    finalContents[i][j] = getTmpContent(sheet, i, j);
                }
            }
        }

        workbook.close();
        return finalContents;
    }

    /**
     * 解析excel文件
     *
     * @param filePath filePath 文件路径
     * @return array[][]
     * @throws IOException
     * @throws BiffException
     */
    public static String[][] parseXls(String filePath) throws IOException, BiffException {
        //通过Workbook的静态方法getWorkbook选取Excel文件
        WorkbookSettings workbookSettings = getWorkbookSettings();
        Workbook workbook = Workbook.getWorkbook(new File(filePath), workbookSettings);
        int sheetTotal = workbook.getNumberOfSheets();
        Sheet sheet;
        int rows, cols;
        Cell cells[][];
        String[][] finalContents = {};
        String[][] tmpContents;
        //通过Workbook的getSheet方法选择第一个工作簿（从0开始）
        for (int sheetNum = 0; sheetNum < sheetTotal; sheetNum++) {
            sheet = workbook.getSheet(sheetNum);
            rows = sheet.getRows();
            cols = sheet.getColumns();
            cells = new Cell[cols][rows];
            tmpContents = new String[rows][cols];
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    cells[i][j] = sheet.getCell(i, j);
                    if (cells[i][j].getType() == CellType.DATE) {//对日期数据进行特殊处理，如果不处理的话 默认24h制会变成12小时制
                        tmpContents[j][i] = FileProcessor.formatTime(cells[i][j]);
                    } else {
                        String tmpContent = cells[i][j].getContents();
                        tmpContent = tmpContent.replace("\\'", "\'");//防止单引号已经被转义了 如果没有这一步骤的话 被转义的单引号就会出现问题
                        tmpContent = tmpContent.replace("\'", "\\'");//将所有的单引号进行转义
                        tmpContents[j][i] = tmpContent;
                    }
                }
            }
            if (finalContents.length > 0) {
                finalContents = unite(finalContents, tmpContents);//合并到最终的数组中
            } else {
                finalContents = tmpContents;
            }
        }

        workbook.close();
        return finalContents;
    }

    /**
     * 将指定的字符串写入到指定的文件中
     *
     * @param contents 将要写入文件的内容字符串
     * @param descFile 将要写入文件的路径
     * @param encoding 文件编码
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeToFile(String contents, File descFile, String encoding) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        if (!descFile.getParentFile().exists()) {
            if (!descFile.getParentFile().mkdirs()) {
                JOptionPane.showMessageDialog(null, "创建目录文件所在的目录失败", "信息提示", JOptionPane.ERROR_MESSAGE);
                System.out.println("创建目录文件所在的目录失败！");
            }
        }
        if (!descFile.exists()) {
            descFile.createNewFile();
        }
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(descFile), encoding));
        writer.write(contents);
        writer.flush();
        writer.close();
    }

    public static String[][] arrayPop(String[][] oldArrayContent) {
        int rows = oldArrayContent.length;
        int cols = oldArrayContent[0].length;
        String[][] cleanupedArrayContent = new String[rows - 1][cols];
        for (int row = 0; row < rows - 1; row++) {
            cleanupedArrayContent[row] = oldArrayContent[row + 1];
        }

        return cleanupedArrayContent;
    }

    /**
     * 字符串替换功能
     *
     * @param strSource 将要被替换的字符串
     * @param strFrom 需要被替换的字符串
     * @param strTo
     * @return
     */
    public static String replace(String strSource, String strFrom, String strTo) {
        if (strSource == null) {
            return null;
        }
        int i = 0;
        if ((i = strSource.indexOf(strFrom, i)) >= 0) {
            char[] cSrc = strSource.toCharArray();
            char[] cTo = strTo.toCharArray();
            int len = strFrom.length();
            StringBuilder buf = new StringBuilder(cSrc.length);
            buf.append(cSrc, 0, i).append(cTo);
            i += len;
            int j = i;
            while ((i = strSource.indexOf(strFrom, i)) > 0) {
                buf.append(cSrc, j, i - j).append(cTo);
                i += len;
                j = i;
            }
            buf.append(cSrc, j, cSrc.length - j);
            return buf.toString();
        }
        return strSource;
    }

    /**
     * 显示错误信息
     *
     * @param ex
     */
    public static void showMessageDialogMessage(Exception ex) {
        String exMsg = ex.toString();
        JOptionPane.showMessageDialog(null, exMsg, "错误信息提示", JOptionPane.ERROR_MESSAGE);
    }

    public static void initFileMapping() {
        fileMapping = new HashMap< String, Map>();

        Map mapping = new HashMap();
        mapping.put("fileName", "t图腾.xls");

//        fileMapping.put("TOTEM", "t图腾.xls");
//        fileMapping.put("UPGRADE_BUILDING", "j建筑升级.xls");
//        fileMapping.put("BIGPACK", "d大礼包.xls");
//        fileMapping.put("EXCHANGE", "e活动兑换--兑换任务.xls");
//        fileMapping.put("TIGERITEMINFO", "y游乐场老虎机--实验室种子机.xls");
//        fileMapping.put("FLOWERLAND_SPIN", "y游乐场老虎机--实验室种子机.xls");
//        fileMapping.put("KEYMAPPING", "key mapping.xls");
//        fileMapping.put("ADSGENERALIZE", "x小语种广告推广.xls");
//        fileMapping.put("FSGENERALIZE", "x小语种花店推广.xls");
//        fileMapping.put("SAPPHIREEXCHANGE", "b宝石兑换.xls");
//        fileMapping.put("FLOWERCRAFT", "h花艺品.xls");
//        fileMapping.put("FLORALBENCHUPRADE", "h花艺制作台升级.xls");
//        fileMapping.put("UNLOCKFLOWERCRAFT", "h花艺品解锁.xls");
//        fileMapping.put("FLORALBENCHGROUP", "h花艺制作台分组.xls");
//        fileMapping.put("FLORALBENCHMAKELIST", "h花艺制作台制作清单.xls");
//        fileMapping.put("GARDENEXPAND", "h后花园扩地.xls");
//        fileMapping.put("ITEMEXTEND", "w物品扩展.xls");
//        fileMapping.put("SLOTINFO", "l拉霸.xls");
//        fileMapping.put("DIVINATION_INFO", "p评比占卜.xls");
//        fileMapping.put("DIVINATION_EXCHANGE_INFO", "p评比兑换.xls");
//        fileMapping.put("DIVINATION_COMMON_INFO", "p评比占卜通用信息.xls");
//        fileMapping.put("VIRTUAL_CURRENCY", "x虚拟货币.xls");
    }

    public static Map fieldMapping(String[] columnNames) {
        int columnCount = columnNames.length;
        Map mapping = new HashMap();
        for (int column = 0; column < columnCount; column++) {
            String columnName = columnNames[column];
            String[] strArr = columnName.split("[*#]");//http://hi.baidu.com/jszhangdaxu/item/e808680d99501b8d03ce1b13
            int prefixCount = 0;
            String prefixStr = "";
            String realColumnName = "";
            for (int i = 0; i < strArr.length; i++) {
                if (strArr[i].isEmpty()) {
                    prefixCount++;
                } else {
                    realColumnName = strArr[i];
                    prefixStr = columnName.replace(realColumnName, "");
                    break;
                }
            }
            Map columnInfo = new HashMap();
            columnInfo.put("level", prefixCount);//当前column所属分层数
            columnInfo.put("columnName", realColumnName);//当前column 的列名 
            columnInfo.put("prefixStr", prefixStr);//当前column 的列前缀
            columnInfo.put("column", column);//当前column
            mapping.put(column, columnInfo);
            HashMap levelMappingMap;
            if (mapping.containsKey("levelDistribution")) {
                levelMappingMap = (HashMap) mapping.get("levelDistribution");
            } else {
                levelMappingMap = new HashMap();
            }
            if (levelMappingMap.containsKey(prefixStr)) {
                Map levelDistributionLH;
                levelDistributionLH = (Map) levelMappingMap.get(prefixStr);
                levelDistributionLH.put("high", column);
                levelMappingMap.put(prefixStr, levelDistributionLH);
                mapping.put("levelDistribution", levelMappingMap);
            } else {
                Map levelDistributionLH = new HashMap();
                levelDistributionLH.put("low", column);
                levelDistributionLH.put("high", column);
                levelMappingMap.put(prefixStr, levelDistributionLH);
                mapping.put("levelDistribution", levelMappingMap);
            }
        }
        return mapping;
    }

    public static void transformCommonConfig(String configFilePath, String func, HashMap parseRuleConfig, String outputPath) throws IOException, BiffException {
        String tempConfigContent = "";
        /**
         * * 3.把一个map对象放到放到entry里，然后根据entry同时得到key和值
         */
        Set set = parseRuleConfig.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<String, HashMap> entry = (Entry<String, HashMap>) it.next();
            Map cfgInfo = entry.getValue();
            String relationSheetIndexStr = (String) cfgInfo.get("relationSheetIndex");
            String[] relationSheetIndexArray = relationSheetIndexStr.split(",");
            Arrays.sort(relationSheetIndexArray);
            int relationSheetIndexArrayCount = relationSheetIndexArray.length;
            for (int index = 0; index < relationSheetIndexArrayCount; index++) {
                int sheetIndex = Integer.parseInt(relationSheetIndexArray[index]);
                String[][] cfg = FileProcessor.parseXls(configFilePath, sheetIndex);
                String tplNameKey = "relationSheetIndex-" + sheetIndex + "-templateFileName";
                String tplNameValue = (String) cfgInfo.get(tplNameKey);
                tempConfigContent += DataProvider.buildStringFromStringArray(func, sheetIndex, cfg, tplNameValue);
            }
            String needGenerateFile = (String) cfgInfo.get("needGenerateFile");
            if (needGenerateFile.equals("1")) {//需要生成file
                String generateFileName = (String) cfgInfo.get("generateFileName");
                String fileContentEncoding = (String) cfgInfo.get("generateFileContentEncoding");
                File fileOutput = new File(outputPath + generateFileName);
                FileProcessor.writeToFile(tempConfigContent, fileOutput, fileContentEncoding);
            }
        }

    }

    public static int[] getSheetIndexArrayWithoutParseRule(String configFilePath) throws IOException, BiffException {
        int sheetCount = getSheetNumber(configFilePath);
        int parseRuleSheetIndex = getSheetIndexBySheetName(configFilePath, "parseRuleCfg");
        int sheetMax = sheetCount - 1;
        int[] indexArray = new int[sheetMax];
        for (int index = 0, indexArrayIndex = 0; index < sheetMax; index++) {
            if (index != parseRuleSheetIndex) {
                indexArray[indexArrayIndex++] = index;
            }
        }
        return indexArray;
    }

}
