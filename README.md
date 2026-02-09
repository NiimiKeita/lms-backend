# SkillBridge LMS - Backend

学習管理システム（LMS）のバックエンド API サーバー。

## 技術スタック

- Java 21
- Spring Boot 3.5.0
- Spring Security + JWT (jjwt 0.13.0)
- MySQL 9.3 + Flyway
- Gradle Kotlin DSL

## セットアップ

### 前提条件
- Java 21+
- MySQL 9.3
- データベース `skillbridge_lms` を作成済み

### 起動

```bash
./gradlew bootRun
```

サーバーは `http://localhost:8080` で起動します。

### テスト

```bash
./gradlew test
```

### ビルド

```bash
./gradlew build
```

## プロジェクト構成

```
src/main/java/com/skillbridge/lms/
├── config/          # SecurityConfig, CorsConfig
├── controller/      # REST Controllers (10個)
├── dto/
│   ├── request/     # リクエスト DTO
│   └── response/    # レスポンス DTO
├── entity/          # JPA エンティティ
├── enums/           # UserRole, EnrollmentStatus
├── exception/       # カスタム例外 + GlobalExceptionHandler
├── repository/      # Spring Data JPA リポジトリ
├── security/        # JWT, UserDetails
└── service/         # ビジネスロジック

src/main/resources/
├── application.yml
└── db/migration/    # Flyway マイグレーション (V1-V7)

docs/
├── progress.md      # 開発進捗
└── decisions.md     # アーキテクチャ決定記録
```

## API 概要

| カテゴリ | Endpoints | 認証 |
|---------|-----------|------|
| 認証 | 4 (register/login/refresh/logout) | 一部不要 |
| コース管理 | 6 (CRUD + 公開切替) | ADMIN |
| レッスン管理 | 6 (CRUD + 並び替え) | ADMIN |
| 受講管理 | 5 (登録/解除/一覧/状態) | 必要 |
| 進捗管理 | 4 (完了/取消/進捗取得) | 必要 |
| プロファイル | 2 (取得/更新) | 必要 |
| ユーザー管理 | 5 (CRUD + 有効切替) | ADMIN |
| 管理者進捗 | 3 (一覧/詳細/統計) | ADMIN |
| コンテンツ | 1 (Markdown取得) | 必要 |

**合計: 30 endpoints**

詳細は [docs/progress.md](docs/progress.md) を参照。
