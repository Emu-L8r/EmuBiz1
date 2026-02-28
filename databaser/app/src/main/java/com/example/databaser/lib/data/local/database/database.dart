
import 'package_drift/drift.dart';
import 'dart_io';

part 'database.g.dart';

// --- Table Definitions ---

class BusinessProfiles extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get businessName => text()();
  TextColumn get abnOrAcn => text()();
  TextColumn get contactInfo => text()();
  TextColumn get bankingDetailsBsb => text()();
  TextColumn get bankingDetailsAcc => text()();
  BoolColumn get isGstRegistered => boolean().withDefault(const Constant(false))();
}

class Customers extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text()();
  TextColumn get businessName => text().nullable()();
  TextColumn get abnOrAcn => text().nullable()();
  TextColumn get email => text()();
  TextColumn get phone => text()();
  TextColumn get address => text()();
}

@DataClassName('Document')
class Documents extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get customerId => integer().references(Customers, #id, onDelete: KeyAction.restrict)();
  TextColumn get type => text()(); // "Quote" or "Invoice"
  TextColumn get referenceNumber => text()();
  TextColumn get status => text()(); // "Draft", "Sent", "Paid"
  DateTimeColumn get createdAt => dateTime()();
  DateTimeColumn get updatedAt => dateTime()();
  RealColumn get gstAmount => real().withDefault(const Constant(0.0))();
  RealColumn get totalAmount => real()();
}

class LineItems extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get documentId => integer().references(Documents, #id, onDelete: KeyAction.cascade)();
  TextColumn get description => text()();
  IntColumn get quantity => integer()();
  RealColumn get unitPrice => real()();
  RealColumn get subtotal => real()();
}

class Notes extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get customerId => integer().references(Customers, #id, onDelete: KeyAction.cascade)();
  TextColumn get text => text()();
  DateTimeColumn get createdAt => dateTime()();
}

// --- Database Class ---

@DriftDatabase(tables: [BusinessProfiles, Customers, Documents, LineItems, Notes])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'db.sqlite'));
    return NativeDatabase(file);
  });
}
