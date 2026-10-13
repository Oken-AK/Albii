@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion

echo ============================================================
echo  Kiro-style Copilot Setup
echo  .github/ と docs/adr/ を展開するで〜！
echo ============================================================
echo.

:: ============================================================
:: プロジェクト情報を入力させる
:: ============================================================
set /p PROJECT_NAME="プロジェクト名を入力してな（例: MyApp）: "
set /p TECH_STACK="技術スタックを入力してな（例: Unity C# / React TypeScript / Python FastAPI）: "
set /p AGENT_TONE="エージェントの口調を入力してな（例: 関西弁ギャル / 標準的なプロフェッショナル / 丁寧語）: "

echo.
echo プロジェクト名  : %PROJECT_NAME%
echo 技術スタック    : %TECH_STACK%
echo 口調            : %AGENT_TONE%
echo.

:: 上書き確認
if exist ".github\copilot-instructions.md" (
    set /p OVERWRITE=".github\copilot-instructions.md が既に存在するで。上書きする？ [y/N]: "
    if /i not "!OVERWRITE!"=="y" (
        echo キャンセルしたで〜。
        pause
        exit /b 0
    )
)

:: ============================================================
:: ディレクトリ作成
:: ============================================================
echo ディレクトリを作成中...
if not exist ".github\instructions" mkdir ".github\instructions"
if not exist "docs\adr" mkdir "docs\adr"
echo   .github\instructions\ を作成したで！
echo   docs\adr\ を作成したで！
echo.

:: ============================================================
:: .github/copilot-instructions.md
:: ============================================================
echo .github\copilot-instructions.md を生成中...
(
echo # GitHub Copilot エージェント指示書
echo # %PROJECT_NAME% — AI仕様駆動開発ガイド
echo.
echo ^> このファイルは GitHub Copilot Agent（チャット・エージェントモード）が自動読込する指示書。
echo ^> 全ての会話・実装作業にこのルールが適用される。
echo.
echo ---
echo.
echo ## 🎯 基本姿勢：仕様駆動開発オーケストレーター
echo.
echo あなたは **オーケストレーター** として振る舞うこと。実装の前に必ず計画し、計画に沿って実装し、結果を検証してから完了を宣言する。
echo.
echo ### コアサイクル（絶対に守ること）
echo.
echo ```
echo 1. READ   → 既存コードと計画書を読んで現状を把握
echo 2. PLAN   → plan/ に計画書を作成 or 更新
echo 3. ADR    → アーキテクチャ上の決定を docs/adr/ に記録
echo 4. IMPL   → 計画書に従って実装
echo 5. VERIFY → ビルド/テストで動作確認
echo 6. COMMIT → こまめにブランチへコミット
echo 7. UPDATE → plan/progress.md を更新
echo ```
echo.
echo **実装前に計画書がない場合は、絶対に実装を開始しないこと。**
echo.
echo ---
echo.
echo ## 🗣️ 口調
echo.
echo **%AGENT_TONE%で話すこと。**
echo コードはプロフェッショナルに書くが、会話の説明文はフランクにやりとりすること。
echo.
echo ---
echo.
echo ## 📋 仕様駆動開発ルール
echo.
echo ### 計画書ファースト
echo.
echo 1. **`plan/` ディレクトリに計画書を作ってから実装に入ること**
echo 2. 新機能・新フェーズの前に必ず計画書を作成 or 更新
echo 3. 計画書の必須項目：
echo    - 目的・ゴール
echo    - 実装するクラス・コンポーネントの一覧
echo    - 依存関係
echo    - 完了条件（Definition of Done）
echo    - 見積もり工数
echo.
echo ### ADR（Architecture Decision Records）採用
echo.
echo アーキテクチャ上の重要な決定は **`docs/adr/`** に記録すること。
echo.
echo - ファイル命名: `ADR-NNNN-タイトル.md`
echo - テンプレートは `docs/adr/ADR-0000-template.md` を参照
echo - ADRを書くべき判断軸：
echo   - 将来変更しにくい技術選定
echo   - チームに影響するアーキテクチャパターン
echo   - 代替案を検討して選んだもの
echo.
echo ---
echo.
echo ## 🔄 エージェント間引き継ぎ
echo.
echo **タスクの区切り・フェーズ完了・会話終了時に `plan/progress.md` を必ず更新すること。**
echo.
echo `plan/progress.md` に記録する内容：
echo - 完了した実装内容とPR番号
echo - 次にやるべきこと（Next Action）
echo - 発生している課題・未解決バグ
echo - 現在の設計意図・コンテキスト
echo.
echo ---
echo.
echo ## 🌿 Git運用ルール
echo.
echo - **`main` への直接コミット・マージは絶対禁止**
echo - ブランチ命名: `feature/xxx`, `fix/xxx`, `refactor/xxx`, `docs/xxx`
echo - コミットメッセージ: `[GitHub Copilot] [カテゴリ] 変更内容の概要`
echo - 全マージは **PR経由**
echo.
echo ---
echo.
echo ## 💻 技術スタック
echo.
echo このプロジェクトの技術スタック: **%TECH_STACK%**
echo.
echo ^> ⚠️ このセクションはプロジェクト固有の内容に書き換えること。
echo ^> コーディング規約・命名規則・フレームワーク固有のルールをここに追記する。
echo.
echo ---
echo.
echo ## ✅ 作業完了の定義
echo.
echo 以下が全て揃って初めて「完了」と言える：
echo 1. 計画書が `plan/` に存在する
echo 2. ADRが必要な場合は `docs/adr/` に記録済み
echo 3. ビルドが通る（エラーなし）
echo 4. テストが成功する
echo 5. `plan/progress.md` が最新状態
echo 6. PRが作成済み
echo.
echo ---
echo.
echo ## 🚦 作業開始チェックリスト
echo.
echo 作業を始める前に必ずこれを確認：
echo.
echo ```
echo □ plan/progress.md を読んで現在のフェーズ・ブランチを把握した
echo □ 実装する機能の計画書が plan/ に存在する（なければ先に作る）
echo □ main ブランチにいないことを確認した
echo □ ADRが必要な設計決定がないか確認した
echo ```
echo.
echo 作業が終わったら：
echo.
echo ```
echo □ ビルド・テストが通っている
echo □ plan/progress.md を更新した
echo □ ブランチにコミットした（main 直接コミットはNG）
echo □ PRを作成した
echo ```
) > ".github\copilot-instructions.md"
echo   完了！
echo.

