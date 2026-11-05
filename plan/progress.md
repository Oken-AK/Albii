# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: 動画再生クラッシュ（C0）の修正と、アプリ内動画再生＋シークバー追従（C1）を実装し、実機（Pixel 10a）で確認済み。PR は `fix/video-playback-crash`（PR #10 の計画書の上に積んだ）。次は G1（写真を開く遷移・下スワイプで閉じる）
- **直近の変更**: ADR-0006（Media3 ExoPlayer によるアプリ内再生）。`ExternalIntents.kt`（読み取り権限のみ・例外を握る）、`VideoPlayerState` / `VideoPage` / `VideoControls`、`ScrubSeekThrottle`、`ViewerImages`（画像デコードを IO スレッドへ）。計画書: [viewer-video-improvements-plan.md](./viewer-video-improvements-plan.md)
- **残り**: G1（開く遷移・下スワイプで閉じる）。Coil 化・Telephoto 化・ピンチ連続スケール等のリファクタ（P1〜P6）は、体感が良好なため必須ではなくなった。今後の体感確認は `install-release-measure-device.bat` のビルドで行う
- **Macrobenchmark**: 作りかけは `git stash`（"macrobenchmark WIP (paused)"）に退避

## 要件・計画
- 計画書: [android-album-app-plan.md](./android-album-app-plan.md)、[viewer-video-improvements-plan.md](./viewer-video-improvements-plan.md)（ビューア・動画の改善）
- ADR: [ADR-0001](../docs/adr/ADR-0001-android-tech-stack.md)（Kotlin + Compose）、[ADR-0002](../docs/adr/ADR-0002-thumbnail-listing-and-di.md)（Coil / Paging 3 撤回）、[ADR-0003](../docs/adr/ADR-0003-navigation-settings-and-albums.md)（ナビ・設定・アルバム）、[ADR-0004](../docs/adr/ADR-0004-gesture-handling-and-in-app-picker.md)（ジェスチャー・アプリ内ピッカー）、[ADR-0005](../docs/adr/ADR-0005-exif-info-panel.md)（EXIF 情報パネル）、[ADR-0006](../docs/adr/ADR-0006-media3-video-playback.md)（Media3 による動画再生）

## 完了済みの内容
- Phase 0: プロジェクト雛形、ビルド環境（JDK 17 / Gradle 8.11.1 / SDK zip 版）
- Phase 1: 権限、MediaStore 読み込み、グリッド、フォルダ表示、ハンバーガーメニュー → 設定、下部タブ（フォト / アルバム / 端末）
- 実機フィードバック1・2 の反映: ピンチで列数変更、全画面の横スワイプ、ピンチ拡大時のパン制限、スクロールバーのオーバーレイと自動非表示、アプリ内ピッカーでのアルバム追加

## 動画再生（C0 / C1・実装済み）
- **C0 の原因（実機で再現・確定）**: ビューアの再生ボタンが `ACTION_VIEW` のチューザーを `FLAG_GRANT_WRITE_URI_PERMISSION` 付きで起動していた。他アプリ所有の動画（Google カメラ・LINE）に本アプリは書き込み権限を持たず、`startActivity` が `SecurityException` を投げて落ちていた。画像の「編集」も同じ関数だったので同じ条件で落ちる
- **C0 の修正**: 外部アプリへは読み取り権限のみを付与。起動失敗は握ってトースト。フラグ方針は `ExternalIntentsTest` で固定
- **C1**: 動画はアプリ内（Media3 ExoPlayer 1.8.1 / TextureView）で再生。▶ か画面タップで再生・一時停止。シークバーを動かすと ExoPlayer のスクラブモードで映像が前後どちらにも追従し、離した位置で止まる（再生中なら再開）。`media3-ui` は使わず Compose で自作
- **実機確認（debug・Pixel 10a / Android 17）**: 動画を開く → 再生 → 一時停止、シークの前方向・後方向ドラッグ（0:32→0:27→0:09→0:04 で映像も一致）、再生中にドラッグ → 離すと再生再開、終端での再生ボタン、共有・編集のチューザー表示でクラッシュなし
- **実機確認（releaseMeasure / R8）**: 起動・動画オープン・再生の操作でクラッシュ・エラーなし（ログ確認）。見た目の体感確認はユーザー待ち
- **未確認**: 長時間・高ビットレート動画でのスクラブ応答、画面回転中の再生、ページ送りでの動画の入れ替え
- マージ後のマニフェストに INTERNET 権限が増えていないことを確認済み
- 実装: `ui/viewer/ExternalIntents.kt`、`VideoPlayerState.kt`、`VideoPage.kt`、`VideoControls.kt`、`ScrubSeekThrottle.kt`、`PlaybackTime.kt`、`ViewerImages.kt`

## 写真情報パネル（完了・マージ済み）
- 上スワイプ（等倍時）または情報ボタンで下からシートを表示。実機で表示を確認済み
- 設定の「写真情報」で項目ごとに ON/OFF（既定 ON: 基本項目。OFF: 機材・GPS・向き・色空間）
- 未確認: 設定切り替えの反映、GPS・機材項目の表示
- 実装: `info/InfoItem.kt`、`info/InfoFormat.kt`（単体テストあり）、`info/PhotoInfoRepository.kt`、`ui/viewer/PhotoInfoSheet.kt`

