不能有文件名相同的文件
比如：我要加载一个upgradeBuilding.httl的模板文件，但是 模板文件目录下面有一个“upgradeBuilding - 副本 (2).httl” 这样的文件，
这个时候就会报错：
SEVERE: Failed to compile class, cause: No such class name in java code., class: null, code: 
================================
package httl.spi.translators.templates;

import java.util.*;
import httl.*;

public final class Template__upgradeBuilding______(2)_httl_UTF_8_1378288758636_writer extends httl.spi.translators.templates.WriterTemplate {