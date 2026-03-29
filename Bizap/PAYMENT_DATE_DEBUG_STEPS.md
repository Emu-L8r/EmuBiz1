# Steps to capture logs and debug the payment date issue

## 1. Clear logcat
```bash
adb logcat -c
```

## 2. Open the app and reproduce the issue
1. Open Bizap
2. Go to Modern Interface (GUI2)
3. Create a NEW invoice (or select existing one)
4. Click Payment icon
5. WAIT for the dialog to fully appear
6. Try to enter an amount (e.g., 50)
7. Observe the error message

## 3. Capture the logs
```bash
adb logcat | grep "RecordPaymentViewModel" > payment_logs.txt
```

## 4. What to look for in the logs:
```
RecordPaymentViewModel INIT:
  Invoice ID: 
  Invoice Date (raw): 
  Invoice Date (midnight): 
  Default Payment Date: 
  Outstanding: 
  Dates equal? 
  Payment >= Invoice? 

Date validation: paymentDate=..., invoiceDate=...
```

The key is to compare these numbers:
- If "Dates equal? true" → Should work, dates match
- If "Payment >= Invoice? true" → Should work, payment is same day or later
- If both are false → We have a problem that needs investigation

## 5. Share the output
Please share the logcat output when you reproduce the issue, and I can see exactly what dates are being compared and why the validation is failing.

