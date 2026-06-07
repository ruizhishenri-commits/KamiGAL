# KamiGAL - iOS版

KamiGAL是一款Galgame管理工具，支持游戏库管理、VNDB搜索、收藏筛选等功能。

## 功能

- 📚 游戏库管理（网格视图、搜索、分类筛选）
- ⭐ 收藏系统
- 🎮 模拟器管理（Winlator、ExaGear、KRKR等）
- 🔍 VNDB搜索集成（搜索并导入游戏信息）
- 📸 识图识别（拍照识别Galgame封面）
- 🏷️ 标签系统
- ⚙️ 游戏状态管理（未开始/正在玩/已通关/已弃坑）

## 开发环境

- Xcode 15.0+
- Swift 5.9+
- iOS 16.0+

## 构建

1. 克隆仓库
2. 用Xcode打开 `ios/` 目录
3. 选择目标设备或模拟器
4. Cmd+R 运行

## 项目结构

```
ios/
└── KamiGAL/
    ├── App/
    │   └── KamiGALApp.swift      # 应用入口
    ├── Models/
    │   └── Game.swift             # 数据模型
    ├── Views/
    │   ├── ContentView.swift      # 主界面 + 工具 + 设置
    │   ├── LibraryView.swift      # 游戏库（列表/筛选）
    │   └── GameDetailView.swift   # 游戏详情 + 搜索
    ├── Services/
    │   ├── GameStore.swift        # 数据存储（SQLite）
    │   └── VNDBService.swift      # VNDB API服务
    └── Resources/
        └── Info.plist             # 应用配置
```

## 版本

v1.0.5 - iOS版初始版本