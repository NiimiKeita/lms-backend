# SkillBridge LMS - 開発進捗

## プロジェクト概要

学習管理システム（LMS）。Spring Boot + Next.js のフルスタック構成。

| 項目 | 値 |
|------|-----|
| Backend | Java 21, Spring Boot 3.5.0, Gradle Kotlin DSL |
| Frontend | Next.js 16.1.6, React 19, Tailwind CSS 4 |
| DB | MySQL 9.3, Flyway (V1-V13) |
| Auth | Spring Security + JWT (jjwt 0.13.0) |
| GitHub | SkillBridge-LMS org |

---

## Sprint 1 (2/7-2/20) - 環境構築 + 認証

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 0 | 環境構築 (リポジトリ, CI/CD) | 完了 |
| Phase 1 | Backend scaffolding (Spring Boot) | 完了 |
| Phase 2 | Frontend scaffolding (Next.js) | 完了 |
| Phase 3 | DB migration (Flyway V1-V2) | 完了 |
| Phase 4 | 認証 Backend (JWT, Spring Security) | 完了 |
| Phase 5 | 認証 Frontend (ログイン/登録/パスワードリセット) | 完了 |
| Phase 6 | 認証統合テスト | 完了 |

**成果物:**
- ユーザー認証 (登録/ログイン/ログアウト/JWT リフレッシュ)
- Flyway V1: users テーブル, V2: auth_tokens テーブル

---

## Sprint 2 (2/21-3/6) - コース・レッスン管理

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 7 | コース管理 Backend API (6 endpoints + RBAC) | 完了 |
| Phase 8 | レッスン管理 Backend API (6 endpoints) | 完了 |
| Phase 9 | コース管理 Frontend (一覧/詳細/作成/編集 + サイドナビ) | 完了 |
| Phase 10 | テスト (46テスト) | 完了 |

**成果物:**
- コース CRUD (6 endpoints), レッスン CRUD (6 endpoints)
- RBAC: ADMIN のみコース/レッスン管理可
- Flyway V3: courses, lessons テーブル
- テスト: AuthService, CourseService, LessonService, 統合テスト

---

## Sprint 3 (2/10-2/23) - 受講・進捗管理

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 11 | Enrollment Backend API (5 endpoints) | 完了 |
| Phase 12 | 進捗管理 Backend API (4 endpoints) | 完了 |
| Phase 13 | 受講・進捗 Frontend (3新規ページ + 2改修) | 完了 |
| Phase 14 | ユーザープロファイル + テスト (30テスト追加) | 完了 |

**成果物:**
- 受講登録/解除, マイコース一覧, コース受講者一覧
- レッスン完了/未完了, コース進捗取得
- ユーザープロファイル取得/更新
- Flyway V4: lesson_progress, V5: tasks/submissions, V6: enrollments
- テスト: EnrollmentService, ProgressService, 統合テスト

---

## Sprint 4 (2/10) - ユーザー管理 + 管理者進捗 + コンテンツ表示

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 15 | ユーザー管理 Backend API (5 endpoints, ADMIN限定) | 完了 |
| Phase 16 | ユーザー管理 Frontend (一覧/追加/編集 + トグル) | 完了 |
| Phase 17 | 管理者進捗確認 Backend + Frontend (3 endpoints + 2画面) | 完了 |
| Phase 18 | Markdownコンテンツ表示 (react-markdown + ContentService) | 完了 |
| Phase 19 | テスト (31テスト追加, 全パス) | 完了 |

**成果物:**
- ユーザー CRUD (5 endpoints): 一覧/詳細/作成/更新/有効無効切替
- 管理者進捗 (3 endpoints): 進捗一覧/ユーザー別進捗/統計
- Markdownコンテンツ (1 endpoint): ファイルシステムベースのレッスンコンテンツ配信
- Flyway V7: INSTRUCTOR ロール追加
- テスト: AdminUserServiceTest (14), AdminUserControllerIntegrationTest (11), AdminProgressControllerIntegrationTest (6)

---

