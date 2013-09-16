/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package configparse;

import httl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author Administrator
 */
public class HTTLTest {

    public static void main(String[] args) throws IOException, ParseException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Map upgradeBuilding = new HashMap();
        upgradeBuilding.put("iId", "iIdValue");
        upgradeBuilding.put("iNextLevelId", "iNextLevelIdValue");
        parameters.put("upgradeBuilding", upgradeBuilding);
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream("./httl.properties");
        prop.load(fis);
        Engine engine = Engine.getEngine(prop);
        Template tpl = engine.getTemplate("./upgradeBuilding.httl");
        String tmp = (String) tpl.evaluate(parameters);
        System.out.println("tmp : " + tmp);
    }
}
