package com.emul8r.bizap.data.local
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.charset.Charset
import kotlin.test.assertFalse
import kotlin.test.assertTrue
@RunWith(AndroidJUnit4::class)
class EncryptionTest {
    private lateinit var appContext: Context
    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        appContext.deleteDatabase("test-encrypted-db")
        appContext.deleteDatabase("test-wrong-passphrase-db")
        appContext.deleteDatabase("test-correct-passphrase-db")
    }
    @After
    fun tearDown() {
        appContext.deleteDatabase("test-encrypted-db")
        appContext.deleteDatabase("test-wrong-passphrase-db")
        appContext.deleteDatabase("test-correct-passphrase-db")
    }
    @Test
    fun testDatabaseFileIsEncrypted() {
        val passphrase = "test-passphrase-32-bytes-long-------".toByteArray()
        val database = Room.databaseBuilder(appContext, AppDatabase::class.java, "test-encrypted-db")
            .openHelperFactory(SupportOpenHelperFactory(passphrase)).build()
        try { database.invoiceDao().observeAll().toString() } catch (e: Exception) {}
        database.close()
        val dbFile = File(appContext.getDatabasePath("test-encrypted-db").absolutePath)
        assertTrue("Database file should exist", dbFile.exists())
        val header = ByteArray(20)
        dbFile.inputStream().use { it.read(header) }
        val headerString = String(header, Charset.forName("ASCII"))
        assertFalse("Database should be encrypted", headerString.startsWith("SQLite format"))
        println("? TEST 1 PASSED: Database file is encrypted")
    }
    @Test
    fun testWrongPassphraseThrows() {
        val correctPassphrase = "correct-passphrase-32-bytes-long------".toByteArray()
        val wrongPassphrase = "wrong-passphrase-32-bytes-long---------".toByteArray()
        val database = Room.databaseBuilder(appContext, AppDatabase::class.java, "test-wrong-passphrase-db")
            .openHelperFactory(SupportOpenHelperFactory(correctPassphrase)).build()
        try { database.invoiceDao().observeAll().toString() } catch (e: Exception) {}
        database.close()
        val wrongDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, "test-wrong-passphrase-db")
            .openHelperFactory(SupportOpenHelperFactory(wrongPassphrase)).build()
        var exceptionThrown = false
        try { wrongDatabase.invoiceDao().observeAll().toString() } catch (e: Exception) { exceptionThrown = true }
        wrongDatabase.close()
        assertTrue("Wrong passphrase should throw exception", exceptionThrown)
        println("? TEST 2 PASSED: Wrong passphrase throws exception")
    }
    @Test
    fun testCorrectPassphraseOpens() {
        val passphrase = "correct-passphrase-32-bytes-long------".toByteArray()
        val database1 = Room.databaseBuilder(appContext, AppDatabase::class.java, "test-correct-passphrase-db")
            .openHelperFactory(SupportOpenHelperFactory(passphrase)).build()
        try { database1.invoiceDao().observeAll().toString() } catch (e: Exception) {}
        database1.close()
        val database2 = Room.databaseBuilder(appContext, AppDatabase::class.java, "test-correct-passphrase-db")
            .openHelperFactory(SupportOpenHelperFactory(passphrase)).build()
        var querySucceeded = false
        try { database2.invoiceDao().observeAll().toString(); querySucceeded = true } catch (e: Exception) {}
        database2.close()
        assertTrue("Correct passphrase should open database", querySucceeded)
        println("? TEST 3 PASSED: Database opens successfully")
    }
}
