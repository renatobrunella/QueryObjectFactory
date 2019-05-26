/*
 * Copyright 2007 - 2010 brunella ltd
 *
 * Licensed under the LGPL Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.co.brunella.qof.codegen;

import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.session.DefaultSessionRunner;
import uk.co.brunella.qof.session.SessionPolicy;
import uk.co.brunella.qof.session.TransactionRunnable;

/**
 * Internal - holds constants for type and signature definitions.
 */
public final class Constants {

  private Constants() {
  }

  public static final String FIELD_NAME_DEFAULT_FETCH_SIZE = "DEFAULT_FETCH_SIZE";
  public static final String FIELD_NAME_DEFAULT_BATCH_SIZE = "DEFAULT_BATCH_SIZE";
  public static final String EXCEPTION_COLLECTIONS_DIFFERENT_SIZE = "Input collections have different size";
  public static final String EXCEPTION_EMPTY_RESULT = "Empty result set returned";
  public static final String EXCEPTION_MORE_THAN_ONE_RESULT = "More than one result in result set";
  
  public static final String FIELD_NAME_BATCH_SIZE = "batchSize";
  public static final String FIELD_NAME_FETCH_SIZE = "fetchSize";

  public static final String FIELD_NAME_FIRST_RESULT = "firstResult";
  public static final String FIELD_NAME_MAX_RESULTS = "maxResults";

  // types
  public static final Type TYPE_Object = Type.getType("Ljava/lang/Object;");
  public static final Type TYPE_Byte = Type.getType("Ljava/lang/Byte;");
  public static final Type TYPE_Boolean = Type.getType("Ljava/lang/Boolean;");
  public static final Type TYPE_Short = Type.getType("Ljava/lang/Short;");
  public static final Type TYPE_Integer = Type.getType("Ljava/lang/Integer;");
  public static final Type TYPE_Long = Type.getType("Ljava/lang/Long;");
  public static final Type TYPE_Float = Type.getType("Ljava/lang/Float;");
  public static final Type TYPE_Double = Type.getType("Ljava/lang/Double;");
  public static final Type TYPE_Date = Type.getType("Ljava/util/Date;");
  public static final Type TYPE_String = Type.getType("Ljava/lang/String;");
  public static final Type TYPE_Character = Type.getType("Ljava/lang/Character;");
  public static final Type TYPE_byte = Type.getType("B");
  public static final Type TYPE_boolean = Type.getType("Z");
  public static final Type TYPE_short = Type.getType("S");
  public static final Type TYPE_int = Type.getType("I");
  public static final Type TYPE_long = Type.getType("J");
  public static final Type TYPE_float = Type.getType("F");
  public static final Type TYPE_double = Type.getType("D");
  public static final Type TYPE_char = Type.getType("C");

  public static final Type TYPE_intArray = Type.getType("[I");

  public static final Type TYPE_Throwable = Type.getType("Ljava/lang/Throwable;");
  public static final Type TYPE_RuntimeException = Type.getType("Ljava/lang/RuntimeException;");
  public static final Type TYPE_SystemException = Type.getType("Luk/co/brunella/qof/session/SystemException;");

  public static final Type TYPE_Collection = Type.getType("Ljava/util/Collection;");
  public static final Type TYPE_Map = Type.getType("Ljava/util/Map;");
  public static final Type TYPE_Iterator = Type.getType("Ljava/util/Iterator;");
  public static final Type TYPE_List = Type.getType("Ljava/util/List;");
  public static final Type TYPE_ArrayList = Type.getType("Ljava/util/ArrayList;");
  public static final Type TYPE_HashSet = Type.getType("Ljava/util/HashSet;");
  public static final Type TYPE_HashMap = Type.getType("Ljava/util/HashMap;");

  public static final Type TYPE_Connection = Type.getType("Ljava/sql/Connection;");
  public static final Type TYPE_Statement = Type.getType("Ljava/sql/Statement;");
  public static final Type TYPE_PreparedStatement = Type.getType("Ljava/sql/PreparedStatement;");
  public static final Type TYPE_CallableStatement = Type.getType("Ljava/sql/CallableStatement;");
  public static final Type TYPE_ResultSet = Type.getType("Ljava/sql/ResultSet;");
  public static final Type TYPE_SQLException = Type.getType("Ljava/sql/SQLException;");
  public static final Type TYPE_sqlDate = Type.getType("Ljava/sql/Date;");
  public static final Type TYPE_sqlTime = Type.getType("Ljava/sql/Time;");
  public static final Type TYPE_sqlTimestamp = Type.getType("Ljava/sql/Timestamp;");

  public static final Type TYPE_System = Type.getType("Ljava/lang/System;");

  public static final Signature SIG_init = new Signature("init", "()V");

