<!-- Formatting
## Additions  ?? New features

## Changes  ?? Behaviour changes

## Fixes  ?? Bugfixes

## Other  ?? Technical stuff, what happened behind the scene
-->
## Changes
- Add more info to WorkerInfo page
  - Added "next scheduled run"
  - Added attempt count

## Fixes
- Fixed auto backup, auto extension update, and app update checker stop working
  if it crash/failed

## Other
- Some code refactors
  - Simplify some messy code
  - Rewrite version checker
  - Rewrite Migrator
  - Split the project into several modules
  - Migrated i18n to use Moko Resources
- Update firebase bom to v33.1.0
- Update dependency co.touchlab:kermit-crashlytics to v3.9.0
- Update dependency com.google.android.gms:play-services-oss-licenses to v17.1.0
- Update dependency com.google.gms:google-services to v4.4.2
- Add crashlytics integration for Kermit
- Replace ProgressBar with ProgressIndicator from Material3 to improve UI consistency
- More StorIO to SQLDelight migrations
  - Merge lastFetch and lastRead query into library_view VIEW
  - Migrated a few more chapter related queries
  - Migrated most of manga related queries
- Update Japanese translation (akir45)
- Bump dependency com.github.tachiyomiorg:unifile revision to a9de196cc7
- Update project to Kotlin 2.0
- Update Brazilian Portuguese translation (AshbornXS)
- Update compose bom to v2024.06.00-alpha01
- Refactor archive support to use `libarchive`
- Use version catalog for gradle plugins
- Update dependency org.jsoup:jsoup to v1.7.1
- Bump dependency com.github.tachiyomiorg:image-decoder revision to 41c059e540
- Bump dependency io.coil-kt.coil3 to v3.0.0-alpha08
