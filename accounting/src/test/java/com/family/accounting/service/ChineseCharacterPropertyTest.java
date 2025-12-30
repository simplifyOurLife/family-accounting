package com.family.accounting.service;

import com.family.accounting.dto.*;
import com.family.accounting.entity.*;
import com.family.accounting.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 中文字符存储属性测试
 * <p>
 * Feature: family-accounting, Property 15: 中文字符存储完整性
 * Validates: Requirements 9.5
 * <p>
 * For any Chinese text input (family names, category names, transaction notes),
 * storing and retrieving should preserve the exact characters.
 */
class ChineseCharacterPropertyTest {

    /**
     * Property 15: 中文字符存储完整性 - 家庭名称
     * For any Chinese family name, storing and retrieving should preserve the exact characters.
     * <p>
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 100)
    void familyNameShouldPreserveChineseCharacters(
            @ForAll("chineseFamilyNames") String familyName,
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        InvitationMapper invitationMapper = Mockito.mock(InvitationMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);
        setField(familyService, "invitationMapper", invitationMapper);

        // Setup: user has no family
        when(familyMemberMapper.findByUserId(userId)).thenReturn(null);

        // Capture the inserted family
        AtomicLong familyIdCounter = new AtomicLong(1);
        final Family[] capturedFamily = new Family[1];
        doAnswer(invocation -> {
            Family family = invocation.getArgument(0);
            family.setId(familyIdCounter.getAndIncrement());
            capturedFamily[0] = family;
            return 1;
        }).when(familyMapper).insert(any(Family.class));

        // Setup: member insert
        doAnswer(invocation -> {
            FamilyMember member = invocation.getArgument(0);
            member.setId(1L);
            return 1;
        }).when(familyMemberMapper).insert(any(FamilyMember.class));

        // Setup: category and account book inserts
        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(System.nanoTime());
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        doAnswer(invocation -> {
            AccountBook accountBook = invocation.getArgument(0);
            accountBook.setId(1L);
            return 1;
        }).when(accountBookMapper).insert(any(AccountBook.class));

        // Setup: user lookup for VO conversion
        User user = new User();
        user.setId(userId);
        user.setNickname("测试用户");
        when(userMapper.findById(userId)).thenReturn(user);

        // Setup: member count
        when(familyMemberMapper.countByFamilyId(any())).thenReturn(1);

        // When: creating a family with Chinese name
        FamilyDTO dto = new FamilyDTO();
        dto.setName(familyName);

        FamilyVO result = familyService.createFamily(userId, dto);

        // Then: the family name should be preserved exactly
        assertNotNull(result);
        assertEquals(familyName, result.getName());

        // And: the captured family should have the exact same name
        assertNotNull(capturedFamily[0]);
        assertEquals(familyName, capturedFamily[0].getName());
    }


    /**
     * Property 15: 中文字符存储完整性 - 分类名称
     * For any Chinese category name, storing and retrieving should preserve the exact characters.
     * <p>
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 100)
    void categoryNameShouldPreserveChineseCharacters(
            @ForAll("chineseCategoryNames") String categoryName,
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @IntRange(min = 1, max = 2) int categoryType
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Capture the inserted category
        final Category[] capturedCategory = new Category[1];
        AtomicLong categoryIdCounter = new AtomicLong(1);
        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(categoryIdCounter.getAndIncrement());
            capturedCategory[0] = category;
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        // Setup: count transactions returns 0
        when(categoryMapper.countTransactionsByCategoryId(any())).thenReturn(0);

        // When: creating a category with Chinese name
        CategoryDTO dto = new CategoryDTO();
        dto.setName(categoryName);
        dto.setType(categoryType);
        dto.setParentId(null);

        CategoryVO result = categoryService.createCategory(userId, dto);

        // Then: the category name should be preserved exactly
        assertNotNull(result);
        assertEquals(categoryName, result.getName());

        // And: the captured category should have the exact same name
        assertNotNull(capturedCategory[0]);
        assertEquals(categoryName, capturedCategory[0].getName());
    }

    /**
     * Property 15: 中文字符存储完整性 - 交易备注
     * For any Chinese transaction note, storing and retrieving should preserve the exact characters.
     * <p>
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 100)
    void transactionNoteShouldPreserveChineseCharacters(
            @ForAll("chineseTransactionNotes") String note,
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long accountBookId,
            @ForAll @LongRange(min = 1, max = 10000) Long categoryId,
            @ForAll @IntRange(min = 1, max = 2) int transactionType
    ) {
        // Given: mock mappers
        TransactionMapper transactionMapper = Mockito.mock(TransactionMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        TransactionService transactionService = new TransactionService();
        setField(transactionService, "transactionMapper", transactionMapper);
        setField(transactionService, "accountBookMapper", accountBookMapper);
        setField(transactionService, "categoryMapper", categoryMapper);
        setField(transactionService, "familyMemberMapper", familyMemberMapper);
        setField(transactionService, "userMapper", userMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: account book exists and belongs to family
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        accountBook.setName("测试账本");
        when(accountBookMapper.findById(accountBookId)).thenReturn(accountBook);

        // Setup: category exists and belongs to family
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setName("测试分类");
        category.setType(transactionType);
        when(categoryMapper.findById(categoryId)).thenReturn(category);

        // Setup: user lookup
        User user = new User();
        user.setId(userId);
        user.setNickname("测试用户");
        when(userMapper.findById(userId)).thenReturn(user);

        // Capture the inserted transaction
        final Transaction[] capturedTransaction = new Transaction[1];
        AtomicLong transactionIdCounter = new AtomicLong(1);
        doAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(transactionIdCounter.getAndIncrement());
            capturedTransaction[0] = transaction;
            return 1;
        }).when(transactionMapper).insert(any(Transaction.class));

        // When: creating a transaction with Chinese note
        TransactionDTO dto = new TransactionDTO();
        dto.setAccountBookId(accountBookId);
        dto.setCategoryId(categoryId);
        dto.setType(transactionType);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setNote(note);
        dto.setTransactionDate(LocalDate.now());

        TransactionVO result = transactionService.createTransaction(userId, dto);

        // Then: the transaction note should be preserved exactly
        assertNotNull(result);
        assertEquals(note, result.getNote());

        // And: the captured transaction should have the exact same note
        assertNotNull(capturedTransaction[0]);
        assertEquals(note, capturedTransaction[0].getNote());
    }


    /**
     * Property 15: 中文字符存储完整性 - 特殊中文字符
     * For any Chinese text with special characters (punctuation, numbers mixed with Chinese),
     * storing and retrieving should preserve the exact characters.
     * <p>
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 100)
    void specialChineseCharactersShouldBePreserved(
            @ForAll("mixedChineseText") String text,
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Capture the inserted category
        final Category[] capturedCategory = new Category[1];
        AtomicLong categoryIdCounter = new AtomicLong(1);
        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(categoryIdCounter.getAndIncrement());
            capturedCategory[0] = category;
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        // Setup: count transactions returns 0
        when(categoryMapper.countTransactionsByCategoryId(any())).thenReturn(0);

        // When: creating a category with mixed Chinese text
        CategoryDTO dto = new CategoryDTO();
        dto.setName(text);
        dto.setType(Category.TYPE_EXPENSE);
        dto.setParentId(null);

        CategoryVO result = categoryService.createCategory(userId, dto);

        // Then: the text should be preserved exactly
        assertNotNull(result);
        assertEquals(text, result.getName());

        // And: the captured category should have the exact same text
        assertNotNull(capturedCategory[0]);
        assertEquals(text, capturedCategory[0].getName());

        // And: the text length should be preserved
        assertEquals(text.length(), result.getName().length());
        assertEquals(text.length(), capturedCategory[0].getName().length());
    }

    /**
     * 生成中文家庭名称
     */
    @Provide
    Arbitrary<String> chineseFamilyNames() {
        return Arbitraries.of(
                "张家",
                "李氏家族",
                "王家大院",
                "幸福之家",
                "温馨小窝",
                "快乐家庭",
                "和睦家园",
                "甜蜜的家",
                "我们的家",
                "爱的港湾",
                "三口之家",
                "四世同堂",
                "欢乐一家人",
                "美满家庭",
                "阳光之家"
        );
    }

