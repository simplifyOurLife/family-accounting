# API 文档

家庭记账系统后端 API 接口文档

## 基础信息

- 基础路径: `/api`
- 数据格式: JSON
- 字符编码: UTF-8
- 认证方式: JWT Bearer Token

## 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或 Token 过期） |
| 403 | 禁止访问（无权限） |
| 429 | 请求过于频繁（账户锁定或 IP 限制） |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 描述 |
|--------|------|
| 1001 | 验证码错误 |
| 1002 | 验证码已过期 |
| 1003 | 账户已锁定 |
| 1004 | IP访问频率过高 |
| 1005 | 手机号已注册 |
| 1006 | 用户名或密码错误 |

---

## 认证模块

### 获取图片验证码

获取登录/注册所需的图片验证码。


**请求**

```
GET /api/auth/captcha
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "550e8400-e29b-41d4-a716-446655440000",
    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| captchaKey | string | 验证码唯一标识（UUID），提交时需携带 |
| captchaImage | string | Base64 编码的验证码图片 |

**说明**
- 验证码有效期为 5 分钟
- 验证码验证后立即失效（无论成功或失败）
- 验证码验证时大小写不敏感

---

### 用户注册

注册新用户账号。

**请求**

```
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "phone": "13800138000",
  "password": "123456",
  "nickname": "张三",
  "captchaKey": "550e8400-e29b-41d4-a716-446655440000",
  "captchaCode": "A1B2"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号（11位） |
| password | string | 是 | 密码（6-20位） |
| nickname | string | 否 | 昵称（最长50字符） |
| captchaKey | string | 是 | 验证码标识 |
| captchaCode | string | 是 | 验证码（4位） |


**响应**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "张三",
    "avatar": null,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

---

### 用户登录

用户登录获取 Token。

**请求**

```
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "phone": "13800138000",
  "password": "123456",
  "captchaKey": "550e8400-e29b-41d4-a716-446655440000",
  "captchaCode": "A1B2"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号 |
| password | string | 是 | 密码 |
| captchaKey | string | 是 | 验证码标识 |
| captchaCode | string | 是 | 验证码（4位） |

**响应**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "phone": "13800138000",
    "nickname": "张三"
  }
}
```

**说明**
- 15分钟内登录失败5次将锁定账户30分钟
- Token 需在后续请求的 Header 中携带：`Authorization: Bearer <token>`

---

### 获取当前用户信息

获取当前登录用户的信息。

**请求**

```
GET /api/auth/info
Authorization: Bearer <token>
```


**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

---

### 用户登出

退出登录，使当前 Token 失效。

**请求**

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

## 家庭管理模块

### 创建家庭

创建一个新的家庭，创建者自动成为管理员。

**请求**

```
POST /api/family
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "温馨小家"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 家庭名称（最长50字符） |

**响应**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "温馨小家",
    "adminId": 1,
    "adminNickname": "张三",
    "memberCount": 1,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

**说明**
- 用户只能属于一个家庭
- 创建家庭时会自动创建默认分类和默认账本

---


### 获取当前家庭信息

获取当前用户所属家庭的信息。

**请求**

```
GET /api/family
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "温馨小家",
    "adminId": 1,
    "adminNickname": "张三",
    "memberCount": 3,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

---

### 更新家庭信息

更新家庭名称（仅管理员可操作）。

**请求**

```
PUT /api/family
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "幸福之家"
}
```

**响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "幸福之家",
    "adminId": 1,
    "adminNickname": "张三",
    "memberCount": 3,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

---

### 邀请成员

邀请其他用户加入家庭（仅管理员可操作）。

**请求**

```
POST /api/family/invite
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "phone": "13900139000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 被邀请人手机号 |

**响应**

```json
{
  "code": 200,
  "message": "邀请已发送",
  "data": null
}
```

---


### 获取待处理邀请列表

获取当前用户收到的待处理邀请。

**请求**

```
GET /api/family/invitations
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "familyName": "温馨小家",
      "inviterId": 1,
      "inviterNickname": "张三",
      "inviteeId": 2,
      "inviteeNickname": "李四",
      "inviteePhone": "13900139000",
      "status": 0,
      "statusText": "待处理",
      "createdAt": "2024-01-01T10:00:00",
      "expiredAt": "2024-01-08T10:00:00"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| status | int | 状态: 0-待处理 1-已接受 2-已拒绝 3-已过期 |