## 次のフェーズ計画（優先度順）
1. ~~**C0: 動画再生クラッシュの調査・修正**~~ → 完了（上記）
2. ~~**C1: 動画シークバーの追従**~~ → 完了（上記）
3. **G1: 写真ビューアの遷移・閉じる操作**（次）
   - グリッドから写真を滑らかに開く遷移を追加する（ヒーロー遷移。設計は [viewer-video-improvements-plan.md](./viewer-video-improvements-plan.md)）
   - 画像を下へスワイプして一覧に戻れるようにする。ズーム・横ページ送り・情報シート操作との競合を避ける
   - 先に ADR-0007 を書く。純粋な計算部分 `HeroMath.kt`（単体テストあり）は作成済み
4. **横スワイプ（拡大中のパン後のページ送り）**: 34cc99c で実装済み。実機で指による確認待ち
5. **D: 計算系（被写界深度・超焦点距離・手ブレ限界）、天体系（日の出・ゴールデンアワー）、連写のまとめ**
6. **E: ピント位置（AF）の可視化**（先にスパイクで対応可否を確認）
7. **F: フィルタ、お気に入り、ゴミ箱**
8. **G: 見た目の仕上げ（Apple / Google Photos 風）、タブレット対応**

## 発生している課題・特記事項
- Coil 3 は KMP 版 Compose を引き込みビルドが壊れたため撤回（ADR-0002）
- `loadThumbnail` で動画・HEIC が取れるかは動画で確認済み（Pixel の動画サムネが出る）。HEIC は未確認
- 動画再生クラッシュは解決済み（上記「動画再生」参照）。動画自体の逆再生は対象外のまま
- 写真を開く滑らかな遷移と、画像を下スワイプして一覧へ戻る操作は G1 で対応予定。既存のズーム・横送り・情報シートとのジェスチャー競合を確認する
- 拡大中の横スワイプは実装済み（実機で拡大中の縁を確認待ち）。
- 実機スクロール計測（Pixel 10a・120Hz、adb で上下スクロール 12 往復、dumpsys gfxinfo。adb のスワイプは指と違うため参考値）:

  | ビルド | フレーム | ジャンク | p50 | p90 | p95 | p99 |
  |--------|---------|---------|-----|-----|-----|-----|
  | debug | 1523 / 1610 | 6.8% / 7.2% | 9ms | 19〜20ms | 26〜28ms | 48ms |
  | releaseMeasure（R8 minify） | 1982 | 0.1% | 5ms | 9ms | 10ms | 12ms |

  → debug は Compose が極端に遅く、体感評価に使えない。滑らかさの判断は `install-release-measure-device.bat` のビルドで行う
- 滑らかさ・ジェスチャー改善の分析と計画（Coil 2.7.x + loadThumbnail 用 Fetcher、Telephoto、ピンチ連続スケール、Baseline Profile、BOM 更新）は P0 の結果を見て範囲を確定する。主な仮説: サムネが自前読込でキャンセル・プレースホルダ無し、全セルの `animateItemPlacement()`、ビューアが画像デコード完了まで空で隣ページ先読み無し、ズーム/パンが自前実装
- 実機の画面ロック中は gfxinfo が 0 フレームになる。計測前にロック解除が必要。点灯維持は `adb shell settings put global stay_on_while_plugged_in 7`（この端末は充電器接続が AC 扱いで、`svc power stayon usb` は効かなかった）
- 実機を adb で操作して確認するときは、スクリーンショットや `uiautomator dump` を `/sdcard` に置かない（MediaStore に取り込まれ、アプリの一覧に出てしまう）。`/data/local/tmp` を使う。ユーザーが端末を使い始めると他アプリの画面が写り込むので、撮る前に前面アプリを確認する
- 画像の「編集」は読み取り権限のみで外部エディタを起動する。コピーとして保存するエディタでは使えるが、元ファイルを直接書き換えるエディタは保存できない（書き込みが必要な編集は `MediaStore.createWriteRequest` が必要。今回の範囲外）
- `AlbumDatabase.create()` は VM 1つ前提。複数インスタンスは未対応

## AIへの伝言
「動画再生クラッシュ（C0）と、アプリ内動画再生＋シークバー追従（C1）は実装済みで、実機で確認済み（ブランチ `fix/video-playback-crash`）。次は G1（写真を開く遷移・下スワイプで閉じる）。先に ADR-0007 を書き、`ui/viewer/HeroMath.kt`（作成済み・単体テストあり）を土台に実装すること。グリッドのスクロール性能を落とさないよう、セルごとの `onGloballyPositioned` は使わず `LazyGridState.layoutInfo` から必要なときだけ矩形を取る（計画書参照）。体感確認は install-release-measure-device.bat を使い、既存のジェスチャー（ズーム・横ページ送り・上スワイプの情報シート）を壊さないこと。動画自体の逆再生は範囲外。」

## 今回の更新履歴
- PR #10: 動画・ビューア操作改善を計画に反映（計画書のみ）
- C0 / C1: `fix/video-playback-crash`（実装・実機確認済み。PR は PR #10 の上に積んだ）
