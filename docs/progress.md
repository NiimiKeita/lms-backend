# SkillBridge LMS - 開発進捗

## プロジェクト概要

学習管理システム（LMS）。Spring Boot + Next.js のフルスタック構成。

| 項目 | 値 |
|------|-----|
| Backend | Java 21, Spring Boot 3.5.0, Gradle Kotlin DSL |
| Frontend | Next.js 16.1.6, React 19, Tailwind CSS 4 |
| DB | MySQL 9.3, Flyway (V1-V7) |
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

## Sprint 5 (計画済み・未着手) - 課題提出 + デプロイ

| タスク | 内容 | 状態 |
|--------|------|------|
| TASK-1 | 課題提出 Backend API | 未着手 |
| TASK-2 | 課題提出 Frontend | 未着手 |
| TASK-3 | Docker Compose デプロイ | 未着手 |
| TASK-4 | コンテンツ移行 (17コース・150レッスン) | 未着手 |
| TASK-5 | パスワードリセットメール送信 | 未着手 |

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

### ヘルスチェック (HealthController)
| Method | Path | 説明 | 認証 |
|--------|------|------|------|
| GET | `/api/health` | ヘルスチェック | 不要 |

**合計: 30 endpoints**

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

**合計: 17 ページ**

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

**合計: 約110テスト**