---

### 接受邀请

接受家庭邀请，加入家庭。

**请求**

```
POST /api/family/invitation/{id}/accept
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 邀请ID |

**响应**

```json
{
  "code": 200,
  "message": "已加入家庭",
  "data": null
}
```

---

### 拒绝邀请

拒绝家庭邀请。

**请求**

```
POST /api/family/invitation/{id}/decline
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 邀请ID |

**响应**

```json
{
  "code": 200,
  "message": "已拒绝邀请",
  "data": null
}
```

---


### 获取家庭成员列表

获取当前家庭的所有成员。

**请求**

```
GET /api/family/members
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "phone": "13800138000",
      "userNickname": "张三",
      "nickname": "爸爸",
      "avatar": "https://example.com/avatar.jpg",
      "isAdmin": true,
      "joinedAt": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "userId": 2,
      "phone": "13900139000",
      "userNickname": "李四",
      "nickname": "妈妈",
      "avatar": null,
      "isAdmin": false,
      "joinedAt": "2024-01-02T10:00:00"
    }
  ]
}
```

---

### 设置成员昵称

设置家庭成员在家庭中的昵称（仅管理员可操作）。

**请求**

```
PUT /api/family/member/{id}/nickname
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 成员记录ID |

```json
{
  "nickname": "宝贝"
}
```

**响应**

```json
{
  "code": 200,
  "message": "昵称已更新",
  "data": null
}
```

---

### 移除成员

从家庭中移除成员（仅管理员可操作）。

**请求**

```
DELETE /api/family/member/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 成员记录ID |

**响应**

```json
{
  "code": 200,
  "message": "成员已移除",
  "data": null
}
```

---

## 分类管理模块

### 获取分类树

获取家庭的分类树结构。


**请求**

```
GET /api/category?type=1
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | int | 否 | 类型: 1-支出 2-收入，为空则获取所有 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "parentId": null,
      "name": "餐饮",
      "type": 1,
      "typeText": "支出",
      "icon": "food",
      "sortOrder": 1,
      "createdAt": "2024-01-01T10:00:00",
      "children": [
        {
          "id": 2,
          "familyId": 1,
          "parentId": 1,
          "name": "早餐",
          "type": 1,
          "typeText": "支出",
          "icon": "breakfast",
          "sortOrder": 1,
          "createdAt": "2024-01-01T10:00:00",
          "children": [],
          "hasChildren": false,
          "transactionCount": 5
        }
      ],
      "hasChildren": true,
      "transactionCount": 10
    }
  ]
}
```

---

### 获取分类列表（扁平结构）

获取家庭的分类列表（非树形）。

**请求**

```
GET /api/category/list?type=1
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | int | 否 | 类型: 1-支出 2-收入，为空则获取所有 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "parentId": null,
      "name": "餐饮",
      "type": 1,
      "typeText": "支出",
      "icon": "food",
      "sortOrder": 1,
      "createdAt": "2024-01-01T10:00:00",
      "children": null,
      "hasChildren": true,
      "transactionCount": 10
    }
  ]
}
```

---

### 获取分类详情

根据 ID 获取分类详情。

**请求**

```
GET /api/category/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 分类ID |


**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "familyId": 1,
    "parentId": null,
    "name": "餐饮",
    "type": 1,
    "typeText": "支出",
    "icon": "food",
    "sortOrder": 1,
    "createdAt": "2024-01-01T10:00:00",
    "children": null,
    "hasChildren": true,
    "transactionCount": 10
  }
}
```

---

### 创建分类

创建新的收支分类（仅管理员可操作）。

**请求**

```
POST /api/category
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "水果",
  "parentId": 1,
  "type": 1,
  "icon": "fruit",
  "sortOrder": 2
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 分类名称（最长50字符） |
| parentId | long | 否 | 父分类ID，为空表示根分类 |
| type | int | 是 | 类型: 1-支出 2-收入 |
| icon | string | 否 | 图标名称 |
| sortOrder | int | 否 | 排序值 |

**响应**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "familyId": 1,
    "parentId": 1,
    "name": "水果",
    "type": 1,
    "typeText": "支出",
    "icon": "fruit",
    "sortOrder": 2,
    "createdAt": "2024-01-01T10:00:00",
    "children": null,
    "hasChildren": false,
    "transactionCount": 0
  }
}
```

