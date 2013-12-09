/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package configparse;

import static configparse.ConfigParseJFrame.parseRuleConfig;
import httl.*;
import java.io.*;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.*;
import jxl.read.biff.BiffException;

/**
 *
 * @author Administrator
 */
public class DataParse {

    public static void transformUpgradeBuildingConfig(String configFilePath, String func, String outputPath) {
        try {
            String[][] upgradeBuildingCfg = FileProcessor.parseXls(configFilePath, 0);
            String upgradeBuildingCfgString = DataProvider.buildStringFromStringArray(func, 0, upgradeBuildingCfg, "");
            upgradeBuildingCfgString = "<?php\r\n" + upgradeBuildingCfgString;
            File fileOutput = new File(outputPath + "/upgradableBuilding/upgrading_conditions.php");
            FileProcessor.writeToFile(upgradeBuildingCfgString, fileOutput, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(ConfigParseJFrame.class.getName()).log(Level.SEVERE, null, ex);
            FileProcessor.showMessageDialogMessage(ex);
        } catch (BiffException ex) {
            Logger.getLogger(ConfigParseJFrame.class.getName()).log(Level.SEVERE, null, ex);
            FileProcessor.showMessageDialogMessage(ex);
        }
    }
    

    public static void getTemplateConfig() {

    }

    /**
     *
     * @param content
     * @param tplFileName
     * @return
     */
    public static String parseData(String[][] content, String tplFileName) {
        int rowCount = content.length;
        String[] itemExtendLangModel = getModelNamesFromStringArray(content);
        String buildedContent;
        Map fieldMapping = FileProcessor.fieldMapping(itemExtendLangModel);
        itemExtendLangModel = cleanupModelNames(itemExtendLangModel);
        boolean isLatestRow = false;
        for (int row = 1; row < rowCount; row++) {//去掉表头
            if (row == rowCount - 1) {
                isLatestRow = true;
            }
            parseRow(content[row], itemExtendLangModel, fieldMapping, isLatestRow);
        }
        if (finalInfo.isEmpty()) {

        }
        buildedContent = parseDataToString(tplFileName);
        return buildedContent;
    }
    public static Map atom;//一个完整的配置(不能再分)
    public static String[] latestRowData;//上次处理的数据
    public static int atomCount = 0;//完整的配置的个数
    public static List finalInfo = new ArrayList();//整理好之后的数据

    public static void parseRow(String[] content, String[] itemExtendLangModel, Map fieldMapping, boolean isLatestRow) {
        Map levelDistribution = (HashMap) fieldMapping.get("levelDistribution");
        if (latestRowData == null) {
            latestRowData = new String[itemExtendLangModel.length];
        }

        if (!content[0].equals(latestRowData[0]) || atom == null) {
            if (atom != null) {
                finalInfo.add(atom);
            }
            latestRowData = new String[itemExtendLangModel.length];
            atom = new HashMap();
        }
        /*
         Set set = levelDistribution.keySet();
         Iterator it = set.iterator();
         while (it.hasNext()) {
         String prefixStr= (String) it.next();
         HashMap levelDistributionLH = (HashMap)levelDistribution.get(prefixStr);
         int low = (Integer) levelDistributionLH.get("low");
         int high = (Integer) levelDistributionLH.get("high");
         HashMap currentLevelConent;
         int currentLevelConentCount;
         if (atom.containsKey(prefixStr)) {//
         currentLevelConent = (HashMap) atom.get(prefixStr);
         currentLevelConentCount = currentLevelConent.size();
         } else {
         currentLevelConent = new HashMap();
         currentLevelConentCount = 0;
         }

         if (!content[low].equals(latestRowData[low])) {
         int len = high - low + 1;
         String[] tmpContent = new String[len];
         String[] tmpKey = new String[len];
         System.arraycopy(content, low, tmpContent, 0, len);
         System.arraycopy(itemExtendLangModel, low, tmpKey, 0, len);
         HashMap finalContent = new HashMap();
         for (int i = 0; i < len; i++) {
         finalContent.put(tmpKey[i], tmpContent[i]);
         }
         currentLevelConent.put(currentLevelConentCount, finalContent);
         atom.put(prefixStr, currentLevelConent);
         }
         }
         */
        /**
         * * 3.把一个map对象放到放到entry里，然后根据entry同时得到key和值
         */
        Set set = levelDistribution.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<String, HashMap> entry = (Entry<String, HashMap>) it.next();
            Map levelDistributionLH = entry.getValue();
            String prefixStr = entry.getKey();
            int low = (Integer) levelDistributionLH.get("low");
            int high = (Integer) levelDistributionLH.get("high");
            HashMap currentLevelConent;
            int currentLevelConentCount;
            if (atom.containsKey(prefixStr)) {//
                currentLevelConent = (HashMap) atom.get(prefixStr);
                currentLevelConentCount = currentLevelConent.size();
            } else {
                currentLevelConent = new HashMap();
                currentLevelConentCount = 0;
            }

            if (!content[low].equals(latestRowData[low])) {
                int len = high - low + 1;
                String[] tmpContent = new String[len];
                String[] tmpKey = new String[len];
                System.arraycopy(content, low, tmpContent, 0, len);
                System.arraycopy(itemExtendLangModel, low, tmpKey, 0, len);
                HashMap finalContent = new HashMap();
                for (int i = 0; i < len; i++) {
                    finalContent.put(tmpKey[i], tmpContent[i]);
                }
                currentLevelConent.put(currentLevelConentCount, finalContent);
                atom.put(prefixStr, currentLevelConent);
            }
        }

        latestRowData = content;

        if (isLatestRow) {//最后一次调用
            finalInfo.add(atom);
        }
    }

