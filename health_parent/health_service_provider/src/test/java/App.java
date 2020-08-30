import com.itheima.dao.MenuDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {


    @Autowired
    private MenuDao menuDao;

    @Test
    //获取当前登录用户的用户名获取对应的模块信息
    public void findAllModuleByUsername() {
        List<Map> menuList = menuDao.findAllModuleByUsername("admin");
        // 最后的结果
        List<Map> newMenuList = new ArrayList<>();
        // 先找到所有的一级菜单
        for (int i = 0; i < menuList.size(); i++) {
            Map<String, Object> map = menuList.get(i);
            String parentMenuId = (String) map.get("parentMenuId");
            // 如果是顶级菜单，没有父菜单
            if (parentMenuId == null) {
                newMenuList.add(map);
            }
        }
        // 为一级菜单设置子菜单，getChild是递归调用的
        for (Map menu : newMenuList) {
            menu.put("children", getChild((String) menu.get("id"), menuList));
        }
        System.out.println(newMenuList);
    }

    //    递归查找子菜单
    private List<Map> getChild(String id, List<Map> menuList) {
        // 子菜单
        List<Map> childList = new ArrayList<>();
        for (Map menu : menuList) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (menu.get("parentMenuId") == id) {
                childList.add(menu);
            }
        }
        // 把子菜单的子菜单再循环一遍
        for (Map menu : childList) {
            //如果子菜单的url为空代表还有子菜单。
            if (menu.get("linkUrl") == null) {
                // 递归
                menu.put("children", getChild((String) menu.get("id"), menuList));
            }
        } // 递归退出条件
        if (childList.size() == 0) {
            return null;
        }
        return childList;
    }
    @Test
    public void sdas() {
        String ss = "null";
        System.out.println(ss==null);
    }

}
