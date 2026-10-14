# ADR-0005: 写真情報は ExifInterface で読み、項目ごとに表示を切り替える

**ステータス**: Accepted

**日付**: 2026-10-14

**決定者**: GitHub Copilot（ユーザー指示「auto copilot」により選定）

---

## コンテキスト

上スワイプで開く写真情報パネルを実装する必要がある。要件は次の通り。

- EXIF などの撮影情報を端末内だけで読む（INTERNET 権限は使わない）
- 表示項目は設定でチェックしたものだけ出す
- 項目数が多い（計25項目）ため、読み取りと整形をUIから切り離したい

## 決定内容

EXIF の読み取りには `androidx.exifinterface:exifinterface` を使い、`info` パッケージに読み取り（`PhotoInfoRepository`）・整形（`InfoFormat`）・項目定義（`InfoItem`）を分けて置く。表示項目は `InfoItem` の集合を DataStore に保存し、有効な項目だけを読み込んで表示する。

## 検討した代替案

| 案 | 概要 | メリット | デメリット |
|----|------|---------|----------|
| 案A（採用） | ExifInterface + 自前の整形 | 公式ライブラリで軽い。依存が少ない | 一部の MakerNote は読めない |
| 案B | メタデータ用の外部ライブラリ（metadata-extractor など） | 対応形式と項目が多い | 依存が増える。ライセンス確認が必要 |
| 案C | 画像を毎回デコードして解析 | 画質解析の基盤になる | 重い。今回の項目（EXIF中心）には過剰 |

## 理由

現時点の項目は EXIF で取れるものが中心で、ExifInterface で足りる。依存が少なく、端末内で完結する。画質解析（ヒストグラムなど）は別フェーズで、画像をデコードする必要があるため分けて扱う。

## 結果・影響

- **ポジティブ**: 依存が1つ増えるだけで済む。整形関数は JVM の単体テストで検証できる
- **ネガティブ**: 取得できない項目は「表示しない」扱いになる（空欄で出さない）
- **リスク**: 機種ごとの EXIF の揺れ。GPS や機材情報はプライバシー上、既定で OFF にしている

## 参考資料

- 関連する計画書: [android-album-app-plan.md](../../plan/android-album-app-plan.md)
- 関連するADR: [ADR-0003](./ADR-0003-navigation-settings-and-albums.md)、[ADR-0004](./ADR-0004-gesture-handling-and-in-app-picker.md)
- 注: 計画書で ADR-0005 に予約していた「Media3 による動画再生」は ADR-0006 へ繰り下げる
