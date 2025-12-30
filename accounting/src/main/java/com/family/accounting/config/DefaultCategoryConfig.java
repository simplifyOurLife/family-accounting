package com.family.accounting.config;

import java.util.Arrays;
import java.util.List;

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
                    new CategoryTemplate("话费", "phone", 1),
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
}
