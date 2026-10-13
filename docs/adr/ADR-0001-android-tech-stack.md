> 注記: 「一覧は Paging 3、画像は Coil」の部分は [ADR-0002](./ADR-0002-thumbnail-listing-and-di.md) で置き換えた。

# ADR-0001: Androidアプリの技術スタックに Kotlin + Jetpack Compose を採用する

**ステータス**: Accepted

**日付**: 2026-10-13

**決定者**: ユーザー（GitHub Copilot エージェントが提案）

---

## コンテキスト

端末内の写真・動画を、スムーズなスクロールとリッチな操作感で閲覧するAndroidアプリを新規作成する。
対象はAndroid 14以上、スマホ・タブレット両対応、完全ローカル動作。

## 決定内容

UIは Jetpack Compose（Material 3）、言語は Kotlin で実装する。画像は Coil、動画は Media3、一覧は Paging 3、永続化は Room（データ）と DataStore（設定）を使う。

## 検討した代替案

| 案 | 概要 | メリット | デメリット |
|----|------|---------|----------|
| 案A（採用） | Kotlin + Compose | 宣言的UIで複雑な操作（グリッド・シート・スクロールバー）を書きやすい。Google公式推奨 | 大量グリッドの最適化に知識が要る |
| 案B | Kotlin + XML Views | 大量リストの実績が多い | 独自のスクロールバー・シート等の実装量が多い |
| 案C | Flutter | クロスプラットフォーム | Android専用要件に対して利点が薄い。MediaStore・EXIFのネイティブ連携が増える |

## 理由

- 要件がAndroid専用で、MediaStore・ExifInterface・Media3 などネイティブAPIを多用する
- 上スワイプの詳細シート、ピンチズーム、AF枠オーバーレイなどリッチなUIを宣言的に組みやすい
- 大量データは Paging 3 と Coil のキャッシュで対応する

## 結果・影響

- **ポジティブ**: 公式ライブラリが揃っている。UIのテストがしやすい
- **ネガティブ**: Composeのグリッドは最適化を意識しないと重くなる
- **リスク**: Compose BOM・AGP・Kotlinのバージョン互換を保つ必要がある（変更時は libs.versions.toml で一元管理）

## 参考資料

- 関連する計画書: `plan/android-album-app-plan.md`