## Sprint 5 (2/10) - 課題提出 + デプロイ

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 20 | 課題管理 Backend API (11 endpoints) | 完了 |
| Phase 21 | 課題管理 Frontend (3新規ページ + 2改修) | 完了 |
| Phase 22 | パスワードリセットメール送信 (MailService) | 完了 |
| Phase 23 | Docker Compose デプロイ (MySQL + Backend + Frontend) | 完了 |
| Phase 24 | コンテンツ作成 + テスト (41テスト追加) | 完了 |

**成果物:**
- 課題 CRUD (5 endpoints), 提出管理 (6 endpoints): ADMIN/INSTRUCTOR権限
- パスワードリセットメール送信 (JavaMailSender)
- Docker Compose (3サービス: MySQL + Backend + Frontend)
- テスト: TaskServiceTest (12), TaskSubmissionServiceTest (15), TaskControllerIntegrationTest (14)

---

## Sprint 6 (2/11) - ダッシュボード + 検索 + テスト + ドキュメント

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 25 | INSTRUCTOR ナビゲーション (サイドバー roles ベースフィルタリング) | 完了 |
| Phase 26 | ダッシュボード強化 (DashboardController/Service + Frontend) | 完了 |
| Phase 27 | コース検索・フィルタ改善 (status/sort パラメータ + Frontend UI) | 完了 |
| Phase 28 | フロントエンドテスト基盤 (Vitest + testing-library, 42テスト) | 完了 |
| Phase 29 | OpenAPI ドキュメント (springdoc-openapi, Swagger UI) | 完了 |
| Phase 30 | ビルド確認 + commit/push | 完了 |

**成果物:**
- ダッシュボード API (2 endpoints): 受講者 + 講師ダッシュボード
- コース検索強化: status (all/published/draft), sort (newest/oldest/title) パラメータ
- INSTRUCTOR ナビゲーション: サイドバーに roles ベースのフィルタリング
- Frontend テスト基盤: Vitest + @testing-library/react (42テスト)
- OpenAPI: springdoc-openapi-starter-webmvc-ui, Swagger UI (`/swagger-ui.html`)
- 全13コントローラーに `@Tag` アノテーション追加

---

## Sprint 7 (2/11) - 品質強化 + 実用機能追加

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 31 | CI/CD パイプライン (GitHub Actions, 両リポジトリ) | 完了 |
| Phase 32 | E2E テスト (Playwright, 8テストケース) | 完了 |
| Phase 33 | 通知機能 (Notification API + Frontend ベルアイコン) | 完了 |
| Phase 34 | ファイルアップロード (MultipartFile + コースサムネイル) | 完了 |
| Phase 35 | コースカテゴリ/タグ (Category CRUD + 多対多リレーション) | 完了 |
| Phase 36 | セキュリティ強化 + ビルド確認 (Rate Limiting + テスト全パス) | 完了 |

**成果物:**
- CI/CD: GitHub Actions (Backend: Gradle build+test, Frontend: npm ci+test+build)
- E2E テスト: Playwright (認証フロー + コース閲覧フロー)
- 通知 API (4 endpoints): 一覧/未読数/既読/全既読 + フィードバック時自動通知
- ファイルアップロード (2 endpoints): アップロード + 配信、コースサムネイル対応
- カテゴリ管理 (7 endpoints): CRUD + コースへの紐付け/取得
- Rate Limiting: IP単位 60 req/min
- Flyway V8-V10: notifications, thumbnail_url, categories/course_categories

---

## Sprint 8 (2/12) - 証明書 + レビュー + 分析 + ダークモード + 監査

| Phase | 内容 | 状態 |
|-------|------|------|
| Phase 37 | コース完了証明書 (Certificate entity + PDF生成 + Frontend) | 完了 |
| Phase 38 | コースレビュー/評価 (Review entity + 5 endpoints + StarRating) | 完了 |
| Phase 39 | 分析ダッシュボード + CSVエクスポート (4 endpoints + recharts) | 完了 |
| Phase 40 | ダークモード + レスポンシブ改善 (ThemeToggle + モバイルメニュー) | 完了 |
| Phase 41 | テスト拡充 (Backend 30 + Frontend 10) | 完了 |
| Phase 42 | 監査ログ (AuditLog entity + Spring AOP + 2 endpoints + Frontend) | 完了 |

