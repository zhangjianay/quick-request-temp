package test;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlReplaceTest {


    @Test
    public void test2() {
        String url = "/test/{id}/name/{name}";

        Pattern p = Pattern.compile("\\{([^}]*)}");
        Matcher m = p.matcher(url);
        while (m.find()) {
            System.out.println(m.group(1));//第一次匹配成功是one,第二次匹配成功是two,第三次匹配为three
        }
    }

    @Test
    public void test5() {
        System.out.println(RandomUtil.randomString(5));
        System.out.println(RandomUtil.randomString(0));
        System.out.println(RandomUtil.randomString("xx", 5));
        System.out.println(LocalDate.now(ZoneId.of(JSON.defaultTimeZone.getID())).toString());
        System.out.println(RandomUtil.randomString(0));
    }

    public static CustomNode convertJsonObjectToNode(CustomNode node, JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        keys.parallelStream().forEach(key -> {

            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                JSONObject valueJsonObject = (JSONObject) value;
                CustomNode customNode = new CustomNode(key, null);
                node.add(convertJsonObjectToNode(customNode, valueJsonObject));
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                if (jsonArray.size() == 0) {
                    return;
                }
                convertJsonArrayToNode(key, jsonArray, node);
            } else {
                node.add(new CustomNode(key, value));
            }
        });
        return node;
    }


    public static void convertJsonArrayToNode(String key, JSONArray jsonArray, CustomNode node) {

        jsonArray.parallelStream().forEach(json -> {
            CustomNode nodeArray = new CustomNode(key, null);
            if (json instanceof JSONObject) {
                JSONObject valueJsonObject = (JSONObject) json;
                node.add(convertJsonObjectToNode(nodeArray, valueJsonObject));
            } else if (json instanceof JSONArray) {
                JSONArray tmpJsonArray = (JSONArray) json;
                if (tmpJsonArray.size() == 0) {
                    return;
                }
                convertJsonArrayToNode(key, tmpJsonArray, nodeArray);
                node.add(nodeArray);
            } else {
                node.add(new CustomNode(key, json));
            }
        });
    }


    public static class CustomNode extends DefaultMutableTreeNode {
        private String key;
        private Object value;

        public CustomNode() {
        }

        public CustomNode(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    private enum SearchTypeEnum {
        name,
        url;

        public static SearchTypeEnum fromValue(String name) {
            for (SearchTypeEnum v : values()) {
                if (v.name().equals(name)) {
                    return v;
                }
            }
            return null;
        }
    }

    private static Map<String, String> getQuery(String search) {
        String[] split = search.split("\\|");
        StringBuilder rule = new StringBuilder();
        String query = "";
        for (String s : split) {
            s = s.trim();
            if (SearchTypeEnum.fromValue(s) != null && search.indexOf("|", search.indexOf(s) + 1) != -1) {
                rule.append(s).append(",");
            } else {
                query = s;
            }
        }
        return ImmutableMap.<String, String>builder().put("query", query).put("rule", rule.toString()).build();
    }

    @Test
    public void test6() {

        System.out.println(getQuery("name| a "));


    }


}
