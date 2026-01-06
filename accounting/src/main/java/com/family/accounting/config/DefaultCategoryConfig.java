package com.family.accounting.config;

import java.util.*;

/**
 * 默认分类配置
 * 用于新家庭创建时初始化默认分类数据
 */
public class DefaultCategoryConfig {

    /**
     * 分类类型: 支出
     */
    public static final int TYPE_EXPENSE = 1;

    /**
     * 分类类型: 收入
     */
    public static final int TYPE_INCOME = 2;

    /**
     * 默认图标ID
     */
    public static final String DEFAULT_ICON = "label-o";

    /**
     * 图标信息类
     */
    public static class IconInfo {
        private final String id;
        private final String name;
        private final String type; // vant 或 custom

        public IconInfo(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * 分类模板类
     */
    public static class CategoryTemplate {
        private final String name;
        private final String icon;
        private final int sortOrder;
        private final List<CategoryTemplate> children;

        public CategoryTemplate(String name, String icon, int sortOrder) {
            this(name, icon, sortOrder, null);
        }

        public CategoryTemplate(String name, String icon, int sortOrder, List<CategoryTemplate> children) {
            this.name = name;
            this.icon = icon;
            this.sortOrder = sortOrder;
            this.children = children;
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public List<CategoryTemplate> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }
    }

    /**
     * 图标库 - 按分类组织的图标列表
     * Key: 图标分类名称, Value: 该分类下的图标列表
     */
    public static final Map<String, List<IconInfo>> ICON_LIBRARY;

    /**
     * 所有有效图标ID集合（用于快速验证）
     */
    public static final Set<String> VALID_ICON_IDS;

    static {
        ICON_LIBRARY = new LinkedHashMap<>();

        // 餐饮类图标
        ICON_LIBRARY.put("餐饮", Arrays.asList(
                new IconInfo("food", "餐饮", "custom"),
                new IconInfo("breakfast", "早餐", "custom"),
                new IconInfo("lunch", "午餐", "custom"),
                new IconInfo("dinner", "晚餐", "custom"),
                new IconInfo("snack", "零食", "custom"),
                new IconInfo("drink", "饮料", "custom"),
                new IconInfo("coffee", "咖啡", "custom"),
                new IconInfo("cake", "蛋糕", "custom"),
                new IconInfo("hotpot", "火锅", "custom"),
                new IconInfo("fruit", "水果", "custom"),
                new IconInfo("noodle", "面食", "custom"),
                new IconInfo("rice", "米饭", "custom"),
                new IconInfo("bbq", "烧烤", "custom"),
                new IconInfo("seafood", "海鲜", "custom"),
                new IconInfo("fastfood", "快餐", "custom")
        ));

        // 交通类图标
        ICON_LIBRARY.put("交通", Arrays.asList(
                new IconInfo("transport", "交通", "custom"),
                new IconInfo("bus", "公交", "custom"),
                new IconInfo("subway", "地铁", "custom"),
                new IconInfo("taxi", "打车", "custom"),
                new IconInfo("fuel", "加油", "custom"),
                new IconInfo("parking", "停车", "custom"),
                new IconInfo("car", "汽车", "custom"),
                new IconInfo("bike", "自行车", "custom"),
                new IconInfo("train", "火车", "custom"),
                new IconInfo("plane", "飞机", "custom")
        ));

        // 购物类图标
        ICON_LIBRARY.put("购物", Arrays.asList(
                new IconInfo("shopping", "购物", "custom"),
                new IconInfo("cart-o", "购物车", "vant"),
                new IconInfo("daily", "日用品", "custom"),
                new IconInfo("clothing", "服饰", "custom"),
                new IconInfo("digital", "数码", "custom"),
                new IconInfo("appliance", "家电", "custom"),
                new IconInfo("gift-o", "礼物", "vant"),
                new IconInfo("bag-o", "包包", "vant"),
                new IconInfo("cosmetic", "化妆品", "custom"),
                new IconInfo("jewelry", "首饰", "custom")
        ));

        // 居住类图标
        ICON_LIBRARY.put("居住", Arrays.asList(
                new IconInfo("home", "居住", "custom"),
                new IconInfo("wap-home-o", "家", "vant"),
                new IconInfo("rent", "房租", "custom"),
                new IconInfo("utility", "水电", "custom"),
                new IconInfo("property", "物业", "custom"),
                new IconInfo("repair", "维修", "custom"),
                new IconInfo("furniture", "家具", "custom"),
                new IconInfo("decoration", "装修", "custom"),
                new IconInfo("cleaning", "清洁", "custom"),
                new IconInfo("security", "安保", "custom")
        ));

        // 娱乐类图标
        ICON_LIBRARY.put("娱乐", Arrays.asList(
                new IconInfo("entertainment", "娱乐", "custom"),
                new IconInfo("movie", "电影", "custom"),
                new IconInfo("game", "游戏", "custom"),
                new IconInfo("travel", "旅游", "custom"),
                new IconInfo("sport", "运动", "custom"),
                new IconInfo("music-o", "音乐", "vant"),
                new IconInfo("photo-o", "摄影", "vant"),
                new IconInfo("reading", "阅读", "custom"),
                new IconInfo("ktv", "KTV", "custom"),
                new IconInfo("bar", "酒吧", "custom")
        ));

        // 医疗类图标
        ICON_LIBRARY.put("医疗", Arrays.asList(
                new IconInfo("medical", "医疗", "custom"),
                new IconInfo("medicine", "药品", "custom"),
                new IconInfo("registration", "挂号", "custom"),
                new IconInfo("checkup", "检查", "custom"),
                new IconInfo("healthcare", "保健", "custom"),
                new IconInfo("hospital", "医院", "custom"),
                new IconInfo("dental", "牙科", "custom"),
                new IconInfo("eye", "眼科", "custom"),
                new IconInfo("vaccine", "疫苗", "custom"),
                new IconInfo("insurance", "保险", "custom"),
                new IconInfo("surgery", "手术", "custom"),
                new IconInfo("therapy", "理疗", "custom"),
                new IconInfo("tcm", "中医", "custom"),
                new IconInfo("firstaid", "急救", "custom"),
                new IconInfo("pregnancy", "孕产", "custom")
        ));

        // 教育类图标
        ICON_LIBRARY.put("教育", Arrays.asList(
                new IconInfo("education", "教育", "custom"),
                new IconInfo("tuition", "学费", "custom"),
                new IconInfo("book", "书籍", "custom"),
                new IconInfo("training", "培训", "custom"),
                new IconInfo("stationery", "文具", "custom"),
                new IconInfo("course", "课程", "custom"),
                new IconInfo("exam", "考试", "custom"),
                new IconInfo("certificate", "证书", "custom"),
                new IconInfo("school", "学校", "custom"),
                new IconInfo("library", "图书馆", "custom")
        ));

        // 通讯类图标
        ICON_LIBRARY.put("通讯", Arrays.asList(
                new IconInfo("communication", "通讯", "custom"),
                new IconInfo("phone-o", "电话", "vant"),
                new IconInfo("internet", "网费", "custom"),
                new IconInfo("membership", "会员", "custom"),
                new IconInfo("chat-o", "聊天", "vant"),
                new IconInfo("sms", "短信", "custom"),
                new IconInfo("email", "邮件", "custom"),
                new IconInfo("cloud", "云服务", "custom"),
                new IconInfo("software", "软件", "custom"),
                new IconInfo("subscription", "订阅", "custom")
        ));

        // 收入类图标
        ICON_LIBRARY.put("收入", Arrays.asList(
                new IconInfo("salary", "工资", "custom"),
                new IconInfo("bonus", "奖金", "custom"),
                new IconInfo("investment", "投资", "custom"),
                new IconInfo("parttime", "兼职", "custom"),
                new IconInfo("dividend", "分红", "custom"),
                new IconInfo("interest", "利息", "custom"),
                new IconInfo("rental", "租金收入", "custom"),
                new IconInfo("refund", "退款", "custom"),
                new IconInfo("gift-income", "礼金", "custom"),
                new IconInfo("lottery", "彩票", "custom")
        ));

        // 其他类图标
        ICON_LIBRARY.put("其他", Arrays.asList(
                new IconInfo("other", "其他", "custom"),
                new IconInfo("label-o", "标签", "vant"),
                new IconInfo("star-o", "收藏", "vant"),
                new IconInfo("fire-o", "热门", "vant"),
                new IconInfo("like-o", "喜欢", "vant"),
                new IconInfo("flag-o", "标记", "vant"),
                new IconInfo("bookmark-o", "书签", "vant"),
                new IconInfo("question-o", "问题", "vant"),
                new IconInfo("info-o", "信息", "vant"),
                new IconInfo("warning-o", "警告", "vant")
        ));

        // 构建有效图标ID集合
        Set<String> validIds = new HashSet<>();
        for (List<IconInfo> icons : ICON_LIBRARY.values()) {
            for (IconInfo icon : icons) {
                validIds.add(icon.getId());
            }
        }
        VALID_ICON_IDS = Collections.unmodifiableSet(validIds);
    }

    /**
     * 验证图标ID是否有效
     *
     * @param iconId 图标ID
     * @return true-有效，false-无效
     */
    public static boolean isValidIconId(String iconId) {
        return iconId != null && VALID_ICON_IDS.contains(iconId);
    }

    /**
     * 获取图标ID，如果无效则返回默认图标
     *
     * @param iconId 图标ID
     * @return 有效的图标ID或默认图标
     */
    public static String getIconIdOrDefault(String iconId) {
        if (isValidIconId(iconId)) {
            return iconId;
        }
        return DEFAULT_ICON;
    }

    /**
     * 默认支出分类
     */
    public static final List<CategoryTemplate> DEFAULT_EXPENSE_CATEGORIES = Arrays.asList(
            new CategoryTemplate("餐饮", "food", 1, Arrays.asList(
                    new CategoryTemplate("早餐", "breakfast", 1),
                    new CategoryTemplate("午餐", "lunch", 2),
                    new CategoryTemplate("晚餐", "dinner", 3),
                    new CategoryTemplate("零食", "snack", 4),
                    new CategoryTemplate("饮料", "drink", 5)
            )),
            new CategoryTemplate("交通", "transport", 2, Arrays.asList(
                    new CategoryTemplate("公交", "bus", 1),
                    new CategoryTemplate("地铁", "subway", 2),
                    new CategoryTemplate("打车", "taxi", 3),
                    new CategoryTemplate("加油", "fuel", 4),
                    new CategoryTemplate("停车", "parking", 5)
            )),
            new CategoryTemplate("购物", "shopping", 3, Arrays.asList(
                    new CategoryTemplate("日用品", "daily", 1),
                    new CategoryTemplate("服饰", "clothing", 2),
                    new CategoryTemplate("数码", "digital", 3),
                    new CategoryTemplate("家电", "appliance", 4)
            )),
            new CategoryTemplate("居住", "home", 4, Arrays.asList(
                    new CategoryTemplate("房租", "rent", 1),
                    new CategoryTemplate("水电", "utility", 2),
                    new CategoryTemplate("物业", "property", 3),
                    new CategoryTemplate("维修", "repair", 4)
            )),
            new CategoryTemplate("娱乐", "entertainment", 5, Arrays.asList(
                    new CategoryTemplate("电影", "movie", 1),
                    new CategoryTemplate("游戏", "game", 2),
                    new CategoryTemplate("旅游", "travel", 3),
                    new CategoryTemplate("运动", "sport", 4)
            )),
            new CategoryTemplate("医疗", "medical", 6, Arrays.asList(
                    new CategoryTemplate("药品", "medicine", 1),
                    new CategoryTemplate("挂号", "registration", 2),
                    new CategoryTemplate("检查", "checkup", 3),
                    new CategoryTemplate("保健", "healthcare", 4)
            )),
            new CategoryTemplate("教育", "education", 7, Arrays.asList(
                    new CategoryTemplate("学费", "tuition", 1),
                    new CategoryTemplate("书籍", "book", 2),
                    new CategoryTemplate("培训", "training", 3),
                    new CategoryTemplate("文具", "stationery", 4)
            )),
            new CategoryTemplate("通讯", "communication", 8, Arrays.asList(
                    new CategoryTemplate("话费", "phone-o", 1),
                    new CategoryTemplate("网费", "internet", 2),
                    new CategoryTemplate("会员", "membership", 3)
            )),
            new CategoryTemplate("其他支出", "other", 99)
    );

    /**
     * 默认收入分类
     */
    public static final List<CategoryTemplate> DEFAULT_INCOME_CATEGORIES = Arrays.asList(
            new CategoryTemplate("工资", "salary", 1),
            new CategoryTemplate("奖金", "bonus", 2),
            new CategoryTemplate("投资收益", "investment", 3),
            new CategoryTemplate("兼职", "parttime", 4),
            new CategoryTemplate("其他收入", "other", 99)
    );
}
