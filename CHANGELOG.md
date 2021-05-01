Change Log
==========

Version 1.0.0-alpha67 *(2021-04-22)*
------------------------------------
- Fix: Crash after entering 24th seed word on certain devices
- Fix: Correct errors on certain devices around biometrics.
- Fix: Display information when invalid addresses are scanned.
- Fix: Prevent memo for transparent transactions [Credit @mandeepbhalothia].
- Fix: Security finding: remove aparent logging [Credit @mandeepbhalothia].
- New: View more info for failed sends [Credit @herou].
- New: Switch away from google libraries for QR code parsing [Credit @herou].
- New: Ability to make testnet releases [Credit @herou].
- New: Updated design for wallet history [Credit @mandeepbhalothia].
- New: Added responsible disclosure document for vulnerabilities [Credit: @zebambam]
- New: Update to latest SDK changes, including using one library instead of two.
- New: Auto-shielding Easter Egg.
- New: Periodically solicit user feedback.
- New: Wallet restore improvements including 'clear' feature.
- New: Rescan/Wipe feature for fixing wallet errors
- New: Add ability to copy the contents of the memo.
- New: Add ability to paste and parse an entire seed phrase at once
- New: Basic support for QRs prefixed with zcash:
- New: Made it easier to add vibration and leveraged it whenever text is copied.
- New: Adds cleanup and removal of failed transactions.
- New: Improved logic for determining the wallet birthday.
- New: Capture performance metrics for scanning.
- New: Additional messaging during a scan for better responsiveness.
- New: Improved handling of critical errors.

Version 1.0.0-alpha43 *(2020-12-20)*
------------------------------------
- Fix: Repaired the upgrade flow, which could not reorg because of missing birthday height
- Fix: Repaired create wallet flow which was being covered by the loading screen

Version 1.0.0-alpha42 *(2020-12-19)*
------------------------------------
- Fix: Correct race condition when launching the app
- Fix: Display loading screen while waiting for app to initialize

Version 1.0.0-alpha41 *(2020-12-19)*
------------------------------------
- New: Upgrade to the latest SDK.
- New: Implements ZIP-313, reducing the default fee from 10,000 to 1,000 zats.
- New: Adds authentication prior to viewing backup seed words.
- New: Adds blockchair as the transaction explorer.
- Fix: Authentication bugs on older devices that were preventing sends and mishandling cancels.
- Fix: Users can now upgrade from seed-only prior versions without crashing or needing to restore.
- Fix: Improved internal metrics for troubleshooting issues.

Version 1.0.0-alpha37 *(2020-10-07)*
------------------------------------
- New: Localization in 5 languages Russian, Italian, Spanish, Chinese and Korean.
- New: Store and sync using just the ViewingKey.
- New: Added QA build flavor for better testing.
- New: Ability to change servers (thanks @Nighthawk!)
- Fix: Critical bug in 3rd-party secure storage library impacting large strings.
- Fix: Devices without PIN can use the wallet again.
- Fix: Developer logs now work on all devices.

Version 1.0.0-alpha34 *(2020-08-28)*
------------------------------------
- New: Implemented transaction detail view.
- New: Updated receive screen and scan screen.
- New: Added optional blockchain explorer with privacy warning.
- Fix: Update key dependencies for performance.
- Fix: Iterated on send flow with lots of improvements and fixes.
- Fix: Trim improperly parsed characters from memos.
- Fix: Keypad stops working when navigating back to home screen.
- Fix: Prevent black screen after failed initialization.

Version 1.0.0-alpha33 *(2020-08-13)*
------------------------------------
- New: Fully removed crashlytics, in favor of bugsnag.
- New: Change the default lightwalletd server.
- New: Switched to the latest SDK.