---

### 更新分类

更新分类信息（仅管理员可操作）。

**请求**

```
PUT /api/category/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 分类ID |

```json
{
  "name": "新鲜水果",
  "type": 1,
  "icon": "fresh-fruit",
  "sortOrder": 3
}
```


**响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 3,
    "familyId": 1,
    "parentId": 1,
    "name": "新鲜水果",
    "type": 1,
    "typeText": "支出",
    "icon": "fresh-fruit",
    "sortOrder": 3,
    "createdAt": "2024-01-01T10:00:00",
    "children": null,
    "hasChildren": false,
    "transactionCount": 0
  }
}
```

---

### 删除分类

删除分类（仅管理员可操作）。

**请求**

```
DELETE /api/category/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 分类ID |

**响应**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**说明**
- 有关联交易的分类不能删除

---

### 检查分类是否可删除

检查分类是否可以被删除。

**请求**

```
GET /api/category/{id}/can-delete
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 分类ID |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 账本管理模块

### 获取账本列表

获取家庭的所有账本。

**请求**

```
GET /api/account-book
Authorization: Bearer <token>
```


**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "name": "日常开支",
      "isDefault": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "transactionCount": 50
    },
    {
      "id": 2,
      "familyId": 1,
      "name": "旅游基金",
      "isDefault": false,
      "createdAt": "2024-01-02T10:00:00",
      "updatedAt": "2024-01-02T10:00:00",
      "transactionCount": 10
    }
  ]
}
```

---

### 获取账本详情

根据 ID 获取账本详情。

**请求**

```
GET /api/account-book/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 账本ID |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "familyId": 1,
    "name": "日常开支",
    "isDefault": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "transactionCount": 50
  }
}
```

---

### 获取默认账本

获取家庭的默认账本。

**请求**

```
GET /api/account-book/default
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "familyId": 1,
    "name": "日常开支",
    "isDefault": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "transactionCount": 50
  }
}
```

---

### 创建账本

创建新的账本（仅管理员可操作）。

**请求**

```
POST /api/account-book
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "name": "装修基金"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 账本名称（最长50字符） |


**响应**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "familyId": 1,
    "name": "装修基金",
    "isDefault": false,
    "createdAt": "2024-01-03T10:00:00",
    "updatedAt": "2024-01-03T10:00:00",
    "transactionCount": 0
  }
}
```

---

### 更新账本

更新账本名称（仅管理员可操作）。

**请求**

```
PUT /api/account-book/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 账本ID |

```json
{
  "name": "家庭装修"
}
```

**响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 3,
    "familyId": 1,
    "name": "家庭装修",
    "isDefault": false,
    "createdAt": "2024-01-03T10:00:00",
    "updatedAt": "2024-01-03T11:00:00",
    "transactionCount": 0
  }
}
```

---

### 删除账本

删除账本（仅管理员可操作）。

**请求**

```
DELETE /api/account-book/{id}?force=false
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 账本ID（路径参数） |
| force | boolean | 否 | 是否强制删除（有交易时），默认 false |

**响应**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 设为默认账本

将指定账本设为默认账本（仅管理员可操作）。

**请求**

```
PUT /api/account-book/{id}/default
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 账本ID |

**响应**

```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

---


### 检查账本是否可删除

检查账本是否可以被删除。

**请求**

```
GET /api/account-book/{id}/can-delete
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 账本ID |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 记账模块

### 获取交易记录列表

分页获取交易记录。

**请求**

```
GET /api/transaction?accountBookId=1&startDate=2024-01-01&endDate=2024-01-31&type=1&page=1&size=20
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| startDate | date | 否 | 开始日期（格式: yyyy-MM-dd） |
| endDate | date | 否 | 结束日期（格式: yyyy-MM-dd） |
| type | int | 否 | 类型: 1-支出 2-收入 |
| page | int | 否 | 页码（从1开始），默认1 |
| size | int | 否 | 每页数量，默认20 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "size": 20,
    "total": 100,
    "totalPages": 5,
    "list": [
      {
        "id": 1,
        "accountBookId": 1,
        "accountBookName": "日常开支",
        "categoryId": 1,
        "categoryName": "餐饮",
        "categoryIcon": "food",
        "userId": 1,
        "userNickname": "张三",
        "type": 1,
        "typeText": "支出",
        "amount": 35.50,
        "note": "午餐",
        "transactionDate": "2024-01-15",
        "createdAt": "2024-01-15T12:30:00",
        "updatedAt": "2024-01-15T12:30:00"
      }
    ],
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---


