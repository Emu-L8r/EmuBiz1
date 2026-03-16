# Privacy Policy — Bizap

**Last Updated:** March 16, 2026  
**App Version:** 1.0.0  
**Developer:** Emu-L8r  
**Contact:** [Your contact email]

---

## 1. Introduction

This Privacy Policy describes how Bizap ("the App", "we", "our") handles your information. We are committed to protecting your privacy and being transparent about what data the App collects, stores, and transmits.

**Summary:** Bizap is a local-only app. Your business data never leaves your device and is never sent to our servers. We have no servers to send it to.

---

## 2. Data We Collect and Store

### 2.1 Data Stored Locally on Your Device

The App stores the following data **exclusively on your device**:

| Data Type | Purpose | Storage Location |
|-----------|---------|-----------------|
| Business profile (name, ABN, address, bank details) | Populating invoices | Local SQLite database (encrypted) |
| Customer records (names, emails, phone numbers, addresses) | Invoice recipients | Local SQLite database (encrypted) |
| Invoices and line items | Core app functionality | Local SQLite database (encrypted) |
| Payment records | Tracking invoice payments | Local SQLite database (encrypted) |
| App preferences (theme, GUI selection) | User experience | Android DataStore (local) |
| App PIN | Local authentication | Android Keystore (device-level hardware security) |

**This data is never transmitted to any server, cloud service, or third party controlled by us.**

### 2.2 Encryption

All data stored in the SQLite database is encrypted using:
- **SQLCipher** (AES-256 encryption)
- **Android Keystore** (hardware-backed key storage for the database passphrase)

Your data is encrypted at rest on your device.

---

## 3. Data We Do NOT Collect

We do **not** collect, store, or have access to:
- Your invoice data, customer data, or financial records
- Your business name, ABN, or bank account details
- Any personally identifiable information (PII) about you or your customers
- Your device's contact list, photos, calendar, or location
- Your IP address or browsing history

We have no servers and no database that stores your app data. We literally cannot access your data even if we wanted to.

---

## 4. Third-Party Services

The App uses the following third-party services that may collect limited, anonymous data:

### 4.1 Firebase Crashlytics (Google LLC)

- **Purpose:** Crash reporting to help us identify and fix app bugs
- **Data Collected:** Anonymous crash reports including stack traces, device model, OS version, and app version. **No personally identifiable information is collected.**
- **What is NOT collected:** Your invoice data, customer data, or any business information
- **Data Storage:** Processed and stored by Google on their servers
- **Opt-Out:** You can opt out by disabling the App's crash reporting (see Settings, if available, or uninstall)
- **Google's Privacy Policy:** https://policies.google.com/privacy
- **Firebase Privacy:** https://firebase.google.com/support/privacy

### 4.2 Exchange Rate API

- **Purpose:** Fetching current currency exchange rates for multi-currency invoicing
- **Data Collected:** None. The API request contains only the request for exchange rate data; no user data is transmitted.
- **When Used:** Only when you create an invoice with a foreign currency

---

## 5. Data You Export

When you export invoices as CSV or PDF files:
- The exported file is saved to your device's storage
- You control where you share or store these exports
- We have no access to exported files

---

## 6. No Cloud Storage

**Version 1.0 of Bizap has no cloud storage, no sync, and no remote database.**

All data lives exclusively on the device where the App is installed. There is no account system, no login to any server, and no automatic backup. **If you lose your device or uninstall the App, your data is permanently gone.**

We strongly recommend regularly exporting your data as CSV files and storing copies in a safe location (e.g., your email, cloud storage of your choice, or a computer).

---

## 7. Your Rights and Control Over Your Data

Since all data is stored locally on your device, you have full control:

| Right | How to Exercise |
|-------|----------------|
| **Access** | Open the App and view all your data |
| **Export** | Export invoices as CSV or PDF from the invoice detail screen |
| **Delete** | Delete individual records within the App, or uninstall to delete all data |
| **Portability** | Export all invoices as CSV files at any time |

**Uninstalling the App permanently and irrecoverably deletes all your data.** There is no way to recover data after uninstallation.

---

## 8. Children's Privacy

The App is not directed at children under 13 years of age. We do not knowingly collect any information from children under 13. If you are under 13, please do not use the App.

---

## 9. Data Security

We take reasonable measures to protect your data:
- **AES-256 encryption** for all stored data via SQLCipher
- **Android Keystore** for secure key storage (hardware-backed where available)
- **Local PIN authentication** to prevent unauthorized device access
- **No network transmission** of your business data

However, no security measure is perfect. You are responsible for maintaining the physical security of your device and keeping your PIN confidential.

---

## 10. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. The updated policy will be posted with a new "Last Updated" date. Your continued use of the App after changes constitutes your acceptance of the updated policy. We recommend reviewing this policy periodically.

For significant changes that affect your rights, we will attempt to notify you through the App.

---

## 11. Contact Us

If you have questions or concerns about this Privacy Policy or how we handle data, please contact us:

**Email:** [Your contact email]  
**Developer:** Emu-L8r

---

## 12. Compliance

This Privacy Policy is intended to comply with:
- **GDPR** (General Data Protection Regulation — EU)
- **Australian Privacy Act 1988** and the Australian Privacy Principles (APPs)
- **Google Play Store** requirements for app privacy disclosures

---

*By using Bizap, you confirm that you have read and understood this Privacy Policy.*