Version 1.0.0-alpha32 *(2020-08-01)*
------------------------------------
- New: entirely revamped send flow
- New: added biometric authentication support
- New: add robust support for tx cancellation
- New: support precise birthday heights for faster restore
- New: switched to Reply-To standard for memos
- New: improved feedback while scanning QRs
- New: more compatible with memo reply-to formats
- New: update to latest librustzcash crates
- New: checkpoints
- Fix: amount not clearing on return to home screen
- Fix: address cursor resetting while typing
- Fix: app crash when opening application logs
- Fix: limit decimal places to 8 places
- Fix: wallet history now scrolls to the top
- Fix: consistent currency formatting
- Fix: security finding around compromised file system

Version 1.0.0-alpha31 *(2020-06-11)*
------------------------------------
- Source code now available on github!
- New: Improved mnemonic phrase handling and correctness
- Fix: mitigated several security findings
- New: Integrated with latest SDK, now available on jcenter
- New: Improved error handling in several areas
- New: Built-in support for the Heartwood consensus branch
- Fix: Wallet details screen now refreshes data
- Fix: Seed phrase display error

Version 1.0.0-alpha29 *(2020-06-10)*
------------------------------------
- Fix: Removed 3rd party mnemonic library due to restrictive license
- New: Verify checksum for imported mnemonics and warn user
- Fix: Validate address to mitigate security finding
- New: Integrated with latest SDK, now available on jcenter
- New: Improved error handling in several areas
- New: Updated all dependencies
- New: Built-in support for the Heartwood consensus branch
- Fix: Wallet details screen was not refreshing values
- Fix: Polling interval vulnerability

Version 1.0.0-alpha25 *(2020-03-27)*
------------------------------------
- New: added full memo support
- New: added feedback screen and related logging
- New: added full wallet restore, including outbound txs, outbound recipients and inbound memos
- New: show sender address in details list, when we can parse it from the memo
- New: long press transaction details to copy related address
- New: clarified UI for pending transactions
- New: improved the handling of disconnected state
- New: improved the behavior when returning from the background
- New: changed doc format to html instead of markdown
- New: iterated on button styles, including intial send button animation
- Fix: last digit of amount no longer lingers when returning to home screen
- Fix: database migration issues in certain versions
- Fix: added more detailed logs around network failures for future troubleshooting
- Fix: avoid negative numbers in the UI
- Fix: occasional crashes while closing the camera
- New: Simplified some SDK APIs so they are easier to use
- New: added more checkpoints so new wallets initialize faster

Version 1.0.0-alpha23 *(2020-02-21)*
------------------------------------
- Fix: reorg improvements, squashing critical bugs that disabled wallets
- New: extend analytics to include taps, screen views, and send flow.
- New: add crash reporting via Crashlytics.
- New: expose user logs and developer logs as files.
- New: improve feature for creating checkpoints.
- New: added DB schemas to the repository for tracking.
- Fix: numerous bug fixes, test fixes and cleanup.
- New: improved error handling and user experience

Version 1.0.0-alpha17 *(2020-02-07)*
------------------------------------
- New: implemented wallet import
- New: display the memo when tapping outbound transactions
- Fix: removed the sad zebra and softened wording for sending z->t
- Fix: removed restriction on smallest sendable ZEC amount
- Fix: removed "fund now"
- New: turned on developer logging to help with troubleshooting
- New: improved wallet details ability to handle small amounts of ZEC
- New: added ability to clear the memo
- Fix: changed "SEND WITHOUT MEMO" to "OMIT MEMO"
- Fix: corrected wording when the address is included in the memo
- New: display the approximate wallet birthday with the backup words
- New: improved crash reporting
- Fix: fixed bug when returning from the background
- New: added logging for failed transactions
- New: added logic to verify setup and offer explanation when the wallet is corrupted
- New: refactored and improved wallet initialization
- New: added ability to contribute 'plugins' to the SDK
- New: added tons more checkpoints to reduce startup/import time
- New: exposed logic to derive addresses directly from seeds
- Fix: fixed several crashes

Version 1.0.0-alpha11 *(2020-01-15)*
------------------------------------
- Initial ECC release

Version 1.0.0-alpha03 *(2019-12-18)*
------------------------------------
- Initial internal wallet team release