### 获取交易记录详情

根据 ID 获取交易记录详情。

**请求**

```
GET /api/transaction/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 交易ID |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "accountBookId": 1,
    "accountBookName": "日常开支",
    "categoryId": 1,
    "categoryName": "餐饮",
    "categoryIcon": "food",
    "userId": 1,
    "userNickname": "张三",
    "type": 1,
    "typeText": "支出",
    "amount": 35.50,
    "note": "午餐",
    "transactionDate": "2024-01-15",
    "createdAt": "2024-01-15T12:30:00",
    "updatedAt": "2024-01-15T12:30:00"
  }
}
```

---

### 创建交易记录

创建新的收支记录。

**请求**

```
POST /api/transaction
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "accountBookId": 1,
  "categoryId": 1,
  "type": 1,
  "amount": 35.50,
  "note": "午餐",
  "transactionDate": "2024-01-15"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| categoryId | long | 是 | 分类ID |
| type | int | 是 | 类型: 1-支出 2-收入 |
| amount | decimal | 是 | 金额（必须大于0） |
| note | string | 否 | 备注（最长200字符） |
| transactionDate | date | 是 | 交易日期（格式: yyyy-MM-dd） |

**响应**

```json
{
  "code": 200,
  "message": "记账成功",
  "data": {
    "id": 1,
    "accountBookId": 1,
    "accountBookName": "日常开支",
    "categoryId": 1,
    "categoryName": "餐饮",
    "categoryIcon": "food",
    "userId": 1,
    "userNickname": "张三",
    "type": 1,
    "typeText": "支出",
    "amount": 35.50,
    "note": "午餐",
    "transactionDate": "2024-01-15",
    "createdAt": "2024-01-15T12:30:00",
    "updatedAt": "2024-01-15T12:30:00"
  }
}
```

---


### 更新交易记录

更新已有的交易记录。

**请求**

```
PUT /api/transaction/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 交易ID |

```json
{
  "categoryId": 2,
  "type": 1,
  "amount": 40.00,
  "note": "午餐加饮料",
  "transactionDate": "2024-01-15"
}
```

**响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "accountBookId": 1,
    "accountBookName": "日常开支",
    "categoryId": 2,
    "categoryName": "饮品",
    "categoryIcon": "drink",
    "userId": 1,
    "userNickname": "张三",
    "type": 1,
    "typeText": "支出",
    "amount": 40.00,
    "note": "午餐加饮料",
    "transactionDate": "2024-01-15",
    "createdAt": "2024-01-15T12:30:00",
    "updatedAt": "2024-01-15T13:00:00"
  }
}
```

---

### 删除交易记录

删除交易记录。

**请求**

```
DELETE /api/transaction/{id}
Authorization: Bearer <token>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 交易ID |

**响应**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 统计模块

### 获取日统计

获取指定日期的收支统计。

**请求**

```
GET /api/statistics/daily?accountBookId=1&date=2024-01-15
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| date | date | 否 | 日期（格式: yyyy-MM-dd），默认今天 |


**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "periodType": "daily",
    "startDate": "2024-01-15",
    "endDate": "2024-01-15",
    "totalIncome": 0.00,
    "totalExpense": 150.50,
    "balance": -150.50,
    "categoryStats": [
      {
        "categoryId": 1,
        "categoryName": "餐饮",
        "categoryIcon": "food",
        "amount": 100.50,
        "percentage": 66.78,
        "count": 3,
        "type": 1
      },
      {
        "categoryId": 2,
        "categoryName": "交通",
        "categoryIcon": "transport",
        "amount": 50.00,
        "percentage": 33.22,
        "count": 2,
        "type": 1
      }
    ]
  }
}
```