    public static Map<String, Object> parameters;
    public static Properties properties;
    public static Engine engine;
    public static Template tpl;

    public static String parseDataToString(String tplFileName) {
        String content = "";
        try {
            if (parameters == null) {
                parameters = new HashMap<String, Object>();
                properties = new Properties();
                FileInputStream fis = new FileInputStream("./httl.properties");
                properties.load(fis);
                engine = Engine.getEngine(properties);
                tpl = engine.getTemplate(tplFileName);
            }

            parameters.put("finalInfo", finalInfo);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("H:m:s");
            parameters.put("dateString", dateFormat.format(calendar.getTime()));
            parameters.put("timeString", timeFormat.format(calendar.getTime()));

            String ip, address;
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress().toString();//获得本机IP　　
            address = addr.getHostName().toString();//获得本机名称
            parameters.put("ip", ip);
            parameters.put("address", address);
            String tmp = (String) tpl.evaluate(parameters);
            content = tmp;
            System.out.println("tmp : " + tmp);
        } catch (IOException e) {
            System.out.println("IOException : " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("ParseException : " + e.getMessage());
        }
        return content;
    }

    /**
     * 解析规则 TODO
     *
     * @param atom
     * @return
     *
     * public static String parseDataToString(Map atom, Map fieldMapping) {
     * String content = ""; try { if (parameters == null) { parameters = new
     * HashMap<String, Object>(); properties = new Properties(); FileInputStream
     * fis = new FileInputStream("./httl.properties"); properties.load(fis);
     * engine = Engine.getEngine(properties); tpl =
     * engine.getTemplate("/upgradeBuilding.httl"); } ArrayList levelZeroList =
     * (ArrayList) (atom.get(1)); String[] levelZeroArray = (String[])
     * (levelZeroList.get(0)); ArrayList levelZeroListFinal = new ArrayList();
     * for (int i = 0; i < levelZeroArray.length; i++) {
     * levelZeroListFinal.add(levelZeroArray[i]); }
     *
     * ArrayList levelOneList = (ArrayList) (atom.get(2)); String[]
     * levelOneArray = (String[]) (levelOneList.get(0)); ArrayList
     * levelOneListFinal = new ArrayList(); for (int i = 0; i <
     * levelOneArray.length; i++) { levelOneListFinal.add(levelOneArray[i]); }
     *
     * ArrayList levelTwoList = (ArrayList) (atom.get(3));
     *
     * parameters.put("levelZeroList", levelZeroListFinal);
     * parameters.put("levelOneList", levelOneListFinal);
     * parameters.put("levelTwoList", levelTwoList); // parameters.put("atom",
     * atom); String tmp = (String) tpl.evaluate(parameters); content = tmp;
     * System.out.println("tmp : " + tmp); } catch (Exception e) {
     * System.out.println("e : " + e.getMessage()); }
     *
     * return content; }
     */
    /**
     *
     * @param content
     * @return
     */
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

    public static String[] cleanupModelNames(String[] modelNames) {
        int i = 0;
        for (; i < modelNames.length; i++) {
            modelNames[i] = modelNames[i].replaceAll("[*#]", "");
        }

        return modelNames;
    }

    public static String parseLevelContent(ArrayList contentList, HashMap rule) {
        String levelContent = "";

        return levelContent;
    }

    public static Map getUpgradeBuildingParseRule() {
        Map<String, Object> map = new HashMap<String, Object>();

        Map<String, String> ruleMap = new HashMap<String, String>();

        String atomRuleBenginWith, atomRuleAtom, atomRuleEndWith;
        atomRuleBenginWith = "'COLVALUE-iId' => array(\n";
        atomRuleAtom = "RULEMAPKEY-** ++ RULEMAPKEY-***";
        atomRuleEndWith = "),";
        ruleMap.put("startWith", atomRuleBenginWith);
        ruleMap.put("atom", atomRuleAtom);
        ruleMap.put("endWith", atomRuleEndWith);
        map.put("1", ruleMap);

        String virtualRuleBenginWith, virtualRuleAtom, virtualRuleEndWith;
        virtualRuleBenginWith = "'virtual' => array(\n";
        virtualRuleAtom = "'COLNAME' => 'COLVALUE'";
        virtualRuleEndWith = "),";
        ruleMap.put("startWith", virtualRuleBenginWith);
        ruleMap.put("atom", virtualRuleAtom);
        ruleMap.put("endWith", virtualRuleEndWith);
        map.put("2", ruleMap);

        String requireItemRuleBenginWith, requireItemRuleAtom, requireItemRuleEndWith;
        requireItemRuleBenginWith = "'COLVALUE-requireItemId' => array(\n";
        requireItemRuleAtom = "'COLNAME' => 'COLVALUE'";
        requireItemRuleEndWith = "),";
        ruleMap.put("startWith", requireItemRuleBenginWith);
        ruleMap.put("atom", requireItemRuleAtom);
        ruleMap.put("endWith", requireItemRuleEndWith);
        map.put("3", ruleMap);

        virtualRuleBenginWith = "$J7CONFIG['upgradeBuildingCfg'] = array(";
        virtualRuleAtom = "RULEMAPKEY-*";
        virtualRuleEndWith = "),";
        ruleMap.put("startWith", virtualRuleBenginWith);
        ruleMap.put("atom", virtualRuleAtom);
        ruleMap.put("endWith", virtualRuleEndWith);
        map.put("dataStruct", ruleMap);
        return map;
    }
}