**成果物:**
- 証明書 (3 endpoints): 一覧/詳細/PDFダウンロード + コース完了時自動発行
- レビュー (5 endpoints): CRUD + 平均評価・レビュー数をコースレスポンスに統合
- 分析 (4 endpoints): 受講者推移/完了率/人気コース/CSVエクスポート (ADMIN)
- 監査ログ (2 endpoints): ログ一覧(フィルタ付き)/CSVエクスポート (ADMIN)
- ダークモード: ThemeToggle + CSS custom-variant dark + localStorage永続化
- レスポンシブ: モバイルハンバーガーメニュー (768px以下)
- Flyway V11-V13: certificates, reviews, audit_logs テーブル
- 新規依存: com.github.librepdf:openpdf:2.0.3, spring-boot-starter-aop, recharts
- テスト: NotificationService(6), CategoryService(8), FileStorage(6), RateLimit(4), Certificate(6), NotificationBell(4), StarRating(3), ThemeToggle(3)

---

## API エンドポイント一覧

### 認証 (AuthController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| POST | `/api/auth/register` | ユーザー登録 | 不要 |
| POST | `/api/auth/login` | ログイン | 不要 |
| POST | `/api/auth/refresh` | トークンリフレッシュ | 不要 |
| POST | `/api/auth/logout` | ログアウト | 必要 |

### コース管理 (CourseController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/courses` | コース一覧 | 必要 |
| GET | `/api/courses/{id}` | コース詳細 | 必要 |
| POST | `/api/courses` | コース作成 | ADMIN |
| PUT | `/api/courses/{id}` | コース更新 | ADMIN |
| DELETE | `/api/courses/{id}` | コース削除 | ADMIN |
| PATCH | `/api/courses/{id}/publish` | 公開/非公開切替 | ADMIN |

### レッスン管理 (LessonController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/courses/{courseId}/lessons` | レッスン一覧 | 必要 |
| GET | `/api/courses/{courseId}/lessons/{id}` | レッスン詳細 | 必要 |
| POST | `/api/courses/{courseId}/lessons` | レッスン作成 | ADMIN |
| PUT | `/api/courses/{courseId}/lessons/{id}` | レッスン更新 | ADMIN |
| DELETE | `/api/courses/{courseId}/lessons/{id}` | レッスン削除 | ADMIN |
| PATCH | `/api/courses/{courseId}/lessons/reorder` | 並び替え | ADMIN |

### 受講管理 (EnrollmentController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| POST | `/api/courses/{courseId}/enroll` | 受講登録 | 必要 |
| DELETE | `/api/courses/{courseId}/enroll` | 受講解除 | 必要 |
| GET | `/api/my-courses` | マイコース一覧 | 必要 |
| GET | `/api/courses/{courseId}/enrollments` | 受講者一覧 | ADMIN |
| GET | `/api/courses/{courseId}/enrollment-status` | 受講状態確認 | 必要 |

### 進捗管理 (ProgressController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| POST | `/api/courses/{courseId}/lessons/{lessonId}/complete` | レッスン完了 | 必要 |
| DELETE | `/api/courses/{courseId}/lessons/{lessonId}/complete` | 完了取消 | 必要 |
| GET | `/api/courses/{courseId}/progress` | コース進捗取得 | 必要 |
| GET | `/api/courses/{courseId}/lessons/{lessonId}/progress` | レッスン進捗取得 | 必要 |

### ユーザープロファイル (UserController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/users/profile` | プロファイル取得 | 必要 |
| PUT | `/api/users/profile` | プロファイル更新 | 必要 |

