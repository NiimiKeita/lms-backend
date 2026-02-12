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

---

## Sprint 5 設計判断 (2026-02-10)

### ADR-016: 課題管理API の権限設計
- **決定**: 課題のCRUDはADMIN/INSTRUCTORのみ、提出は認証済みユーザー、レビューはADMIN/INSTRUCTOR
- **理由**: 3ロール体制を活用し、INSTRUCTORにも課題管理権限を付与

### ADR-017: 課題提出方式 (GitHub URL)
- **決定**: GitHub URL を提出物として受け付ける方式
- **理由**: ファイルアップロード管理の複雑さを回避、GitHubで成果物管理するワークフローに適合

### ADR-018: パスワードリセットメール実装
- **決定**: Spring Boot Starter Mail + SimpleMailMessage で実装
- **理由**: テキストメールで十分、メール送信失敗はログのみ (email enumeration対策)

### ADR-019: Docker Compose デプロイ構成
- **決定**: MySQL + Backend + Frontend の3サービス構成 (multi-stage build)
- **理由**: 1コマンドで起動可能、application-docker.yml でDocker専用設定分離

### ADR-020: Next.js standalone出力
- **決定**: `output: "standalone"` でビルド
- **理由**: Docker デプロイ時に node_modules 不要、イメージサイズ削減

---

## Sprint 6 設計判断 (2026-02-11)

### ADR-021: ダッシュボード集約パターン
- **決定**: DashboardService で複数リポジトリを集約し、受講者/講師向けに専用レスポンスを返す
- **理由**: フロントエンドで複数 API を叩く代わりに、1リクエストでダッシュボードに必要な全データを取得
- **実装**: LearnerDashboardResponse (受講コース, 未提出課題, 最新フィードバック), InstructorDashboardResponse (未レビュー数, 最近の提出一覧)

### ADR-022: コース検索のバックエンド実装
- **決定**: CourseService に status/sort パラメータを追加し、Repository クエリで絞り込み
- **理由**: DB側でフィルタリングすることでパフォーマンスを確保、ページネーションとの整合性
- **実装**: status (all/published/draft), sort (newest/oldest/title) → applySorting() で Pageable を動的生成

### ADR-023: フロントエンドテスト基盤の選定
- **決定**: Vitest + @testing-library/react + jsdom
- **理由**: Next.js との親和性、Vite ベースの高速テスト実行、Jest 互換 API
- **注意**: Input コンポーネントが htmlFor を使わないため getByLabelText 不可 → getByPlaceholderText で代替

### ADR-024: OpenAPI ドキュメントの導入
- **決定**: springdoc-openapi-starter-webmvc-ui:2.8.6 で Swagger UI を自動生成
- **理由**: コード内のアノテーションから自動でAPI仕様書を生成、メンテナンスコスト最小
- **実装**: SecurityConfig で `/swagger-ui/**`, `/v3/api-docs/**` を permitAll に追加

### ADR-025: サイドバーの roles ベースフィルタリング
- **決定**: NavItem に `roles?: string[]` フィールドを追加し、`adminOnly` と共存
- **理由**: INSTRUCTOR にも提出管理を許可するため、単純な adminOnly では不十分
- **実装**: roles 配列にユーザーのロールが含まれるかチェック → adminOnly は後方互換として残す

---

## Sprint 7

### ADR-026: GitHub Actions CI/CD パイプライン
- **決定**: 両リポジトリに `.github/workflows/ci.yml` を作成
- **理由**: PR・push 時の自動ビルド/テスト検証、品質ゲートの導入
- **実装**: Backend は Gradle build + test (H2テストプロファイル)、Frontend は npm ci + test + build

### ADR-027: Playwright E2E テスト導入
- **決定**: Playwright (Chromium) による E2E テスト基盤
- **理由**: クリティカルパス（認証、コース閲覧）の UI 回帰テスト
- **実装**: `e2e/` ディレクトリに auth/courses のテストケース配置

### ADR-028: 通知システム
- **決定**: Notification エンティティ + REST API + フロントエンドドロップダウン
- **理由**: 課題フィードバック等のイベント通知をリアルタイム的に提供
- **実装**: Flyway V8 で notifications テーブル、30秒ポーリングで未読数更新、フィードバック時に自動通知生成

### ADR-029: ファイルアップロード（ローカルストレージ）
- **決定**: MultipartFile + ローカル uploads/ ディレクトリ保存
- **理由**: MVP 段階で S3 等の外部依存を避け、シンプルに実装
- **実装**: Flyway V9 で thumbnail_url カラム追加、FileStorageService で JPEG/PNG/GIF/WebP のみ 5MB 制限

