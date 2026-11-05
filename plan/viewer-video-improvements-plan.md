# ビューア・動画再生の改善 計画書

親計画: [android-album-app-plan.md](./android-album-app-plan.md)（Phase 2a・F-15〜F-17）／進捗: [progress.md](./progress.md)

## 目的・ゴール

実機フィードバックの 4 点を、優先度順に実装する。

| 段階 | 内容 | 位置づけ |
|---|---|---|
| C0 | 動画を再生するとアプリが落ちる不具合の修正 | 最優先（バグ修正） |
| C1 | 動画をアプリ内で再生し、シークバー操作に映像が追従する（前後どちらの方向も） | 機能追加 |
| G1-a | 写真を押してから開くまでを、Apple 風に滑らかな遷移にする | 機能追加 |
| G1-b | 写真を開いた状態で下にスワイプすると一覧へ戻る | 機能追加 |

「逆方向」は、シークバーを左へ動かしたときに過去の位置のフレームへ追従する逆方向シークのこと。動画自体の逆再生は対象外。

## 背景・前提

### C0 の再現結果（実機・原因確定）

- 端末: Pixel 10a（Android 17 / API 37）。debug ビルド。
- 手順: フォトタブで動画セル（▶ 付き）をタップ → ビューアの「再生」ボタンをタップ。
- 結果: 毎回アプリが落ちる。

```
FATAL EXCEPTION: main
java.lang.SecurityException: UID 10529 does not have permission to content://media/external/video/media/8535 [user 0]
  at android.app.IActivityTaskManager$Stub$Proxy.startActivity
  at android.app.Activity.startActivity
  at com.oken.albii.ui.viewer.ViewerScreenKt.launchExternal(ViewerScreen.kt:278)
  at com.oken.albii.ui.viewer.ViewerScreenKt.ViewerScreen$...(ViewerScreen.kt:150)   // 再生ボタン
```

- 原因: `launchExternal` が `FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION` を付けて `startActivity` している。動画は他アプリ（Google カメラ・LINE）の所有物で、本アプリは `READ_MEDIA_VIDEO` による読み取りしか持たない。持っていない書き込み権限を他アプリへ付与しようとしてシステムが拒否し、`catch` が無いため落ちる。
- 同じ関数を使う画像の「編集」（`ACTION_EDIT`）も同じ条件で落ちる。
- 動画は現状、サムネイル表示＋外部アプリ起動のみで、アプリ内再生は未実装（Media3 は未導入）。

### 制約

- 完全ローカル（INTERNET 権限を追加しない）。minSdk 34。
- ツールチェーンは AGP 8.7.3 / Kotlin 2.0.21 / compileSdk 35 のまま。Media3 は `minCompileSdk=35` の範囲で最新の安定版 **1.8.1** を使う（1.10 以降は compileSdk 36 が必要）。
- スクロール性能は維持する。グリッドのセルごとにコールバックやモディファイアを足さない（滑らかさは release 相当ビルドで確認済みの前提を壊さない）。
- 体感確認は `install-release-measure-device.bat` のビルドで行う。

## 設計方針

### C0: 外部起動のクラッシュ修正

- 外部アプリへ渡す Intent は読み取り権限（`FLAG_GRANT_READ_URI_PERMISSION`）だけを付ける。書き込み権限は付けない。
- `startActivity` の `ActivityNotFoundException` / `SecurityException` を握り、トーストで知らせる（アプリは落とさない）。共有にも同じ保護を入れる。
- Intent の組み立てと権限フラグの方針を `ExternalIntents.kt` に切り出し、フラグ方針を単体テストで固定する。
- 動画の再生は C1 でアプリ内再生に置き換えるため、外部アプリ起動の再生ボタンは廃止する。

### C1: Media3 によるアプリ内動画再生とシーク追従（ADR-0006）

