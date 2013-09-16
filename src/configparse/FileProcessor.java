/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package configparse;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
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
     * @param Sheet sheet
     * @param int i
     * @param int j
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
     * @param String str 需要判断类型的字符串
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
     * @param Cell formatCell
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
     * 获得指定worksheet的名称
     *
     * @param String filePath 文件路径
     * @param int sheetIndex worksheet索引
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
     * @param String filePath
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
     * @param String file_path
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
//                        System.out.println(configBaseDir);
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
     * @param String filePath 文件路径
     * @param int sheetNum work sheet 索引
     * @param boolean reverse 是否需要reverse所得二维数组
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
     * @param String filePath filePath 文件路径
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
     * @param String contents 将要写入文件的内容字符串
     * @param String descFile 将要写入文件的路径
     * @param String encoding 文件编码
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
     * @param String strSource 将要被替换的字符串
     * @param String strFrom 需要被替换的字符串
     * @param String strTo
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
     * @param Exception ex
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
        int maxLevel = 0;//最大分层数
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
            if (prefixCount > maxLevel) {
                maxLevel = prefixCount;
                List levelMappingList;
                if (mapping.containsKey("levelDistribution")) {
                    levelMappingList = (ArrayList) mapping.get("levelDistribution");
                } else {
                    levelMappingList = new ArrayList();
                    levelMappingList.add("init Level");
                }
                Map levelDistributionLH = new HashMap();
                levelDistributionLH.put("low", column);
                levelDistributionLH.put("high", column);
                levelMappingList.add(maxLevel, levelDistributionLH);
                mapping.put("levelDistribution", levelMappingList);
            } else {
                Map levelDistributionLH;
                List levelMappingList;
                levelMappingList = (ArrayList) mapping.get("levelDistribution");
                levelDistributionLH = (Map) levelMappingList.get(maxLevel);
                levelDistributionLH.put("high", column);
                levelMappingList.set(maxLevel, levelDistributionLH);
                mapping.put("levelDistribution", levelMappingList);
            }
        }
        mapping.put("maxLevel", maxLevel);

        return mapping;
    }
}
