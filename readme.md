# 📦 StockScout

StockScout is a lightweight Android inventory application built to demonstrate offline-first architecture, alias-based item resolution, and reliable background syncing.

It simulates real-world warehouse workflows where items can have multiple identifiers (UPC, EAN, GS1, etc.) and operations must continue even without network connectivity.

## Screenshots

<div align="center">
  <img src="https://github.com/user-attachments/assets/5694e69d-4af7-4590-a0be-943fbc08c169" alt="Splash Screen" width="220" title="Sync Done">
  <img src="https://github.com/user-attachments/assets/c005aae8-4863-41ee-8c82-4748fec0822e" alt="List" width="220" title="Splash">
  <img src="https://github.com/user-attachments/assets/e3e9e02a-939d-4355-abd0-691d8f810b01" alt="Scanner" width="220" title="List">
 <img src="https://github.com/user-attachments/assets/2f94841b-0628-470f-b776-0ab2dd4bff68" alt="Pending Picks" width="220" title="Scanner">
  <img src="https://github.com/user-attachments/assets/d8528945-2958-4aab-93db-438485c45c71" alt="Sync Done" width="220" title="Pending Picks">
</div>

## Video Demo

* Offline Queue & Auto Sync Demonstration
* This video shows StockScout’s offline-first sync mechanism in detail:
  * Picks made while offline are stored locally and queued for synchronization.
  * They persist across app restarts and are automatically uploaded when network connectivity returns via WorkManager.
    



https://github.com/user-attachments/assets/36330f3c-dcd0-490b-8927-d8fdaf09a6ce







## 🚀 Features
### 🔄 Offline-first data handling:
* Items are cached locally using Room
* App remains functional without internet

### 🔍 Flexible item search
* Users can search by:
  * Item code
  * Barcode (UPC, EAN, GS1)
  * Custom aliases
  * Partial matches
  
### 📷 Barcode scanning:
* Powered by CameraX + ML Kit
* Supports multiple barcode formats
  
### 📊 Real-time item details:
View stock levels, unit of measure, and aliases

### Pick operation:
* Decrements stock instantly
* Queues operation for background sync
  
### 🔁 Reliable background sync:
* WorkManager handles retries with exponential backoff
* Sync status is always visible to the user
  
## 🧭 App Flow
### 1. Splash Screen:

* Launch point: SplashFragment
* Triggers initial data sync:
  
  ```
  itemRepository.syncFromRemote()
  ```

* Outcomes:
Scenario	Behavior

  ✅ Success	Navigate to Item List with fresh data

  ❌ Failure	Show error (2s) → Navigate anyway (uses cached data)

### 2. Item List Screen:
* Observes Room database using Flow
* Fully reactive UI (no manual refresh)
* Search Options
* Manual input (keyboard search or button)
* Barcode scan → returns via SavedStateHandle

* Behavior:

  Match found → Navigate to detail screen

  No match → Show "Item not found"

### 3. Item Detail Screen

* Displays:
  * Item name
  * Unit of measure (UOM)
  * On-hand quantity
  * Alias list
  * Sync status badge
  * Record Pick
    
* When user taps Record Pick:
  * Quantity is decremented locally (instant UI update)
  * Pick is stored in PendingPickEntity
  * WorkManager job is scheduled
    
* 🔄 Sync Behavior:
  * Uses WorkManager
  * Constraints: Network required
  * Retry strategy: Exponential backoff
    
* Sync Status Indicator:
  
  ✅ All picks synced
  
  ⏳ Pending picks count
  
  🔎 Search & Alias Resolution
  
* The resolver matches input against:

  Item code (exact + partial)

  Alias values (all types)
  
* Matching Strategy:
  
  Uses contains, not strict equality
  
  First match wins
  
* GS1 Barcode Handling:
  
  Parses GS1 string
  
  Extracts GTIN (AI 01)
  
  Uses GTIN for lookup instead of raw input
  
* 📷 Barcode Scanning:
  * CameraX for lifecycle-aware camera handling
  * ML Kit for barcode recognition
  * Supported Formats
    * UPC-A
    * EAN-13
    * GS1-128
    * QR Codes
    * Others (multi-format support)

* Why CameraX?
  * Cleaner Jetpack integration
  * No manual camera handling required

    
## 🌐 Mock API

Hosted on mockapi.io

* Method
  * GET:
    * https://69f8e100f7044aa0103e98d7.mockapi.io/items
    * Fetch item master data

  * POST:
    * https://69f8e100f7044aa0103e98d7.mockapi.io/picks
    * Record pick operation
      
## 🧱 Tech Stack
* Layer	Technology
  * UI	Fragments
  * ViewBinding
  * Navigation Component
  * SafeArgs
* State	ViewModel + StateFlow
* Local DB	Room
* Networking	Retrofit + Gson
* Camera	CameraX
* Barcode	ML Kit
* Background Jobs	WorkManager
* DI	Manual (AppContainer)
  
## 🏗️ Architecture Overview
* MVVM + Repository pattern
* Single source of truth → Room DB
* UI observes local data only
* Remote sync is decoupled via repository + workers