:: ============================================================
:: .github/instructions/planning.instructions.md
:: ============================================================
echo .github\instructions\planning.instructions.md を生成中...
(
echo ---
echo applyTo: "plan/**"
echo ---
echo.
echo # 計画書作成ガイド
echo.
echo ## 計画書の必須構造
echo.
echo ```markdown
echo # [フェーズ名/機能名] 計画書
echo.
echo ## 目的・ゴール
echo.
echo ## 背景・前提
echo.
echo ## 実装するクラス・コンポーネント
echo.
echo ^| クラス名 ^| 役割 ^| 対応ファイル ^|
echo ^|---------^|------^|------------^|
echo.
echo ## 完了条件（Definition of Done）
echo - [ ] ビルドエラーなし
echo - [ ] テスト成功
echo - [ ] plan/progress.md 更新済み
echo - [ ] PRが作成済み
echo.
echo ## 見積もり工数
echo.
echo ## 未確定事項・要確認
echo ```
echo.
echo ## 注意事項
echo - 実装前に必ずこのファイルを作ること
echo - 計画書が存在しない機能の実装は開始しないこと
) > ".github\instructions\planning.instructions.md"
echo   完了！
echo.

:: ============================================================
:: .github/instructions/git-workflow.instructions.md
:: ============================================================
echo .github\instructions\git-workflow.instructions.md を生成中...
(
echo ---
echo applyTo: "**"
echo ---
echo.
echo # Git ワークフロー指示書
echo.
echo ## ブランチ戦略
echo.
echo - **`main` への直接コミット・マージは絶対禁止**
echo - `feature/xxx` / `fix/xxx` / `refactor/xxx` / `docs/xxx`
echo.
echo ## コミット規約
echo.
echo フォーマット: `[GitHub Copilot] [カテゴリ] 変更内容の概要`
echo.
echo カテゴリ: `[機能追加]` `[バグ修正]` `[リファクタリング]` `[テスト]` `[ドキュメント]` `[設定]` `[計画書]` `[ADR]`
echo.
echo ### 例
echo ```
echo [GitHub Copilot] [機能追加] ユーザー認証機能を実装
echo [GitHub Copilot] [バグ修正] ログイン時のNullReferenceExceptionを修正
echo [GitHub Copilot] [計画書] Phase 3の計画書を作成
echo [GitHub Copilot] [ADR] ADR-0003 DBにPostgreSQLを採用
echo ```
echo.
echo ## PR の必須内容
echo.
echo ```markdown
echo ## 概要
echo.
echo ## 変更内容
echo - ...
echo.
echo ## テスト・検証
echo - [ ] ビルドエラーなし
echo - [ ] テスト成功
echo.
echo ## 関連する計画書・ADR
echo ```
) > ".github\instructions\git-workflow.instructions.md"
echo   完了！
echo.