### ADR-030: コースカテゴリ (多対多リレーション)
- **決定**: Category エンティティ + course_categories 中間テーブル
- **理由**: コースの分類・フィルタリング機能の提供
- **実装**: Flyway V10 で categories, course_categories テーブル、フロントエンドにカテゴリフィルタ UI

### ADR-031: Rate Limiting (手動実装)
- **決定**: OncePerRequestFilter ベースの IP 単位レートリミッター (60 req/min)
- **理由**: DDoS 対策の基本レイヤー、外部ライブラリ不要
- **実装**: ConcurrentHashMap で IP ごとの時間窓ベースカウント、テストプロファイルでは無効化
- **修正 (2026-02-12)**: CORS preflight (OPTIONS) リクエストをカウント対象外に変更 → 実質リミットが 30→60 API calls/min に正常化

---

## バグ修正 ADR (2026-02-12)

### ADR-032: CORS Preflight のレートリミット除外
- **問題**: RateLimitFilter が OPTIONS リクエストもカウントし、SPA のクロスオリジン通信で実質リミットが半減
- **決定**: `OPTIONS` メソッドをレートリミットカウントから除外
- **理由**: OPTIONS は CORS の仕組みでブラウザが自動送信するもので、ユーザーの実アクションではない
- **影響**: ダッシュボードの複数回ロード + ログインフローで 429 エラーが発生していた問題を解消

### ADR-033: 401 インターセプターのソフトリダイレクト
- **問題**: `window.location.href = "/login"` によるハードリダイレクトが React の状態管理と競合する可能性
- **決定**: カスタムイベント `auth:force-logout` を dispatch し、AuthContext がリスナーで `setUser(null)` → React Router による遷移
- **理由**: ハードリダイレクトはページ全体のリロードを伴い、並行リクエストとの競合リスクがある
- **実装**: `api.ts` で `window.dispatchEvent(new Event("auth:force-logout"))`、`AuthContext.tsx` で `addEventListener`

### ADR-034: @BatchSize による N+1 クエリ対策
- **問題**: `open-in-view: false` 環境で `CourseResponse.from()` が lazy-loaded な `categories` と `lessons` にアクセスし N+1 クエリ発生
- **決定**: Course エンティティの `lessons`, `categories` コレクションに `@BatchSize(size = 20)` を付与
- **理由**: JOIN FETCH はページネーションと相性が悪い（Hibernate がメモリ内ページネーションに切り替える）。@BatchSize なら既存クエリを変更せず、20 件ずつバッチロードで N+1 を軽減
- **実装**: `Course.java` の `@OneToMany lessons` と `@ManyToMany categories` に `@BatchSize(size = 20)` 追加

---

## Sprint 8

### ADR-035: コース完了証明書 (OpenPDF)
- **決定**: OpenPDF (librepdf/openpdf 2.0.3) でPDF生成、証明書番号はUUID
- **理由**: OpenPDF は iText のフォーク（LGPL）で商用利用可、軽量
- **実装**: コース全レッスン完了時に ProgressService から CertificateService.issueCertificate() を自動呼び出し

### ADR-036: コースレビュー/評価
- **決定**: 1ユーザー1コース1レビュー制約 (UNIQUE(user_id, course_id))
- **理由**: 重複レビュー防止、平均評価の信頼性確保
- **実装**: CourseResponse に averageRating, reviewCount を追加、一覧/詳細で表示

### ADR-037: 分析ダッシュボード (recharts)
- **決定**: recharts ライブラリで棒グラフ・折れ線・円グラフ、CSV エクスポート
- **理由**: React 19 互換、宣言的API、SSR不要（client component）
- **実装**: AnalyticsController (ADMIN限定) + AnalyticsService でデータ集約

### ADR-038: ダークモード実装方式
- **決定**: CSS カスタムプロパティ + Tailwind `@custom-variant dark` + localStorage + OS設定フォールバック
- **理由**: Tailwind 4 のカスタムバリアント機能で .dark クラスベースの切替を実現
- **実装**: ThemeToggle コンポーネント、document.documentElement.classList で .dark を切替

### ADR-039: モバイルレスポンシブ
- **決定**: md:breakpoint (768px) でサイドバー折りたたみ、ハンバーガーメニューで表示
- **理由**: Tailwind のレスポンシブプレフィックスで最小限の実装
- **実装**: Sidebar の mobile prop + fixed overlay パターン

### ADR-040: 監査ログ (Spring AOP)
- **決定**: Spring AOP @Aspect で管理者アクション (CRUD) を自動記録
- **理由**: ビジネスロジックに影響を与えずに横断的関心事として実装
- **実装**: AuditAspect (AdminUserService, CourseService, CategoryService の CUD メソッドをポイントカット)、@Profile("!test") でテスト時除外
