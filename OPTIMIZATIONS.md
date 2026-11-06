# :core Speed & Allocation Optimization Plan

**Scope**: everything under `core/src/main`. No code has been changed. Every item below is meant to keep current behaviour. Where an item has a subtle edge case, it is called out under ⚠ Behaviour note.

**Notation**: $M$ = methods per class, $P$ = previews per class, $K$ = cross-module custom preview annotations, $C$ = classes scanned.

> [!NOTE]
> **ClassGraph 4.8.184 internals checked against its source:**
> - `MethodInfo.getAnnotationInfo()` is cached, so annotation lookups don't allocate. They are still linear scans.
> - `ClassInfo.getDeclaredMethodInfo()` and `hasDeclaredMethodAnnotation()` build a new `MethodInfoList` on every call.
> - `Class.getDeclaredMethods()` / `getMethods()` return fresh copies of every `Method` object on every call.

---

## 1. Speed: Highest Impact First

| # | Change | Where | Hot path | Est. impact |
|---|--------|-------|----------|-------------|
| **S1** | Reuse one `URLClassLoader` per `SourceSetClassLoader` | `SourceSetClassLoader.kt#L15-L26` | Once per preview method × finder (`setTargetSourceSet` mode) | 🔴 Very high |
| **S2** | Resolve `Class` + `Method[]` once per class in `MethodFinder`; evaluate candidates once | `MethodFinder.kt#L15-L40` + the 3 finders | Once per preview method | 🔴 High |
| **S3** | Move `containedAnnotations` out of the per-method loop (it's currently $O(M^2 \cdot K)$) | `CrossModuleCustomPreviewsInCompiledClassFinder.kt#L32-L87` | Once per class × method | 🔴 High (compiled-class mode) |
| **S4** | Precompile the 2 Regexes and cache per-method reflection in `methodParametersTypeAsString` | `ProvideComposablePreview.kt#L15-L32` | Once per generated preview × parameter | 🟠 Medium-high |
| **S5** | Cache the `PreviewParameter` class lookup process-wide; use Java reflection to read annotation members | `ComposablePreviewWithPreviewParameterMapper.kt#L32-L70` | Once per mapper | 🟠 Medium (high on pure JVM, where the class is missing) |
| **S6** | Precompile the Regexes in `GetDeviceScreenshotId` | `GetDeviceScreenshotId.kt#L25-L76` | Once per screenshot id (up to ~8 compiles per call) | 🟠 Medium |
| **S7** | Build each class's declared-method list once and share it across finders; precompute `"$…$Container"` | `ComposablePreviewsInCompiledClassFinder.kt`, `MultiplePreviewsInCompiledClassFinder.kt#L28-L39` | Once per scanned class ($C$ can be tens of thousands with `scanAllPackages`) | 🟠 Medium |
| **S8** | Cache the cross-module custom preview → $T$ mapping per annotation | `CrossModuleCustomPreviewsInCompiledClassFinder.kt#L57-L59` | Once per method × custom annotation × preview | 🟡 Medium-low |
| **S9** | Cache per-handler reflection in `ComposablePreviewInvocationHandler` | `ComposablePreviewInvocationHandler.kt#L31-L196` | Once per `invoke()`, including each recomposition | 🟡 Medium-low (render time) |
| **S10** | Compute the "included annotations info" filter once per method, not once per `@Preview` | All 3 finders (e.g. `ComposablePreviewsAtBuildTimeFinder.kt#L71`) | Once per `@Preview` annotation | 🟡 Low-medium |
| **S11** | Device lookup by map instead of 8 linear enum scans | `DevicePreviewInfoParser.kt`, `GetDeviceByIdentifier.kt` | Once per device parse | 🟢 Low |
| **S12** | Small fixes (see list) | various | — | 🟢 Very low |

---

### S1: Reuse the URLClassLoader (biggest win)

- **Today**: Every `loadClass(classInfo)` call builds a new `URLClassLoader` and a new `File` → `URI` → `URL`. A class that exists only in the source-set build dir gets read from disk, parsed, defined and verified again for every preview method, and once more in each of the 3 compiled-class finders.
- **Change**: Create the `URL` and the `URLClassLoader` lazily, once per `SourceSetClassLoader` instance. Optionally also memoize `name` → `Class<*>`.
- **Why scope stays the same**: A `SourceSetClassLoader` is created in `createPreviewsFinder()`, and that cache is reset at the start of every scan (`ClasspathPreviewsFinder.kt#L52-L72`). Each scan still sees freshly compiled classes.
- ⚠ **Behaviour note**: Within one scan, previews declared in the same class would share one `Class` object. Today each one gets its own copy, except when the parent loader can already load it. The visible difference: a file-level static initializer would run once per class instead of once per preview. Please confirm this is acceptable. I expect it is, since it matches normal JVM semantics.
- Keep the `catch (ClassNotFoundException)` → `ClassNotFoundInSourceSetException` mapping exactly as it is.

---

### S2: MethodFinder

- **Today**:
  - A new `MethodFinder` is created per method in every finder.
  - Each `find()` calls `loadClass` again and copies the whole `declaredMethods`/`methods` array. That's $P \times M$ `Method` copies per class.
  - The filtered `Sequence` is evaluated up to 3 times: `count()`, then `first()`, then `firstOrNull {}`.
  - The `methodInfo.parameterInfo` descriptor list is rebuilt for every candidate.
- **Change**:
  - Create one `MethodFinder` per `findPreviewsFor(classInfo)` call, with a lazy `clazz`, `declaredMethods`, and `methods`. Even better, share it across the 3 finders (see S7).
  - Materialize the name-filtered candidates into a list once.
  - Compute the `methodInfo` parameter-type list once, before matching.
  - Compare against cached `parameterTypes` canonical names.
- Selection order must stay the same: single match → that one; else first full-signature match; else first.

---

### S3: CrossModuleCustomPreviewsInCompiledClassFinder

- **Today**: `containedAnnotations` (a `Map.filter` that calls `hasDeclaredMethodAnnotation` for each of the $K$ keys, each building a `MethodInfoList` and scanning every method) runs inside the per-method loop, even though it only depends on the class. `hasPreviewsIn()` does the same work again. `MethodFinder.find` also runs once per (method, matching annotation) instead of once per method.
- **Change**: Compute `containedAnnotations` once per class and use `isNotEmpty()` on it as the `hasPreviewsIn` check. Iterate in the same map order to keep output order. Resolve the `Method` lazily once per method.

---

### S4: methodParametersTypeAsString

- **Today**: Compiles `Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\.")` and `"\\s+".toRegex()` for each parameter of each generated preview. It also calls `asComposableMethod()` and copies `genericParameterTypes` per preview. A `@PreviewParameter` provider with $N$ values repeats all of this $N$ times.
- **Change**: Hoist both regexes into private vals at top level. Inside the mapper, cache `realParametersCount` and `genericParameterTypes`. Only index 0 depends on parameter, so the resolved names for indices $\ge 1$ can be cached per method.
- `getUnderlyingType()` (`declaredMethods` copy) can be memoized per `Class`, e.g. with a `ClassValue`.

---

### S5: ComposablePreviewWithPreviewParameterMapper

- `Class.forName(previewParameterClassName)` runs once per mapper (through a per-instance lazy). On pure JVM, where it fails, that means a `ClassNotFoundException` with a filled stack trace per preview method. Cache the result, including `null`, in a companion `ConcurrentHashMap<String, Optional<Class<*>>>`. The caller class loader is the same for every instance, so the result is identical.
- `getPropertyValue(annotation, "provider"/"limit")` uses `kotlin-reflect` `memberProperties` on an annotation proxy. Replace it with `(annotation as java.lang.annotation.Annotation).annotationType().getMethod(name).invoke(annotation)`. For annotation members this returns the same value.
- Keep `kotlin-reflect` for the provider instance's `values`/`count`/`getDisplayName`. Switching those has `DefaultImpls`/interface-default edge cases, so it's not worth the risk.
- `parameterAnnotations.flatMap { it.toList() }.find {}` → nested loop with early exit.

---

### S6: GetDeviceScreenshotId

- 5 regex literals are compiled on every call. `Regex("id=reference_|name:|…")` sits inside `.map {}`, so it compiles once per spec segment. Move all of them into private vals on the object.

---

### S7: Declared-Method List per Class

- For every class in `scanResult.allClasses`, each finder calls `hasDeclaredMethodAnnotation()` and/or `declaredMethodInfo`, and each call allocates a new `MethodInfoList`. In compiled-class mode that's at least $2 + 2 + (K + 1)$ lists per class, even for the many classes with no previews at all.
- **Change**: Inside the internal finders (keep the public `PreviewsFinderWithResult` signature), fetch `classInfo.declaredMethodInfo` once. Run a single pass that checks whether any method has any relevant annotation name, using a precomputed `HashSet` of `annotationToScanClassName`, `…$Container` and the custom keys. Return early otherwise. Precompute `"$annotationToScanClassName\$Container"` as a val.

---

### S8: Cache Custom Preview Info

- `previewInfoMapper.mapToComposablePreviewInfo(preview.parameterValues)` runs for each method that uses a given custom annotation, but the input only depends on the annotation. Memoize `annotationKey` → `List<T>` per finder instance, which lives for one scan.
- ⚠ **Behaviour note**: This assumes `ComposablePreviewInfoMapper` (a public interface) is pure and that $T$ isn't mutated later. Keep calling `meetsPreviewCriteria` per preview. If purity can't be assumed, skip this item.

---

### S9: ComposablePreviewInvocationHandler (Render Time)

On every `invoke()`, the handler currently:
- Copies `parameterTypes` to compute `isComposable`
- Calls `asComposableMethod()`
- Copies `parameterAnnotations` in `requirePreviewParameterIsFirstArgument`
- Scans `annotationsInfo` for the wrapper
- Calls `getDeclaredConstructor()` and `setAccessible`

**Change**: Turn the per-method facts into lazy fields. Still throw the exception on every call when the index is $> 0$, and still create a new receiver instance per call, caching only the `Constructor`.

`getContextFromComposer` (non-composable path: Glance/Wear) calls `Class.forName` with an exception fallback, plus methods on a large `*Kt` class, on every call. Cache the resolved `getLocalContext` `Method` (or its failure) in the companion.

---

### S10: Included-Annotations Filter per Method

- `methodInfo.annotationInfo.filter { namesOfIncludeAnnotationsInfo.contains(it.name) }` runs once per `@Preview` on a method, but it only depends on the method. Compute it once per method.
- ⚠ **Behaviour note**: This shares one `AnnotationInfoList` instance between previews of the same method. Lists stay equals, so `toSet()` dedup is unchanged. It's only observable if a caller mutates `otherAnnotationsInfo`.

---

### S11: Device Lookup

- `findByDeviceId`/`Name` calls `enumValues<T>()` (clones the array) across 8 enums, linearly. Build 2 lazy `HashMap<String, Device>`s (id and name) in the same priority order (Phone → … → XR), using `putIfAbsent` so the first match wins like today. At minimum, use `entries` instead of `enumValues()`.
- `DEFAULT` is a getter that builds a new `Device`, including screen-size math, every time `parse(device)` is called, even when it isn't used. Make it a stored val. The JVM `getDEFAULT()` stays binary-compatible; the only difference is reference identity.

---

### S12: Small Fixes

- **`ComposablePreview.getAnnotation()`**:
  - `filter {}.firstOrNull()` → `firstOrNull {}`
  - `annotationParams.map { name } + getValue(name)` is $O(n^2)$ → iterate the entries directly
  - Build a `name` → `KParameter` map once
- **`ScanResultCustomPreviewAnnotationLoader` / `PackageTreesCustomPreviewAnnotationLoader`**: `associate {}.filterValues {}` → a single loop that builds one map (same insertion order).
- **`PreviewScanningLogger` / `ScanResultLogger`**: Skip `joinToString` string building when logging is disabled.
- **`ComposablePreviewScanner.classGraphSourceScanner`**: Builds a new scanner and logger on each access. This is trivial and kept as-is for clarity.

---

## 2. Allocations: Highest Impact First

| # | Change | Where | What is saved | Est. impact |
|---|--------|-------|---------------|-------------|
| **A1** | Same as S1: one `URLClassLoader` per scan | `SourceSetClassLoader.kt` | One loader + one duplicate `Class` in Metaspace per preview method. Since `ComposablePreview` holds a `Method` → `Class` → loader, all of them stay reachable for the whole test run (none are closed either). | 🔴 Very high |
| **A2** | Same as S2: method array per class | `MethodFinder.kt` | $P \times M$ `Method` copies, `parameterTypes` arrays, intermediate `List<String>`s | 🔴 High |
| **A3** | Same as S3 + S10 + S8 | CrossModule finder | One `LinkedHashMap` per method, $K$ `MethodInfoList`s per method, one filtered `AnnotationInfoList` per preview, repeated $T$ instances | 🟠 Medium-high |
| **A4** | Precompiled regexes (S4, S6) | `ProvideComposablePreview.kt`, `GetDeviceScreenshotId.kt` | Pattern + node graph + Matcher per compile | 🟠 Medium |
| **A5** | Close the custom-previews `ScanResult` in the dumper with `.use {}` | `ScanResultDumper.kt#L119-L128` | Releases ClassGraph's internal maps and mapped buffers right away instead of at GC. The object is local and never escapes, so behaviour is unchanged. | 🟠 Medium (dump flow) |
| **A6** | Same as S7: one declared-method list per class | Finders | $(2 + 2 + K + 1) \times C$ `MethodInfoList`s, $\sim C \times 5$ with `scanAllPackages` | 🟠 Medium |
| **A7** | Lighter `ComposablePreviewWithPreviewParameterMapper` | `ComposablePreviewWithPreviewParameterMapper.kt#L32-L45, L132-L138` | Per mapper: `SynchronizedLazyImpl` + lambda (→ shared cache, S5); the non-WithResult wrapper's delegate gets a shared `ProvideComposablePreview` instead of a new one (delegate isn't part of equals, so this is safe); `flatMap { toList }` lists | 🟡 Medium-low |
| **A8** | Same as S9: per-invoke arrays | `ComposablePreviewInvocationHandler.kt` | `parameterTypes`/`parameterAnnotations` clones, `localContextClass.methods` (hundreds of `Method` copies) per invoke, spread arrays | 🟡 Medium-low (render time) |
| **A9** | Lean finder pipelines | All 3 finders, `ScanResultFilter.kt#L163-L180` | Replace `asSequence().flatMap { …previewMethods.asSequence() }` / `emptySequence()` wrappers, `previews.map { it as AnnotationInfo }` (Multiple finder), and the per-class `mutableListOf()` with a direct `LinkedHashSet` + loop and one result `ArrayList`. Keep `toSet()` dedup semantics (`LinkedHashSet`, same order). | 🟡 Low-medium |
| **A10** | Same as S11: device parsing | `DevicePreviewInfoParser.kt`, `Default.kt` | Throwaway `DEFAULT` `Device` per `parse()`, 8 enum-array clones per lookup | 🟢 Low |
| **A11** | Remove the unused `SCREEN_SIZE_EXCEPTIONS` | `ScreenSizeCalculator.kt#L8-L10` | Dead private map (never read). Its initializer also eagerly loads the whole `Automotive` enum the first time any `Device` is built. | 🟢 Low |
| **A12** | Hoist the per-preview `arrayOf(proxyPreviewWithResult, proxyPreview)` | `ProvideComposablePreview.kt#L108, L168` | One array per preview (`Proxy` clones it internally anyway) | 🟢 Very low |
| **A13** | Micro | various | `PreviewScreenshotIdBuilder.unsafeFileNameChars` per instance → companion; `buildList {}.joinToString` in `build()`/`asString()` → `StringBuilder`; logger strings when disabled; double maps in annotation loaders | 🟢 Very low |