### ユーザー管理 (AdminUserController) - Sprint 4
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/admin/users` | ユーザー一覧 | ADMIN |
| GET | `/api/admin/users/{id}` | ユーザー詳細 | ADMIN |
| POST | `/api/admin/users` | ユーザー作成 | ADMIN |
| PUT | `/api/admin/users/{id}` | ユーザー更新 | ADMIN |
| PATCH | `/api/admin/users/{id}/toggle-enabled` | 有効/無効切替 | ADMIN |

### 管理者進捗 (AdminProgressController) - Sprint 4
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/admin/progress` | 全ユーザー進捗一覧 | ADMIN |
| GET | `/api/admin/users/{userId}/progress` | ユーザー別進捗 | ADMIN |
| GET | `/api/admin/stats` | 統計情報 | ADMIN |

### コンテンツ (ContentController) - Sprint 4
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/courses/{courseId}/lessons/{lessonId}/content` | レッスンコンテンツ取得 | 必要 |

### 課題管理 (TaskController) - Sprint 5
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/courses/{courseId}/tasks` | 課題一覧 | 必要 |
| GET | `/api/courses/{courseId}/tasks/{taskId}` | 課題詳細 | 必要 |
| POST | `/api/courses/{courseId}/tasks` | 課題作成 | ADMIN/INSTRUCTOR |
| PUT | `/api/courses/{courseId}/tasks/{taskId}` | 課題更新 | ADMIN/INSTRUCTOR |
| DELETE | `/api/courses/{courseId}/tasks/{taskId}` | 課題削除 | ADMIN/INSTRUCTOR |

### 課題提出 (TaskSubmissionController) - Sprint 5
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| POST | `/api/tasks/{taskId}/submissions` | 課題提出 | 必要 |
| GET | `/api/tasks/{taskId}/submissions/my` | 自分の提出一覧 | 必要 |
| GET | `/api/tasks/{taskId}/submissions` | 全提出一覧 | ADMIN/INSTRUCTOR |
| GET | `/api/tasks/submissions/{id}` | 提出詳細 | 必要 |
| PATCH | `/api/tasks/submissions/{id}/status` | ステータス変更 | ADMIN/INSTRUCTOR |
| POST | `/api/tasks/submissions/{id}/feedback` | フィードバック追加 | ADMIN/INSTRUCTOR |

### ヘルスチェック (HealthController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/health` | ヘルスチェック | 不要 |

### 認証追加 (AuthController) - Sprint 3/5
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/auth/me` | 現在のユーザー情報取得 | 必要 |
| POST | `/api/auth/forgot-password` | パスワードリセット要求 | 不要 |
| POST | `/api/auth/reset-password` | パスワードリセット | 不要 |

### ダッシュボード (DashboardController) - Sprint 6
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/dashboard/learner` | 受講者ダッシュボード | 必要 |
| GET | `/api/dashboard/instructor` | 講師ダッシュボード | INSTRUCTOR/ADMIN |

### 通知 (NotificationController) - Sprint 7
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/notifications` | 通知一覧 | 必要 |
| GET | `/api/notifications/unread-count` | 未読数取得 | 必要 |
| PATCH | `/api/notifications/{id}/read` | 既読にする | 必要 |
| POST | `/api/notifications/read-all` | 全て既読にする | 必要 |

### ファイル (FileController) - Sprint 7
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| POST | `/api/files/upload` | ファイルアップロード | ADMIN/INSTRUCTOR |
| GET | `/api/files/{filename}` | ファイル配信 | 不要 |

### カテゴリ (CategoryController) - Sprint 7
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/categories` | カテゴリ一覧 | 必要 |
| GET | `/api/categories/{id}` | カテゴリ詳細 | 必要 |
| POST | `/api/categories` | カテゴリ作成 | ADMIN |
| PUT | `/api/categories/{id}` | カテゴリ更新 | ADMIN |
| DELETE | `/api/categories/{id}` | カテゴリ削除 | ADMIN |
| GET | `/api/categories/courses/{courseId}` | コースのカテゴリ取得 | 必要 |
| PUT | `/api/categories/courses/{courseId}` | コースのカテゴリ設定 | ADMIN |