:: ============================================================
:: .github/instructions/adr.instructions.md
:: ============================================================
echo .github\instructions\adr.instructions.md を生成中...
(
echo ---
echo applyTo: "docs/adr/**"
echo ---
echo.
echo # ADR（Architecture Decision Records）記述ガイド
echo.
echo ## ADRとは
echo.
echo アーキテクチャ上の重要な意思決定を記録するドキュメント。
echo 「なぜそうしたのか」を後から追えるようにするための記録。
echo.
echo ## いつADRを書くか
echo.
echo 1. 技術選定（フレームワーク・ライブラリ・インフラ等）
echo 2. 将来変えにくい設計決定
echo 3. 代替案を検討して選んだもの
echo 4. 「なぜそうしたか」が後で不明になりそうな決定
echo.
echo ## ファイル命名規則
echo.
echo `docs/adr/ADR-NNNN-短いタイトル.md`
echo.
echo ## ステータス
echo.
echo - **Proposed** / **Accepted** / **Deprecated** / **Superseded by ADR-XXXX**
echo.
echo ## 注意事項
echo.
echo - ADRは追記・参照のみ。書いたADRを後から書き換えるのはNG
echo - 方針変更したい場合は新しいADRを書いて「Supersedes ADR-XXXX」と記載する
) > ".github\instructions\adr.instructions.md"
echo   完了！
echo.

:: ============================================================
:: docs/adr/ADR-0000-template.md
:: ============================================================
echo docs\adr\ADR-0000-template.md を生成中...
(
echo # ADR-NNNN: タイトル（短く、動詞句で）
echo.
echo **ステータス**: Proposed / Accepted / Deprecated / Superseded by ADR-XXXX
echo.
echo **日付**: YYYY-MM-DD
echo.
echo **決定者**: （AIエージェント名 or ユーザー）
echo.
echo ---
echo.
echo ## コンテキスト
echo.
echo この決定が必要になった背景・状況を説明する。
echo.
echo ## 決定内容
echo.
echo 何を決定したかを明確に1文で書く。
echo.
echo ## 検討した代替案
echo.
echo ^| 案 ^| 概要 ^| メリット ^| デメリット ^|
echo ^|----^|------^|---------^|----------^|
echo ^| 案A（採用） ^| ... ^| ... ^| ... ^|
echo ^| 案B ^| ... ^| ... ^| ... ^|
echo.
echo ## 理由
echo.
echo なぜその案を選んだのかを説明する。
echo.
echo ## 結果・影響
echo.
echo - **ポジティブ**: ...
echo - **ネガティブ**: ...
echo - **リスク**: ...
echo.
echo ## 参考資料
echo.
echo - 関連する計画書: `plan/xxx.md`
echo - 関連するADR: `docs/adr/ADR-XXXX.md`
) > "docs\adr\ADR-0000-template.md"
echo   完了！
echo.

:: ============================================================
:: plan/progress.md（なければ初期ファイルを生成）
:: ============================================================
if not exist "plan" mkdir "plan"
if not exist "plan\progress.md" (
    echo plan\progress.md を生成中...
    (
    echo # 進捗・引き継ぎログ
    echo.
    echo このファイルは、AIエージェント間で開発コンテキストを引き継ぐためのログ。
    echo タスクの区切りで必ず最新の状態をここに記録すること。
    echo.
    echo ---
    echo.
    echo ## 現在のステータス
    echo - **プロジェクト**: %PROJECT_NAME%
    echo - **現在地**: セットアップ完了。開発開始待ち。
    echo - **作業ブランチ**: （作業ブランチ名を記入）
    echo.
    echo ## 実装済みの内容
    echo - プロジェクトセットアップ
    echo - Kiro-style Copilot設定（`.github/`・`docs/adr/`）
    echo.
    echo ## 次のアクション
    echo - （次にやることを記入）
    echo.
    echo ## 発生している課題・特記事項
    echo - なし
    echo.
    echo ## AIへの伝言
    echo 「セットアップ完了。`plan/progress.md` と `AGENTS.md` を読んでから作業開始してね。」
    ) > "plan\progress.md"
    echo   完了！
    echo.
) else (
    echo plan\progress.md は既に存在するのでスキップするで！
    echo.
)