  // methods of Connection/PreparedStament/CallableStatement
  public static final Signature SIG_prepareStatement = new Signature("prepareStatement",
      "(Ljava/lang/String;)Ljava/sql/PreparedStatement;");
  public static final Signature SIG_prepareCall = new Signature("prepareCall",
      "(Ljava/lang/String;)Ljava/sql/CallableStatement;");
  public static final Signature SIG_setNull = new Signature("setNull", "(II)V");
  public static final Signature SIG_setByte = new Signature("setByte", "(IB)V");
  public static final Signature SIG_getByte = new Signature("getByte", "(I)B");
  public static final Signature SIG_getByteNamed = new Signature("getByte", "(Ljava/lang/String;)B");
  public static final Signature SIG_setShort = new Signature("setShort", "(IS)V");
  public static final Signature SIG_getShort = new Signature("getShort", "(I)S");
  public static final Signature SIG_getShortNamed = new Signature("getShort", "(Ljava/lang/String;)S");
  public static final Signature SIG_setInt = new Signature("setInt", "(II)V");
  public static final Signature SIG_getInt = new Signature("getInt", "(I)I");
  public static final Signature SIG_getIntNamed = new Signature("getInt", "(Ljava/lang/String;)I");
  public static final Signature SIG_setLong = new Signature("setLong", "(IJ)V");
  public static final Signature SIG_getLong = new Signature("getLong", "(I)J");
  public static final Signature SIG_getLongNamed = new Signature("getLong", "(Ljava/lang/String;)J");
  public static final Signature SIG_setFloat = new Signature("setFloat", "(IF)V");
  public static final Signature SIG_getFloat = new Signature("getFloat", "(I)F");
  public static final Signature SIG_getFloatNamed = new Signature("getFloat", "(Ljava/lang/String;)F");
  public static final Signature SIG_setDouble = new Signature("setDouble", "(ID)V");
  public static final Signature SIG_getDouble = new Signature("getDouble", "(I)D");
  public static final Signature SIG_getDoubleNamed = new Signature("getDouble", "(Ljava/lang/String;)D");
  public static final Signature SIG_setBoolean = new Signature("setBoolean", "(IZ)V");
  public static final Signature SIG_getBoolean = new Signature("getBoolean", "(I)Z");
  public static final Signature SIG_getBooleanNamed = new Signature("getBoolean", "(Ljava/lang/String;)Z");
  public static final Signature SIG_setString = new Signature("setString", "(ILjava/lang/String;)V");
  public static final Signature SIG_getString = new Signature("getString", "(I)Ljava/lang/String;");
  public static final Signature SIG_getStringNamed = new Signature("getString", "(Ljava/lang/String;)Ljava/lang/String;");
  public static final Signature SIG_setDate = new Signature("setDate", "(ILjava/sql/Date;)V");
  public static final Signature SIG_getDate = new Signature("getDate", "(I)Ljava/sql/Date;");
  public static final Signature SIG_getDateNamed = new Signature("getDate", "(Ljava/lang/String;)Ljava/sql/Date;");
  public static final Signature SIG_setTime = new Signature("setTime", "(ILjava/sql/Time;)V");
  public static final Signature SIG_getTime = new Signature("getTime", "(I)Ljava/sql/Time;");
  public static final Signature SIG_getTimeNamed = new Signature("getTime", "(Ljava/lang/String;)Ljava/sql/Time;");
  public static final Signature SIG_setTimestamp = new Signature("setTimestamp", "(ILjava/sql/Timestamp;)V");
  public static final Signature SIG_getTimestamp = new Signature("getTimestamp", "(I)Ljava/sql/Timestamp;");
  public static final Signature SIG_getTimestampNamed = new Signature("getTimestamp", "(Ljava/lang/String;)Ljava/sql/Timestamp;");

  public static final Signature SIG_wasNull = new Signature("wasNull", "()Z");
  public static final Signature SIG_next = new Signature("next", "()Z");
  public static final Signature SIG_close = new Signature("close", "()V");
  public static final Signature SIG_executeQuery = new Signature("executeQuery", "()Ljava/sql/ResultSet;");
  public static final Signature SIG_execute = new Signature("execute", "()Z");
  public static final Signature SIG_executeUpdate = new Signature("executeUpdate", "()I");
  public static final Signature SIG_executeBatch = new Signature("executeBatch", "()[I");
  public static final Signature SIG_addBatch = new Signature("addBatch", "()V");
  public static final Signature SIG_setFetchSize = new Signature("setFetchSize", "(I)V");
  public static final Signature SIG_registerOutParameter = new Signature("registerOutParameter", "(II)V");

  public static final Signature SIG_byteValue = new Signature("byteValue", "()B");
  public static final Signature SIG_shortValue = new Signature("shortValue", "()S");
  public static final Signature SIG_intValue = new Signature("intValue", "()I");
  public static final Signature SIG_longValue = new Signature("longValue", "()J");
  public static final Signature SIG_floatValue = new Signature("floatValue", "()F");
  public static final Signature SIG_doubleValue = new Signature("doubleValue", "()D");
  public static final Signature SIG_booleanValue = new Signature("booleanValue", "()Z");