---

## 3. Considered and Rejected (They Would Change Behaviour)

- **Sharing the default `ProvideComposablePreviewWithResult()` in `ComposablePreviewWithPreviewParameterMapperWithResult`**: It's a data-class constructor property, so it takes part in `equals`/`hashCode`. Sharing it would make the finders' `toSet()` start de-duplicating Wear mappers that are distinct today.
- **Dropping `toSet()` in the finders**: It de-duplicates, so it's part of the output contract.
- **Caching custom-preview metadata or `ScanResult` across scans**: The per-scan reset is deliberate (`ClasspathPreviewsFinder.kt#L27-L54`).
- **Pre-filtering classes with `ScanResult.getClassesWithMethodAnnotation()`**: ClassGraph's index also includes meta-annotations and subclasses (a different set from `hasDeclaredMethodAnnotation`) and may differ for JSON-loaded results. At most it could serve as a verified superset filter.
- **Parallel `findPreviewsFor` across classes**: That affects ordering and the thread-safety of user-supplied mappers and predicates.
- **ClassGraph scan-spec tweaks** (disabling jar/module scanning, etc.) or **closing `ScanResult` in `ScanResultProcessor`** (it's reused across `dump*` calls).

---

## 4. Suggested Rollout and Verification

1. **Baseline first**: Run `:benchmark` (`kotlinx-benchmark`). Add a compiled-class / `setTargetSourceSet` scenario and a `-prof gc` (allocation rate) run, since S1–S3 mostly affect that path.
2. **Implement in order**: $\text{S1} \to \text{S2} \to \text{S3} \to \text{S7/A6} \to \text{S4/S6} \to \text{S5} \to \text{the rest}$. Use one PR per item so diffs stay reviewable and benchmark deltas can be attributed to each change.
3. **Regression safety**: `:core` has no `src/test`. Rely on the `:tests`, `:paparazzi-plugin-tests`, `:android`/`:wear`/`:glance` suites. Also add a golden test that checks that the ordered list of `ComposablePreview.toString()` ids and screenshot ids is identical before and after each change.