- `androidx.media3:media3-exoplayer:1.8.1` を使う。`media3-ui` は使わず、コントロールは Compose で作る（依存を最小にし、スクラブの挙動を自前で制御する）。
- 映像は `TextureView` に出す。ページ送り・遷移アニメーション（拡大縮小・フェード・角丸クリップ）と相性が良い。
- 再生プレイヤーは設定済み（現在）ページの動画だけに作り、ページが離れるか閉じるときに解放する。バックグラウンドに回ったら一時停止する。
- 動画ページはまずサムネイルを表示し、最初のフレームが描画されたら映像へ切り替える。初期状態は一時停止（先頭フレーム）で、▶ か画面タップで再生する。
- シークバー（`Slider`）を操作すると、指の位置に応じて `seekTo` を発行して映像を追従させる。
  - 操作開始で ExoPlayer のスクラブモード（`setScrubbingModeEnabled(true)`）に入る。再生は自動で抑制され、再生中だったかは `playWhenReady` がそのまま保持する。
  - スクラブ中は、最新位置を優先する軽い間引き（約 16 ms）で `seekTo` する。指を離したらスクラブモードを抜け、離した位置へ確定シークする（再生中だったなら自動で再開）。
  - 前方向・後方向で処理を分けない（左へ動かしても同じ経路でフレームが追従する）。
  - 動画が終端に達していた場合は、スクラブ後に勝手に再生が始まらないよう一時停止にする。
- 再生位置の表示は毎フレーム更新（`withFrameNanos`）し、120Hz でもつまみが滑らかに動くようにする。
- 再生エラーは画面内のメッセージで表示し、落とさない。

### G1-a / G1-b: 開く遷移と下スワイプで閉じる操作（ADR-0007）

- `SharedTransitionLayout` は使わない。全セルへの共有要素モディファイアがグリッドのスクロール性能を落とす懸念と、サムネイル（正方形に Crop）→ 全画面（Fit）の形の変化を自然に扱いにくいため。
- ビューア側に「ヒーロー層」を持つ。ヒーローは画像を **常に Crop で枠に描画**し、枠（矩形）をサムネイルの矩形 ⇄ 画像の Fit 矩形へアニメーションする。枠の縦横比が連続的に変わるので、正方形のサムネイルから元の縦横比へ自然に広がる。
- サムネイルの矩形は、グリッドの `LazyGridState.layoutInfo` から**必要なときだけ**計算する（`ThumbnailBounds`）。セルごとの `onGloballyPositioned` は使わない。表示中でないセルへ戻るときは、グリッド側を先にスクロールして対象を見える位置へ出す。
- 下スワイプ: 1 本指・等倍・縦方向優勢のドラッグのときだけ開始する。指に追従して画像が縮み（触った点を基準）、背景が薄くなる。離したときの距離・速度で、サムネイルへ縮みながら閉じるか、元へ戻る（スプリング）かを決める。拡大中は従来どおり画像のパンを優先する。
- 戻るボタン・閉じるボタンも同じ閉じるアニメーションを通す（`BackHandler` はビューア側へ移す）。
- ビューアの画像ページは、フルサイズのデコード完了までサムネイルを仮表示する（黒→遅れて表示を避け、ヒーローとページの切り替えを連続させる）。
- 純粋な計算（Fit 矩形・ドラッグ中の矩形・閉じる判定・角丸）は `HeroMath` に切り出して単体テストする。

## 実装するクラス・コンポーネント

| クラス / コンポーネント | 役割 | 対応ファイル |
|---|---|---|
| `ExternalIntents`（`buildExternalIntent` / `launchEdit` / `launchShare`） | 読み取り権限のみの Intent 生成と、例外を握る起動 | `ui/viewer/ExternalIntents.kt` |
| `ExternalIntentsTest` | 書き込み権限を付けない方針の回帰テスト | `src/test/.../ui/viewer/ExternalIntentsTest.kt` |
| Media3 依存 | ExoPlayer 1.8.1 | `gradle/libs.versions.toml`、`app/build.gradle.kts` |
| `VideoPlayerState` | ExoPlayer を包み、再生状態・位置・シーク操作を Compose に公開 | `ui/viewer/VideoPlayerState.kt` |
| `ScrubSeekThrottle` | スクラブ中の `seekTo` を間引き、最新位置を優先する（純粋ロジック） | `ui/viewer/ScrubSeekThrottle.kt` |
| `PlaybackTime` | `m:ss` / `h:mm:ss` 表示 | `ui/viewer/PlaybackTime.kt` |
| `VideoPage` | サムネイル → TextureView 映像、タップで再生/一時停止、エラー表示 | `ui/viewer/VideoPage.kt` |
| `VideoControls` | 再生/一時停止・経過/総時間・シークバー | `ui/viewer/VideoControls.kt` |
| `ViewerPage` / `ZoomState` | 画像ページ（サムネイル仮表示つき）とズーム状態の持ち上げ | `ui/viewer/ViewerPage.kt`、`ui/viewer/ZoomState.kt` |
| `ThumbnailBounds` | グリッド内のサムネイル矩形の取得と、対象セルの表示位置への移動 | `ui/viewer/ThumbnailBounds.kt`、`ui/grid/MediaGridView.kt`、`ui/albums/AlbumsScreen.kt` |
| `HeroMath` | Fit 矩形・ドラッグ中の矩形・閉じる判定・角丸の計算 | `ui/viewer/HeroMath.kt` |
| `ViewerTransitionState` / `HeroLayer` | 開く・閉じる・ドラッグのアニメーション状態と描画 | `ui/viewer/ViewerTransition.kt` |
| `ViewerScreen`（改修） | ページャ・ヒーロー・コントロールの統合、下スワイプ判定、戻る操作 | `ui/viewer/ViewerScreen.kt`、`ui/main/MainScreen.kt` |