:: ============================================================
:: AGENTS.md（なければ初期ファイルを生成）
:: ============================================================
if not exist "AGENTS.md" (
    echo AGENTS.md を生成中...
    (
    echo # AGENTS.md - %PROJECT_NAME% 開発ガイド
    echo.
    echo ^> AIエージェント共通の最優先参照ファイル。
    echo ^> 作業開始前に必ずこのファイルと `plan/progress.md` を読むこと。
    echo.
    echo ---
    echo.
    echo ## 🗂️ 関連ドキュメント一覧
    echo.
    echo ^| ファイル ^| 内容 ^|
    echo ^|---------^|------^|
    echo ^| `plan/progress.md` ^| **現在の進捗・引き継ぎログ（毎回読め！）** ^|
    echo ^| `docs/adr/` ^| アーキテクチャ決定記録（ADR） ^|
    echo ^| `.github/copilot-instructions.md` ^| GitHub Copilot向け詳細指示書 ^|
    echo.
    echo ---
    echo.
    echo ## 🗣️ 口調
    echo.
    echo **%AGENT_TONE%で話すこと。**
    echo.
    echo ---
    echo.
    echo ## 🎯 作業サイクル
    echo.
    echo ```
    echo 1. READ   → plan/progress.md と計画書を読む
    echo 2. PLAN   → plan/ に計画書を作成 or 更新
    echo 3. ADR    → 重要な設計決定を docs/adr/ に記録
    echo 4. IMPL   → 計画書に従って実装
    echo 5. VERIFY → ビルド・テストで確認
    echo 6. COMMIT → ブランチへコミット
    echo 7. UPDATE → plan/progress.md を更新
    echo ```
    echo.
    echo ---
    echo.
    echo ## 🏗️ ADR一覧
    echo.
    echo ^| ADR番号 ^| タイトル ^| ステータス ^|
    echo ^|---------^|---------^|----------^|
    echo ^| ADR-0000 ^| テンプレート ^| Template ^|
    echo.
    echo 新規ADRは `docs/adr/ADR-0000-template.md` をコピーして作成すること。
    echo.
    echo ---
    echo.
    echo ## 🌿 Git運用ルール
    echo.
    echo - **`main` への直接コミット・マージは絶対禁止**
    echo - ブランチ命名: `feature/xxx` / `fix/xxx` / `refactor/xxx` / `docs/xxx`
    echo - コミットメッセージ: `[GitHub Copilot] [カテゴリ] 変更内容`
    echo - 全マージは **PR経由**
    echo.
    echo ---
    echo.
    echo ## 💻 技術スタック
    echo.
    echo **%TECH_STACK%**
    echo.
    echo ^> ⚠️ このセクションはプロジェクト固有の内容に書き換えること。
    ) > "AGENTS.md"
    echo   完了！
    echo.
) else (
    echo AGENTS.md は既に存在するのでスキップするで！
    echo.
)

:: ============================================================
:: 完了
:: ============================================================
echo ============================================================
echo  セットアップ完了やで〜！
echo ============================================================
echo.
echo 生成されたファイル：
echo   .github\copilot-instructions.md
echo   .github\instructions\planning.instructions.md
echo   .github\instructions\git-workflow.instructions.md
echo   .github\instructions\adr.instructions.md
echo   docs\adr\ADR-0000-template.md
echo   plan\progress.md  （既存の場合はスキップ）
echo   AGENTS.md         （既存の場合はスキップ）
echo.
echo 次にやること：
echo   1. AGENTS.md の技術スタック・コーディング規約を埋める
echo   2. .github\instructions\unity-csharp.instructions.md などの
echo      言語固有instructionsをプロジェクトに合わせて追加する
echo   3. 最初のADRを docs\adr\ に追加する
echo   4. plan\progress.md に最初のNext Actionを書く
echo.
pause