### 証明書 (CertificateController) - Sprint 8
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/certificates/my` | 自分の証明書一覧 | 必要 |
| GET | `/api/certificates/{id}` | 証明書詳細 | 必要 |
| GET | `/api/certificates/{id}/pdf` | PDFダウンロード | 必要 |

### レビュー (ReviewController) - Sprint 8
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| POST | `/api/courses/{courseId}/reviews` | レビュー投稿 | 必要 |
| GET | `/api/courses/{courseId}/reviews` | レビュー一覧 | 必要 |
| GET | `/api/courses/{courseId}/reviews/my` | 自分のレビュー | 必要 |
| PUT | `/api/courses/{courseId}/reviews/my` | レビュー更新 | 必要 |
| DELETE | `/api/courses/{courseId}/reviews/my` | レビュー削除 | 必要 |

### 分析 (AnalyticsController) - Sprint 8
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/admin/analytics/enrollments` | 受講者推移 | ADMIN |
| GET | `/api/admin/analytics/completions` | 完了率統計 | ADMIN |
| GET | `/api/admin/analytics/popular-courses` | 人気コースランキング | ADMIN |
| GET | `/api/admin/analytics/export/csv` | CSVエクスポート | ADMIN |

### 監査ログ (AuditLogController) - Sprint 8
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/admin/audit-logs` | ログ一覧 | ADMIN |
| GET | `/api/admin/audit-logs/export` | CSVエクスポート | ADMIN |

**合計: 80 endpoints**

---

## バグ修正 (Sprint 7 後)

### BUG-001: レートリミッターによるログイン失敗 (2026-02-12)

**症状**: 管理者ログイン → ログアウト → 生徒ログイン → ログアウト → 管理者ログイン時に「ログインに失敗しました」エラーが発生。

**根本原因**: `RateLimitFilter` が CORS preflight (OPTIONS) リクエストもカウントしていた。ブラウザはクロスオリジンの各APIコールで OPTIONS + 実リクエストの2つを送信するため、実質リミットが 30 API calls/min に半減。ダッシュボード1回のロードで約15 APIコール = 30 HTTPリクエストとなり、2回のログイン/ログアウトサイクルで 60 リクエストを超過して 429 Too Many Requests が返されていた。

**修正内容**:

| ファイル | 変更 |
|---------|------|
| `RateLimitFilter.java` | OPTIONS リクエストをレートリミットカウントから除外 |
| `api.ts` | 401 インターセプターの `window.location.href` → カスタムイベント `auth:force-logout` に変更 |
| `AuthContext.tsx` | `auth:force-logout` イベントリスナー追加で React 的にログアウト処理 |
| `Course.java` | `lessons`, `categories` に `@BatchSize(size = 20)` 追加 (N+1 クエリ対策) |

**調査過程**:
1. バックエンドログにエラーなし → フロントエンドかインフラ層の問題と判断
2. `open-in-view: false` 設定下での LazyInitializationException を疑うも @Transactional 内で問題なし
3. 認証フロー (JWT, AuthContext, api interceptor) を精査 → 正常
4. バックエンドログの詳細分析で、login POST の "Securing" 後に "Secured" が欠落 → フィルタチェーン途中で応答が返されたことを確認
5. CORS preflight + RateLimitFilter の組合せで 60 req/min 超過が根本原因と特定

---

## Frontend ページ一覧

### 認証
| パス | 説明 |
|------|------|
| `/login` | ログイン |
| `/register` | ユーザー登録 |
| `/forgot-password` | パスワードリセット要求 |
| `/reset-password` | パスワードリセット |

### ダッシュボード
| パス | 説明 |
|------|------|
| `/dashboard` | ダッシュボード |
| `/courses` | コース一覧 |
| `/courses/[id]` | コース詳細 |
| `/courses/[id]/learn` | 学習ページ (Markdownレンダリング) |
| `/my-courses` | マイコース |
| `/profile` | プロファイル |

### 管理者
| パス | 説明 |
|------|------|
| `/admin/courses/new` | コース新規作成 |
| `/admin/courses/[id]/edit` | コース編集 |
| `/admin/users` | ユーザー管理一覧 |
| `/admin/users/new` | ユーザー追加 |
| `/admin/users/[id]/edit` | ユーザー編集 |
| `/admin/users/[id]/progress` | ユーザー進捗詳細 |
| `/admin/progress` | 進捗管理一覧 |
| `/admin/submissions` | 提出管理一覧 |
| `/admin/submissions/[id]` | 提出レビュー詳細 |
| `/admin/analytics` | 分析ダッシュボード (ADMIN) |
| `/admin/audit-logs` | 監査ログ (ADMIN) |
| `/courses/[id]/tasks/[taskId]` | 課題提出 |
| `/my-certificates` | 証明書一覧 |

**合計: 24 ページ** (+ ルートリダイレクト + 通知ドロップダウン + ThemeToggle)

---

## DB マイグレーション

| Version | 説明 | Sprint |
|---------|------|--------|
| V1 | users テーブル | 1 |
| V2 | auth_tokens テーブル (refresh_tokens, password_reset_tokens) | 1 |
| V3 | courses, lessons テーブル | 2 |
| V4 | lesson_progress テーブル | 2 |
| V5 | tasks, submissions テーブル | 2 |
| V6 | lesson_progress 再作成 + enrollments テーブル | 3 |
| V7 | INSTRUCTOR ロール追加 | 4 |
| V8 | notifications テーブル | 7 |
| V9 | courses.thumbnail_url カラム追加 | 7 |
| V10 | categories, course_categories テーブル | 7 |
| V11 | certificates テーブル | 8 |
| V12 | reviews テーブル | 8 |
| V13 | audit_logs テーブル | 8 |

---

## テスト

| テストクラス | テスト数 | 種別 | Sprint |
|-------------|---------|------|--------|
| AuthServiceTest | 8 | Unit | 1 |
| CourseServiceTest | 12 | Unit | 2 |
| LessonServiceTest | 10 | Unit | 2 |
| AuthControllerIntegrationTest | 8 | Integration | 2 |
| CourseControllerIntegrationTest | 8 | Integration | 2 |
| SecurityConfigTest | 3 | Integration | 2 |
| EnrollmentServiceTest | 10 | Unit | 3 |
| ProgressServiceTest | 8 | Unit | 3 |
| EnrollmentControllerIntegrationTest | 6 | Integration | 3 |
| ProgressControllerIntegrationTest | 6 | Integration | 3 |
| AdminUserServiceTest | 14 | Unit | 4 |
| AdminUserControllerIntegrationTest | 11 | Integration | 4 |
| AdminProgressControllerIntegrationTest | 6 | Integration | 4 |
| TaskServiceTest | 12 | Unit | 5 |
| TaskSubmissionServiceTest | 15 | Unit | 5 |
| TaskControllerIntegrationTest | 14 | Integration | 5 |
| NotificationServiceTest | 6 | Unit | 8 |
| CategoryServiceTest | 8 | Unit | 8 |
| FileStorageServiceTest | 6 | Unit | 8 |
| RateLimitFilterTest | 4 | Unit | 8 |
| CertificateServiceTest | 6 | Unit | 8 |

**Backend 合計: 175テスト**

### Frontend テスト (Vitest + testing-library) - Sprint 6

| テストファイル | テスト数 | 種別 |
|---------------|---------|------|
| Button.test.tsx | 8 | Component |
| Input.test.tsx | 6 | Component |
| Card.test.tsx | 3 | Component |
| TaskForm.test.tsx | 5 | Component |
| LessonForm.test.tsx | 5 | Component |
| TaskList.test.tsx | 8 | Component |
| LessonList.test.tsx | 7 | Component |
| NotificationBell.test.tsx | 4 | Component | 8 |
| StarRating.test.tsx | 3 | Component | 8 |
| ThemeToggle.test.tsx | 3 | Component | 8 |

**Frontend 合計: 52テスト**

**プロジェクト全体: 227テスト (Backend 175 + Frontend 52)**

### E2E テスト (Playwright) - Sprint 7

| テストファイル | テスト数 | 種別 |
|---------------|---------|------|
| auth.spec.ts | 5 | E2E |
| courses.spec.ts | 3 | E2E |

**E2E 合計: 8テスト**

**全テスト合計: 235テスト (Backend 175 + Frontend Unit 52 + E2E 8)**
