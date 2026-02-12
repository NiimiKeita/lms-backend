# SkillBridge LMS - Backend

学習管理システム（LMS）のバックエンド API サーバー。

## 技術スタック

- Java 21
- Spring Boot 3.5.0
- Spring Security + JWT (jjwt 0.13.0)
- MySQL 9.3 + Flyway (V1-V7)
- Gradle Kotlin DSL
- Spring Boot Starter Mail

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

### Docker で起動

```bash
# リポジトリルートで実行
cp .env.example .env
# .env を編集 (DB_PASSWORD, JWT_SECRET 等)
docker-compose up -d
```

MySQL + Backend + Frontend が一括起動します。

## プロジェクト構成

```
src/main/java/com/skillbridge/lms/
├── config/          # SecurityConfig, CorsConfig
├── controller/      # REST Controllers (12個)
├── dto/
│   ├── request/     # リクエスト DTO
│   └── response/    # レスポンス DTO
├── entity/          # JPA エンティティ (9個)
├── enums/           # UserRole, EnrollmentStatus, SubmissionStatus
├── exception/       # カスタム例外 + GlobalExceptionHandler
├── repository/      # Spring Data JPA リポジトリ (10個)
├── security/        # JWT, UserDetails
└── service/         # ビジネスロジック (9個)

src/main/resources/
├── application.yml
├── application-docker.yml   # Docker 専用設定
└── db/migration/            # Flyway マイグレーション (V1-V7)

content/courses/             # Markdown レッスンコンテンツ
docs/
├── progress.md              # 開発進捗
└── decisions.md             # アーキテクチャ決定記録 (ADR)
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
| 課題管理 | 5 (CRUD) | ADMIN/INSTRUCTOR |
| 課題提出 | 6 (提出/一覧/レビュー/フィードバック) | 必要/ADMIN |
| ヘルスチェック | 1 | 不要 |

**合計: 41 endpoints** (Sprint 1-5)

## テスト

| テストクラス | テスト数 | 種別 |
|-------------|---------|------|
| AuthServiceTest | 8 | Unit |
| CourseServiceTest | 12 | Unit |
| LessonServiceTest | 10 | Unit |
| EnrollmentServiceTest | 10 | Unit |
| ProgressServiceTest | 8 | Unit |
| AdminUserServiceTest | 14 | Unit |
| TaskServiceTest | 12 | Unit |
| TaskSubmissionServiceTest | 15 | Unit |
| AuthControllerIntegrationTest | 8 | Integration |
| CourseControllerIntegrationTest | 8 | Integration |
| SecurityConfigTest | 3 | Integration |
| EnrollmentControllerIntegrationTest | 6 | Integration |
| ProgressControllerIntegrationTest | 6 | Integration |
| AdminUserControllerIntegrationTest | 11 | Integration |
| AdminProgressControllerIntegrationTest | 6 | Integration |
| TaskControllerIntegrationTest | 14 | Integration |

**合計: 約151テスト**

詳細は [docs/progress.md](docs/progress.md) を参照。