---

### 获取周统计

获取指定周的收支统计。

**请求**

```
GET /api/statistics/weekly?accountBookId=1&date=2024-01-15
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| date | date | 否 | 周内任意日期（格式: yyyy-MM-dd），默认今天 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "periodType": "weekly",
    "startDate": "2024-01-15",
    "endDate": "2024-01-21",
    "totalIncome": 5000.00,
    "totalExpense": 1200.50,
    "balance": 3799.50,
    "categoryStats": [...]
  }
}
```

---

### 获取月统计

获取指定月份的收支统计。

**请求**

```
GET /api/statistics/monthly?accountBookId=1&year=2024&month=1
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| year | int | 否 | 年份，默认当前年 |
| month | int | 否 | 月份（1-12），默认当前月 |


**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "periodType": "monthly",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "totalIncome": 20000.00,
    "totalExpense": 8500.50,
    "balance": 11499.50,
    "categoryStats": [...]
  }
}
```

---

### 获取年统计

获取指定年份的收支统计。

**请求**

```
GET /api/statistics/yearly?accountBookId=1&year=2024
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| year | int | 否 | 年份，默认当前年 |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "periodType": "yearly",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "totalIncome": 240000.00,
    "totalExpense": 180000.50,
    "balance": 59999.50,
    "categoryStats": [...]
  }
}
```

---

### 获取分类统计

获取指定时间范围内的分类统计。

**请求**

```
GET /api/statistics/category?accountBookId=1&type=1&startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <token>
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountBookId | long | 否 | 账本ID，为空则使用默认账本 |
| type | int | 是 | 类型: 1-支出 2-收入 |
| startDate | date | 是 | 开始日期（格式: yyyy-MM-dd） |
| endDate | date | 是 | 结束日期（格式: yyyy-MM-dd） |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "餐饮",
      "categoryIcon": "food",
      "amount": 3500.50,
      "percentage": 41.18,
      "count": 45,
      "type": 1
    },
    {
      "categoryId": 2,
      "categoryName": "交通",
      "categoryIcon": "transport",
      "amount": 1200.00,
      "percentage": 14.12,
      "count": 30,
      "type": 1
    }
  ]
}
```

---


## 用户设置模块

### 修改密码

修改当前用户的密码。

**请求**

```
PUT /api/user/password
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 旧密码 |
| newPassword | string | 是 | 新密码（6-20位） |

**响应**

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

**说明**
- 密码修改成功后，所有现有的 Token 将失效，需要重新登录

---

### 更新个人信息

更新当前用户的个人信息。

**请求**

```
PUT /api/user/profile
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "nickname": "小明",
  "avatar": "https://example.com/new-avatar.jpg"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 否 | 昵称（最长50字符） |
| avatar | string | 否 | 头像URL（最长255字符） |

**响应**

```json
{
  "code": 200,
  "message": "个人信息更新成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "小明",
    "avatar": "https://example.com/new-avatar.jpg",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

---

## 数据类型说明

### 通用类型

| 类型 | 说明 | 示例 |
|------|------|------|
| long | 64位整数 | 1, 100, 999999 |
| int | 32位整数 | 1, 100 |
| decimal | 精确小数 | 35.50, 1000.00 |
| string | 字符串 | "张三" |
| boolean | 布尔值 | true, false |
| date | 日期 | "2024-01-15" |
| datetime | 日期时间 | "2024-01-15T12:30:00" |

### 枚举值

**交易类型 (type)**
| 值 | 说明 |
|----|------|
| 1 | 支出 |
| 2 | 收入 |

**邀请状态 (status)**
| 值 | 说明 |
|----|------|
| 0 | 待处理 |
| 1 | 已接受 |
| 2 | 已拒绝 |
| 3 | 已过期 |

---

## 安全说明

1. 所有需要认证的接口必须在请求头中携带 Token：
   ```
   Authorization: Bearer <token>
   ```

2. Token 过期后需要重新登录获取新 Token

3. 登录失败 5 次后账户将被锁定 30 分钟

4. 单个 IP 每分钟请求超过 100 次将被限制

5. 密码修改后所有现有 Token 将失效