## 実装ステップと依存関係

1. 計画書・ADR-0006（本書）→ 2. C0 修正 → 3. Media3 追加 → 4. C1 プレイヤー・コントロール → 5. C1 実機確認 → PR（C0/C1）
6. ADR-0007 → 7. G1-a ヒーロー遷移 → 8. G1-b 下スワイプ → 9. G1 実機確認 → PR（G1）

- 2 が 3 の前提（まず落ちない状態にする）。4 は 3 に依存。
- 6〜9 は C1 のページ構造（`VideoPage` を含む `ViewerScreen` の分割）に依存するため、C1 の PR の上に積む。
- G1 は C1 のビューア改修を前提にするが、機能としては独立している。

## 完了条件（Definition of Done）

- [x] ビルドエラーなし（`assembleDebug`、`assembleReleaseMeasure`）— C0/C1 時点で確認済み。G1 の後に再確認する
- [x] テスト成功（`testDebugUnitTest`）— C0/C1 時点で 58 件成功（HeroMath を含む）。G1 の後に再確認する
- [ ] plan/progress.md 更新済み（C0/C1 分は更新済み。G1 完了時に更新する）
- [ ] PRが作成済み（C0/C1 は作成。G1 は未）
- [x] C0: 再現手順（動画セル → 再生）で落ちない。画像の「編集」「共有」も落ちない（実機確認済み）
- [x] C0: Intent に書き込み権限を付けない方針が単体テストで固定されている
- [x] C1: 動画がアプリ内で再生・一時停止できる。マージ後のマニフェストに INTERNET 権限が増えていない
- [x] C1: シークバーを右へも左へも動かすと、指の位置の映像に追従する。離した位置で止まる（再生中だったなら再開）（実機確認済み）
- [ ] C1: ページ送り・バックグラウンド・画面回転・動画の終端でも落ちない（終端は確認済み。ページ送り・バックグラウンド・画面回転は未確認）
- [ ] G1-a: 写真タップで、サムネイルから全画面へ滑らかに広がる（3 つのタブすべて）
- [ ] G1-b: 等倍で下スワイプすると指に追従して一覧へ戻る。途中で離すと元へ戻る。拡大中・横ページ送り・上スワイプの情報シートは従来どおり
- [ ] G1: 戻るボタン・閉じるボタンでも閉じるアニメーションが入る。別の写真へ移動してから閉じても、その写真のセルへ戻る
- [ ] グリッドのスクロール性能が悪化していない（release 相当ビルドで `dumpsys gfxinfo` を比較）

## 見積もり工数

時間の見積もりは含めない（プロジェクト方針）。規模感: C0 = 小、C1 = 中〜大（新規依存と新規 UI）、G1 = 大（ジェスチャー・アニメーション・グリッド連携）。

## 未確定事項・要確認

- スクラブ中のシークは、スクラブモードの既定（すべて EXACT）で始める。長尺・長 GOP の動画で応答が遅ければ `ScrubbingModeParameters.setFractionalSeekTolerance` で許容幅を設ける。実機で短尺・長尺の動画を触って決める。
- 画像の「編集」は読み取り権限のみで外部エディタを起動する。コピーとして保存するエディタでは使えるが、元ファイルを直接書き換えるエディタは保存できない。書き込みが必要な編集（`MediaStore.createWriteRequest` による同意）は今回の範囲外。
- 動画の自動再生はしない（初期は一時停止）。Apple 写真のように自動再生へ寄せるかは、使ってみてから判断する。
- Apple 写真のようなフィルムストリップ（サムネイルを並べた）スクラバーは今回の範囲外。まずは通常のシークバーで追従させる。
- 拡大中に閉じる場合は、拡大した矩形からヒーローを始める想定。実機で違和感があれば簡略化する。
- 画面のロック中は gfxinfo が 0 フレームになるため、計測前にロック解除と点灯維持が必要。