    /**
     * 生成中文分类名称
     */
    @Provide
    Arbitrary<String> chineseCategoryNames() {
        return Arbitraries.of(
                "餐饮",
                "交通出行",
                "日常购物",
                "水电煤气",
                "房租物业",
                "医疗健康",
                "教育培训",
                "娱乐休闲",
                "人情往来",
                "工资收入",
                "奖金补贴",
                "投资理财",
                "兼职收入",
                "其他收入",
                "服装配饰"
        );
    }

    /**
     * 生成中文交易备注
     */
    @Provide
    Arbitrary<String> chineseTransactionNotes() {
        return Arbitraries.of(
                "午餐",
                "地铁充值",
                "超市购物",
                "电费缴纳",
                "房租支付",
                "看病买药",
                "孩子学费",
                "电影票",
                "朋友结婚随礼",
                "本月工资",
                "年终奖金",
                "股票分红",
                "周末兼职",
                "卖闲置物品",
                "买了一件新衣服",
                "给妈妈买的生日礼物",
                "全家外出聚餐",
                "网购日用品",
                "话费充值100元"
        );
    }

    /**
     * 生成混合中文文本（包含特殊字符、数字等）
     */
    @Provide
    Arbitrary<String> mixedChineseText() {
        return Arbitraries.of(
                "餐饮（外卖）",
                "交通-地铁",
                "购物：超市",
                "水电费2024年1月",
                "房租￥3000",
                "医疗/药品",
                "教育【培训班】",
                "娱乐、休闲",
                "人情往来——红包",
                "工资收入12月",
                "奖金+补贴",
                "投资&理财",
                "兼职@周末",
                "其他#杂项",
                "服装50%折扣",
                "测试中文123",
                "中英混合Test",
                "特殊符号！@#￥%",
                "括号（测试）"
        );
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
