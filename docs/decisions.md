# SkillBridge LMS - アーキテクチャ決定記録 (ADR)

## Sprint 1

### ADR-001: JWT認証方式の採用
- **決定**: Spring Security + JWT (jjwt 0.13.0) によるステートレス認証
- **理由**: SPAフロントエンドとの相性、スケーラビリティ
- **実装**: AccessToken (15分) + RefreshToken (7日) の二重トークン方式

### ADR-002: Flyway によるDBマイグレーション
- **決定**: Flyway でバージョン管理されたSQLマイグレーション
- **理由**: 再現可能なDB構成、チーム開発でのスキーマ管理
- **注意**: MySQL 9.3 は公式非サポート（警告出るが動作OK）

### ADR-003: Gradle Kotlin DSL の採用
- **決定**: build.gradle.kts (Kotlin DSL)
- **理由**: 型安全性、IDE補完、Spring Boot 3.5.0 との親和性

---

## Sprint 2

### ADR-004: RBAC (ロールベースアクセス制御)
- **決定**: `@PreAuthorize("hasRole('ADMIN')")` によるメソッドレベル認可
- **理由**: Spring Security の標準機能で宣言的に制御可能
- **実装**: UserRole enum (LEARNER, ADMIN) → Sprint 4 で INSTRUCTOR 追加

### ADR-005: ページネーション方式
- **決定**: `PageResponse<T>` 汎用レスポンスクラス + Spring Data の `Pageable`
- **理由**: 統一的なページネーションIF、フロントエンドとの整合性
- **実装**: content, page, size, totalElements, totalPages, first, last

### ADR-006: コース/レッスンの公開状態管理
- **決定**: `published` boolean フラグによる公開/非公開制御
- **理由**: シンプルで直感的。ADMIN は全件閲覧可、LEARNER は published のみ

---

## Sprint 3

### ADR-007: 受講登録 (Enrollment) の状態管理
- **決定**: EnrollmentStatus enum (ACTIVE, COMPLETED, CANCELLED)
- **理由**: 受講のライフサイクルを明示的に管理
- **実装**: users-courses 間の中間テーブル + unique制約

### ADR-008: レッスン進捗の管理方式
- **決定**: lesson_progress テーブル (user_id, lesson_id, completed)
- **理由**: レッスン単位の完了/未完了をシンプルに管理
- **実装**: コース進捗 = 完了レッスン数 / 総レッスン数

### ADR-009: フロントエンド状態管理
- **決定**: React Context (AuthContext) + ローカルstate (useState/useCallback)
- **理由**: 小規模〜中規模アプリでは Redux/Zustand は過剰。Context で十分

---

## Sprint 4

### ADR-010: INSTRUCTOR ロールの追加
- **決定**: UserRole enum に INSTRUCTOR を追加、Flyway V7 で DB ENUM 変更
- **理由**: 管理者・講師・受講者の3ロール体制で運用
- **影響**: `ALTER TABLE users MODIFY COLUMN role ENUM('LEARNER','INSTRUCTOR','ADMIN')`

### ADR-011: ユーザー管理における自己無効化防止
- **決定**: toggle-enabled で自分自身のID一致を検知し BadRequestException をスロー
- **理由**: 管理者が自身を無効化するとシステムにアクセスできなくなるため
- **実装**: Controller で `@AuthenticationPrincipal` から現在ユーザーID を取得、Service で比較

### ADR-012: Markdownコンテンツのファイルシステム管理
- **決定**: `content/courses/{courseId}/lessons/{lessonId}.md` のファイルパス構造
- **理由**: DB保存よりもファイルでの管理が直感的、Git管理可能、大量コンテンツに適する
- **実装**: ContentService が `content.base-path` 設定値を参照してファイル読込
- **フォールバック**: ファイル未存在時はデフォルトメッセージを返却

### ADR-013: react-markdown によるレンダリング
- **決定**: react-markdown + remark-gfm + rehype-highlight
- **理由**: GFM (テーブル, チェックリスト等) + コードハイライト対応
- **実装**: MarkdownRenderer コンポーネントとして分離、highlight.js の github テーマ使用

### ADR-014: Zod v4 のenum バリデーション
- **決定**: `z.enum([...], { message: "..." })` を使用 (`errorMap` は v4 で非対応)
- **理由**: Zod v4 の破壊的変更により `errorMap` パラメータが廃止された
- **影響**: adminCreateUserSchema, adminUpdateUserSchema の role フィールド

### ADR-015: ユーザー編集時のemail変更不可
- **決定**: AdminUpdateUserRequest には email フィールドを含めない
- **理由**: email はログインIDとして使用されるため、変更時の影響範囲が大きい
- **実装**: 編集フォームでは name と role のみ変更可能