  public static final Signature SIG_getTimeLong = new Signature("getTime", "()J");

  public static final Signature SIG_Constructor_int = TypeUtils.parseConstructor("int");
  
  public static final Signature SIG_size = new Signature("size", "()I");
  public static final Signature SIG_iterator = new Signature("iterator", "()Ljava/util/Iterator;");
  public static final Signature SIG_hasNext = new Signature("hasNext", "()Z");
  public static final Signature SIG_iterator_next = new Signature("next", "()Ljava/lang/Object;");
  public static final Signature SIG_add = new Signature("add", "(Ljava/lang/Object;)Z");
  public static final Signature SIG_put = new Signature("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

  public static final Signature SIG_getConnection = new Signature("getConnection", "()Ljava/sql/Connection;");
  public static final Signature SIG_postGetConnection = new Signature("postGetConnection", "(Ljava/sql/Connection;)V");
  public static final Signature SIG_ungetConnection = new Signature("ungetConnection", "(Ljava/sql/Connection;)V");
  public static final Signature SIG_setConnection = new Signature("setConnection", "(Ljava/sql/Connection;)V");

  public static final Signature SIG_toString = new Signature("toString", "()Ljava/lang/String;");
  public static final Signature SIG_length = new Signature("length", "()I");

  public static final Signature SIG_arraycopy = new Signature("arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V");

  public static final Signature SIG_Byte_valueOf = new Signature("valueOf", "(B)Ljava/lang/Byte;");
  public static final Signature SIG_Short_valueOf = new Signature("valueOf", "(S)Ljava/lang/Short;");
  public static final Signature SIG_Integer_valueOf = new Signature("valueOf", "(I)Ljava/lang/Integer;");
  public static final Signature SIG_Long_valueOf = new Signature("valueOf", "(J)Ljava/lang/Long;");
  public static final Signature SIG_Float_valueOf = new Signature("valueOf", "(F)Ljava/lang/Float;");
  public static final Signature SIG_Double_valueOf = new Signature("valueOf", "(D)Ljava/lang/Double;");
  public static final Signature SIG_Character_valueOf = new Signature("valueOf", "(C)Ljava/lang/Character;");
  public static final Signature SIG_Boolean_valueOf = new Signature("valueOf", "(Z)Ljava/lang/Boolean;");
  
  public static final Signature SIG_Byte_byteValue = new Signature("byteValue", "()B");
  public static final Signature SIG_Short_shortValue = new Signature("shortValue", "()S");
  public static final Signature SIG_Integer_intValue = new Signature("intValue", "()I");
  public static final Signature SIG_Long_longValue = new Signature("longValue", "()J");
  public static final Signature SIG_Float_floatValue = new Signature("floatValue", "()F");
  public static final Signature SIG_Double_doubleValue = new Signature("doubleValue", "()D");
  public static final Signature SIG_Character_charValue = new Signature("charValue", "()C");
  public static final Signature SIG_Boolean_booleanValue = new Signature("booleanValue", "()Z");

  public static final Signature SIG_getCause = new Signature("getCause", "()Ljava/lang/Throwable;");
  public static final Signature SIG_getMessage = new Signature("getMessage", "()Ljava/lang/String;");

  public static final Type TYPE_SessionPolicy = Type.getType(SessionPolicy.class);
  public static final Type TYPE_TransactionRunnable = Type.getType(TransactionRunnable.class);
  public static final Signature SIG_TransactionRunnable_run = new Signature("run", "(Ljava/sql/Connection;[Ljava/lang/Object;)Ljava/lang/Object;");
  
  public static final Type TYPE_DefaultSessionRunner = Type.getType(DefaultSessionRunner.class);
  public static final Signature SIG_DefaultSessionRunner_execute = 
    new Signature("execute", "(Luk/co/brunella/qof/session/TransactionRunnable;Ljava/lang/String;Luk/co/brunella/qof/session/SessionPolicy;[Ljava/lang/Object;)Ljava/lang/Object;");
  public static final Signature SIG_DefaultSessionRunner_executeBeanManaged = 
    new Signature("executeBeanManaged", "(Luk/co/brunella/qof/session/TransactionRunnable;Ljava/lang/String;Luk/co/brunella/qof/session/SessionPolicy;[Ljava/lang/Object;)Ljava/lang/Object;");
  public static final Signature SIG_DefaultSessionRunner_executeContainerManaged = 
    new Signature("executeContainerManaged", "(Luk/co/brunella/qof/session/TransactionRunnable;Ljava/lang/String;Luk/co/brunella/qof/session/SessionPolicy;[Ljava/lang/Object;)Ljava/lang/Object;");
}
