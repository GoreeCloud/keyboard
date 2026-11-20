# Third-party provenance

- **GoreeCloud UI Compose adapter**: copied from the first-party public
  `GoreeCloud/gc-ui/android/material3-expressive/gc-ui-compose` tree at
  the exact commit in `GC-UI-REVISION`. The canonical UI token JSON
  snapshot is `gc-ui.tokens.json`; its source remains `GoreeCloud/gc-ui`.
  Copyright and MIT license: `GC-UI-LICENSE`. The adapter source is included
  as a real, buildable Gradle module `:gc-ui-compose`.
- **AndroidX / Jetpack Compose / Material 3 / Kotlin**: Maven/Google
  dependencies; retain their own upstream copyright/license terms (commonly
  Apache-2.0). No upstream source is represented as newly authored here.
- **FlorisBoard** (Apache-2.0), **HeliBoard** (GPL-3.0), **OpenBoard**
  (GPL-3.0): architecture/feature research only; **none of their source,
  models, dictionaries or assets are copied here**. Import decisions require
  independent license, permission, provenance, and dependency review.
- **SwiftKey and Gboard**: feature comparison only; no proprietary source
  or assets reused.

To update the UI adapter, inspect the canonical gc-ui revision, its API
and license, reproduce its source into this repository, verify the token
generator, and run the full Android validation suite in a reviewable PR.
