/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classyfts;

import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import au.com.cybersearch2.classyfts.FtsEngine.FtsStatus;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * FtsDatabaseTest
 * @author Andrew Bowley
 * 04/07/2014
 */
@RunWith(RobolectricTestRunner.class)
public class FtsEngineTest
{
    class TestFtsEngine extends FtsEngine
    {
        boolean callRealLoad;
        
        public TestFtsEngine(String targetTable,
                Map<String, String> targetColumnMap)
        {
            super(testFtsOpenHelper, targetTable, targetColumnMap);
            
        }

        @Override
        protected FtsStatus loadDictionary(SQLiteDatabase targetDb, SQLiteDatabase lexDb) 
        {
            if (callRealLoad)
                return super.loadDictionary(targetDb, lexDb);
            return FtsStatus.Loaded;
        }

        @Override
        protected SQLiteQueryBuilder getSQLiteQueryBuilder()
        {
            return sqLiteQueryBuilder;
        }
}

    class TestWordFilter implements WordFilter
    {
        Transcript transcript = new Transcript();
        
        @Override
        public String filter(String key, String word) 
        {
            transcript.add(key + "=" + word);
            return " " + word + " FILTERED ";
        }
        
    }
    
    Map<String,String> COLUMN_MAP;
    FtsEngine ftsEngine;
    static FtsOpenHelper testFtsOpenHelper;
    TestWordFilter text1Filter;
    TestWordFilter text2Filter;
    SQLiteQueryBuilder sqLiteQueryBuilder;
    
    
    @Before
    public void setUp()
    {
        if (testFtsOpenHelper == null)
            testFtsOpenHelper = mock(FtsOpenHelper.class);
        sqLiteQueryBuilder = mock(SQLiteQueryBuilder.class);
        reset(testFtsOpenHelper);
        COLUMN_MAP = new HashMap<String,String>();
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "title");
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "model");
        COLUMN_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "_id");
        ftsEngine = new TestFtsEngine("all_nodes", COLUMN_MAP);
        COLUMN_MAP = null; // Prove the engine makes a copy of the map
        text1Filter = new TestWordFilter();
        text2Filter = new TestWordFilter();
        ftsEngine.setText1Filter(text1Filter);
        ftsEngine.setText2Filter(text2Filter);
    }

    @Test
    public void test_FtsEngine_constructor()
    {
        assertThat(ftsEngine.ftsOpenHelper).isNotNull();
        assertThat(ftsEngine.getStatus()).isEqualTo(FtsStatus.Initial);
        assertThat(ftsEngine.SOURCE_COLUMN_MAP.size()).isEqualTo(3);
        assertThat(FtsEngine.COLUMN_MAP.size()).isEqualTo(5);
    }

    @Test
    public void test_FtsEngine_query()
    {
        Cursor cursor = mock(Cursor.class);
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] { "23" };
        ArgumentCaptor<String[]> columnsArg = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> orderByArg = ArgumentCaptor.forClass(String.class);
        SQLiteDatabase dictionaryDb = mock(SQLiteDatabase.class);
        when(testFtsOpenHelper.getReadableDictionaryDatabase()).thenReturn(dictionaryDb);
        // columns, selection, selectionArgs, null, null, orderBy, Integer.toString(limit)
        when(sqLiteQueryBuilder.query(eq(dictionaryDb), columnsArg.capture(), eq(selection), eq(selectionArgs), isNull(String.class), isNull(String.class), orderByArg.capture(), eq("50"))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        ftsEngine.setOrderbyText2(true);
        assertThat(ftsEngine.query(selection, selectionArgs, null, 50)).isEqualTo(cursor);
        assertThat(columnsArg.getValue()).isNull();
        assertThat(orderByArg.getValue()).isEqualTo(SearchManager.SUGGEST_COLUMN_TEXT_2 + " ASC, " + SearchManager.SUGGEST_COLUMN_TEXT_1 + " ASC");
    }
    
    @Test
    public void test_FtsEngine_initialize()
    {
        SQLiteDatabase ftsDb = mock(SQLiteDatabase.class);
        Cursor countCursor = mock(Cursor.class);
        SQLiteDatabase targetDb = mock(SQLiteDatabase.class);
        when(ftsDb.getVersion()).thenReturn(1);
        when(testFtsOpenHelper.getWritableDictionaryDatabase()).thenReturn(ftsDb);
        when(ftsDb.rawQuery(eq("select count(*) from FTSlexicon"), isNull(String[].class))).thenReturn(countCursor);
        when(countCursor.moveToFirst()).thenReturn(true);
        when(countCursor.getColumnIndex("count(*)")).thenReturn(0);
        when(countCursor.getInt(0)).thenReturn(0);
        when(testFtsOpenHelper.getReadableDatabase()).thenReturn(targetDb);
        assertThat(ftsEngine.initialize()).isEqualTo(FtsStatus.Loaded);
        verify(countCursor).close();
    }
    
    @Test
    public void test_FtsEngine_initialize_null_cursor()
    {
        SQLiteDatabase ftsDb = mock(SQLiteDatabase.class);
        Cursor countCursor = null;
        when(ftsDb.getVersion()).thenReturn(1);
        when(testFtsOpenHelper.getWritableDictionaryDatabase()).thenReturn(ftsDb);
        when(ftsDb.rawQuery(eq("select count(*) from FTSlexicon"), isNull(String[].class))).thenReturn(countCursor);
        assertThat(ftsEngine.initialize()).isEqualTo(FtsStatus.Error);
    }
    
    @Test
    public void test_FtsEngine_initialize_empty_cursor()
    {
        SQLiteDatabase ftsDb = mock(SQLiteDatabase.class);
        Cursor countCursor = null;
        when(ftsDb.getVersion()).thenReturn(1);
        when(testFtsOpenHelper.getWritableDictionaryDatabase()).thenReturn(ftsDb);
        when(ftsDb.rawQuery(eq("select count(*) from FTSlexicon"), isNull(String[].class))).thenReturn(countCursor);
        assertThat(ftsEngine.initialize()).isEqualTo(FtsStatus.Error);
    }
    
    @Test
    public void test_FtsEngine_initialize_data_loaded()
    {
        SQLiteDatabase ftsDb = mock(SQLiteDatabase.class);
        Cursor countCursor = mock(Cursor.class);
        when(ftsDb.getVersion()).thenReturn(1);
        when(testFtsOpenHelper.getWritableDictionaryDatabase()).thenReturn(ftsDb);
        when(ftsDb.rawQuery(eq("select count(*) from FTSlexicon"), isNull(String[].class))).thenReturn(countCursor);
        when(countCursor.moveToFirst()).thenReturn(true);
        when(countCursor.getColumnIndex("count(*)")).thenReturn(0);
        when(countCursor.getInt(0)).thenReturn(1000);
        assertThat(ftsEngine.initialize()).isEqualTo(FtsStatus.Loaded);
    }
    
    @Test
    public void test_FtsEngine_loadDictionary()
    {
        int ID1 = 14;
        int ID2 = 15;
        SQLiteDatabase ftsDb = mock(SQLiteDatabase.class);
        SQLiteDatabase targetDb = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(targetDb.rawQuery(eq("select _id, title, model from all_nodes"), isNull(String[].class))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getColumnIndex("_id")).thenReturn(0);
        when(cursor.getInt(0)).thenReturn(ID1, ID2);
        when(cursor.getColumnIndex("title")).thenReturn(1);
        when(cursor.getString(1)).thenReturn("text1", "text3");
        when(cursor.getColumnIndex("model")).thenReturn(2);
        when(cursor.getString(2)).thenReturn("text2", "text4");
        ArgumentCaptor<ContentValues> contentValuesArg = ArgumentCaptor.forClass(ContentValues.class);
        when(ftsDb.insert(eq("FTSlexicon"), isNull(String.class), contentValuesArg.capture())).thenReturn(1l);
        when(cursor.moveToNext()).thenReturn(true, false);
        ((TestFtsEngine)ftsEngine).callRealLoad = true;
        assertThat(ftsEngine.loadDictionary(targetDb, ftsDb)).isEqualTo(FtsStatus.Loaded);
        text1Filter.transcript.assertEventsSoFar("title=text1", "title=text3");
        text2Filter.transcript.assertEventsSoFar("model=text2", "model=text4");
        ContentValues contentValues = contentValuesArg.getAllValues().get(0);
        assertThat(contentValues.getAsInteger(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID)).isEqualTo(ID1);
        assertThat(contentValues.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_1)).isEqualTo( "text1 FILTERED");
        assertThat(contentValues.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2)).isEqualTo( "text2 FILTERED");
        contentValues = contentValuesArg.getAllValues().get(1);
        assertThat(contentValues.getAsInteger(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID)).isEqualTo(ID2);
        assertThat(contentValues.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_1)).isEqualTo( "text3 FILTERED");
        assertThat(contentValues.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2)).isEqualTo( "text4 FILTERED");
        verify(cursor).close();
    }

    @Test
    public void test_FtsEngine_loadDictionary_insert_fail()
    {
        int ID1 = 14;
        int ID2 = 15;
        SQLiteDatabase ftsDb = mock(SQLiteDatabase.class);
        SQLiteDatabase targetDb = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(targetDb.rawQuery(eq("select _id, title, model from all_nodes"), isNull(String[].class))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getColumnIndex("_id")).thenReturn(0);
        when(cursor.getInt(0)).thenReturn(ID1, ID2);
        when(cursor.getColumnIndex("title")).thenReturn(1);
        when(cursor.getString(1)).thenReturn("text1", "text3");
        when(cursor.getColumnIndex("model")).thenReturn(2);
        when(cursor.getString(2)).thenReturn("text2", "text4");
        ArgumentCaptor<ContentValues> contentValuesArg = ArgumentCaptor.forClass(ContentValues.class);
        when(ftsDb.insert(eq("FTSlexicon"), isNull(String.class), contentValuesArg.capture())).thenReturn(-1l);
        ((TestFtsEngine)ftsEngine).callRealLoad = true;
        assertThat(ftsEngine.loadDictionary(targetDb, ftsDb)).isEqualTo(FtsStatus.Error);
        text1Filter.transcript.assertEventsSoFar("title=text1");
        text2Filter.transcript.assertEventsSoFar("model=text2");
        ContentValues contentValues = contentValuesArg.getValue();
        assertThat(contentValues.getAsInteger(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID)).isEqualTo(ID1);
        assertThat(contentValues.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_1)).isEqualTo( "text1 FILTERED");
        assertThat(contentValues.getAsString(SearchManager.SUGGEST_COLUMN_TEXT_2)).isEqualTo( "text2 FILTERED");
        verify(cursor).close();
    }
}
